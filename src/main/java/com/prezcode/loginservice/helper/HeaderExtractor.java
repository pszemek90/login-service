package com.prezcode.loginservice.helper;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@Component
public class HeaderExtractor {

  public String extract(
      ResponseEntity<Void> response,
      Function<? super HttpHeaders, Optional<String>> headerMapper,
      UnaryOperator<String> resultMapper
  ) {
    String extractionResult = Optional.of(response)
        .map(HttpEntity::getHeaders)
        .flatMap(headerMapper)
        .orElseThrow(() -> new IllegalStateException("Header mapping failed!"));
    return resultMapper.apply(extractionResult);
  }
}
