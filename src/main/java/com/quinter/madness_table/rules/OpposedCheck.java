package com.quinter.madness_table.rules;

public final class OpposedCheck {

    private OpposedCheck() {
    }

    public static OpposedOutcome resolve(CheckResult first, CheckResult second) {
        if (!first.level().isSuccess() && !second.level().isSuccess()){
            return OpposedOutcome.NO_WINNER;
        }

        int byLevel = first.level().compareTo(second.level());
        if (byLevel != 0) {
            return byLevel > 0 ? OpposedOutcome.FIRST_WINS : OpposedOutcome.SECOND_WINS;
        }

        if (first.skill() != second.skill()) {
            return first.skill() > second.skill()
                    ? OpposedOutcome.FIRST_WINS
                    : OpposedOutcome.SECOND_WINS;
        }

        return OpposedOutcome.TIE;
    }

    public static OpposedResult perform(RandomSource random,
                                              int firstSkill, int firstBonus, int firstPenalty,
                                              int secondSkill, int secondBonus, int seconfPenalty) {
        CheckResult first = SkillCheck.perform(random, firstSkill, firstBonus, firstPenalty);
        CheckResult second = SkillCheck.perform(random, secondSkill, secondBonus, seconfPenalty);
        return new OpposedResult(first, second, resolve(first, second));
    }
}
