package com.nsv.backend.user.service;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.user.entity.Role;
import com.nsv.backend.user.entity.User;
import com.nsv.backend.user.entity.UserStatus;
import com.nsv.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Quản lý trạng thái tài khoản (module admin gọi qua service này). */
@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserRepository userRepository;

    /** Khóa tài khoản: users.status = LOCKED. Không khóa được Admin; khóa rồi thì 409. */
    @Transactional
    public void lock(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy tài khoản"));
        if (user.getRole() == Role.ADMIN) {
            throw ApiException.forbidden("Không thể khóa tài khoản Admin");
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new ApiException(HttpStatus.CONFLICT, "INVALID_STATE", "Tài khoản đã bị khóa", null);
        }
        user.setStatus(UserStatus.LOCKED);
        userRepository.save(user);
    }
}
