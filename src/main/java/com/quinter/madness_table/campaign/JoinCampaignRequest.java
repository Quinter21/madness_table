package com.quinter.madness_table.campaign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinCampaignRequest(
        @NotBlank @Size(max = 20) String inviteCode
) {
}
