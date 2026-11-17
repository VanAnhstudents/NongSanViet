package com.nsv.backend.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpsEnforcementFilter extends OncePerRequestFilter {

    private final boolean requireHttps;

    public HttpsEnforcementFilter(@Value("${app.security.require-https:false}") boolean requireHttps) {
        this.requireHttps = requireHttps;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!requireHttps || isHttpsRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Redirect GET/HEAD sang HTTPS; từ chối trực tiếp các API mutation (POST/PUT/DELETE...)
        if ("GET".equalsIgnoreCase(request.getMethod()) || "HEAD".equalsIgnoreCase(request.getMethod())) {
            String httpsUrl = buildHttpsUrl(request);
            response.setStatus(HttpStatus.PERMANENT_REDIRECT.value());
            response.setHeader(HttpHeaders.LOCATION, httpsUrl);
            return;
        }

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
            {"code":"HTTPS_REQUIRED","message":"Hệ thống chỉ chấp nhận kết nối bảo mật qua HTTPS"}
            """);
    }

    private boolean isHttpsRequest(HttpServletRequest request) {
        if (request.isSecure()) {
            return true;
        }
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return "https".equalsIgnoreCase(forwardedProto);
    }

    private String buildHttpsUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder("https://")
            .append(request.getServerName())
            .append(request.getRequestURI());
        if (request.getQueryString() != null) {
            url.append('?').append(request.getQueryString());
        }
        return url.toString();
    }
}
