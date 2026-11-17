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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userService, jwtService, new MenuProvider());
    }

    @Test
    void register_farmer_returnsTokenAndFarmerMenus() {
        UserDto farmer = new UserDto(1L, "Bác Ba", "ba@mail.com", null, Role.FARMER);
        when(userService.createUser("Bác Ba", "ba@mail.com", null, "Matkhau123", Role.FARMER)).thenReturn(farmer);
        when(jwtService.generateToken(1L, "FARMER")).thenReturn("token-1");
        when(jwtService.expiresInSeconds()).thenReturn(3600L);

        AuthResponse res = authService.register(
                new RegisterRequest("Bác Ba", "ba@mail.com", null, "Matkhau123", Role.FARMER));

        assertThat(res.accessToken()).isEqualTo("token-1");
        assertThat(res.tokenType()).isEqualTo("Bearer");
        assertThat(res.user().role()).isEqualTo(Role.FARMER);
        assertThat(res.menus()).extracting("code").contains("products", "harvest-schedule");
    }

    @Test
    void register_adminRole_isRejectedWithoutTouchingUserModule() {
        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Hacker", "h@mail.com", null, "Matkhau123", Role.ADMIN)))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getFieldErrors()).extracting("field").containsExactly("role");
                });
        verifyNoInteractions(userService);
    }

    @Test
    void login_buyer_getsOnlyBuyerMenus() {
        UserDto buyer = new UserDto(2L, "Lan", "lan@mail.com", null, Role.BUYER);
        when(userService.authenticate("lan@mail.com", "Matkhau123")).thenReturn(buyer);
        when(jwtService.generateToken(2L, "BUYER")).thenReturn("token-2");
        when(jwtService.expiresInSeconds()).thenReturn(3600L);

        AuthResponse res = authService.login(new LoginRequest("lan@mail.com", "Matkhau123"));

        assertThat(res.menus()).extracting("code").contains("shop", "cart")
                .doesNotContain("products", "accounts");
    }
}
