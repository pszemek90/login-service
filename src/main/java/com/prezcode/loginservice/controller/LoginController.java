package com.prezcode.loginservice.controller;

import com.prezcode.loginservice.model.LoginRequest;
import com.prezcode.loginservice.model.LoginResponse;
import com.prezcode.loginservice.service.LoginService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/login")
public class LoginController {

  private final LoginService loginService;

  public LoginController(LoginService loginService) {
    this.loginService = loginService;
  }

  @PostMapping
  public Mono<LoginResponse> authenticate(@RequestBody LoginRequest loginRequest) {
    return loginService.logUserIn(loginRequest);

  }
}
