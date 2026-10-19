package com.quinter.madness_table.auth;

import com.quinter.madness_table.AbstactIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIT extends AbstactIntegrationTest {

    private String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    @Test
    void registerReturns201AndNeverExposesThePassword() {
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/register",
                new RegisterRequest(uniqueEmail(), "Angel", PASSWORD), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).doesNotContain(PASSWORD).doesNotContain("password");
    }

    @Test
    void registeringTheSameEmailTwiceReturns409() {
        String email = uniqueEmail();
        restTemplate.postForEntity("/api/auth/register",
                new RegisterRequest(email, "Angel", PASSWORD), String.class);

        ResponseEntity<String> second = restTemplate.postForEntity("/api/auth/register",
                new RegisterRequest(email, "Otro", PASSWORD), String.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void registerWithInvalidDataReturns400() {
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/register",
                new RegisterRequest("no-es-un-email", "Angel", "123"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void loginWithWrongPasswordReturns401() {
        String email = uniqueEmail();
        restTemplate.postForEntity("/api/auth/register",
                new RegisterRequest(email, "Angel", PASSWORD), String.class);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login",
                new LoginRequest(email, "contraseña-incorrecta"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedEndpointWithoutTokenReturns401() {
        assertThat(get("/api/auth/me", null, String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(get("/api/campaigns", null, String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedEndpointWithGarbageTokenReturns401() {
        assertThat(get("/api/auth/me", "esto.no.es.un.jwt", String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void validTokenIdentifiesTheUser() {
        TestUser user = createUser("angel");

        ResponseEntity<Map> response = get("/api/auth/me", user.token(), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("id")).isEqualTo(user.id().intValue());
    }
}
