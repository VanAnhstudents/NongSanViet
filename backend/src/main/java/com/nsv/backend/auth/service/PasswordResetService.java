package com.nsv.backend.auth.service;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.common.exception.FieldErrorDetail;
import com.nsv.backend.common.util.IdentifierNormalizer;
import com.nsv.backend.user.service.UserPasswordService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quên mật khẩu bằng OTP (PBI-15).
 * Schema hiện không có bảng lưu OTP nên mã được giữ trong bộ nhớ (chỉ lưu bản băm, có hạn dùng).
 * Hệ quả: khởi động lại ứng dụng thì các OTP đang chờ mất hiệu lực.
 */
@Service
public class PasswordResetService {

    static final int MAX_ATTEMPTS = 5;

    private final UserPasswordService userPasswordService;
    private final PasswordEncoder passwordEncoder;
    private final OtpSender otpSender;
    private final Clock clock;
    private final Duration ttl;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();

    public PasswordResetService(UserPasswordService userPasswordService, PasswordEncoder passwordEncoder,
            OtpSender otpSender, Clock clock, @Value("${app.otp.ttl-minutes:5}") long ttlMinutes) {
        this.userPasswordService = userPasswordService;
        this.passwordEncoder = passwordEncoder;
        this.otpSender = otpSender;
        this.clock = clock;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    /**
     * Gửi OTP nếu số điện thoại thuộc một tài khoản ACTIVE. Số không tồn tại cũng không báo lỗi
     * để không lộ số nào đã đăng ký.
     */
    public void requestOtp(String phone) {
        String norm = IdentifierNormalizer.normalizePhone(phone);
        if (norm == null) {
            return;
        }
        userPasswordService.findActiveUserIdByPhone(norm).ifPresent(userId -> {
            String otp = String.format("%06d", random.nextInt(1_000_000));
            store.put(norm, new OtpEntry(passwordEncoder.encode(otp), clock.instant().plus(ttl)));
            otpSender.send(norm, otp);
        });
    }

    /** Chỉ đặt mật khẩu mới sau khi OTP đúng và còn hạn. */
    public void resetPassword(String phone, String otp, String newPassword) {
        String norm = IdentifierNormalizer.normalizePhone(phone);
        OtpEntry entry = norm == null ? null : store.get(norm);
        if (entry == null) {
            throw otpError("Mã OTP không đúng hoặc chưa được yêu cầu");
        }
        synchronized (entry) {
            if (clock.instant().isAfter(entry.expiresAt)) {
                store.remove(norm);
                throw otpError("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới");
            }
            if (entry.attempts >= MAX_ATTEMPTS) {
                store.remove(norm);
                throw otpError("Bạn đã nhập sai quá nhiều lần. Vui lòng yêu cầu mã mới");
            }
            if (!passwordEncoder.matches(otp, entry.otpHash)) {
                entry.attempts++;
                throw otpError("Mã OTP không đúng");
            }
            Long userId = userPasswordService.findActiveUserIdByPhone(norm)
                    .orElseThrow(() -> otpError("Mã OTP không đúng hoặc chưa được yêu cầu"));
            userPasswordService.changePassword(userId, newPassword);
            store.remove(norm);
        }
    }

    private ApiException otpError(String message) {
        return ApiException.badRequest("Dữ liệu không hợp lệ", List.of(new FieldErrorDetail("otp", message)));
    }

    private static final class OtpEntry {
        private final String otpHash;
        private final Instant expiresAt;
        private int attempts;

        private OtpEntry(String otpHash, Instant expiresAt) {
            this.otpHash = otpHash;
            this.expiresAt = expiresAt;
        }
    }
}
