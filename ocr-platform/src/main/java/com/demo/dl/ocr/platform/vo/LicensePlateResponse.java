package com.demo.dl.ocr.platform.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicensePlateResponse {
  private String taskId;
  private String plateNumber;
  private Integer confidence;
  private String province;
  private String cityCode;
  private String plateColor;
  private String vehicleType;
  private Integer plateType;
  private Long processingTimeMs;
  private String status;
  private String imageUrl; // 标注了车牌位置的图片URL
  private Boolean isValid; // 车牌号是否有效

  /** 从识别结果创建响应 */
  public static LicensePlateResponse fromResult(String taskId, LicensePlateResult result) {
    return LicensePlateResponse.builder()
        .taskId(taskId)
        .plateNumber(result.getPlateNumber())
        .confidence(result.getConfidence())
        .province(result.getProvince())
        .cityCode(result.getCityCode())
        .plateColor(result.getPlateColor())
        .vehicleType(result.getVehicleType())
        .plateType(result.getPlateType())
        .processingTimeMs(result.getProcessingTimeMs())
        .status("SUCCESS")
        .isValid(result.isValid())
        .build();
  }
}
