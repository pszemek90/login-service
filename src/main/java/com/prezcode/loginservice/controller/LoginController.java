package com.prezcode.loginservice.controller;

import com.prezcode.loginservice.model.LoginRequest;
import com.prezcode.loginservice.model.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/login")
public class LoginController {

  private static final Logger log = LoggerFactory.getLogger(LoginController.class);

  @Value("${authorization-server.base-url}")
  private String baseUrl;
  @Value("${authorization-server.redirect-base-uri}")
  private String redirectBaseUri;

  @PostMapping
  public Mono<TokenResponse> authenticate(@RequestBody LoginRequest loginRequest) {
    String username = loginRequest.getUsername();
    String password = loginRequest.getPassword();
    WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
    return webClient
        .post()
        .uri("/login")
        .bodyValue("username=" + username + "&password=" + password)
        .headers(
            headers -> headers.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"))
        .retrieve()
        .toBodilessEntity()
        .flatMap(
            loginResponse ->
                webClient
                    .get()
                    .uri(
                        uriBuilder ->
                            uriBuilder
                                .path("/oauth2/authorize")
                                .queryParam("response_type", "code")
                                .queryParam("client_id", "api-gateway")
                                .queryParam("scope", "openid profile")
                                .queryParam(
                                    "redirect_uri",
                                    redirectBaseUri + "/login/oauth2/code/api-gateway")
                                .build())
                    .header(HttpHeaders.COOKIE, extractSessionCookie(loginResponse))
                    .retrieve()
                    .toBodilessEntity())
        .flatMap(
            authResponse ->
                webClient
                    .post()
                    .uri("/oauth2/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(
                        BodyInserters.fromFormData("grant_type", "authorization_code")
                            .with("code", extractCode(authResponse))
                            .with(
                                "redirect_uri",
                                redirectBaseUri + "/login/oauth2/code/api-gateway")
                            .with("client_id", "api-gateway")
                            .with("client_secret", "test"))
                    .retrieve()
                    .bodyToMono(TokenResponse.class));
  }

  private String extractCode(ResponseEntity<Void> authResponse) {
    String codeQueryParam = authResponse.getHeaders().getLocation().getQuery();
    String code = codeQueryParam.substring(codeQueryParam.indexOf('=') + 1);
    log.info("Code: {}", code);
    return code;
  }

  private String extractSessionCookie(ResponseEntity<Void> loginResponse) {
    String setCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    return setCookie.substring(0, setCookie.indexOf(';'));
  }
}
