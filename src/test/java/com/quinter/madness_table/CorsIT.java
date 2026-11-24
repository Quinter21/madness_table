package com.quinter.madness_table;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

class CorsIT extends AbstractIntegrationTest {

    private ResponseEntity<String> preflight(String origin) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ORIGIN, origin);
        headers.set(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
        headers.set(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization, content-type");
        return restTemplate.exchange("/api/campaigns", HttpMethod.OPTIONS,
                new HttpEntity<>(headers), String.class);
    }

    @Test
    void theAngularOriginPassesThePreflightWithoutAToken() {
        ResponseEntity<String> response = preflight("http://localhost:4200");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getAccessControlAllowOrigin())
                .isEqualTo("http://localhost:4200");
    }

    @Test
    void anyOtherOriginIsRejected() {
        ResponseEntity<String> response = preflight("http://example.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getHeaders().getAccessControlAllowOrigin()).isNull();
    }
}
