package com.quinter.madness_table.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OpposedCheckTest {

    private static CheckResult result (SuccessLevel level, int skill){
        return new CheckResult(new PercentileRoll(0, List.of(0), 0, 50), skill, level);
    }

    @ParameterizedTest(name = "{0} (hab {1} contra {2} (hab {3}) -> {4}")
    @CsvSource({
            // Gana el mejor nivel, aunque su habilidad sea menor
            "CRITICAL, 30, EXTREME,  80, FIRST_WINS",
            "HARD,     40, REGULAR,  90, FIRST_WINS",
            "REGULAR,  40, HARD,     30, SECOND_WINS",
            "EXTREME,  50, CRITICAL, 20, SECOND_WINS",

            // Un éxito siempre gana a un fallo, sea cual sea la habilidad
            "REGULAR,  30, FAILURE,  90, FIRST_WINS",
            "FAILURE,  90, REGULAR,  30, SECOND_WINS",
            "REGULAR,  10, FUMBLE,   99, FIRST_WINS",
            "FUMBLE,   99, REGULAR,  10, SECOND_WINS",

            // Mismo nivel: decide la habilidad
            "REGULAR,  60, REGULAR,  40, FIRST_WINS",
            "REGULAR,  40, REGULAR,  60, SECOND_WINS",
            "CRITICAL, 20, CRITICAL, 60, SECOND_WINS",
            "EXTREME,  80, EXTREME,  79, FIRST_WINS",

            // Mismo nivel y misma habilidad: empate
            "REGULAR,  50, REGULAR,  50, TIE",
            "EXTREME,  70, EXTREME,  70, TIE",
            "CRITICAL, 90, CRITICAL, 90, TIE",

            // Nadie acierta: nadie gana, sea cual sea la habilidad o la pifia
            "FAILURE,  90, FAILURE,  10, NO_WINNER",
            "FAILURE,  50, FAILURE,  50, NO_WINNER",
            "FUMBLE,   50, FAILURE,  50, NO_WINNER",
            "FAILURE,  50, FUMBLE,   50, NO_WINNER",
            "FUMBLE,   10, FUMBLE,   90, NO_WINNER"
    })
    void resolvesEveryCase(SuccessLevel firstLevel, int firstSkill,
                           SuccessLevel secondLevel, int secondSkill,
                           OpposedOutcome expected){
        OpposedOutcome outcome = OpposedCheck.resolve(result(firstLevel, firstSkill), result(secondLevel, secondSkill));

        assertThat(outcome).isEqualTo(expected);
    }

    @Test
    void swappingTheSidesMirrorsTheOutcome() {
        int[] skills = {10, 40, 50, 75};
        for (SuccessLevel levelA : SuccessLevel.values()) {
            for (SuccessLevel levelB : SuccessLevel.values()) {
                for (int skillA : skills) {
                    for (int skillB : skills) {
                        OpposedOutcome direct = OpposedCheck.resolve(result(levelA, skillA), result(levelB, skillB));
                        OpposedOutcome indirect = OpposedCheck.resolve(result(levelB, skillB), result(levelA, skillA));

                        assertThat(indirect).as("%s/%d contra %s/%d", levelA, skillA, levelB, skillB)
                                .isEqualTo(mirror(direct));
                    }
                }
            }
        }
    }

    private static OpposedOutcome mirror(OpposedOutcome outcome) {
        return switch (outcome){
            case FIRST_WINS -> OpposedOutcome.SECOND_WINS;
            case SECOND_WINS -> OpposedOutcome.FIRST_WINS;
            case TIE -> OpposedOutcome.TIE;
            case NO_WINNER -> OpposedOutcome.NO_WINNER;
        };
    }

    @Test
    void performRollsTheFirstSideBeforeTheSecond() {
        OpposedResult result = OpposedCheck.perform(
                FixedRandomSource.of(9, 4, 3, 2),
                67, 0, 0,
                40, 0, 0
        );

        assertThat(result.first().result()).isEqualTo(38);
        assertThat(result.second().result()).isEqualTo(12);
        assertThat(result.first().level()).isEqualTo(SuccessLevel.REGULAR);
        assertThat(result.second().level()).isEqualTo(SuccessLevel.HARD);
        assertThat(result.outcome()).isEqualTo(OpposedOutcome.SECOND_WINS);
    }

    @Test
    void eachSideHasItsOwnBonusAndPenaltyDice() {
        FixedRandomSource dice = FixedRandomSource.of(9, 4, 2, 9, 4);
        OpposedResult result = OpposedCheck.perform(dice,
                65, 1, 0,
                65, 0, 0);

        assertThat(result.first().roll().netDice()).isEqualTo(1);
        assertThat(result.first().result()).isEqualTo(18);
        assertThat(result.first().level()).isEqualTo(SuccessLevel.HARD);
        assertThat(result.second().roll().netDice()).isZero();
        assertThat(result.second().result()).isEqualTo(38);
        assertThat(result.outcome()).isEqualTo(OpposedOutcome.FIRST_WINS);
    }

    @Test
    void performKeepsBothFullResults() {
        OpposedResult result = OpposedCheck.perform(
                FixedRandomSource.of(9, 4, 9, 4),
                65, 0, 0,
                65, 0, 0
        );

        assertThat(result.first().skill()).isEqualTo(65);
        assertThat(result.second().skill()).isEqualTo(65);
        assertThat(result.outcome()).isEqualTo(OpposedOutcome.TIE);
    }

    @Test
    void realRollsAlwaysProduceAConsistentOutcome() {
        JavaRandomSource random = new JavaRandomSource();
        for (int i = 0; i < 5000; i++) {
            OpposedResult result = OpposedCheck.perform(random, 60, 0, 0, 40, 0, 0);

            assertThat(result.outcome())
                    .isEqualTo(OpposedCheck.resolve(result.first(), result.second()));
            if (result.outcome() == OpposedOutcome.NO_WINNER) {
                assertThat(result.first().level().isSuccess()).isFalse();
                assertThat(result.second().level().isSuccess()).isFalse();
            }
        }
    }
}
