package com.nsv.backend.user;

import com.nsv.backend.user.entity.Role;
import com.nsv.backend.user.entity.User;
import com.nsv.backend.user.entity.UserStatus;
import com.nsv.backend.user.repository.UserRepository;
import com.nsv.backend.user.service.UserAccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TcAdmin01AccountTest {

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("TC-ADMIN-01: Admin khóa tài khoản -> users.status chuyển sang LOCKED")
    void tc01_lockSetsStatusLocked() {
        User user = User.builder().id(5L).phone("0988888888").passwordHash("x").fullName("Cô Lan")
                .role(Role.BUYER).status(UserStatus.ACTIVE).build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        new UserAccountService(userRepository).lock(5L);

        assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
        verify(userRepository).save(user);
    }
}
