package com.demo.dl.ocr.platform.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  private Integer code;
  private String message;
  private T data;
  private LocalDateTime timestamp;
  private String traceId;

  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder()
        .code(200)
        .message("success")
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return ApiResponse.<T>builder()
        .code(200)
        .message(message)
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static <T> ApiResponse<T> error(Integer code, String message) {
    return ApiResponse.<T>builder()
        .code(code)
        .message(message)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static <T> ApiResponse<T> error(Integer code, String message, String traceId) {
    return ApiResponse.<T>builder()
        .code(code)
        .message(message)
        .traceId(traceId)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static <T> ApiResponse<T> badRequest(String message) {
    return error(400, message);
  }

  public static <T> ApiResponse<T> unauthorized(String message) {
    return error(401, message);
  }

  public static <T> ApiResponse<T> forbidden(String message) {
    return error(403, message);
  }

  public static <T> ApiResponse<T> notFound(String message) {
    return error(404, message);
  }

  public static <T> ApiResponse<T> internalError(String message) {
    return error(500, message);
  }
}
