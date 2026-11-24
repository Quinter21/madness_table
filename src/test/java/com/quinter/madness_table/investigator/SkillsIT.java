package com.quinter.madness_table.investigator;

import com.quinter.madness_table.AbstractIntegrationTest;
import com.quinter.madness_table.campaign.CampaignResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class SkillsIT extends AbstractIntegrationTest {

    private static String catalog(Long campaignId) {
        return "/api/campaigns/" + campaignId + "/skills";
    }

    private static String sheetSkills(Long campaignId, Long investigatorId) {
        return "/api/campaigns/" + campaignId + "/investigators/" + investigatorId + "/skills";
    }

    @Test
    void guardianManagesTheCatalogAndNamesAreUniqueIgnoringCase() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña");
        SkillResponse descubrir = createSkill(guardian, campaign.id(), "Descubrir", 20);
        createSkill(guardian, campaign.id(), "Charlataneria", 5);

        assertThat(post(catalog(campaign.id()), guardian.token(),
                new SkillRequest("descubrir", 10), String.class).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        ResponseEntity<SkillResponse> renamed = put(catalog(campaign.id()) + "/" + descubrir.id(),
                guardian.token(), new SkillRequest("DESCUBRIR", 25), SkillResponse.class);
        assertThat(renamed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(renamed.getBody().baseValue()).isEqualTo(25);

        assertThat(put(catalog(campaign.id()) + "/" + descubrir.id(), guardian.token(),
                new SkillRequest("charlataneria", 35), String.class).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(post(catalog(campaign.id()), guardian.token(),
                new SkillRequest("No", 101), String.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void playerCanReadTheCatalogButNotModifyIt() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña");
        SkillResponse descubrir = createSkill(guardian, campaign.id(), "Descubrir", 20);
        TestUser player = createPlayerInCampaign(guardian, campaign, "Player");

        assertThat(get(catalog(campaign.id()), player.token(), SkillResponse[].class).getBody())
                .extracting(SkillResponse::name).containsExactly("Descubrir");

        assertThat(post(catalog(campaign.id()), player.token(),
                new SkillRequest("Charlar", 25), String.class).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(put(catalog(campaign.id()) + "/" + descubrir.id(), player.token(),
                new SkillRequest("descubrir", 5), String.class).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(delete(catalog(campaign.id()) + "/" + descubrir.id(), player.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void outsiderGetsNotFoundOnTheCatalog() {
        TestUser guardian = createUser("Guardian");
        TestUser outsider = createUser("Outsider");
        CampaignResponse campaign = createCampaign(guardian, "Campaña");

        assertThat(get(catalog(campaign.id()), outsider.token(), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(post(catalog(campaign.id()), outsider.token(), new SkillRequest("Descubrir", 20),
                String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void aGuardianCannotEditTheCatalogOfAnotherCampaign() {
        TestUser guardian1 = createUser("Guardian1");
        TestUser guardian2 = createUser("Guardian2");
        CampaignResponse campaign1 = createCampaign(guardian1, "Campaña1");
        CampaignResponse campaign2 = createCampaign(guardian2, "Campaña2");
        SkillResponse skill = createSkill(guardian1, campaign1.id(), "Descubrir", 20);

        assertThat(get(catalog(campaign2.id()), guardian1.token(), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(post(catalog(campaign1.id()), guardian2.token(), new SkillRequest("Luchar", 20),
                String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        String url = catalog(campaign1.id()) + "/" + skill.id();
        assertThat(put(url, guardian2.token(), new SkillRequest("descubrir", 20), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete(url, guardian2.token(), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void aSheetUsesTheBaseValueUntilIsChangedAndCanGoBack() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña");
        SkillResponse skill = createSkill(guardian, campaign.id(), "Descubrir", 20);
        createSkill(guardian, campaign.id(), "Luchar", 10);
        InvestigatorResponse investigator = createInvestigator(guardian, campaign.id(), "Paco");
        String url = sheetSkills(campaign.id(), investigator.id());

        InvestigatorSkillResponse[] before = get(url, guardian.token(), InvestigatorSkillResponse[].class).getBody();
        assertThat(before).hasSize(2).allSatisfy(s -> {
            assertThat(s.value()).isEqualTo(s.baseValue());
            assertThat(s.modified()).isFalse();
        });

        ResponseEntity<InvestigatorSkillResponse> changed = put(url + "/" + skill.id(), guardian.token(),
                new SetSkillValueRequest(60), InvestigatorSkillResponse.class);
        assertThat(changed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(changed.getBody().value()).isEqualTo(60);
        assertThat(changed.getBody().modified()).isTrue();
        assertThat(changed.getBody().baseValue()).isEqualTo(skill.baseValue());

        InvestigatorSkillResponse[] after = get(url, guardian.token(), InvestigatorSkillResponse[].class).getBody();
        assertThat(after).filteredOn(s -> s.name().equals("Descubrir"))
                .singleElement().satisfies(s -> assertThat(s.value()).isEqualTo(60));
        assertThat(after).filteredOn(s -> s.name().equals("Luchar"))
                .singleElement().satisfies(s -> assertThat(s.modified()).isFalse());

        assertThat(delete(url + "/" + skill.id(), guardian.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        InvestigatorSkillResponse[] reset = get(url, guardian.token(), InvestigatorSkillResponse[].class).getBody();
        assertThat(reset).allSatisfy(s -> assertThat(s.modified()).isFalse());
    }

    @Test
    void aNewCatalogSkillAppearsOnExistingSheets() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña");
        InvestigatorResponse investigator = createInvestigator(guardian, campaign.id(), "Paco");

        String url = sheetSkills(campaign.id(), investigator.id());

        assertThat(get(url, guardian.token(), InvestigatorSkillResponse[].class).getBody()).isEmpty();
        createSkill(guardian, campaign.id(), "Descubrir", 20);
        assertThat(get(url, guardian.token(), InvestigatorSkillResponse[].class).getBody())
                .extracting(InvestigatorSkillResponse::name).containsExactly("Descubrir");
    }

    @Test
    void valuesOutOfRangeAndForeignSkillsAreRejected() {
        TestUser guardian1 = createUser("Guardian");
        TestUser guardian2 = createUser("Guardian");
        CampaignResponse campaign1 = createCampaign(guardian1, "Campaña1");
        CampaignResponse campaign2 = createCampaign(guardian2, "Campaña2");
        SkillResponse skill = createSkill(guardian1, campaign1.id(), "Descubrir", 20);
        SkillResponse foreign = createSkill(guardian2, campaign2.id(), "Lucha", 10);
        InvestigatorResponse investigator = createInvestigator(guardian1, campaign1.id(), "Paco");
        String url = sheetSkills(campaign1.id(), investigator.id());

        assertThat(put(url + "/" + skill.id(), guardian1.token(),
                new SetSkillValueRequest(101), String.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(put(url + "/" + skill.id(), guardian1.token(),
                new SetSkillValueRequest(-1), String.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(put(url + "/" + foreign.id(), guardian1.token(),
                new SetSkillValueRequest(50), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void playerCannotTouchTheSkillsOfAnotherPlayerSheet() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña");
        TestUser player1 = createPlayerInCampaign(guardian, campaign, "Player1");
        TestUser player2 = createPlayerInCampaign(guardian, campaign, "PLayer2");
        SkillResponse skill = createSkill(guardian, campaign.id(), "Descubrir", 20);
        InvestigatorResponse investigator = createInvestigator(player1, campaign.id(), "Paco");

        String url = sheetSkills(campaign.id(), investigator.id());

        assertThat(get(url, player2.token(), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(put(url + "/" + skill.id(), player2.token(),new SetSkillValueRequest(99),
                String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(delete(url + "/" + skill.id(), player2.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(put(url + "/" + skill.id(), guardian.token(), new SetSkillValueRequest(80),
                String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void aSkillInUseCannotBeDeletedUntilNobodyUsesIt() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña");
        TestUser player = createPlayerInCampaign(guardian, campaign, "Player");
        SkillResponse usedSkill = createSkill(guardian, campaign.id(), "Descubrir", 20);
        SkillResponse unusedSkill = createSkill(guardian, campaign.id(), "Luchar", 10);
        InvestigatorResponse investigator = createInvestigator(player, campaign.id(), "Paco");

        put(sheetSkills(campaign.id(), investigator.id()) + "/" + usedSkill.id(), player.token(),
                new SetSkillValueRequest(80), String.class);

        assertThat(delete(catalog(campaign.id()) + "/" + usedSkill.id(), guardian.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(delete(catalog(campaign.id()) + "/" + unusedSkill.id(), guardian.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        delete(sheetSkills(campaign.id(), investigator.id()) + "/" + usedSkill.id(), player.token(), String.class);
        assertThat(delete(catalog(campaign.id()) + "/" + usedSkill.id(), guardian.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
