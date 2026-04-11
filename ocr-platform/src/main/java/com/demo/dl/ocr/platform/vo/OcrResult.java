package com.demo.dl.ocr.platform.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrResult {
  private String text;
  private Integer confidence;
  private Long processingTimeMs;
  private Integer characterCount;
  private String language;
  private String[] detectedWords;
  private BoundingBox[] textBoxes;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BoundingBox {
    private int x;
    private int y;
    private int width;
    private int height;
    private String text;
    private Double confidence;
  }
}
