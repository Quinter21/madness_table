package com.quinter.madness_table.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SkillCheckTest {
    @ParameterizedTest(name = "tirada {0} con habilidad {1} -> {2}")
    @CsvSource({
            "  1, 65, CRITICAL",
            "  2, 65, EXTREME",
            " 13, 65, EXTREME",
            " 14, 65, HARD",
            " 32, 65, HARD",
            " 33, 65, REGULAR",
            " 65, 65, REGULAR",
            " 66, 65, FAILURE",
            " 95, 65, FAILURE",
            " 96, 65, FAILURE",
            " 99, 65, FAILURE",
            "100, 65, FUMBLE",

            // Habilidad 40 (< 50): difícil <= 20, extremo <= 8; pifia desde 96
            "  8, 40, EXTREME",
            "  9, 40, HARD",
            " 20, 40, HARD",
            " 21, 40, REGULAR",
            " 40, 40, REGULAR",
            " 41, 40, FAILURE",
            " 95, 40, FAILURE",
            " 96, 40, FUMBLE",
            "100, 40, FUMBLE",

            // El umbral de la pifia cambia justo en 50
            " 96, 49, FUMBLE",
            " 96, 50, FAILURE",
            " 10, 50, EXTREME",
            " 25, 50, HARD",
            " 26, 50, REGULAR",
            "100, 50, FUMBLE",

            // Habilidad muy alta
            " 19, 99, EXTREME",
            " 20, 99, HARD",
            " 49, 99, HARD",
            " 50, 99, REGULAR",
            " 99, 99, REGULAR",
            "100, 99, FUMBLE",
            "100,100, FUMBLE",
            " 99,100, REGULAR",

            // Habilidad muy baja
            "  1,  1, CRITICAL",
            "  2,  1, FAILURE",
            "  1,  4, CRITICAL",
            "  1,  5, CRITICAL",
            "  2,  5, HARD",

            // Habilidad 0: nada se supera
            "  1,  0, FAILURE",
            " 50,  0, FAILURE",
            " 96,  0, FUMBLE"
    })
    void evaluatesEveryBoundary(int roll, int skill, SuccessLevel expected) {
        assertThat(SkillCheck.evaluate(roll, skill)).isEqualTo(expected);
    }

    @Test
    void invalidInputsAreRejected() {
        assertThatThrownBy(() -> SkillCheck.evaluate(0, 50))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SkillCheck.evaluate(101, 50))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SkillCheck.evaluate(10, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anySkillFrom1To99HasExactlyAsManySeccessesAsItsValue() {
        for (int skill = 1; skill <= 99; skill++){
            int successes = 0;
            for (int roll = 1; roll <= 100; roll++) {
                if (SkillCheck.evaluate(roll, skill).isSuccess()){
                    successes++;
                }
            }
            assertThat(successes).as("éxitos con habilidad %d", skill).isEqualTo(skill);
        }
    }

    @Test
    void betterRollsNeverGiveWorseLevels() {
        for (int skill = 0; skill <= 100; skill++){
            SuccessLevel previous = SuccessLevel.CRITICAL;
            for (int roll = 1; roll <= 100; roll++){
                SuccessLevel level = SkillCheck.evaluate(roll, skill);
                if (level != SuccessLevel.FUMBLE) {
                    assertThat(level.compareTo(previous))
                            .as("habilidad %d, tirada %d", skill, roll)
                            .isLessThanOrEqualTo(0);
                    previous = level;
                }
            }
        }
    }

    @Test
    void levelOrderIsWorstToBest() {
        assertThat(SuccessLevel.values()).containsExactly(
                SuccessLevel.FUMBLE, SuccessLevel.FAILURE, SuccessLevel.REGULAR,
                SuccessLevel.HARD, SuccessLevel.EXTREME, SuccessLevel.CRITICAL
        );
    }

    @Test
    void onlyRegularOrBetterAreSuccesses() {
        assertThat(SuccessLevel.FUMBLE.isSuccess()).isFalse();
        assertThat(SuccessLevel.FAILURE.isSuccess()).isFalse();
        assertThat(SuccessLevel.REGULAR.isSuccess()).isTrue();
        assertThat(SuccessLevel.HARD.isSuccess()).isTrue();
        assertThat(SuccessLevel.EXTREME.isSuccess()).isTrue();
        assertThat(SuccessLevel.CRITICAL.isSuccess()).isTrue();
        assertThat(SuccessLevel.HARD.isBetterThan(SuccessLevel.REGULAR)).isTrue();
        assertThat(SuccessLevel.REGULAR.isBetterThan(SuccessLevel.REGULAR)).isFalse();
    }

    @Test
    void performUsesTheDiceAndEvaluatesTheResult() {
        CheckResult result = SkillCheck.perform(FixedRandomSource.of(9, 4), 65, 0, 0);

        assertThat(result.result()).isEqualTo(38);
        assertThat(result.skill()).isEqualTo(65);
        assertThat(result.level()).isEqualTo(SuccessLevel.REGULAR);
    }

    @Test
    void aBonusDieCanImproveTheLevel() {
        CheckResult result = SkillCheck.perform(FixedRandomSource.of(9, 4, 2), 65, 1, 0);

        assertThat(result.result()).isEqualTo(18);
        assertThat(result.level()).isEqualTo(SuccessLevel.HARD);
        assertThat(result.roll().netDice()).isEqualTo(1);
    }

    @Test
    void aPenaltyDieCanWorsenTheLevel() {
        CheckResult result = SkillCheck.perform(FixedRandomSource.of(9, 4, 8), 65, 0, 1);

        assertThat(result.result()).isEqualTo(78);
        assertThat(result.level()).isEqualTo(SuccessLevel.FAILURE);
    }
}
