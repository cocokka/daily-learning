package com.demo.dl.ocr.platform.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrResponse {
  private String taskId;
  private String recognizedText;
  private String pdfUrl;
  private Integer confidence;
  private String status;
  private Long processingTimeMs;
  private Integer textLength;
  private String language;
  private String[] keywords;
  private String summary; // 文本摘要（可选）
  private Boolean pdfGenerated;

  /** 从OCR结果创建响应 */
  public static OcrResponse fromResult(String taskId, OcrResult result, String pdfUrl) {
    return OcrResponse.builder()
        .taskId(taskId)
        .recognizedText(result.getText())
        .pdfUrl(pdfUrl)
        .confidence(result.getConfidence())
        .status("SUCCESS")
        .processingTimeMs(result.getProcessingTimeMs())
        .textLength(result.getCharacterCount())
        .language(result.getLanguage())
        .pdfGenerated(pdfUrl != null && !pdfUrl.isEmpty())
        .build();
  }

  /** 创建失败响应 */
  public static OcrResponse failed(String taskId, String errorMessage) {
    return OcrResponse.builder()
        .taskId(taskId)
        .status("FAILED")
        .recognizedText(errorMessage)
        .build();
  }
}
