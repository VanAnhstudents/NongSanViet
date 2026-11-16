package com.nsv.backend.auth.controller;

import com.nsv.backend.auth.dto.AuthResponse;
import com.nsv.backend.auth.dto.LoginRequest;
import com.nsv.backend.auth.dto.ProfileResponse;
import com.nsv.backend.auth.dto.RegisterRequest;
import com.nsv.backend.auth.service.AuthService;
import com.nsv.backend.common.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** Thông tin người đang đăng nhập + menu theo vai trò. */
    @GetMapping("/me")
    public ProfileResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return authService.me(principal.id());
    }
}
