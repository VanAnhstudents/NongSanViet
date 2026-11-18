package com.nsv.backend.user.dto;

import com.nsv.backend.user.entity.Role;

/** Thông tin người dùng trả ra ngoài module (không chứa mật khẩu). */
public record UserDto(Long id, String fullName, String email, String phone, Role role) {
}
