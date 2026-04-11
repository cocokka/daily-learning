package com.demo.dl.ocr.platform.controller;

import com.demo.dl.ocr.platform.dto.PageQueryRequest;
import com.demo.dl.ocr.platform.entity.OcrRecord;
import com.demo.dl.ocr.platform.exception.BusinessException;
import com.demo.dl.ocr.platform.repository.OcrRecordRepository;
import com.demo.dl.ocr.platform.service.OcrRecognitionService;
import com.demo.dl.ocr.platform.service.PdfGenerationService;
import com.demo.dl.ocr.platform.service.S3FileStorageService;
import com.demo.dl.ocr.platform.util.SnowflakeIdGenerator;
import com.demo.dl.ocr.platform.vo.ApiResponse;
import com.demo.dl.ocr.platform.vo.LicensePlateResponse;
import com.demo.dl.ocr.platform.vo.OcrResponse;
import com.demo.dl.ocr.platform.vo.OcrResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
@Tag(name = "OCR识别接口", description = "图片文字识别、车牌识别、PDF生成等功能")
@Validated
public class OcrController {

  private final OcrRecognitionService ocrService;
  private final S3FileStorageService storageService;
  private final PdfGenerationService pdfService;
  private final OcrRecordRepository recordRepository;
  private final SnowflakeIdGenerator snowflakeIdGenerator;

