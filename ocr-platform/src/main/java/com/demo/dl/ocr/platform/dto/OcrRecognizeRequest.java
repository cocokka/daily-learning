package com.demo.dl.ocr.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrRecognizeRequest {

  @NotBlank(message = "任务ID不能为空")
  private String taskId;

  @NotBlank(message = "文件类型不能为空")
  private String fileType;

  private Boolean generatePdf;

  private String language;
}
