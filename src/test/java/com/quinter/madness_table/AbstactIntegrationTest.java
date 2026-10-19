package com.quinter.madness_table;

import com.quinter.madness_table.auth.LoginRequest;
import com.quinter.madness_table.auth.RegisterRequest;
import com.quinter.madness_table.auth.TokenResponse;
import com.quinter.madness_table.auth.UserResponse;
import com.quinter.madness_table.campaign.CampaignResponse;
import com.quinter.madness_table.campaign.CreateCampaignRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
public abstract class AbstactIntegrationTest {

    protected static final String PASSWORD = "ClaveSegura123";

    @Autowired
    protected TestRestTemplate restTemplate;

    public record TestUser(Long id, String token){
    }

    protected TestUser createUser(String name){
        String email = name + "-" + UUID.randomUUID() + "@example.com";

        UserResponse user = restTemplate.postForEntity("/api/auth/register",
                new RegisterRequest(email, name, PASSWORD), UserResponse.class).getBody();

        TokenResponse token = restTemplate.postForEntity("/api/auth/login",
                new LoginRequest(email, PASSWORD), TokenResponse.class).getBody();

        return new TestUser(user.id(), token.accessToken());
    }

    protected CampaignResponse createCampaign(TestUser guardian, String name) {
        return post("/api/campaigns", guardian.token(),
                new CreateCampaignRequest(name, "Descripción de prueba"),
                CampaignResponse.class).getBody();
    }

    protected <T> ResponseEntity<T> get(String url, String token, Class<T> type) {
        return send(HttpMethod.GET, url, token, null, type);
    }

    protected <T> ResponseEntity<T> post(String url, String token, Object body, Class<T> type) {
        return send(HttpMethod.POST, url, token, body, type);
    }

    protected <T> ResponseEntity<T> delete(String url, String token, Class<T> type) {
        return send(HttpMethod.DELETE, url, token, null, type);
    }

    private <T> ResponseEntity<T> send(HttpMethod method, String url, String token,
                                       Object body, Class<T> type) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), type);
    }

}
