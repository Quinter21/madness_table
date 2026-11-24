package com.quinter.madness_table.investigator;

import com.quinter.madness_table.AbstractIntegrationTest;
import com.quinter.madness_table.campaign.CampaignResponse;
import com.quinter.madness_table.rules.SkillCheck;
import com.quinter.madness_table.rules.SuccessLevel;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class RollIT extends AbstractIntegrationTest {

    private static String rolls(Long campaignId, Long investigatorId) {
        return "/api/campaigns/" + campaignId + "/investigators/" + investigatorId + "/rolls";
    }

    private ResponseEntity<RollResponse> roll(TestUser user, Long campaignId, Long investigatorId,
                                              Long skillId, int bonus, int penalty) {
        return post(rolls(campaignId, investigatorId), user.token(),
                new RollRequest(skillId, bonus, penalty), RollResponse.class);
    }

    private ResponseEntity<String> rollRaw(TestUser user, Long campaignId, Long investigatorId,
                                           Long skillId, int bonus, int penalty) {
        return post(rolls(campaignId, investigatorId), user.token(),
                new RollRequest(skillId, bonus, penalty), String.class);
    }

    @Test
    void theReturedLevelAlwaysMatchesWhatTheEngineSaysForThatResult() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña");
        SkillResponse skill = createSkill(guardian, campaign.id(), "Skill", 50);
        InvestigatorResponse sheet = createInvestigator(guardian, campaign.id(), "Paco");

        for (int i = 0; i < 300; i++) {
            ResponseEntity<RollResponse> response =
                    roll(guardian, campaign.id(), sheet.id(), skill.id(), 0, 0);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            RollResponse roll = response.getBody();
            assertThat(roll.result()).isBetween(1, 100);
            assertThat(roll.skillValue()).isEqualTo(50);
            assertThat(roll.netDice()).isZero();
            assertThat(roll.level()).isEqualTo(SkillCheck.evaluate(roll.result(), roll.skillValue()));
            assertThat(roll.success()).isEqualTo(roll.level().isSuccess());
        }
    }

    @Test
    void aSkillOfZeroNeverSucceedsAndSkillOf100NeverFailsPlainly() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña");
        SkillResponse best = createSkill(guardian, campaign.id(), "Best", 100);
        SkillResponse worst = createSkill(guardian, campaign.id(), "Worst", 0);
        InvestigatorResponse sheet = createInvestigator(guardian, campaign.id(), "Paco");

        for (int i = 0; i < 100; i++) {
            assertThat(roll(guardian, campaign.id(), sheet.id(), worst.id(), 0, 0)
                    .getBody().success()).isFalse();

            assertThat(roll(guardian, campaign.id(), sheet.id(), best.id(), 0, 0)
                    .getBody().success()).isNotEqualTo(SuccessLevel.FAILURE);
        }
    }

    @Test
    void bonusAndPenaltyDiceAreRegisteredAsNetDice() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña");
        SkillResponse skill = createSkill(guardian, campaign.id(), "Skill", 50);
        InvestigatorResponse sheet = createInvestigator(guardian, campaign.id(), "Paco");

        assertThat(roll(guardian, campaign.id(), sheet.id(), skill.id(), 2, 2)
                .getBody().netDice()).isZero();
        assertThat(roll(guardian, campaign.id(), sheet.id(), skill.id(), 2, 0)
                .getBody().netDice()).isEqualTo(2);
        assertThat(roll(guardian, campaign.id(), sheet.id(), skill.id(), 0, 1)
                .getBody().netDice()).isEqualTo(-1);
    }

    @Test
    void theSheetsOwnValueIsTheOneUsedForTheRoll() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña");
        TestUser player = createPlayerInCampaign(guardian, campaign, "Player");
        SkillResponse skill = createSkill(guardian, campaign.id(), "Skill", 50);
        InvestigatorResponse sheet = createInvestigator(player, campaign.id(), "Paco");

        assertThat(roll(player, campaign.id(), sheet.id(), skill.id(), 0, 0)
                .getBody().skillValue()).isEqualTo(50);

        put("/api/campaigns/" + campaign.id() + "/investigators/" + sheet.id() + "/skills/" + skill.id(),
                player.token(), new SetSkillValueRequest(80), String.class);

        assertThat(roll(player, campaign.id(), sheet.id(), skill.id(), 0, 0)
                .getBody().skillValue()).isEqualTo(80);
    }

    @Test
    void invalidRequestsAreRejectedWith400() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña");
        SkillResponse skill = createSkill(guardian, campaign.id(), "Skill", 20);
        InvestigatorResponse investigator = createInvestigator(guardian, campaign.id(), "Paco");

        assertThat(rollRaw(guardian, campaign.id(), investigator.id(), skill.id(), 3, 0)
                .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(rollRaw(guardian, campaign.id(), investigator.id(), skill.id(), 0, -1)
                .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(rollRaw(guardian, campaign.id(), investigator.id(), null, 0, 0)
                .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void permissionsFollowTheSheetsVisibility() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña");
        TestUser player1 = createPlayerInCampaign(guardian, campaign, "Player1");
        TestUser player2 = createPlayerInCampaign(guardian, campaign, "Player2");
        TestUser outsider = createUser("outsider");
        SkillResponse skill = createSkill(guardian, campaign.id(), "Skill", 25);
        InvestigatorResponse investigator = createInvestigator(player1, campaign.id(), "Paco");

        assertThat(rollRaw(player2, campaign.id(), investigator.id(), skill.id(), 0, 0)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(rollRaw(outsider, campaign.id(), investigator.id(), skill.id(), 0, 0)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get(rolls(campaign.id(), investigator.id()), player2.token(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get(rolls(campaign.id(), investigator.id()), player1.token(), RollResponse[].class)
                .getBody()).isEmpty();

        RollResponse byGuardian = roll(guardian, campaign.id(), investigator.id(), skill.id(), 0, 0)
                .getBody();
        assertThat(byGuardian.rolledById()).isEqualTo(guardian.id());

        RollResponse byOwner = roll(player1, campaign.id(), investigator.id(), skill.id(), 0, 0)
                .getBody();
        assertThat(byOwner.rolledById()).isEqualTo(player1.id());
    }

    @Test
    void aSkillFromAnotherCampaignCannotBeRolled() {
        TestUser guardian1 = createUser("Guardian1");
        CampaignResponse campaign1 = createCampaign(guardian1, "Campaña1");
        TestUser guardian2 = createUser("Guardian2");
        CampaignResponse campaign2 = createCampaign(guardian2, "Campaña2");
        SkillResponse skill = createSkill(guardian2, campaign2.id(), "Skill", 25);
        InvestigatorResponse investigator = createInvestigator(guardian1, campaign1.id(), "Paco");

        assertThat(rollRaw(guardian1, campaign1.id(), investigator.id(), skill.id(), 0, 0)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void historyIsNewestFirstAndKeepsTheSkillSnapshot() {
        TestUser guardian = createUser("Guardian");
        CampaignResponse campaign = createCampaign(guardian, "Campaña");
        SkillResponse skill = createSkill(guardian, campaign.id(), "Skill", 25);
        InvestigatorResponse investigator = createInvestigator(guardian, campaign.id(), "Paco");

        Long first = roll(guardian, campaign.id(), investigator.id(), skill.id(), 0, 0).getBody().id();
        Long second = roll(guardian, campaign.id(), investigator.id(), skill.id(), 0, 0).getBody().id();
        Long third = roll(guardian, campaign.id(), investigator.id(), skill.id(), 0, 0).getBody().id();

        put("/api/campaigns/" + campaign.id() + "/skills/" + skill.id(), guardian.token(),
                new SkillRequest("Descubrir", 50), String.class);

        RollResponse[] history = get(rolls(campaign.id(), investigator.id()), guardian.token(),
                RollResponse[].class).getBody();

        assertThat(history).extracting(RollResponse::id).containsExactly(third, second, first);
        assertThat(history).allSatisfy(r -> {
            assertThat(r.skillName()).isEqualTo("Skill");
            assertThat(r.skillValue()).isEqualTo(25);
            assertThat(r.rollerByName()).isEqualTo("Guardian");
        });
    }
}
