package com.quinter.madness_table.rules;

public final class SkillCheck {

    private SkillCheck() {}

    public static SuccessLevel evaluate(int roll, int skill) {
        if (roll < 1 || roll > 100) {
            throw new IllegalArgumentException("El resultado debe estar entre 1 y 100: " + roll);
        }

        if (skill < 0) {
            throw new IllegalArgumentException("La habilidad no puede ser negativa");
        }

        if (isFumble(roll, skill)) {
            return SuccessLevel.FUMBLE;
        }
        if (roll > skill) {
            return SuccessLevel.FAILURE;
        }
        if (roll == 1) {
            return SuccessLevel.CRITICAL;
        }
        if (roll <= skill/5) {
            return SuccessLevel.EXTREME;
        }
        if (roll <= skill/2) {
            return SuccessLevel.HARD;
        }
            return SuccessLevel.REGULAR;
    }

    public static CheckResult perform(RandomSource random, int skill,
                                      int bonusDice, int penaltyDice){
        PercentileRoll roll = PercentileRoll.roll(random, bonusDice, penaltyDice);
        return new CheckResult(roll, skill, evaluate(roll.result(), skill));
    }

    private static boolean isFumble(int roll, int skill) {
        return skill < 50 ? roll >= 96 : roll == 100;
    }
}
