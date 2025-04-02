package com.prezcode.loginservice.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
public class CorrelationIdFilter implements WebFilter {

  private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    log.info("Adding correlation id to MDC.");
    String correlationId = Optional.of(exchange)
        .map(ServerWebExchange::getRequest)
        .map(HttpMessage::getHeaders)
        .map(headers -> headers.getFirst("X-correlation-id"))
        .orElseGet(() -> {
          log.warn("X-correlation-id header not found, generating new correlation id!");
          return UUID.randomUUID().toString();
        });
    MDC.put("correlationId", correlationId);
    return chain.filter(exchange);
  }
}
