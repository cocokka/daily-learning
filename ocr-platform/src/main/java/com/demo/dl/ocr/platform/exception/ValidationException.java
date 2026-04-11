package com.demo.dl.ocr.platform.exception;

import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ValidationException extends BusinessException {

  private final List<FieldError> fieldErrors;

  public ValidationException(List<FieldError> fieldErrors) {
    super(HttpStatus.BAD_REQUEST.value(), "参数校验失败");
    this.fieldErrors = fieldErrors;
  }

  @Getter
  public static class FieldError {
    private final String field;
    private final String message;
    private final Object rejectedValue;

    public FieldError(String field, String message, Object rejectedValue) {
      this.field = field;
      this.message = message;
      this.rejectedValue = rejectedValue;
    }

    @Override
    public String toString() {
      return field + ": " + message;
    }
  }
}
