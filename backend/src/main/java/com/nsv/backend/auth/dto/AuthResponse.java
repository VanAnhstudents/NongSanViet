package com.nsv.backend.auth.dto;

import com.nsv.backend.user.dto.UserDto;

import java.util.List;

public record AuthResponse(String accessToken, String tokenType, long expiresIn,
                           UserDto user, List<MenuItem> menus) {
}
