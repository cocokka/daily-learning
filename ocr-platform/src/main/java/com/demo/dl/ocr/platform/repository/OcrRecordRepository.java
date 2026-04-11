package com.demo.dl.ocr.platform.repository;

import com.demo.dl.ocr.platform.entity.OcrRecord;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OcrRecordRepository extends ReactiveCrudRepository<OcrRecord, Long> {

  Mono<OcrRecord> findByTaskId(String taskId);

  Flux<OcrRecord> findByStatus(String status);

  @Query("SELECT * FROM ocr_records WHERE recognized_text LIKE CONCAT('%', :keyword, '%')")
  Flux<OcrRecord> searchByKeyword(String keyword);

  Flux<OcrRecord> findByOcrTypeOrderByCreateTimeDesc(String ocrType);

  Mono<Long> countByStatus(String status);
}
