package com.quinter.madness_table.investigator;

public record SkillResponse(Long id, String name, int baseValue) {

    public static SkillResponse from(CampaignSkill skill) {
        return new SkillResponse(skill.getId(), skill.getName(), skill.getBaseValue());
    }
}
