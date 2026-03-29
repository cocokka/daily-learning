package com.demo.dl.cache.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class CacheEvictListener implements MessageListener {

  private final TwoLevelCacheManager cacheManager;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    String msg = new String(message.getBody());
    log.debug("收到缓存清除消息: {}", msg);

    // 消息格式: cacheName:key
    String[] parts = msg.split(":", 2);
    if (parts.length == 2) {
      cacheManager.evictLocal(parts[0], parts[1]);
    }
  }
}
