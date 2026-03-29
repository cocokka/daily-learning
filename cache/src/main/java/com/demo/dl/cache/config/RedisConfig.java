package com.demo.dl.cache.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class RedisConfig {

  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory factory, ObjectMapper objectMapper) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);

    // JSON序列化配置
    JacksonJsonRedisSerializer<Object> jacksonJsonRedisSerializer =
        new JacksonJsonRedisSerializer<>(objectMapper, Object.class);

    // String序列化
    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

    // key使用String序列化
    template.setKeySerializer(stringRedisSerializer);
    // value使用JSON序列化
    template.setValueSerializer(jacksonJsonRedisSerializer);
    // hash key使用String序列化
    template.setHashKeySerializer(stringRedisSerializer);
    // hash value使用JSON序列化
    template.setHashValueSerializer(jacksonJsonRedisSerializer);

    template.afterPropertiesSet();
    return template;
  }

  @Bean
  public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
    return new StringRedisTemplate(factory);
  }

  /** Redis消息监听器容器（用于缓存清除广播） */
  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer(
      RedisConnectionFactory factory, MessageListenerAdapter cacheEvictListenerAdapter) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(factory);
    container.addMessageListener(cacheEvictListenerAdapter, new PatternTopic("cache:evict"));
    return container;
  }

  @Bean
  public MessageListenerAdapter cacheEvictListenerAdapter(CacheEvictListener listener) {
    return new MessageListenerAdapter(listener, "onMessage");
  }
}
