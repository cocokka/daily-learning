package com.demo.dl.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 两级缓存注解 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TwoLevelCache {

  /** 缓存名称（用于区分不同业务） */
  String cacheName();

  /** 缓存Key（支持SpEL表达式） */
  String key();

  /** 本地缓存过期时间（秒），默认60秒 */
  long localExpire() default 60;

  /** Redis缓存过期时间（秒），默认300秒 */
  long redisExpire() default 300;

  /** 是否允许缓存null值 */
  boolean allowNull() default false;
}
