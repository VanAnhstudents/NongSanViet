package com.nsv.backend.admin.service;

import com.nsv.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Tạo tài khoản Admin đầu tiên từ biến môi trường ADMIN_PHONE / ADMIN_PASSWORD (nếu có). */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserService userService;

    @Value("${app.admin.phone:}")
    private String phone;

    @Value("${app.admin.password:}")
    private String password;

    @Value("${app.admin.full-name:Quản trị viên}")
    private String fullName;

    @Override
    public void run(ApplicationArguments args) {
        if (phone.isBlank() || password.isBlank()) {
            return;
        }
        if (userService.ensureAdmin(phone, password, fullName)) {
            log.info("Đã tạo tài khoản Admin: {}", phone);
        }
    }
}
