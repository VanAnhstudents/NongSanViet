package com.nsv.backend.auth.security;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.common.security.AuthenticatedUser;
import com.nsv.backend.user.service.UserLookupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Mỗi request đã xác thực đều kiểm tra tài khoản còn ACTIVE. Nhờ vậy tài khoản bị khóa mất quyền thao tác ngay,
 * dù JWT cũ chưa hết hạn (JWT chỉ chứa id và vai trò). Mỗi request có thêm một truy vấn users theo khóa chính.
 */
@Component
@RequiredArgsConstructor
public class AccountStatusInterceptor implements HandlerInterceptor {

    private final UserLookupService userLookupService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user
                && userLookupService.findActiveById(user.id()).isEmpty()) {
            throw ApiException.forbidden("Tài khoản đã bị khóa hoặc không còn hoạt động");
        }
        return true;
    }
}
