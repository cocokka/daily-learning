package com.demo.dl.ocr.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
  SUCCESS(200, "操作成功"),
  BAD_REQUEST(400, "请求参数错误"),
  UNAUTHORIZED(401, "未授权"),
  FORBIDDEN(403, "禁止访问"),
  NOT_FOUND(404, "资源不存在"),
  METHOD_NOT_ALLOWED(405, "请求方法不允许"),
  CONFLICT(409, "资源冲突"),
  INTERNAL_ERROR(500, "服务器内部错误"),
  SERVICE_UNAVAILABLE(503, "服务不可用");

  private final Integer code;
  private final String message;
}