  @PostMapping(value = "/recognize/text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "图片文字识别", description = "上传图片进行OCR文字识别")
  public Mono<ApiResponse<OcrResponse>> recognizeText(
      @RequestPart("file") Mono<MultipartFile> filePartMono,
      @RequestParam(value = "generatePdf", defaultValue = "false") boolean generatePdf) {

    String taskId = snowflakeIdGenerator.generateTaskId();

    return filePartMono
        .switchIfEmpty(Mono.error(new BusinessException("上传文件不能为空")))
        .flatMap(
            filePart -> {
              if (filePart.getOriginalFilename() == null
                  || filePart.getOriginalFilename().isEmpty()) {
                return Mono.error(new BusinessException("文件名不能为空"));
              }

              log.info("开始处理OCR识别任务: {}, 文件名: {}", taskId, filePart.getOriginalFilename());

              OcrRecord record =
                  OcrRecord.builder()
                      .taskId(taskId)
                      .fileName(filePart.getOriginalFilename())
                      .status("PROCESSING")
                      .ocrType("TEXT")
                      .createTime(LocalDateTime.now())
                      .build();

              return recordRepository
                  .save(record)
                  .flatMap(
                      saved ->
                          storageService
                              .uploadFile(filePart, "ocr", taskId)
                              .flatMap(
                                  fileKey -> {
                                    saved.setFilePath(fileKey);
                                    return recordRepository.save(saved);
                                  })
                              .flatMap(updated -> ocrService.recognizeText(updated.getFilePath()))
                              .flatMap(
                                  result -> {
                                    record.setRecognizedText(result.getText());
                                    record.setConfidence(result.getConfidence());
                                    record.setStatus("SUCCESS");
                                    record.setUpdateTime(LocalDateTime.now());
                                    return recordRepository.save(record);
                                  })
                              .flatMap(
                                  finalRecord -> {
                                    if (generatePdf) {
                                      return pdfService
                                          .generatePdfFromText(
                                              finalRecord.getRecognizedText(),
                                              taskId,
                                              finalRecord.getFileName())
                                          .map(
                                              pdfUrl -> {
                                                finalRecord.setPdfUrl(pdfUrl);
                                                return finalRecord;
                                              });
                                    }
                                    return Mono.just(finalRecord);
                                  })
                              .flatMap(recordRepository::save)
                              .map(
                                  finalRecord -> {
                                    OcrResult result =
                                        OcrResult.builder()
                                            .text(finalRecord.getRecognizedText())
                                            .confidence(finalRecord.getConfidence())
                                            .characterCount(
                                                finalRecord.getRecognizedText() != null
                                                    ? finalRecord.getRecognizedText().length()
                                                    : 0)
                                            .language("zh-CN")
                                            .build();
                                    OcrResponse response =
                                        OcrResponse.fromResult(
                                            taskId, result, finalRecord.getPdfUrl());
                                    return ApiResponse.success("OCR识别成功", response);
                                  }));
            })
        .onErrorResume(
            e -> {
              log.error("OCR识别失败: {}", e.getMessage(), e);
              return recordRepository
                  .findByTaskId(taskId)
                  .flatMap(
                      record -> {
                        record.setStatus("FAILED");
                        record.setErrorMessage(e.getMessage());
                        return recordRepository.save(record);
                      })
                  .thenReturn(ApiResponse.error(500, "OCR识别失败: " + e.getMessage()));
            });
  }

  @PostMapping(value = "/recognize/license-plate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "车牌识别", description = "上传包含车牌的图片进行车牌号码识别")
  public Mono<ApiResponse<LicensePlateResponse>> recognizeLicensePlate(
      @RequestPart("file") Mono<MultipartFile> filePartMono) {
    String taskId = snowflakeIdGenerator.generateTaskId();

    return filePartMono
        .switchIfEmpty(Mono.error(new BusinessException("上传文件不能为空")))
        .flatMap(
            filePart -> {
              if (filePart.getOriginalFilename() == null
                  || filePart.getOriginalFilename().isEmpty()) {
                return Mono.error(new BusinessException("文件名不能为空"));
              }

              OcrRecord record =
                  OcrRecord.builder()
                      .taskId(taskId)
                      .fileName(filePart.getOriginalFilename())
                      .status("PROCESSING")
                      .ocrType("LICENSE_PLATE")
                      .createTime(LocalDateTime.now())
                      .build();

              return recordRepository
                  .save(record)
                  .flatMap(saved -> storageService.uploadFile(filePart, "ocr", taskId))
                  .flatMap(fileUrl -> ocrService.recognizeLicensePlate(fileUrl, taskId))
                  .flatMap(
                      result -> {
                        return recordRepository
                            .findByTaskId(taskId)
                            .flatMap(
                                record1 -> {
                                  record1.setLicensePlateNumber(result.getPlateNumber());
                                  record1.setConfidence(result.getConfidence());
                                  record1.setStatus("SUCCESS");
                                  record1.setUpdateTime(LocalDateTime.now());
                                  return recordRepository.save(record1);
                                })
                            .thenReturn(
                                ApiResponse.success(
                                    "车牌识别成功", LicensePlateResponse.fromResult(taskId, result)));
                      });
            })
        .onErrorResume(
            e -> {
              log.error("车牌识别失败: {}", e.getMessage(), e);
              return recordRepository
                  .findByTaskId(taskId)
                  .flatMap(
                      record -> {
                        record.setStatus("FAILED");
                        record.setErrorMessage(e.getMessage());
                        return recordRepository.save(record);
                      })
                  .thenReturn(ApiResponse.error(500, "车牌识别失败: " + e.getMessage()));
            });
  }

  @GetMapping("/result/{taskId}")
  @Operation(summary = "查询识别结果", description = "根据任务ID查询OCR识别结果")
  public Mono<ApiResponse<OcrRecord>> getResult(@PathVariable String taskId) {
    return recordRepository
        .findByTaskId(taskId)
        .map(record -> ApiResponse.success("查询成功", record))
        .switchIfEmpty(Mono.just(ApiResponse.notFound("任务不存在: " + taskId)));
  }

  @GetMapping("/records")
  @Operation(summary = "获取所有记录", description = "分页获取OCR识别记录")
  public Mono<ApiResponse<Map<String, Object>>> getAllRecords(
      @Valid @ModelAttribute PageQueryRequest request) {
    return recordRepository
        .count()
        .flatMap(
            total -> {
              Flux<OcrRecord> records =
                  recordRepository
                      .findAll()
                      .skip((long) request.getPage() * request.getSize())
                      .take(request.getSize());

              return records
                  .collectList()
                  .map(
                      recordList -> {
                        var pageData = new java.util.HashMap<String, Object>();
                        pageData.put("content", recordList);
                        pageData.put("totalElements", total);
                        pageData.put(
                            "totalPages", (int) Math.ceil((double) total / request.getSize()));
                        pageData.put(
                            "pageable",
                            Map.of("pageNumber", request.getPage(), "pageSize", request.getSize()));
                        return ApiResponse.success("查询成功", pageData);
                      });
            });
  }

  @GetMapping("/records/image/{taskId}")
  @Operation(summary = "回看图片", description = "获取原始上传图片")
  public Mono<byte[]> getImage(@PathVariable String taskId) {
    return recordRepository
        .findByTaskId(taskId)
        .switchIfEmpty(Mono.error(new BusinessException("任务不存在: " + taskId)))
        .flatMap(record -> storageService.downloadFile(record.getFilePath()));
  }

  @GetMapping("/search")
  @Operation(summary = "搜索识别内容", description = "根据关键词搜索OCR识别结果")
  public Mono<ApiResponse<Map<String, Object>>> search(
      @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword,
      @Valid @ModelAttribute PageQueryRequest request) {

    Flux<OcrRecord> records = recordRepository.searchByKeyword(keyword);

    return records
        .collectList()
        .map(
            recordList -> {
              var pageData = new java.util.HashMap<String, Object>();
              pageData.put("content", recordList);
              pageData.put("totalElements", recordList.size());
              pageData.put("totalPages", 1);
              pageData.put("pageable", Map.of("pageNumber", 0, "pageSize", recordList.size()));
              return ApiResponse.success("搜索成功", pageData);
            });
  }

  @DeleteMapping("/record/{id}")
  @Operation(summary = "删除记录", description = "删除OCR识别记录及相关文件")
  public Mono<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
    return recordRepository
        .findById(id)
        .switchIfEmpty(Mono.error(new BusinessException("记录不存在: " + id)))
        .flatMap(
            record -> {
              if (record.getFilePath() != null) {
                return storageService
                    .deleteFile(record.getFilePath())
                    .then(recordRepository.deleteById(id));
              }
              return recordRepository.deleteById(id);
            })
        .then(Mono.just(ApiResponse.success("删除成功", null)));
  }
}
