package com.nsv.backend.auth;

import com.nsv.backend.auth.dto.AuthResponse;
import com.nsv.backend.auth.dto.LoginRequest;
import com.nsv.backend.auth.dto.RegisterRequest;
import com.nsv.backend.auth.service.AuthService;
import com.nsv.backend.auth.service.MenuProvider;
import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.common.security.JwtService;
import com.nsv.backend.user.dto.UserDto;
import com.nsv.backend.user.entity.Role;
import com.nsv.backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * TC-AUTH-01 — PBI-01: Đăng ký, đăng nhập, phân quyền
 *
 * Phạm vi kiểm thử:
 *   (1a) Đăng ký thành công với vai trò FARMER → nhận token + menu đúng vai trò
 *   (1b) Đăng ký thành công với vai trò BUYER  → nhận token + menu đúng vai trò
 *   (1c) Chặn tự đăng ký vai trò ADMIN          → ném ApiException 400 với lỗi trường "role"
 *   (2a) Đăng nhập BUYER thành công             → menu chỉ chứa quyền của BUYER
 *   (2b) Đăng nhập FARMER thành công            → menu chỉ chứa quyền của FARMER
 *   (3)  Phân quyền menu ADMIN                  → menu chỉ chứa quyền quản trị
 */
@ExtendWith(MockitoExtension.class)
class TcAuth01Test {

    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userService, jwtService, new MenuProvider());
    }

    // ─────────────────────────── Đăng ký ───────────────────────────

    @Test
    @DisplayName("TC-AUTH-01 (1a): Đăng ký FARMER thành công → token Bearer + menu FARMER trả về")
    void register_farmer_receivesTokenAndFarmerMenus() {
        UserDto farmer = new UserDto(1L, "Nguyễn Văn A", "a@farm.vn", null, Role.FARMER);
        when(userService.createUser("Nguyễn Văn A", "a@farm.vn", null, "Matkhau1", Role.FARMER))
                .thenReturn(farmer);
        when(jwtService.generateToken(1L, "FARMER")).thenReturn("tok-farmer");
        when(jwtService.expiresInSeconds()).thenReturn(3600L);

        AuthResponse res = authService.register(
                new RegisterRequest("Nguyễn Văn A", "a@farm.vn", null, "Matkhau1", Role.FARMER));

        assertThat(res.accessToken()).isEqualTo("tok-farmer");
        assertThat(res.tokenType()).isEqualTo("Bearer");
        assertThat(res.expiresIn()).isEqualTo(3600L);
        assertThat(res.user().role()).isEqualTo(Role.FARMER);
        assertThat(res.menus()).extracting("code")
                .contains("products", "harvest-schedule", "farm-profile", "farmer-orders")
                .doesNotContain("shop", "cart", "accounts");
    }

    @Test
    @DisplayName("TC-AUTH-01 (1b): Đăng ký BUYER thành công → token Bearer + menu BUYER trả về")
    void register_buyer_receivesTokenAndBuyerMenus() {
        UserDto buyer = new UserDto(2L, "Trần Thị B", "b@buyer.vn", null, Role.BUYER);
        when(userService.createUser("Trần Thị B", "b@buyer.vn", null, "Matkhau2", Role.BUYER))
                .thenReturn(buyer);
        when(jwtService.generateToken(2L, "BUYER")).thenReturn("tok-buyer");
        when(jwtService.expiresInSeconds()).thenReturn(3600L);

        AuthResponse res = authService.register(
                new RegisterRequest("Trần Thị B", "b@buyer.vn", null, "Matkhau2", Role.BUYER));

        assertThat(res.accessToken()).isEqualTo("tok-buyer");
        assertThat(res.tokenType()).isEqualTo("Bearer");
        assertThat(res.user().role()).isEqualTo(Role.BUYER);
        assertThat(res.menus()).extracting("code")
                .contains("shop", "cart", "buyer-orders", "trace")
                .doesNotContain("products", "accounts");
    }

    @Test
    @DisplayName("TC-AUTH-01 (1c): Đăng ký vai trò ADMIN bị từ chối 400 — không gọi UserService")
    void register_adminRole_rejectedWith400_andUserServiceNotTouched() {
        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Hacker", "hack@evil.com", null, "Matkhau3", Role.ADMIN)))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getFieldErrors()).extracting("field").containsExactly("role");
                    assertThat(ex.getFieldErrors().get(0).message())
                            .contains("FARMER", "BUYER");
                });
        verifyNoInteractions(userService);
    }

    // ─────────────────────────── Đăng nhập ───────────────────────────

    @Test
    @DisplayName("TC-AUTH-01 (2a): Đăng nhập BUYER thành công → menu chỉ chứa quyền BUYER")
    void login_buyer_getsOnlyBuyerMenus() {
        UserDto buyer = new UserDto(3L, "Lê Thị C", "c@buyer.vn", null, Role.BUYER);
        when(userService.authenticate("c@buyer.vn", "Matkhau4")).thenReturn(buyer);
        when(jwtService.generateToken(3L, "BUYER")).thenReturn("tok-login-buyer");
        when(jwtService.expiresInSeconds()).thenReturn(3600L);

        AuthResponse res = authService.login(new LoginRequest("c@buyer.vn", "Matkhau4"));

        assertThat(res.accessToken()).isEqualTo("tok-login-buyer");
        assertThat(res.user().role()).isEqualTo(Role.BUYER);
        assertThat(res.menus()).extracting("code")
                .contains("shop", "cart")
                .doesNotContain("products", "harvest-schedule", "accounts", "complaints");
    }

    @Test
    @DisplayName("TC-AUTH-01 (2b): Đăng nhập FARMER thành công → menu chỉ chứa quyền FARMER")
    void login_farmer_getsOnlyFarmerMenus() {
        UserDto farmer = new UserDto(4L, "Phạm Văn D", "d@farm.vn", null, Role.FARMER);
        when(userService.authenticate("d@farm.vn", "Matkhau5")).thenReturn(farmer);
        when(jwtService.generateToken(4L, "FARMER")).thenReturn("tok-login-farmer");
        when(jwtService.expiresInSeconds()).thenReturn(3600L);

        AuthResponse res = authService.login(new LoginRequest("d@farm.vn", "Matkhau5"));

        assertThat(res.accessToken()).isEqualTo("tok-login-farmer");
        assertThat(res.user().role()).isEqualTo(Role.FARMER);
        assertThat(res.menus()).extracting("code")
                .contains("products", "harvest-schedule")
                .doesNotContain("shop", "cart", "accounts", "complaints");
    }

    // ─────────────────────────── Phân quyền menu ───────────────────────────

    @Test
    @DisplayName("TC-AUTH-01 (3): Đăng nhập ADMIN → menu chỉ chứa quyền quản trị, không có menu FARMER/BUYER")
    void login_admin_getsOnlyAdminMenus() {
        UserDto admin = new UserDto(99L, "Super Admin", "admin@nsv.vn", null, Role.ADMIN);
        when(userService.authenticate("admin@nsv.vn", "AdminPass9")).thenReturn(admin);
        when(jwtService.generateToken(99L, "ADMIN")).thenReturn("tok-admin");
        when(jwtService.expiresInSeconds()).thenReturn(3600L);

        AuthResponse res = authService.login(new LoginRequest("admin@nsv.vn", "AdminPass9"));

        assertThat(res.user().role()).isEqualTo(Role.ADMIN);
        assertThat(res.menus()).extracting("code")
                .contains("accounts", "product-moderation", "complaints", "reports")
                .doesNotContain("shop", "cart", "products", "harvest-schedule");
    }
}
