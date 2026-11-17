package com.nsv.backend.user.service;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.user.entity.User;
import com.nsv.backend.user.entity.UserStatus;
import com.nsv.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Dịch vụ của module user cho luồng quên mật khẩu (module auth chỉ gọi qua service này). */
@Service
@RequiredArgsConstructor
public class UserPasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Trả về id của tài khoản ACTIVE ứng với số điện thoại (đã chuẩn hóa), nếu có. */
    @Transactional(readOnly = true)
    public Optional<Long> findActiveUserIdByPhone(String normalizedPhone) {
        return userRepository.findByPhone(normalizedPhone)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(User::getId);
    }

    @Transactional
    public void changePassword(Long userId, String rawPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy tài khoản"));
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }
}
