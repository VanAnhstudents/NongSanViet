package com.nsv.backend.user.service;

import com.nsv.backend.user.dto.UserDto;
import com.nsv.backend.user.entity.UserStatus;
import com.nsv.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Tra cứu tài khoản cho các module khác (module chat gọi qua service này, không truy cập bảng users). */
@Service
@RequiredArgsConstructor
public class UserLookupService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<UserDto> findActiveById(Long id) {
        return userRepository.findById(id)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(u -> new UserDto(u.getId(), u.getFullName(), u.getPhone(), u.getRole()));
    }
}
