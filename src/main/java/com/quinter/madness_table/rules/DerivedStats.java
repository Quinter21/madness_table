package com.quinter.madness_table.rules;

public record DerivedStats(int maxHitPoints, int maxMagicPoints, int startingSanity,
                           DiceExpression damageBonus, int build, int move) {
}
