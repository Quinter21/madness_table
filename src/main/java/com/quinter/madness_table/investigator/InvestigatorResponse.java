package com.quinter.madness_table.investigator;

import com.quinter.madness_table.rules.CharacterRules;
import com.quinter.madness_table.rules.DerivedStats;

import java.time.Instant;

public record InvestigatorResponse(
        Long id,
        Long campaignId,
        Long ownerId,
        String ownerName,
        String name,
        String occupation,
        int age,
        String backstory,
        CharacteristicsDto characteristics,
        int hitPoints,
        int magicPoints,
        int sanity,
        int luck,
        Derived derived,
        Instant createdAt
) {

    public record Derived(int maxHitPoints, int maxMagicPoints, String damageBonus,
                          int build, int move){
    }

    public static InvestigatorResponse from(Investigator i) {
        Characteristics c = i.getCharacteristics();
        DerivedStats d = CharacterRules.derive(c.toAttributes(), i.getAge());

        return new InvestigatorResponse(
                i.getId(), i.getCampaign().getId(), i.getUser().getId(),
                i.getUser().getDisplayName(), i.getName(), i.getOccupation(),
                i.getAge(),i.getBackstory(), CharacteristicsDto.from(c),
                i.getHitPoints(), i.getMagicPoints(), i.getSanity(), i.getLuck(),
                new Derived(d.maxHitPoints(), d.maxMagicPoints(),
                        d.damageBonus().toString(), d.build(), d.move()),
                i.getCreatedAt()
        );
    }
}
