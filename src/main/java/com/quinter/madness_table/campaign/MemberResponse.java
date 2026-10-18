package com.quinter.madness_table.campaign;

import java.time.Instant;

public record MemberResponse(Long userId, String displayName, CampaignRole role, Instant joinedAt) {

    public static MemberResponse from(CampaignMember m) {
        return new MemberResponse(m.getUser().getId(), m.getUser().getDisplayName(),
                m.getRole(), m.getJoinedAt());
    }
}
