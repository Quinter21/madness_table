package com.quinter.madness_table;

import com.quinter.madness_table.auth.LoginRequest;
import com.quinter.madness_table.auth.RegisterRequest;
import com.quinter.madness_table.auth.TokenResponse;
import com.quinter.madness_table.auth.UserResponse;
import com.quinter.madness_table.campaign.CampaignResponse;
import com.quinter.madness_table.campaign.CreateCampaignRequest;
import com.quinter.madness_table.campaign.JoinCampaignRequest;
import com.quinter.madness_table.campaign.JoinRequestResponse;
import com.quinter.madness_table.investigator.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

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

    protected TestUser createPlayerInCampaign(TestUser guardian, CampaignResponse campaign, String name) {
        TestUser player = createUser(name);
        JoinRequestResponse request = post("/api/campaigns/join", player.token(),
                new JoinCampaignRequest(campaign.inviteCode()), JoinRequestResponse.class).getBody();

        ResponseEntity<String> approved = post(
                "/api/campaigns/" + campaign.id() + "/requests/" + request.id() + "/approve",
                guardian.token(), null, String.class);

        assertThat(approved.getStatusCode()).isEqualTo(HttpStatus.OK);
        return player;
    }

    protected InvestigatorResponse createInvestigator(TestUser owner, Long campaignId, String name) {
        ResponseEntity<InvestigatorResponse> response = post("/api/campaigns/" + campaignId + "/investigators",
                owner.token(), new CreateInvestigatorRequest(name, "Detective", 16, "Blablabla",
                        new CharacteristicsDto(50, 50, 50, 50,
                                50, 50, 50, 50)), InvestigatorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    protected SkillResponse createSkill(TestUser guardian, Long campaignId, String name, int baseValue) {
        ResponseEntity<SkillResponse> response = post("/api/campaigns/" + campaignId + "/skills",
                guardian.token(), new SkillRequest(name, baseValue), SkillResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    protected <T> ResponseEntity<T> get(String url, String token, Class<T> type) {
        return send(HttpMethod.GET, url, token, null, type);
    }

    protected <T> ResponseEntity<T> post(String url, String token, Object body, Class<T> type) {
        return send(HttpMethod.POST, url, token, body, type);
    }

    protected <T> ResponseEntity<T> put(String url, String token, Object body, Class<T> type) {
        return send(HttpMethod.PUT, url, token, body, type);
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
