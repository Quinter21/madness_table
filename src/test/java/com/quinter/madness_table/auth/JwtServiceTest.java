package com.quinter.madness_table.auth;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-0123456789";
    private static final String OTHER_SECRET = "another-secret-another-secret-another-secret-99";

    private JwtService serviceWith(String secret, long expirationMinutes) {
        return new JwtService(new JwtProperties(secret, expirationMinutes));
    }

    @Test
    void generatedTokenIsValidAndReturnsTheUserId() {
        JwtService service = serviceWith(SECRET, 60);

        String token = service.generateToken(42L, "angel@example.com");

        assertThat(service.validateAndGetUserId(token)).isEqualTo(42L);
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        String token = serviceWith(SECRET, 60).generateToken(1L, "angel@example.com");
        JwtService otherServer = serviceWith(OTHER_SECRET, 60);

        assertThatThrownBy(() -> otherServer.validateAndGetUserId(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService service = serviceWith(SECRET, -1);
        String token = service.generateToken(1L, "angel@example.com");

        assertThatThrownBy(() -> service.validateAndGetUserId(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void garbageIsRejected() {
        JwtService service = serviceWith(SECRET, 60);

        assertThatThrownBy(() -> service.validateAndGetUserId("estoNoValeComoToken"))
                .isInstanceOf(JwtException.class);
    }
}
