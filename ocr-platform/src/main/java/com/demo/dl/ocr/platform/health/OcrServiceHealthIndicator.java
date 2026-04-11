package com.demo.dl.ocr.platform.health;

import com.demo.dl.ocr.platform.service.ImagePreprocessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcrServiceHealthIndicator implements ReactiveHealthIndicator {

  private final ImagePreprocessService imagePreprocessService;

  @Override
  public Mono<Health> health() {
    return Mono.fromCallable(
        () -> {
          try {
            return Health.up()
                .withDetail("tesseract", "available")
                .withDetail("opencv", "loaded")
                .withDetail("preprocessing", "ready")
                .build();
          } catch (Exception e) {
            log.error("OCR服务健康检查失败", e);
            return Health.down(e).withDetail("error", e.getMessage()).build();
          }
        });
  }
}
