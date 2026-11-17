package com.nsv.backend.auth.controller;

import com.nsv.backend.auth.dto.ForgotPasswordRequest;
import com.nsv.backend.auth.dto.MessageResponse;
import com.nsv.backend.auth.dto.ResetPasswordRequest;
import com.nsv.backend.auth.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestOtp(request.phone());
        return new MessageResponse("Nếu số điện thoại đã đăng ký, mã OTP đã được gửi tới số này");
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.phone(), request.otp(), request.newPassword());
        return new MessageResponse("Đặt lại mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới");
    }
}
