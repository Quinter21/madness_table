package com.quinter.madness_table.campaign;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CampaignAccessTest {

    private static final Long CAMPAIGN_ID = 10L;
    private static final Long USER_ID = 5L;

    @Mock
    private CampaignMemberRepository memberRepository;

    @InjectMocks
    private CampaignAccess access;

    private CampaignMember memberWithRole(CampaignRole role) {
        return new CampaignMember(null, null, role);
    }

    @Test
    void nonMemberGetsNotFound() {
        when(memberRepository.findByCampaignIdAndUserId(CAMPAIGN_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> access.requireMember(CAMPAIGN_ID, USER_ID))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void nonMemberIsAlsoNotFoundWhenGuardianIsRequired() {
        when(memberRepository.findByCampaignIdAndUserId(CAMPAIGN_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> access.requireGuardian(CAMPAIGN_ID, USER_ID))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void playerIsForbiddenWhenGuardianIsRequired() {
        when(memberRepository.findByCampaignIdAndUserId(CAMPAIGN_ID, USER_ID))
                .thenReturn(Optional.of(memberWithRole(CampaignRole.PLAYER)));

        assertThatThrownBy(() -> access.requireGuardian(CAMPAIGN_ID, USER_ID))
                .isInstanceOf(NotGuardianException.class);
    }

    @Test
    void playerPasesMemberCheck() {
        CampaignMember player = memberWithRole(CampaignRole.PLAYER);
        when(memberRepository.findByCampaignIdAndUserId(CAMPAIGN_ID, USER_ID))
                .thenReturn(Optional.of(player));

        assertThat(access.requireMember(CAMPAIGN_ID, USER_ID)).isSameAs(player);
    }

    @Test
    void guardianPassesTheGuardianCheck() {
        CampaignMember guardian = memberWithRole(CampaignRole.GUARDIAN);
        when(memberRepository.findByCampaignIdAndUserId(CAMPAIGN_ID, USER_ID))
                .thenReturn(Optional.of(guardian));

        assertThat(access.requireGuardian(CAMPAIGN_ID, USER_ID)).isSameAs(guardian);
    }
}
