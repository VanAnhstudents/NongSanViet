package com.nsv.backend.auth.service;

import com.nsv.backend.auth.dto.AuthResponse;
import com.nsv.backend.auth.dto.LoginRequest;
import com.nsv.backend.auth.dto.ProfileResponse;
import com.nsv.backend.auth.dto.RegisterRequest;
import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.common.exception.FieldErrorDetail;
import com.nsv.backend.common.security.JwtService;
import com.nsv.backend.user.dto.UserDto;
import com.nsv.backend.user.entity.Role;
import com.nsv.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final MenuProvider menuProvider;

    public AuthResponse register(RegisterRequest request) {
        // Chỉ cho tự đăng ký Farmer/Buyer; Admin do hệ thống cấp.
        if (request.role() == Role.ADMIN) {
            throw ApiException.badRequest("Dữ liệu không hợp lệ",
                    List.of(new FieldErrorDetail("role", "Chỉ được đăng ký vai trò FARMER hoặc BUYER")));
        }
        UserDto user = userService.createUser(request.fullName(), request.email(), request.phone(),
                request.password(), request.role());
        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        UserDto user = userService.authenticate(request.identifier(), request.password());
        return buildResponse(user);
    }

    public ProfileResponse me(Long userId) {
        UserDto user = userService.getById(userId);
        return new ProfileResponse(user, menuProvider.menusFor(user.role()));
    }

    private AuthResponse buildResponse(UserDto user) {
        String token = jwtService.generateToken(user.id(), user.role().name());
        return new AuthResponse(token, "Bearer", jwtService.expiresInSeconds(), user,
                menuProvider.menusFor(user.role()));
    }
}
