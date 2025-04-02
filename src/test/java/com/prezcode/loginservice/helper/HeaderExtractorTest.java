package com.prezcode.loginservice.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class HeaderExtractorTest {

  private final HeaderExtractor headerExtractor = new HeaderExtractor();
  private final Function<? super HttpHeaders, Optional<String>> cookieMapper = httpHeaders ->
      Optional.of(httpHeaders)
      .map(headers -> headers.getFirst(HttpHeaders.SET_COOKIE));
  private final UnaryOperator<String> cookieUnaryOperator = setCookie ->
      setCookie.indexOf(';') == -1
      ? setCookie
      : setCookie.substring(0, setCookie.indexOf(';'));
  private final Function<? super HttpHeaders, Optional<String>> codeParamMapper = httpHeaders ->
      Optional.of(httpHeaders)
      .map(HttpHeaders::getLocation)
      .map(URI::getQuery);
  private final UnaryOperator<String> codeParamUnaryOperator = codeQueryParam ->
      codeQueryParam.substring(codeQueryParam.indexOf('=') + 1);

  @ParameterizedTest
  @ValueSource(strings = {"cookie1;HttpOnly;Domain=some-domain", "cookie1"})
  void shouldExtractCookieHeader_WhenCorrectDataPassed(String cookieValue) {
    //given
    ResponseEntity<Void> response = ResponseEntity.ok().headers(
        headers -> headers.add(HttpHeaders.SET_COOKIE, cookieValue)
    ).build();
    //when
    String result = headerExtractor.extract(response, cookieMapper, cookieUnaryOperator);
    //then
    assertEquals("cookie1", result);
  }

  @Test
  void shouldThrowException_WhenSetCookieHeaderNotPresent() {
    //given
    ResponseEntity<Void> response = ResponseEntity.ok().build();
    //when, then
    assertThrows(IllegalStateException.class, () -> headerExtractor.extract(response, cookieMapper, cookieUnaryOperator));
  }

  @Test
  void shouldExtractCodeParam_WhenCorrectDataPassed() {
    //given
    ResponseEntity<Void> response = ResponseEntity.ok().headers(
        headers -> headers.setLocation(
            UriComponentsBuilder
                .fromPath("/example")
                .queryParam("code", "someCode")
                .build()
                .toUri())
    ).build();
    //when
    String result = headerExtractor.extract(response, codeParamMapper, codeParamUnaryOperator);
    //then
    assertEquals("someCode", result);
  }

  @Test
  void shouldThrowException_WhenNoLocationHeaderPresent() {
    //given
    ResponseEntity<Void> response = ResponseEntity.ok().build();
    //when, then
    assertThrows(IllegalStateException.class,
        () -> headerExtractor.extract(response, codeParamMapper, codeParamUnaryOperator));
  }
}