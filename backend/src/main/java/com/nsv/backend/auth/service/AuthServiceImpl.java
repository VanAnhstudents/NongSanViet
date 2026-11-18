package com.nsv.backend.auth.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.common.security.JwtTokenProvider;
import com.nsv.backend.user.entity.Role;
import com.nsv.backend.user.entity.User;
import com.nsv.backend.user.entity.UserStatus;
import com.nsv.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public User register(String phone, String rawPassword, String fullName, String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "FORBIDDEN_ROLE", "Không thể tự đăng ký tài khoản Quản trị viên");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new BusinessException(HttpStatus.CONFLICT, "PHONE_EXISTS", "Số điện thoại đã được đăng ký");
        }

        // Bắt buộc băm mật khẩu bằng BCrypt trước khi lưu xuống database
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
            .phone(phone.trim())
            .passwordHash(hashedPassword)
            .fullName(fullName.trim())
            .role(Role.valueOf(role.toUpperCase()))
            .status(UserStatus.ACTIVE)
            .build();

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String login(String phone, String rawPassword) {
        User user = userRepository.findByPhone(phone.trim())
            .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Sai số điện thoại hoặc mật khẩu"));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCOUNT_LOCKED", "Tài khoản đã bị khóa");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Sai số điện thoại hoặc mật khẩu");
        }

        return jwtTokenProvider.generateToken(user.getId(), user.getRole().name());
    }
}
