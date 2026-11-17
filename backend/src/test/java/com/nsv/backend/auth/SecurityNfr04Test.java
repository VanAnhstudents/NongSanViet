package com.nsv.backend.auth;

import com.nsv.backend.auth.service.AuthServiceImpl;
import com.nsv.backend.common.config.SecurityConfig;
import com.nsv.backend.common.security.HttpsEnforcementFilter;
import com.nsv.backend.common.security.JwtAuthenticationFilter;
import com.nsv.backend.common.security.JwtTokenProvider;
import com.nsv.backend.common.security.SecurityErrorHandlers;
import com.nsv.backend.user.entity.User;
import com.nsv.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityNfr04Test.DummyProtectedController.class)
@Import({
    SecurityConfig.class,
    HttpsEnforcementFilter.class,
    JwtAuthenticationFilter.class,
    JwtTokenProvider.class,
    SecurityErrorHandlers.class,
    SecurityNfr04Test.TestConfig.class
})
@TestPropertySource(properties = {
    "app.security.require-https=true",
    "app.security.jwt.secret=TestSecretKeyForNfr04SecurityVerificationMustBeLongEnough2026"
})
class SecurityNfr04Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("TC-SEC-01 (1a): Request HTTP GET bị redirect 308 sang HTTPS")
    void httpGetRequest_RedirectedToHttps() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .header("X-Forwarded-Proto", "http")
                .secure(false))
            .andExpect(status().isPermanentRedirect())
            .andExpect(header().string(HttpHeaders.LOCATION, "https://localhost/api/v1/products"));
    }

    @Test
    @DisplayName("TC-SEC-01 (1b): Request HTTP POST bị từ chối với mã 403 Forbidden")
    void httpPostRequest_Rejected() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .header("X-Forwarded-Proto", "http")
                .secure(false))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-SEC-01 (2): Mật khẩu lưu DB được băm BCrypt, không lưu dạng plaintext")
    void registerUser_PasswordHashedWithBCrypt() {
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.existsByPhone("0912345678")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthServiceImpl authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtTokenProvider);
        String rawPassword = "MatKhauBiMat@123";

        authService.register("0912345678", rawPassword, "Nguyễn Văn A", "FARMER");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        String storedHash = userCaptor.getValue().getPasswordHash();
        assertNotNull(storedHash);
        assertNotEquals(rawPassword, storedHash);
        assertTrue(storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$"));
        assertTrue(passwordEncoder.matches(rawPassword, storedHash));
    }

    @Test
    @DisplayName("TC-SEC-01 (3a): BUYER gọi API của ADMIN và FARMER bị từ chối 403 Forbidden")
    void buyerRole_AccessAdminOrFarmerApi_Forbidden() throws Exception {
        String buyerToken = jwtTokenProvider.generateToken(20L, "BUYER");

        mockMvc.perform(get("/api/v1/admin/users")
                .header("X-Forwarded-Proto", "https")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + buyerToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/farms")
                .header("X-Forwarded-Proto", "https")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + buyerToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-SEC-01 (3b): Request không có token bị từ chối 401; ADMIN truy cập đúng phạm vi thành công 200")
    void adminRole_AccessAdminApi_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                .header("X-Forwarded-Proto", "https"))
            .andExpect(status().isUnauthorized());

        String adminToken = jwtTokenProvider.generateToken(1L, "ADMIN");
        mockMvc.perform(get("/api/v1/admin/users")
                .header("X-Forwarded-Proto", "https")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    @Configuration
    static class TestConfig {
        @Bean
        DummyProtectedController dummyProtectedController() {
            return new DummyProtectedController();
        }
    }

    @RestController
    static class DummyProtectedController {
        @GetMapping("/api/v1/products")
        public String publicProducts() {
            return "ok";
        }

        @GetMapping("/api/v1/admin/users")
        public String adminUsers() {
            return "admin-ok";
        }

        @PostMapping("/api/v1/farms")
        public String createFarm() {
            return "farm-ok";
        }
    }
}
