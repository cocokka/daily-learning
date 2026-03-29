package com.demo.dl.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Slf4j
@Component
public class TwoLevelCacheManager {

  // 维护多个Caffeine实例，按cacheName隔离
  private final Map<String, Cache<String, String>> localCaches = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper;
  private final RedissonClient redissonClient;
  private final RedisTemplate<String, String> redisTemplate;

  @Value("${cache.two-level.local.maximum-size:10000}")
  private long localMaximumSize;

  @Value("${cache.two-level.local.default-expire-seconds:60}")
  private long defaultLocalExpire;

  /** 获取本地缓存实例 */
  private Cache<String, String> getLocalCache(String cacheName, long expireSeconds) {
    long expire = expireSeconds > 0 ? expireSeconds : defaultLocalExpire;
    return localCaches.computeIfAbsent(
        cacheName,
        k ->
            Caffeine.newBuilder()
                .expireAfterWrite(expire, TimeUnit.SECONDS)
                .maximumSize(localMaximumSize) // 防止内存溢出
                .recordStats() // 监控命中率
                .build());
  }

  /** 从缓存获取数据（带分布式锁防击穿） */
  public <T> T get(
      String cacheName,
      String key,
      long localExpireSeconds,
      long redisExpireSeconds,
      TypeReference<T> valueTypeRef,
      Supplier<T> dbSupplier)
      throws Exception {
    // 查本地
    Cache<String, String> localCache = getLocalCache(cacheName, localExpireSeconds);
    String localValue = localCache.getIfPresent(key);
    if (localValue != null) {
      return objectMapper.readValue(localValue, valueTypeRef);
    }

    // 查 redis（使用分布式锁防缓存击穿）
    String redisKey = buildRedisKey(cacheName, key);
    String redisValue = redisTemplate.opsForValue().get(redisKey);
    if (redisValue != null) {
      localCache.put(key, redisValue);
      return objectMapper.readValue(redisValue, valueTypeRef);
    }

    // 3. 缓存未命中，加分布式锁查询DB
    return loadFromDB(
        cacheName,
        key,
        redisKey,
        localExpireSeconds,
        redisExpireSeconds,
        valueTypeRef,
        dbSupplier,
        localCache);
  }

  /** 从数据库加载数据（带分布式锁） */
  private <T> T loadFromDB(
      String cacheName,
      String key,
      String redisKey,
      long localExpire,
      long redisExpire,
      TypeReference<T> typeReference,
      Supplier<T> dbSupplier,
      Cache<String, String> localCache) {
    String lockKey = "LOCK:" + redisKey;
    RLock lock = redissonClient.getLock(lockKey);

    try {
      // 尝试获取锁（最多等待3秒，锁持有时间10秒）
      boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
      if (!locked) {
        log.info("获取锁失败，等待后重试: {}", redisKey);
        Thread.sleep(100);
        // 重试读取缓存
        String retryValue = redisTemplate.opsForValue().get(redisKey);
        if (retryValue != null) {
          localCache.put(key, retryValue);
          return objectMapper.readValue(retryValue, typeReference);
        }
        return null;
      }

      // 双重检查：再次检查Redis（防止在等待锁期间其他线程已加载）
      String doubleCheck = redisTemplate.opsForValue().get(redisKey);
      if (doubleCheck != null) {
        log.info("双重检查命中: {}", redisKey);
        localCache.put(key, doubleCheck);
        return objectMapper.readValue(doubleCheck, typeReference);
      }

      // 查询数据库
      log.info("缓存未命中，查询数据库: {}", redisKey);
      long startTime = System.currentTimeMillis();
      T result = dbSupplier.get();
      long dbTime = System.currentTimeMillis() - startTime;

      if (result != null) {
        String jsonValue = objectMapper.writeValueAsString(result);

        // 写入Redis（添加随机过期时间，防止缓存雪崩）
        long expire = redisExpire + ThreadLocalRandom.current().nextInt(60);
        redisTemplate.opsForValue().set(redisKey, jsonValue, Duration.ofSeconds(expire));
        log.info("写入Redis成功: {}, 过期时间: {}秒", redisKey, expire);

        // 写入本地缓存
        localCache.put(key, jsonValue);
        log.info("写入本地缓存成功: {}", key);
      } else {
        // 处理空值缓存，防止缓存穿透
        if (true) { // allowNull
          redisTemplate.opsForValue().set(redisKey, "", Duration.ofSeconds(60));
          localCache.put(key, "");
        }
      }

      log.info("数据库查询耗时: {}ms", dbTime);
      return result;

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("获取分布式锁被中断", e);
      return dbSupplier.get();
    } catch (Exception e) {
      log.error("加载数据失败", e);
      return dbSupplier.get();
    } finally {
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  /** 清除缓存（包括本地和Redis） */
  public void evict(String cacheName, String key) {
    log.info("清除缓存: cacheName={}, key={}", cacheName, key);

    // 清除本地缓存
    evictLocal(cacheName, key);

    // 清除Redis缓存
    String redisKey = buildRedisKey(cacheName, key);
    redisTemplate.delete(redisKey);

    // 广播清除消息给其他节点
    redisTemplate.convertAndSend("cache:evict", cacheName + ":" + key);
  }

  /** 清除本地缓存 */
  public void evictLocal(String cacheName, String key) {
    Cache<String, String> localCache = localCaches.get(cacheName);
    if (localCache != null) {
      localCache.invalidate(key);
      log.info("清除本地缓存: {}-{}", cacheName, key);
    }
  }

  /** 构建Redis Key */
  private String buildRedisKey(String cacheName, String key) {
    return cacheName + ":" + key;
  }

  /** 获取本地缓存统计信息 */
  public String getLocalCacheStats(String cacheName) {
    Cache<String, String> localCache = localCaches.get(cacheName);
    if (localCache != null) {
      com.github.benmanes.caffeine.cache.stats.CacheStats stats = localCache.stats();
      return String.format(
          "命中率: %.2f%%, 命中次数: %d, 未命中次数: %d, 淘汰次数: %d",
          stats.hitRate() * 100, stats.hitCount(), stats.missCount(), stats.evictionCount());
    }
    return "缓存未找到";
  }
}
