package com.nsv.backend.auth.dto;

import com.nsv.backend.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cần ít nhất một trong hai: email hoặc phone (kiểm tra ở UserService). */
public record RegisterRequest(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
        String fullName,

        @Email(regexp = ".+@.+\\..+", message = "Email không hợp lệ")
        @Size(max = 254, message = "Email tối đa 254 ký tự")
        String email,

        @Pattern(regexp = "^(|(0|\\+84)\\d{9})$",
                message = "Số điện thoại không hợp lệ (ví dụ 0912345678 hoặc +84912345678)")
        String phone,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, max = 72, message = "Mật khẩu dài từ 8 đến 72 ký tự")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$", message = "Mật khẩu phải có cả chữ và số")
        String password,

        @NotNull(message = "Vui lòng chọn vai trò (FARMER hoặc BUYER)")
        Role role) {
}
