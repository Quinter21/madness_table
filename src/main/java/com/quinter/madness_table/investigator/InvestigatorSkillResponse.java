package com.quinter.madness_table.investigator;

public record InvestigatorSkillResponse(Long skillId, String name, int baseValue,
                                        int value, boolean modified) {
    public static InvestigatorSkillResponse of (CampaignSkill skill, Integer customValue){
        return new InvestigatorSkillResponse(skill.getId(), skill.getName(), skill.getBaseValue(),
                customValue != null ? customValue : skill.getBaseValue(), customValue != null);
    }
}
