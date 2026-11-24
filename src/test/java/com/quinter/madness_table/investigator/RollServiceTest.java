package com.quinter.madness_table.investigator;

import com.quinter.madness_table.auth.user.User;
import com.quinter.madness_table.auth.user.UserRepository;
import com.quinter.madness_table.campaign.ResourceNotFoundException;
import com.quinter.madness_table.rules.FixedRandomSource;
import com.quinter.madness_table.rules.RandomSource;
import com.quinter.madness_table.rules.SuccessLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RollServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long CAMPAIGN_ID = 10L;
    private static final Long INVESTIGATOR_ID = 20L;
    private static final Long SKILL_ID = 30L;

    @Mock
    private InvestigatorAccess investigatorAccess;
    @Mock
    private CampaignSkillRepository skillRepository;
    @Mock
    private InvestigatorSkillRepository investigatorSkillRepository;
    @Mock
    private RollLogRepository logRepository;
    @Mock
    private UserRepository userRepository;

    private final Investigator investigator = new Investigator(null, null, "Jose", "Periodista",
            32, null, null, 12, 12, 60, 50);

    private RollService serviceWith(RandomSource source) {
        return new RollService(investigatorAccess, skillRepository,
                investigatorSkillRepository, logRepository, userRepository, source);
    }

    private CampaignSkill skill(String name, int baseValue) {
        CampaignSkill skill = new CampaignSkill(null, name, baseValue);
        ReflectionTestUtils.setField(skill, "id", SKILL_ID);
        return skill;
    }

    private void givenVisible(CampaignSkill skill) {
        when(investigatorAccess.requireVisible(USER_ID, CAMPAIGN_ID, INVESTIGATOR_ID))
                .thenReturn(investigator);
        when(skillRepository.findByIdAndCampaignId(SKILL_ID, CAMPAIGN_ID))
                .thenReturn(Optional.of(skill));
        when(userRepository.getReferenceById(USER_ID))
                .thenReturn(new User("angel@example.com", "Angel", "HASH"));
        when(logRepository.save(any(RollLog.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private RollResponse roll(RollService service, int bonus, int penalty) {
        return service.roll(USER_ID, CAMPAIGN_ID, INVESTIGATOR_ID,
                new RollRequest(SKILL_ID, bonus, penalty));
    }

    @Test
    void usesTheCatalogBaseValueWhetTheSheetHasNoOwnValue() {
        givenVisible(skill("Descubrir", 40));
        RollService service = serviceWith(FixedRandomSource.of(6, 3));

        RollResponse response = roll(service, 0, 0);

        assertThat(response.skillName()).isEqualTo("Descubrir");
        assertThat(response.skillValue()).isEqualTo(40);
        assertThat(response.result()).isEqualTo(25);
        assertThat(response.level()).isEqualTo(SuccessLevel.REGULAR);
        assertThat(response.success()).isTrue();
        assertThat(response.netDice()).isZero();
    }

    @Test
    void usesTheSheetValueWhenItHasOne() {
        CampaignSkill descubrir = skill("Descubrir", 40);
        givenVisible(descubrir);
        when(investigatorSkillRepository
                .findByInvestigatorIdAndCampaignSkillId(INVESTIGATOR_ID, SKILL_ID))
                .thenReturn(Optional.of(new InvestigatorSkill(null, descubrir, 20)));

        RollResponse response = roll(serviceWith(FixedRandomSource.of(6, 3)), 0, 0);

        assertThat(response.skillValue()).isEqualTo(20);
        assertThat(response.result()).isEqualTo(25);
        assertThat(response.level()).isEqualTo(SuccessLevel.FAILURE);
        assertThat(response.success()).isFalse();
    }

    @Test
    void aBonusDieKeepsTheBestResult() {
        givenVisible(skill("Descubrir", 40));

        RollResponse response = roll(serviceWith(FixedRandomSource.of(6, 3, 8)), 1, 0);

        assertThat(response.result()).isEqualTo(25);
        assertThat(response.netDice()).isEqualTo(1);
    }

    @Test
    void aPenaltyDieKeepsTheWorstResult() {
        givenVisible(skill("Descubrir", 40));

        RollResponse response = roll(serviceWith(FixedRandomSource.of(6, 3, 8)), 0, 1);

        assertThat(response.result()).isEqualTo(75);
        assertThat(response.netDice()).isEqualTo(-1);
        assertThat(response.level()).isEqualTo(SuccessLevel.FAILURE);
    }

    @Test
    void aHighRollWithALowSkillIsAFumble() {
        givenVisible(skill("Descubrir", 40));

        RollResponse response = roll(serviceWith(FixedRandomSource.of(10, 10)), 0, 0);

        assertThat(response.result()).isEqualTo(99);
        assertThat(response.level()).isEqualTo(SuccessLevel.FUMBLE);

        response = roll(serviceWith(FixedRandomSource.of(1, 1)), 0, 0);
        assertThat(response.result()).isEqualTo(100);
    }

    @Test
    void theRollIsPersistedWithASnapshotOfTheSkill() {
        givenVisible(skill("Descubrir", 40));

        roll(serviceWith(FixedRandomSource.of(6, 3)), 0, 0);

        ArgumentCaptor<RollLog> captor = ArgumentCaptor.forClass(RollLog.class);
        verify(logRepository).save(captor.capture());
        RollLog log = captor.getValue();
        assertThat(log.getInvestigator()).isSameAs(investigator);
        assertThat(log.getSkillName()).isEqualTo("Descubrir");
        assertThat(log.getSkillValue()).isEqualTo(40);
        assertThat(log.getRollResult()).isEqualTo(25);
        assertThat(log.getNetDice()).isEqualTo(0);
        assertThat(log.getSuccessLevel()).isEqualTo(SuccessLevel.REGULAR);
    }

    @Test
    void anUnknownSkillIsNotFoundAndNothingIsSaved() {
        when(investigatorAccess.requireVisible(USER_ID, CAMPAIGN_ID, INVESTIGATOR_ID))
                .thenReturn(investigator);
        when(skillRepository.findByIdAndCampaignId(SKILL_ID, CAMPAIGN_ID))
                .thenReturn(Optional.empty());
        RollService service = serviceWith(FixedRandomSource.of());

        assertThatThrownBy(() -> roll(service, 0, 0))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(logRepository);
    }

    @Test
    void withoutPermissionNothingIsLookedUpOrSaved() {
        when(investigatorAccess.requireVisible(USER_ID, CAMPAIGN_ID, INVESTIGATOR_ID))
                .thenThrow(new ResourceNotFoundException("Investigador no encontrado"));
        RollService service = serviceWith(FixedRandomSource.of());
        assertThatThrownBy(() -> roll(service, 0, 0))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(skillRepository, investigatorSkillRepository, logRepository, userRepository);
    }
}
