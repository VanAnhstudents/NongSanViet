package com.nsv.backend.auth;

import com.nsv.backend.auth.security.AccountStatusInterceptor;
import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.common.security.AuthenticatedUser;
import com.nsv.backend.user.dto.UserDto;
import com.nsv.backend.user.entity.Role;
import com.nsv.backend.user.service.UserLookupService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountStatusInterceptorTest {

    @Mock
    private UserLookupService userLookupService;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(long id) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(id, "BUYER"), null, List.of()));
    }

    @Test
    @DisplayName("TC-ADMIN-01: Tài khoản đã bị khóa dùng token cũ -> bị từ chối (403)")
    void tc01_lockedAccountIsRejected() {
        loginAs(5L);
        when(userLookupService.findActiveById(5L)).thenReturn(Optional.empty());
        AccountStatusInterceptor interceptor = new AccountStatusInterceptor(userLookupService);

        assertThatThrownBy(() -> interceptor.preHandle(new MockHttpServletRequest(),
                new MockHttpServletResponse(), new Object())).isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("TC-ADMIN-01: Tài khoản còn ACTIVE vẫn thao tác bình thường")
    void tc01_activeAccountPasses() {
        loginAs(5L);
        when(userLookupService.findActiveById(5L))
                .thenReturn(Optional.of(new UserDto(5L, "Cô Lan", null, "0988888888", Role.BUYER)));
                .thenReturn(Optional.of(new UserDto(5L, "Cô Lan", "colan@example.com", "0988888888", Role.BUYER)));
        AccountStatusInterceptor interceptor = new AccountStatusInterceptor(userLookupService);

        assertThat(interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()))
                .isTrue();
    }
}
