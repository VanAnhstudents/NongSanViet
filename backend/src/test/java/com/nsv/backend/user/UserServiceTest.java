package com.nsv.backend.user;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.user.dto.UserDto;
import com.nsv.backend.user.entity.Role;
import com.nsv.backend.user.entity.User;
import com.nsv.backend.user.entity.UserStatus;
import com.nsv.backend.user.repository.UserRepository;
import com.nsv.backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Kiểm thử PBI-01 / TC-AUTH-01 ở mức service. */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, encoder);
    }

    // ---- Đăng ký ----

    @Test
    void createUser_validData_savesNormalizedAndHashedPassword() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(false);
        when(userRepository.existsByPhone("0912345678")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserDto dto = userService.createUser(" Nguyễn Văn A ", " A@B.com ", "+84912345678", "Matkhau123", Role.FARMER);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.email()).isEqualTo("a@b.com");
        assertThat(dto.phone()).isEqualTo("0912345678");
        assertThat(dto.role()).isEqualTo(Role.FARMER);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getFullName()).isEqualTo("Nguyễn Văn A");
        assertThat(saved.getPasswordHash()).isNotEqualTo("Matkhau123");
        assertThat(encoder.matches("Matkhau123", saved.getPasswordHash())).isTrue();
    }

    @Test
    void createUser_duplicateEmail_throwsConflictWithFieldError() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser("A", "a@b.com", null, "Matkhau123", Role.BUYER))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getFieldErrors()).extracting("field").containsExactly("email");
                });
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void createUser_duplicatePhone_throwsConflictWithFieldError() {
        when(userRepository.existsByPhone("0912345678")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser("A", null, "0912345678", "Matkhau123", Role.BUYER))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getFieldErrors()).extracting("field").containsExactly("phone");
                });
    }

    @Test
    void createUser_noEmailAndNoPhone_throwsBadRequest() {
        assertThatThrownBy(() -> userService.createUser("A", " ", null, "Matkhau123", Role.BUYER))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // ---- Đăng nhập ----

    private User existingUser(UserStatus status) {
        return User.builder().id(5L).fullName("Lan").email("lan@mail.com").phone("0988888888")
                .passwordHash(encoder.encode("Matkhau123")).role(Role.BUYER).status(status).build();
    }

    @Test
    void authenticate_byEmail_success() {
        when(userRepository.findByEmail("lan@mail.com")).thenReturn(Optional.of(existingUser(UserStatus.ACTIVE)));

        UserDto dto = userService.authenticate(" Lan@Mail.com ", "Matkhau123");

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.role()).isEqualTo(Role.BUYER);
    }

    @Test
    void authenticate_byPhone_success() {
        when(userRepository.findByPhone("0988888888")).thenReturn(Optional.of(existingUser(UserStatus.ACTIVE)));

        assertThat(userService.authenticate("+84988888888", "Matkhau123").id()).isEqualTo(5L);
    }

    @Test
    void authenticate_wrongPassword_throwsUnauthorized() {
        when(userRepository.findByEmail("lan@mail.com")).thenReturn(Optional.of(existingUser(UserStatus.ACTIVE)));

        assertThatThrownBy(() -> userService.authenticate("lan@mail.com", "SaiMatKhau1"))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void authenticate_unknownAccount_throwsUnauthorized() {
        when(userRepository.findByEmail("none@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.authenticate("none@mail.com", "Matkhau123"))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void authenticate_lockedAccount_throwsForbidden() {
        when(userRepository.findByEmail("lan@mail.com")).thenReturn(Optional.of(existingUser(UserStatus.LOCKED)));

        assertThatThrownBy(() -> userService.authenticate("lan@mail.com", "Matkhau123"))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }
}
