package com.nsv.backend.auth;

import com.nsv.backend.auth.service.OtpSender;
import com.nsv.backend.auth.service.PasswordResetService;
import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.user.service.UserPasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TcAuth02Test {

    private static final String PHONE = "0912345678";
    private static final long USER_ID = 5L;

    /** Đồng hồ chỉnh được, bắt đầu từ 2026-11-17. */
    static class TestClock extends Clock {
        private Instant now = Instant.parse("2026-11-17T03:00:00Z");

        void advanceMinutes(long minutes) {
            now = now.plusSeconds(minutes * 60);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    @Mock
    private UserPasswordService userPasswordService;

    @Mock
    private OtpSender otpSender;

    private TestClock clock;
    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        clock = new TestClock();
        service = new PasswordResetService(userPasswordService, new BCryptPasswordEncoder(), otpSender, clock, 5);
        when(userPasswordService.findActiveUserIdByPhone(PHONE)).thenReturn(Optional.of(USER_ID));
    }

    private String requestAndCaptureOtp() {
        service.requestOtp(PHONE);
        ArgumentCaptor<String> otp = ArgumentCaptor.forClass(String.class);
        verify(otpSender).send(eq(PHONE), otp.capture());
        return otp.getValue();
    }

    @Test
    @DisplayName("TC-AUTH-02: SĐT đã đăng ký nhận được mã OTP 6 chữ số")
    void tc02_registeredPhoneReceivesOtp() {
        assertThat(requestAndCaptureOtp()).matches("\\d{6}");
    }

    @Test
    @DisplayName("TC-AUTH-02: OTP đúng và còn hạn -> đặt lại mật khẩu thành công")
    void tc02_correctOtpChangesPassword() {
        String otp = requestAndCaptureOtp();

        service.resetPassword(PHONE, otp, "Matkhau456");

        verify(userPasswordService).changePassword(USER_ID, "Matkhau456");
    }

    @Test
    @DisplayName("TC-AUTH-02: OTP sai -> bị từ chối, mật khẩu không đổi")
    void tc02_wrongOtpIsRejected() {
        String otp = requestAndCaptureOtp();
        String wrong = otp.equals("000000") ? "111111" : "000000";

        assertThatThrownBy(() -> service.resetPassword(PHONE, wrong, "Matkhau456"))
                .isInstanceOf(ApiException.class);

        verify(userPasswordService, never()).changePassword(anyLong(), anyString());
    }

    @Test
    @DisplayName("TC-AUTH-02: OTP hết hạn sau 5 phút -> bị từ chối, mật khẩu không đổi")
    void tc02_expiredOtpIsRejected() {
        String otp = requestAndCaptureOtp();
        clock.advanceMinutes(6);

        assertThatThrownBy(() -> service.resetPassword(PHONE, otp, "Matkhau456"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Dữ liệu không hợp lệ");

        verify(userPasswordService, never()).changePassword(anyLong(), any());
    }
}
