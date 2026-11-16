package com.nsv.backend.common;

import com.nsv.backend.common.config.JwtProperties;
import com.nsv.backend.common.security.AuthenticatedUser;
import com.nsv.backend.common.security.JwtService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-with-at-least-32-bytes!!";

    private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, 60));

    @Test
    void generateThenParse_returnsSameUserAndRole() {
        String token = jwtService.generateToken(7L, "FARMER");

        assertThat(jwtService.parse(token)).contains(new AuthenticatedUser(7L, "FARMER"));
    }

    @Test
    void parse_tamperedToken_returnsEmpty() {
        String token = jwtService.generateToken(7L, "FARMER");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.parse(tampered)).isEmpty();
    }

    @Test
    void parse_tokenSignedWithOtherSecret_returnsEmpty() {
        JwtService other = new JwtService(new JwtProperties("another-secret-key-with-at-least-32-bytes!!", 60));

        assertThat(jwtService.parse(other.generateToken(1L, "ADMIN"))).isEmpty();
    }

    @Test
    void parse_expiredToken_returnsEmpty() {
        JwtService expired = new JwtService(new JwtProperties(SECRET, -1));

        assertThat(jwtService.parse(expired.generateToken(1L, "BUYER"))).isEmpty();
    }

    @Test
    void parse_garbage_returnsEmpty() {
        assertThat(jwtService.parse("not-a-jwt")).isEmpty();
    }

    @Test
    void constructor_shortSecret_isRejected() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties("short", 60)))
                .isInstanceOf(IllegalStateException.class);
    }
}
