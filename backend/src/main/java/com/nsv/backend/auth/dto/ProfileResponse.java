package com.nsv.backend.auth.dto;

import com.nsv.backend.user.dto.UserDto;

import java.util.List;

public record ProfileResponse(UserDto user, List<MenuItem> menus) {
}
