package com.demo.dl.ocr.platform.health;

import com.demo.dl.ocr.platform.repository.OcrRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements ReactiveHealthIndicator {

  private final OcrRecordRepository recordRepository;

  @Override
  public Mono<Health> health() {
    return recordRepository
        .count()
        .map(
            count ->
                Health.up()
                    .withDetail("database", "connected")
                    .withDetail("total_records", count)
                    .build())
        .onErrorResume(
            e -> {
              log.error("数据库健康检查失败", e);
              return Mono.just(
                  Health.down(e)
                      .withDetail("database", "disconnected")
                      .withDetail("error", e.getMessage())
                      .build());
            });
  }
}
