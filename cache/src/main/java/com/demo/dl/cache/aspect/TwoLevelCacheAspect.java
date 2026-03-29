package com.demo.dl.cache.aspect;

import com.demo.dl.cache.annotation.TwoLevelCache;
import com.demo.dl.cache.config.TwoLevelCacheManager;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;

@RequiredArgsConstructor
@Slf4j
@Aspect
@Component
public class TwoLevelCacheAspect {

  private final TwoLevelCacheManager cacheManager;

  private final SpelExpressionParser spelParser = new SpelExpressionParser();
  private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

  @Around("@annotation(twoLevelCache)")
  public Object around(ProceedingJoinPoint joinPoint, TwoLevelCache twoLevelCache)
      throws Throwable {
    // 解析缓存Key
    String key = parseKey(twoLevelCache.key(), joinPoint);
    String cacheName = twoLevelCache.cacheName();
    long localExpire = twoLevelCache.localExpire();
    long redisExpire = twoLevelCache.redisExpire();

    // 获取方法返回类型
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    Type returnType = method.getGenericReturnType();

    // 构建TypeReference用于反序列化
    TypeReference<Object> typeReference =
        new TypeReference<>() {
          @Override
          public Type getType() {
            return returnType;
          }
        };

    // 从缓存获取
    return cacheManager.get(
        cacheName,
        key,
        localExpire,
        redisExpire,
        typeReference,
        () -> {
          try {
            return joinPoint.proceed();
          } catch (Throwable e) {
            throw new RuntimeException(e);
          }
        });
  }

  /** 解析SpEL表达式 */
  private String parseKey(String keySpel, ProceedingJoinPoint joinPoint) {
    try {
      MethodSignature signature = (MethodSignature) joinPoint.getSignature();
      String[] paramNames = discoverer.getParameterNames(signature.getMethod());
      Object[] args = joinPoint.getArgs();

      EvaluationContext context = new StandardEvaluationContext();
      for (int i = 0; i < paramNames.length; i++) {
        context.setVariable(paramNames[i], args[i]);
      }

      Expression expression = spelParser.parseExpression(keySpel);
      return expression.getValue(context, String.class);
    } catch (Exception e) {
      log.error("解析缓存Key失败: {}", keySpel, e);
      return keySpel;
    }
  }
}
