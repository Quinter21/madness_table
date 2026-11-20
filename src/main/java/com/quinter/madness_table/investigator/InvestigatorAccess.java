package com.quinter.madness_table.investigator;

import com.quinter.madness_table.campaign.CampaignAccess;
import com.quinter.madness_table.campaign.CampaignMember;
import com.quinter.madness_table.campaign.CampaignRole;
import com.quinter.madness_table.campaign.ResourceNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class InvestigatorAccess {

    private final CampaignAccess campaignAccess;
    private final InvestigatorRepository investigatorRepository;

    public InvestigatorAccess(CampaignAccess campaignAccess, InvestigatorRepository investigatorRepository){
        this.campaignAccess = campaignAccess;
        this.investigatorRepository = investigatorRepository;
    }

    public Investigator requireVisible(Long userId, Long campaignId, Long investigatorId) {
        CampaignMember member = campaignAccess.requireMember(campaignId, userId);
        Investigator investigator = investigatorRepository
                .findByIdAndCampaignId(investigatorId, campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Investigador no encontrado"));

        boolean isOwner = investigator.getUser().getId().equals(userId);
        if (!isOwner && member.getRole() != CampaignRole.GUARDIAN) {
            throw new ResourceNotFoundException("Investigador no encontrado");
        }
        return investigator;
    }
}
