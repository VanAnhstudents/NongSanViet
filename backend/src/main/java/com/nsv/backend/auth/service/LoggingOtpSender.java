package com.nsv.backend.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** OTP mô phỏng (theo SRS: "Email/OTP mô phỏng đơn giản"): ghi mã ra log thay vì gửi SMS thật. */
@Slf4j
@Component
public class LoggingOtpSender implements OtpSender {

    @Override
    public void send(String phone, String otp) {
        log.info("[OTP mô phỏng] Mã đặt lại mật khẩu gửi tới {}: {}", phone, otp);
    }
}
