package com.quinter.madness_table.campaign;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CampaignManagementServiceTest {

    @Mock
    private CampaignAccess access;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private CampaignMemberRepository memberRepository;
    @Mock
    private JoinRequestRepository joinRequestRepository;
    @Mock
    private InviteCodeGenerator inviteCodeGenerator;

    @InjectMocks
    private CampaignManagementService service;

    @Test
    void guardianCannotRemoveThemselves() {
        assertThatThrownBy(() -> service.removeMember(1L, 10L, 1L))
                .isInstanceOf(ConflictException.class);

        verify(memberRepository, never()).delete(any());
    }

    @Test
    void playerCannotListPendingRequests() {
        when(access.requireGuardian(10L, 2L)).thenThrow(new NotGuardianException());

        assertThatThrownBy(() -> service.listPendingRequests(2L, 10L))
                .isInstanceOf(NotGuardianException.class);

        verifyNoInteractions(joinRequestRepository);
    }

    @Test
    void regenerateInviteCodeSkipsCodesAlreadyInUse() {
        Campaign campaign = new Campaign("Test", null, "OLDCODE1", null);
        CampaignMember guardian = new CampaignMember(campaign, null, CampaignRole.GUARDIAN);
        when(access.requireGuardian(10L, 1L)).thenReturn(guardian);
        when(inviteCodeGenerator.generate()).thenReturn("TAKEN222", "FREE3333");
        when(campaignRepository.existsByInviteCode("TAKEN222")).thenReturn(true);
        when(campaignRepository.existsByInviteCode("FREE3333")).thenReturn(false);

        String code = service.regenerateInviteCode(1L, 10L);

        assertThat(code).isEqualTo("FREE3333");
        assertThat(campaign.getInviteCode()).isEqualTo("FREE3333");
    }

    @Test
    void playerCannotRegenerateInviteCode() {
        when(access.requireGuardian(10L, 2L)).thenThrow(new NotGuardianException());

        assertThatThrownBy(() -> service.regenerateInviteCode(2L, 10L))
                .isInstanceOf(NotGuardianException.class);

        verifyNoInteractions(inviteCodeGenerator);
        verifyNoInteractions(campaignRepository);
    }
}
