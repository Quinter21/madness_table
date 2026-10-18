package com.quinter.madness_table.campaign;

import java.time.Instant;

public record PendingRequestResponse(Long id, Long userId, String displayName, Instant joinedAt) {

    public static PendingRequestResponse from(JoinRequest r) {
        return new PendingRequestResponse(r.getId(), r.getUser().getId(),
                r.getUser().getDisplayName(), r.getCreatedAt());
    }
}
