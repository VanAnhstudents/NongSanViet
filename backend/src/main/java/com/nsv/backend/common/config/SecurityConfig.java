package com.nsv.backend.common.config;

import com.nsv.backend.common.security.HttpsEnforcementFilter;
import com.nsv.backend.common.security.JwtAuthenticationFilter;
import com.nsv.backend.common.security.SecurityErrorHandlers;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final HttpsEnforcementFilter httpsEnforcementFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityErrorHandlers securityErrorHandlers;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Băm mật khẩu bằng BCrypt (cost factor = 12)
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(securityErrorHandlers)
                .accessDeniedHandler(securityErrorHandlers)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/auth/**", "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**", "/api/v1/combos/**", "/api/v1/trace/**", "/api/v1/batches/*/trace").permitAll()

                // Phân quyền rõ ràng theo 3 vai trò (ADMIN, FARMER, BUYER)
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/farms/**", "/api/v1/farmer/**").hasRole("FARMER")
                .requestMatchers("/api/v1/cart/**", "/api/v1/addresses/**").hasRole("BUYER")

                // Các API yêu cầu đăng nhập (kiểm tra quyền chi tiết thêm ở tầng Service/@PreAuthorize)
                .anyRequest().authenticated()
            )
            .addFilterBefore(httpsEnforcementFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
