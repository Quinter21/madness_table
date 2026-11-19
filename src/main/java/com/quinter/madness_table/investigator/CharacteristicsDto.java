package com.quinter.madness_table.investigator;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CharacteristicsDto(
        @Min(1) @Max(99) int strength,
        @Min(1) @Max(99) int constitution,
        @Min(1) @Max(99) int size,
        @Min(1) @Max(99) int dexterity,
        @Min(1) @Max(99) int appearance,
        @Min(1) @Max(99) int intelligence,
        @Min(1) @Max(99) int power,
        @Min(1) @Max(99) int education
) {

    public static CharacteristicsDto from(Characteristics c) {
        return new CharacteristicsDto(c.getStrength(), c.getConstitution(), c.getSize(), c.getDexterity(),
                c.getAppearance(), c.getIntelligence(), c.getPower(), c.getEducation());
    }

    public Characteristics toEntity() {
        return new Characteristics(strength, constitution, size, dexterity,
                appearance, intelligence, power, education);
    }
}
