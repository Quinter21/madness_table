package com.quinter.madness_table.investigator;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RollRequest(
        @NotNull Long skillId,
        @Min(0) @Max(2) int bonusDice,
        @Min(0) @Max(2) int penaltyDice
) {
}
