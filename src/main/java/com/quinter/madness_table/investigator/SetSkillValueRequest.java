package com.quinter.madness_table.investigator;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SetSkillValueRequest(
        @Min(0) @Max(100) int value
) {
}
