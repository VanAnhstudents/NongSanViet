package com.nsv.backend.auth.service;

/** Kênh gửi OTP. Hiện mô phỏng bằng log; sau này thay bằng nhà cung cấp SMS mà không đổi nghiệp vụ. */
public interface OtpSender {

    void send(String phone, String otp);
}
