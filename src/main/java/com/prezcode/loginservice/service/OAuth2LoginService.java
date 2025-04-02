package com.prezcode.loginservice.service;

import com.prezcode.loginservice.helper.HeaderExtractor;
import com.prezcode.loginservice.model.LoginRequest;
import com.prezcode.loginservice.model.LoginResponse;
import com.prezcode.loginservice.model.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;

@Service
public class OAuth2LoginService implements LoginService {

  private final WebClient webClient;
  private final HeaderExtractor headerExtractor;

  @Value("${authorization-server.redirect-base-uri}")
  private String redirectBaseUri;

  public OAuth2LoginService(WebClient webClient, HeaderExtractor headerExtractor) {
    this.webClient = webClient;
    this.headerExtractor = headerExtractor;
  }

  @Override
  public Mono<LoginResponse> logUserIn(LoginRequest loginRequest) {
    String username = loginRequest.getUsername();
    String password = loginRequest.getPassword();
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
                    .header(
                        HttpHeaders.COOKIE,
                        headerExtractor.extract(loginResponse,
                            httpHeaders -> Optional.of(httpHeaders)
                                .map(headers -> headers.getFirst(HttpHeaders.SET_COOKIE)),
                            setCookie -> setCookie.indexOf(';') == -1
                                ? setCookie
                                : setCookie.substring(0, setCookie.indexOf(';'))))
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
                            .with(
                                "code",
                                headerExtractor.extract(authResponse,
                                    httpHeaders -> Optional.of(httpHeaders)
                                        .map(HttpHeaders::getLocation)
                                        .map(URI::getQuery),
                                    codeQueryParam -> codeQueryParam.substring(codeQueryParam.indexOf('=') + 1)))
                            .with(
                                "redirect_uri",
                                redirectBaseUri + "/login/oauth2/code/api-gateway")
                            .with("client_id", "api-gateway")
                            .with("client_secret", "test"))
                    .retrieve()
                    .bodyToMono(TokenResponse.class));
  }
}
