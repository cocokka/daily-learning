package com.demo.dl.ocr.platform.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicensePlateResult {
  private String plateNumber;
  private Integer confidence;
  private String province; // 省份简称，如：京、沪、粤
  private String cityCode; // 城市代码
  private String plateColor; // 车牌颜色：蓝、黄、绿、白、黑
  private String vehicleType; // 车辆类型：小型车、大型车、新能源
  private Integer plateType; // 车牌类型：1-普通蓝牌，2-新能源，3-警用，4-军用
  private Long processingTimeMs;
  private PlatePosition position;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PlatePosition {
    private int x;
    private int y;
    private int width;
    private int height;
  }

  /** 验证车牌号格式是否正确 */
  public boolean isValid() {
    if (plateNumber == null || plateNumber.isEmpty()) {
      return false;
    }
    // 中国车牌号正则表达式
    String pattern = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{5}$";
    return plateNumber.matches(pattern);
  }
}
