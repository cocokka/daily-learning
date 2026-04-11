package com.demo.dl.ocr.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageQueryRequest {

  @Min(value = 0, message = "页码不能小于0")
  @Builder.Default
  private Integer page = 0;

  @Min(value = 1, message = "每页大小不能小于1")
  @Max(value = 100, message = "每页大小不能超过100")
  @Builder.Default
  private Integer size = 20;
}
