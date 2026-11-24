package com.quinter.madness_table.investigator;

import com.quinter.madness_table.auth.user.User;
import com.quinter.madness_table.campaign.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestigatorAccessTest {

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_PLAYER_ID = 2L;
    private static final Long GUARDIAN_ID = 3L;
    private static final Long CAMPAIGN_ID = 10L;
    private static final Long INVESTIGATOR_ID = 20L;

    @Mock
    private CampaignAccess campaignAccess;
    @Mock
    private InvestigatorRepository investigatorRepository;

    @InjectMocks
    private InvestigatorAccess access;

    private Investigator investigatorOwnedBy(Long ownerId) {
        User owner = new User("owner@example.com", "Owner", "HASH");
        ReflectionTestUtils.setField(owner, "id", ownerId);
        return new Investigator(null, owner, "Paco", "Geologo", 32,
                null, null, 12, 12, 60, 50);
    }

    private void givenMember(Long userId, CampaignRole role) {
        when(campaignAccess.requireMember(CAMPAIGN_ID, userId))
                .thenReturn(new CampaignMember(null, null, role));
    }

    private void givenInvestigatorExists(Investigator investigator) {
        when(investigatorRepository.findByIdAndCampaignId(INVESTIGATOR_ID, CAMPAIGN_ID))
                .thenReturn(Optional.of(investigator));
    }

    @Test
    void nonMemberGetsNotFoundWithoutTouchingTheInvestigators() {
        when(campaignAccess.requireMember(CAMPAIGN_ID, OTHER_PLAYER_ID))
                .thenThrow(new CampaignNotFoundException(CAMPAIGN_ID));

        assertThatThrownBy(() -> access.requireVisible(OTHER_PLAYER_ID, CAMPAIGN_ID, INVESTIGATOR_ID))
                .isInstanceOf(CampaignNotFoundException.class);

        verifyNoInteractions(investigatorRepository);
    }

    @Test
    void anInvestigatorOutsideTheCampaignIsNotFound() {
        givenMember(GUARDIAN_ID, CampaignRole.GUARDIAN);
        when(investigatorRepository.findByIdAndCampaignId(INVESTIGATOR_ID, CAMPAIGN_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> access.requireVisible(GUARDIAN_ID, CAMPAIGN_ID, INVESTIGATOR_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void theOwnerSeesTheirOwnInvestigator() {
        Investigator investigator = investigatorOwnedBy(OWNER_ID);
        givenMember(OWNER_ID, CampaignRole.PLAYER);
        givenInvestigatorExists(investigator);

        assertThat(access.requireVisible(OWNER_ID, CAMPAIGN_ID, INVESTIGATOR_ID))
                .isSameAs(investigator);
    }

    @Test
    void anotherPlayerGetsNotFound() {
        givenMember(OTHER_PLAYER_ID, CampaignRole.PLAYER);
        givenInvestigatorExists(investigatorOwnedBy(OWNER_ID));

        assertThatThrownBy(() -> access.requireVisible(OTHER_PLAYER_ID, CAMPAIGN_ID, INVESTIGATOR_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void theGuardianSeesAnyInvestigatorOfTheCampaign() {
        givenMember(GUARDIAN_ID, CampaignRole.GUARDIAN);
        Investigator investigator = investigatorOwnedBy(OWNER_ID);
        givenInvestigatorExists(investigator);

        assertThat(access.requireVisible(GUARDIAN_ID, CAMPAIGN_ID, INVESTIGATOR_ID))
                .isSameAs(investigator);
    }
}
