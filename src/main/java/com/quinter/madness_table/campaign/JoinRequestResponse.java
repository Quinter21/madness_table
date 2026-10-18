package com.quinter.madness_table.campaign;

import java.time.Instant;

public record JoinRequestResponse(
        Long id,
        Long campaignId,
        String campaignName,
        JoinRequestStatus status,
        Instant createdAt,
        Instant resolvedAt
) {

    public static JoinRequestResponse from(JoinRequest r){
        return new JoinRequestResponse(r.getId(), r.getCampaign().getId(), r.getCampaign().getName(),
                r.getStatus(), r.getCreatedAt(), r.getResolvedAt());
    }
}
