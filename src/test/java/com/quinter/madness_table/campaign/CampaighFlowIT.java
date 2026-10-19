package com.quinter.madness_table.campaign;

import com.quinter.madness_table.AbstactIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CampaighFlowIT extends AbstactIntegrationTest {

    private JoinRequestResponse requestToJoin(TestUser user, String inviteCode) {
        ResponseEntity<JoinRequestResponse> response = post("/api/campaigns/join",
                user.token(), new JoinCampaignRequest(inviteCode), JoinRequestResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    @Test
    void creatorBecomesGuardianAndSeesTheCampaign() {
        TestUser guardian = createUser("guardian");

        CampaignResponse campaign = createCampaign(guardian, "Las Máscaras de Nyarlathotep");

        assertThat(campaign.myRole()).isEqualTo(CampaignRole.GUARDIAN);
        assertThat(campaign.inviteCode()).hasSize(8);

        ResponseEntity<CampaignResponse[]> mine = get("/api/campaigns", guardian.token(),
                CampaignResponse[].class);
        assertThat(mine.getBody()).extracting(CampaignResponse::id).contains(campaign.id());
    }

    @Test
    void fullJoinFlowWithGuardianApproval() {
        TestUser guardian = createUser("guardian");
        TestUser player = createUser("player");
        CampaignResponse campaign = createCampaign(guardian, "Campaña de prueba");
        String base = "/api/campaigns/" + campaign.id();

        // El jugador solicita unirse: queda PENDING y aún no ve nada
        JoinRequestResponse request = requestToJoin(player, campaign.inviteCode());
        assertThat(request.status()).isEqualTo(JoinRequestStatus.PENDING);
        assertThat(get(base, player.token(), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get("/api/campaigns", player.token(), CampaignResponse[].class).getBody())
                .isEmpty();

        // Una segunda solicitud pendiente es conflicto
        assertThat(post("/api/campaigns/join", player.token(),
                new JoinCampaignRequest(campaign.inviteCode()), String.class).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        // El Guardián la ve y la aprueba
        ResponseEntity<PendingRequestResponse[]> pending = get(base + "/requests",
                guardian.token(), PendingRequestResponse[].class);
        assertThat(pending.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pending.getBody()).extracting(PendingRequestResponse::id)
                .containsExactly(request.id());

        ResponseEntity<MemberResponse> approved = post(
                base + "/requests/" + request.id() + "/approve",
                guardian.token(), null, MemberResponse.class);
        assertThat(approved.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(approved.getBody().role()).isEqualTo(CampaignRole.PLAYER);

        // Aprobar otra vez la misma solicitud es conflicto
        assertThat(post(base + "/requests/" + request.id() + "/approve",
                guardian.token(), null, String.class).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        // Ahora el jugador ve la campaña con rol PLAYER
        ResponseEntity<CampaignResponse> seen = get(base, player.token(), CampaignResponse.class);
        assertThat(seen.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(seen.getBody().myRole()).isEqualTo(CampaignRole.PLAYER);
    }

    @Test
    void playerCannotUseGuardianEndpoints() {
        TestUser guardian = createUser("guardian");
        TestUser player = createUser("player");
        CampaignResponse campaign = createCampaign(guardian, "Campaña de prueba");
        String base = "/api/campaigns/" + campaign.id();
        JoinRequestResponse request = requestToJoin(player, campaign.inviteCode());
        post(base + "/requests/" + request.id() + "/approve", guardian.token(), null,
                String.class);

        assertThat(get(base + "/requests", player.token(), String.class).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(post(base + "/invite-code/regenerate", player.token(), null, String.class)
                .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(delete(base + "/members/" + guardian.id(), player.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Pero un miembro sí puede ver la lista de miembros
        ResponseEntity<MemberResponse[]> members = get(base + "/members", player.token(),
                MemberResponse[].class);
        assertThat(members.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(members.getBody()).hasSize(2);
    }

    @Test
    void outsiderCannotSeeAnythingAboutTheCampaign() {
        TestUser guardian = createUser("guardian");
        TestUser outsider = createUser("outsider");
        CampaignResponse campaign = createCampaign(guardian, "Campaña privada");
        String base = "/api/campaigns/" + campaign.id();

        assertThat(get(base, outsider.token(), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get(base + "/members", outsider.token(), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get(base + "/requests", outsider.token(), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(post(base + "/invite-code/regenerate", outsider.token(), null, String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void guardianOfOneCampaignCannotManageRequestsOfAnother() {
        TestUser guardianA = createUser("guardianA");
        TestUser guardianB = createUser("guardianB");
        TestUser applicant = createUser("applicant");
        CampaignResponse campaignA = createCampaign(guardianA, "Campaña A");
        CampaignResponse campaignB = createCampaign(guardianB, "Campaña B");

        JoinRequestResponse requestToB = requestToJoin(applicant, campaignB.inviteCode());

        // El Guardián A usa SU campaña en la URL, pero el id de una solicitud de B
        String url = "/api/campaigns/" + campaignA.id() + "/requests/" + requestToB.id();
        assertThat(post(url + "/approve", guardianA.token(), null, String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(post(url + "/reject", guardianA.token(), null, String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // La solicitud de B sigue pendiente
        ResponseEntity<PendingRequestResponse[]> stillPending = get(
                "/api/campaigns/" + campaignB.id() + "/requests",
                guardianB.token(), PendingRequestResponse[].class);
        assertThat(stillPending.getBody()).hasSize(1);
    }

    @Test
    void regeneratingTheCodeInvalidatesTheOldOne() {
        TestUser guardian = createUser("guardian");
        TestUser player = createUser("player");
        CampaignResponse campaign = createCampaign(guardian, "Campaña de prueba");

        ResponseEntity<Map> regenerated = post(
                "/api/campaigns/" + campaign.id() + "/invite-code/regenerate",
                guardian.token(), null, Map.class);
        String newCode = (String) regenerated.getBody().get("inviteCode");

        assertThat(newCode).isNotEqualTo(campaign.inviteCode());
        assertThat(post("/api/campaigns/join", player.token(),
                new JoinCampaignRequest(campaign.inviteCode()), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(post("/api/campaigns/join", player.token(),
                new JoinCampaignRequest(newCode), String.class).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void rejectedUserCanApplyAgain() {
        TestUser guardian = createUser("guardian");
        TestUser player = createUser("player");
        CampaignResponse campaign = createCampaign(guardian, "Campaña de prueba");
        String base = "/api/campaigns/" + campaign.id();

        JoinRequestResponse first = requestToJoin(player, campaign.inviteCode());
        assertThat(post(base + "/requests/" + first.id() + "/reject", guardian.token(), null,
                String.class).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        JoinRequestResponse second = requestToJoin(player, campaign.inviteCode());
        assertThat(second.id()).isNotEqualTo(first.id());
        assertThat(second.status()).isEqualTo(JoinRequestStatus.PENDING);
    }

    @Test
    void guardianCannotRemoveThemselvesButCanRemovePlayers() {
        TestUser guardian = createUser("guardian");
        TestUser player = createUser("player");
        CampaignResponse campaign = createCampaign(guardian, "Campaña de prueba");
        String base = "/api/campaigns/" + campaign.id();
        JoinRequestResponse request = requestToJoin(player, campaign.inviteCode());
        post(base + "/requests/" + request.id() + "/approve", guardian.token(), null,
                String.class);

        assertThat(delete(base + "/members/" + guardian.id(), guardian.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        assertThat(delete(base + "/members/" + player.id(), guardian.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(get(base, player.token(), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
