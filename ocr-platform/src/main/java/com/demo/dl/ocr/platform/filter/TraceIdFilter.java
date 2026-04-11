package com.demo.dl.ocr.platform.filter;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TraceIdFilter implements WebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");

    if (traceId == null || traceId.isEmpty()) {
      traceId = UUID.randomUUID().toString().substring(0, 8);
    }

    final String finalTraceId = traceId;

    exchange.getResponse().getHeaders().add("X-Trace-Id", finalTraceId);
    MDC.put("traceId", finalTraceId);

    return chain.filter(exchange).doFinally(signalType -> MDC.clear());
  }
}
