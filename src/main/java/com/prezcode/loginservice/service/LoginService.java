package com.prezcode.loginservice.service;

import com.prezcode.loginservice.model.LoginRequest;
import com.prezcode.loginservice.model.LoginResponse;
import reactor.core.publisher.Mono;

public interface LoginService {
    Mono<LoginResponse> logUserIn(LoginRequest loginRequest);
}
