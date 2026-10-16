package com.quinter.madness_table.campaign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCampaignRequest(
    @NotBlank @Size(max = 150) String name,
    @Size(max = 2000) String description) {

}
