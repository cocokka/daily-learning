package com.demo.dl.ocr.platform.handler;

import com.demo.dl.ocr.platform.exception.BusinessException;
import com.demo.dl.ocr.platform.exception.ValidationException;
import com.demo.dl.ocr.platform.vo.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-2)
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

  private final ObjectMapper objectMapper;

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    ServerHttpResponse response = exchange.getResponse();

    if (response.isCommitted()) {
      return Mono.error(ex);
    }

    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    String traceId = generateOrGetTraceId(exchange);
    ApiResponse<?> apiResponse;

    if (ex instanceof ValidationException validationEx) {
      response.setStatusCode(HttpStatus.BAD_REQUEST);
      apiResponse = handleValidationException(validationEx, traceId);
    } else if (ex instanceof WebExchangeBindException bindEx) {
      response.setStatusCode(HttpStatus.BAD_REQUEST);
      apiResponse = handleBindException(bindEx, traceId);
    } else if (ex instanceof BindException bindEx) {
      response.setStatusCode(HttpStatus.BAD_REQUEST);
      apiResponse = handleBindException(bindEx, traceId);
    } else if (ex instanceof BusinessException businessEx) {
      HttpStatus status = HttpStatus.valueOf(businessEx.getCode());
      response.setStatusCode(status);
      apiResponse = ApiResponse.error(businessEx.getCode(), businessEx.getMessage(), traceId);
    } else if (ex instanceof ResponseStatusException responseStatusEx) {
      HttpStatus status = (HttpStatus) responseStatusEx.getStatusCode();
      response.setStatusCode(status);
      apiResponse = ApiResponse.error(status.value(), responseStatusEx.getReason(), traceId);
    } else {
      response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
      log.error("未处理的异常 - TraceId: {}", traceId, ex);
      apiResponse = ApiResponse.internalError("服务器内部错误");
    }

    return writeResponse(response, apiResponse);
  }

  private ApiResponse<?> handleValidationException(ValidationException ex, String traceId) {
    List<String> errors =
        ex.getFieldErrors().stream()
            .map(error -> String.format("%s: %s", error.getField(), error.getMessage()))
            .collect(Collectors.toList());

    String message = String.join("; ", errors);
    return ApiResponse.error(400, message, traceId);
  }

  private ApiResponse<?> handleBindException(WebExchangeBindException ex, String traceId) {
    List<ValidationException.FieldError> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError ->
                    new ValidationException.FieldError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        fieldError.getRejectedValue()))
            .collect(Collectors.toList());

    return handleFieldErrors(fieldErrors, traceId);
  }

  private ApiResponse<?> handleBindException(BindException ex, String traceId) {
    List<ValidationException.FieldError> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError ->
                    new ValidationException.FieldError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        fieldError.getRejectedValue()))
            .collect(Collectors.toList());

    return handleFieldErrors(fieldErrors, traceId);
  }

  private ApiResponse<?> handleFieldErrors(
      List<ValidationException.FieldError> fieldErrors, String traceId) {
    List<String> errors =
        fieldErrors.stream()
            .map(error -> String.format("%s: %s", error.getField(), error.getMessage()))
            .collect(Collectors.toList());

    String message = String.join("; ", errors);
    return ApiResponse.error(400, message, traceId);
  }

  private String generateOrGetTraceId(ServerWebExchange exchange) {
    String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");
    if (traceId == null || traceId.isEmpty()) {
      traceId = UUID.randomUUID().toString().substring(0, 8);
    }
    return traceId;
  }

  private Mono<Void> writeResponse(ServerHttpResponse response, ApiResponse<?> apiResponse) {
    try {
      byte[] bytes = objectMapper.writeValueAsBytes(apiResponse);
      DataBufferFactory bufferFactory = response.bufferFactory();
      DataBuffer dataBuffer = bufferFactory.wrap(bytes);
      return response.writeWith(Mono.just(dataBuffer));
    } catch (JsonProcessingException e) {
      log.error("序列化响应失败", e);
      String errorJson = "{\"code\":500,\"message\":\"序列化响应失败\"}";
      DataBuffer dataBuffer =
          response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
      return response.writeWith(Mono.just(dataBuffer));
    }
  }
}
