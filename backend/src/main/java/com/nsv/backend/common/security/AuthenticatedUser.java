package com.nsv.backend.common.security;

/** Principal lấy từ JWT: mã người dùng và vai trò (FARMER/BUYER/ADMIN). */
public record AuthenticatedUser(Long id, String role) {
}
