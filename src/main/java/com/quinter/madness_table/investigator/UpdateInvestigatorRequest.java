package com.quinter.madness_table.investigator;

import jakarta.validation.constraints.*;

public record UpdateInvestigatorRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String occupation,
        @Min(15) @Max(90) int age,
        @Size(max = 5000) String backstory
) {
}
