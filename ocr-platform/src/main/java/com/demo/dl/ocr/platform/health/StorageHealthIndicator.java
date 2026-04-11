package com.demo.dl.ocr.platform.health;

import com.demo.dl.ocr.platform.service.S3FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageHealthIndicator implements ReactiveHealthIndicator {

  private final S3FileStorageService storageService;

  @Override
  public Mono<Health> health() {
    return Mono.fromCallable(
        () -> {
          try {
            boolean accessible = storageService.isStorageAccessible();
            if (accessible) {
              return Health.up()
                  .withDetail("storage", "connected")
                  .withDetail("type", "RustFS/S3")
                  .build();
            } else {
              return Health.down().withDetail("storage", "unreachable").build();
            }
          } catch (Exception e) {
            log.error("存储服务健康检查失败", e);
            return Health.down(e)
                .withDetail("storage", "error")
                .withDetail("error", e.getMessage())
                .build();
          }
        });
  }
}
