package com.quinter.madness_table.investigator;

import com.quinter.madness_table.rules.SuccessLevel;

import java.time.Instant;

public record RollResponse(
        Long id,
        Long investigatorId,
        String skillName,
        int skillValue,
        int result,
        SuccessLevel level,
        boolean success,
        int netDice,
        Long rolledById,
        String rollerByName,
        Instant createdAt
) {

    public static RollResponse from(RollLog log) {
        return new RollResponse(
                log.getId(), log.getInvestigator().getId(),
                log.getSkillName(), log.getSkillValue(),
                log.getRollResult(), log.getSuccessLevel(),
                log.getSuccessLevel().isSuccess(), log.getNetDice(),
                log.getRolledBy().getId(), log.getRolledBy().getDisplayName(),
                log.getCreatedAt()
        );
    }
}
