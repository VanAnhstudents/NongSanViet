package com.nsv.backend.user.service;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.common.exception.FieldErrorDetail;
import com.nsv.backend.common.util.IdentifierNormalizer;
import com.nsv.backend.user.dto.UserDto;
import com.nsv.backend.user.entity.Role;
import com.nsv.backend.user.entity.User;
import com.nsv.backend.user.entity.UserStatus;
import com.nsv.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Giao diện nội bộ của module user. Các module khác (auth, admin...) chỉ gọi qua
 * lớp này, không truy cập trực tiếp UserRepository.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto createUser(String fullName, String email, String phone, String rawPassword, Role role) {
        String normEmail = IdentifierNormalizer.normalizeEmail(email);
        String normPhone = IdentifierNormalizer.normalizePhone(phone);

        if (normEmail == null && normPhone == null) {
            throw ApiException.badRequest("Dữ liệu không hợp lệ",
                    List.of(new FieldErrorDetail("email", "Vui lòng nhập email hoặc số điện thoại")));
        }

        List<FieldErrorDetail> duplicates = new ArrayList<>();
        if (normEmail != null && userRepository.existsByEmail(normEmail)) {
            duplicates.add(new FieldErrorDetail("email", "Email này đã được sử dụng"));
        }
        if (normPhone != null && userRepository.existsByPhone(normPhone)) {
            duplicates.add(new FieldErrorDetail("phone", "Số điện thoại này đã được sử dụng"));
        }
        if (!duplicates.isEmpty()) {
            throw ApiException.conflict("Thông tin đăng ký đã tồn tại", duplicates);
        }

        User user = User.builder()
                .fullName(fullName.trim())
                .email(normEmail)
                .phone(normPhone)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
        try {
            return toDto(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException ex) {
            // Hai yêu cầu đăng ký cùng lúc: ràng buộc UNIQUE ở CSDL là lớp bảo vệ cuối.
            throw ApiException.conflict("Thông tin đăng ký đã tồn tại",
                    List.of(new FieldErrorDetail("email", "Email hoặc số điện thoại đã được sử dụng")));
        }
    }

    /** Xác thực bằng email hoặc số điện thoại + mật khẩu. */
    @Transactional(readOnly = true)
    public UserDto authenticate(String identifier, String rawPassword) {
        String id = identifier.trim();
        Optional<User> found = id.contains("@")
                ? userRepository.findByEmail(IdentifierNormalizer.normalizeEmail(id))
                : userRepository.findByPhone(IdentifierNormalizer.normalizePhone(id));

        User user = found
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()))
                .orElseThrow(() -> ApiException.unauthorized("Email/số điện thoại hoặc mật khẩu không chính xác"));

        if (user.getStatus() == UserStatus.PENDING) {
            throw ApiException.forbidden("Tài khoản đang chờ xác thực. Vui lòng liên hệ quản trị viên");
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw ApiException.forbidden("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên");
        }
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        return userRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy người dùng"));
    }

    /** Tạo tài khoản Admin nếu chưa có (dùng cho seed khi khởi động). */
    @Transactional
    public boolean ensureAdmin(String email, String rawPassword, String fullName) {
        String normEmail = IdentifierNormalizer.normalizeEmail(email);
        if (normEmail == null || userRepository.existsByEmail(normEmail)) {
            return false;
        }
        userRepository.save(User.builder()
                .fullName(fullName)
                .email(normEmail)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());
        return true;
    }

    private UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole());
    }
}
