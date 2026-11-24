package com.quinter.madness_table.investigator;

import com.quinter.madness_table.AbstractIntegrationTest;
import com.quinter.madness_table.campaign.CampaignResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class InvestigatorIT extends AbstractIntegrationTest {

    private static String base(Long campaignId) {
        return "/api/campaigns/" + campaignId + "/investigators";
    }

    private static UpdateInvestigatorRequest update(String name) {
        return new UpdateInvestigatorRequest(name, "Detective", 40, "Otro trasfondo");
    }

    @Test
    void playerCreatesAnInvestigatorWithServerSideDerivedValues() {
        TestUser guardian = createUser("guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña de prueba");
        TestUser player = createPlayerInCampaign(guardian, campaign, "player");

        InvestigatorResponse investigator = createInvestigator(player, campaign.id(), "Paco");

        assertThat(investigator.ownerId()).isEqualTo(player.id());
        assertThat(investigator.hitPoints()).isEqualTo(10);
        assertThat(investigator.magicPoints()).isEqualTo(10);
        assertThat(investigator.sanity()).isEqualTo(50);
        assertThat(investigator.luck()).isBetween(15,90).satisfies(l -> assertThat(l % 5).isZero());
        assertThat(investigator.derived().maxHitPoints()).isEqualTo(10);
        assertThat(investigator.derived().damageBonus()).isEqualTo("0");
        assertThat(investigator.derived().move()).isEqualTo(8);
    }

    @Test
    void invalidDataIsRejectedWith400() {
        TestUser guardian = createUser("guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña de prueba");
        CharacteristicsDto valid = new CharacteristicsDto(55, 60, 65, 70,
                50, 75, 60, 80);
        CharacteristicsDto zeroStrength = new CharacteristicsDto(0, 60, 65, 70,
                50, 20, 70, 90);

        assertThat(post(base(campaign.id()), guardian.token(),
                new CreateInvestigatorRequest("A", "B", 10, null, valid), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);  //EDAD MAL (15-90)

        assertThat(post(base(campaign.id()), guardian.token(),
                new CreateInvestigatorRequest("A", "B", 20, null, zeroStrength), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);  //FUERZA (1-99)

        assertThat(post(base(campaign.id()), guardian.token(),
                new CreateInvestigatorRequest("A", "B", 20, null, null), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);  //Sin características
    }

    @Test
    void playerSeesOnlyTheirOwnInvestigatorsAndTheGuardianSeesAll() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña de prueba");
        TestUser player1 = createPlayerInCampaign(guardian, campaign, "player1");
        TestUser player2 = createPlayerInCampaign(guardian, campaign, "player2");
        InvestigatorResponse own = createInvestigator(player1, campaign.id(), "Paco");
        createInvestigator(player2, campaign.id(), "Jose");
        createInvestigator(guardian, campaign.id(), "Ángel");

        InvestigatorResponse[] seenByPlayer = get(base(campaign.id()), player1.token(),
                InvestigatorResponse[].class).getBody();
        InvestigatorResponse[] seenByGuardian = get(base(campaign.id()), guardian.token(),
                InvestigatorResponse[].class).getBody();

        assertThat(seenByPlayer).extracting(InvestigatorResponse::id).containsExactly(own.id());
        assertThat(seenByGuardian).hasSize(3);
    }

    @Test
    void playerCannotReadEditOrDeleteAnotherPlayersInvestigator() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña de prueba");
        TestUser player1 = createPlayerInCampaign(guardian, campaign, "player1");
        TestUser player2 = createPlayerInCampaign(guardian, campaign, "player2");
        InvestigatorResponse investigator = createInvestigator(player1, campaign.id(), "Paco");
        String url = base(campaign.id()) + "/" + investigator.id();

        assertThat(get(url, player2.token(), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(put(url, player2.token(), update("Maripili"), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete(url, player2.token(), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        InvestigatorResponse intact = get(url, player1.token(), InvestigatorResponse.class).getBody();
        assertThat(intact.name()).isEqualTo("Paco");
    }

    @Test
    void ownerCanEditAndDeleteTheirInvestigator() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña de prueba");
        TestUser player1 = createPlayerInCampaign(guardian, campaign, "player1");
        InvestigatorResponse investigator = createInvestigator(player1, campaign.id(), "Paco");
        String url = base(campaign.id()) + "/" + investigator.id();

        ResponseEntity<InvestigatorResponse> edited = put(url, player1.token(),
                update("Jose"), InvestigatorResponse.class);
        assertThat(edited.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(edited.getBody().name()).isEqualTo("Jose");
        assertThat(edited.getBody().occupation()).isEqualTo("Detective");
        assertThat(edited.getBody().luck()).isEqualTo(investigator.luck());

        assertThat(delete(url, player1.token(), String.class).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(get(url, player1.token(), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void guardianCanEditAndDeleteAnyInvestigator() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña de prueba");
        TestUser player1 = createPlayerInCampaign(guardian, campaign, "player1");
        InvestigatorResponse investigator = createInvestigator(player1, campaign.id(), "Paco");
        String url = base(campaign.id()) + "/" + investigator.id();

        assertThat(get(url, guardian.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(put(url, guardian.token(), update("Paquito"), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(delete(url, guardian.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void outsiderGetsNotFoundOnEverything() {
        TestUser guardian = createUser("Guardian");
        TestUser outsider = createUser("Outsider");
        CampaignResponse campaign = createCampaign(guardian, "Campaña de prueba");
        InvestigatorResponse investigator = createInvestigator(guardian, campaign.id(), "Paco");
        String url = base(campaign.id()) + "/" + investigator.id();

        assertThat(get(base(campaign.id()), outsider.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get(url, outsider.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(put(url, outsider.token(), update("Paquito"), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete(url, outsider.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(post(base(campaign.id()), outsider.token(),
                new CreateInvestigatorRequest("Jose", "Geologo", 30, null,
                        new CharacteristicsDto(50, 50, 50, 50,
                                50, 50, 50, 50)),
                String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void anInvestigatorCannotBeReachedThrougAnotherCampaign() {
        TestUser guardian1 = createUser("Guardian1");
        TestUser guardian2 = createUser("Guardian2");
        CampaignResponse campaign1 = createCampaign(guardian1, "Campaña1");
        CampaignResponse campaign2 = createCampaign(guardian2, "Campaña2");
        InvestigatorResponse investigator = createInvestigator(guardian1, campaign1.id(), "Paco");
        String url = base(campaign2.id()) + "/" + investigator.id();

        assertThat(get(url, guardian2.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(put(url, guardian2.token(), update("Jose"), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete(url, guardian2.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void  anExpelledPlayerLosesAccessButTheGuardianKeepsTheSheet() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña de prueba");
        TestUser player = createPlayerInCampaign(guardian, campaign, "player");
        InvestigatorResponse investigator = createInvestigator(player, campaign.id(), "Paco");
        String url = base(campaign.id()) + "/" + investigator.id();

        assertThat(delete("/api/campaigns/" + campaign.id() + "/members/" + player.id(),
                guardian.token(), String.class).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(get(url, player.token(), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        //TODO: BASE DE DATOS TIENE DELETE CASCADE EN user_id
        assertThat(get(url, guardian.token(), String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void requestWithoutTokenGet401() {
        assertThat(get(base(1L), null, String.class)
                .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
