package com.quinter.madness_table.investigator;

import com.quinter.madness_table.campaign.CampaignAccess;
import com.quinter.madness_table.campaign.CampaignMember;
import com.quinter.madness_table.campaign.CampaignRole;
import com.quinter.madness_table.campaign.ResourceNotFoundException;
import com.quinter.madness_table.rules.CharacterRules;
import com.quinter.madness_table.rules.DerivedStats;
import com.quinter.madness_table.rules.RandomSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InvestigatorService {

    private final CampaignAccess access;
    private final InvestigatorRepository investigatorRepository;
    private final RandomSource random;
    private final InvestigatorAccess investigatorAccess;

    public InvestigatorService(CampaignAccess access, InvestigatorRepository investigatorRepository,
                               RandomSource random, InvestigatorAccess investigatorAccess) {
        this.access = access;
        this.investigatorRepository = investigatorRepository;
        this.random = random;
        this.investigatorAccess = investigatorAccess;
    }

    @Transactional
    public InvestigatorResponse create(Long userId, Long campaignId,
                                       CreateInvestigatorRequest request) {
        CampaignMember member = access.requireMember(campaignId, userId);

        Characteristics characteristics = request.characteristics().toEntity();
        DerivedStats derivedStats = CharacterRules.derive(characteristics.toAttributes(), request.age());
        int luck = CharacterRules.rollStartingLuck(random);

        Investigator saved = investigatorRepository.save(new Investigator(
                member.getCampaign(), member.getUser(),
                request.name().trim(), request.occupation().trim(),
                request.age(), request.backstory(), characteristics,
                derivedStats.maxHitPoints(), derivedStats.maxMagicPoints(),
                derivedStats.startingSanity(), luck
        ));

        return InvestigatorResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<InvestigatorResponse> list(Long userId, Long campaignId) {
        CampaignMember member = access.requireMember(campaignId, userId);

        List<Investigator> investigators = member.getRole() == CampaignRole.GUARDIAN
                ? investigatorRepository.findByCampaignIdOrderByNameAsc(campaignId)
                : investigatorRepository.findByCampaignIdAndUserIdOrderByNameAsc(campaignId, userId);

        return investigators.stream().map(InvestigatorResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public InvestigatorResponse get(Long userId, Long campaignId, Long investigatorId) {
        return InvestigatorResponse.from(investigatorAccess.requireVisible(userId, campaignId, investigatorId));
    }

    @Transactional
    public InvestigatorResponse update(Long userId, Long campaignId, Long investigatorId,
                                       UpdateInvestigatorRequest request) {
        Investigator investigator = investigatorAccess.requireVisible(userId, campaignId, investigatorId);
        investigator.updateDetails(request.name().trim(), request.occupation().trim(), request.age(), request.backstory());
        return InvestigatorResponse.from(investigator);
    }

    @Transactional
    public void delete (Long userId, Long campaignId, Long investigatorId) {
        investigatorRepository.delete(investigatorAccess.requireVisible(userId, campaignId, investigatorId));
    }
}
