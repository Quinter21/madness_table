package com.quinter.madness_table.campaign;

import java.time.Instant;

public record CampaignResponse(
        Long id,
        String name,
        String description,
        CampaignRole myRole,
        String inviteCode,
        Instant createdAt
) {

    public static CampaignResponse from(Campaign campaign, CampaignRole role){
        return new CampaignResponse(campaign.getId(), campaign.getName(),
                campaign.getDescription(), role, campaign.getInviteCode(), campaign.getCreatedAt());
    }
}
