package com.quinter.madness_table.investigator;

import com.quinter.madness_table.auth.user.User;
import com.quinter.madness_table.campaign.Campaign;
import com.quinter.madness_table.campaign.CampaignAccess;
import com.quinter.madness_table.campaign.CampaignMember;
import com.quinter.madness_table.campaign.CampaignRole;
import com.quinter.madness_table.rules.RandomSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestigatorServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long CAMPAIGN_ID = 10L;
    private static final Long INVESTIGATOR_ID = 20L;

    @Mock
    private CampaignAccess access;
    @Mock
    private InvestigatorAccess investigatorAccess;
    @Mock
    private InvestigatorRepository investigatorRepository;
    @Mock
    private RandomSource random;

    @InjectMocks
    private InvestigatorService service;

    private static CreateInvestigatorRequest request(String name) {
        return new CreateInvestigatorRequest(name, "Geologo", 32, "Background",
                new CharacteristicsDto(55, 60, 40, 70,
                        50, 70, 80, 60));
    }

    @Test
    void createDerivesTheInitialValuesWithTheRulesEngine() {
        Campaign campaign = new Campaign("Campaña", null, "CODE1234", null);
        User user = new User("user@example.com", "user", "HASH");
        when(access.requireMember(CAMPAIGN_ID, USER_ID))
                .thenReturn(new CampaignMember(campaign, user, CampaignRole.PLAYER));
        when(random.roll(6)).thenReturn(3, 4, 5);
        when(investigatorRepository.save(any(Investigator.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        InvestigatorResponse response = service.create(USER_ID, CAMPAIGN_ID, request("Paco Picapuertas"));
        ArgumentCaptor<Investigator> captor = ArgumentCaptor.forClass(Investigator.class);
        verify(investigatorRepository).save(captor.capture());
        Investigator saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Paco Picapuertas");
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getHitPoints()).isEqualTo(10);
        assertThat(saved.getMagicPoints()).isEqualTo(16);
        assertThat(saved.getSanity()).isEqualTo(80);
        assertThat(saved.getLuck()).isEqualTo(60);

        assertThat(response.derived().maxHitPoints()).isEqualTo(10);
        assertThat(response.derived().damageBonus()).isEqualTo("0");
        assertThat(response.derived().build()).isZero();
        assertThat(response.derived().move()).isEqualTo(9);
    }

    @Test
    void theGuardianListsEveryInvestigatorOfTheCampaign() {
        when(access.requireMember(CAMPAIGN_ID, USER_ID))
                .thenReturn(new CampaignMember(null, null, CampaignRole.GUARDIAN));
        when(investigatorRepository.findByCampaignIdOrderByNameAsc(CAMPAIGN_ID))
                .thenReturn(List.of());

        service.list(USER_ID, CAMPAIGN_ID);

        verify(investigatorRepository, never())
                .findByCampaignIdAndUserIdOrderByNameAsc(any(), any());
    }

    @Test
    void aPlayerOnlyListsTheirOwnInvestigators() {
        when(access.requireMember(CAMPAIGN_ID, USER_ID))
                .thenReturn(new CampaignMember(null, null, CampaignRole.PLAYER));
        when(investigatorRepository.findByCampaignIdAndUserIdOrderByNameAsc(CAMPAIGN_ID, USER_ID))
                .thenReturn(List.of());

        service.list(USER_ID, CAMPAIGN_ID);

        verify(investigatorRepository, never()).findByCampaignIdOrderByNameAsc(any());
    }

    @Test
    void updateChangesOnlyTheEditableDetails() {
        Campaign campaign = new Campaign("Campaña", null, "CODE1234", null);
        User user = new User("user@example.com", "User", "HASH");
        Investigator investigator = new Investigator(campaign, user, "Paquito", "Geologo",
                32, "Trasfondo", new Characteristics(60, 60, 70, 50,
                40, 80, 75, 20), 12, 12, 60 ,50);

        when(investigatorAccess.requireVisible(USER_ID, CAMPAIGN_ID, INVESTIGATOR_ID))
                .thenReturn(investigator);

        service.update(USER_ID, CAMPAIGN_ID, INVESTIGATOR_ID,
                new UpdateInvestigatorRequest("Paco", "Periodista", 25, "Nuevo trasfondo"));

        assertThat(investigator.getName()).isEqualTo("Paco");
        assertThat(investigator.getOccupation()).isEqualTo("Periodista");
        assertThat(investigator.getAge()).isEqualTo(25);
        assertThat(investigator.getBackstory()).isEqualTo("Nuevo trasfondo");
        assertThat(investigator.getHitPoints()).isEqualTo(12);
        assertThat(investigator.getLuck()).isEqualTo(50);
        assertThat(investigator.getUser()).isSameAs(user);
    }
}
