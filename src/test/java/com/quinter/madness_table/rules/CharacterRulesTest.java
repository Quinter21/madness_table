package com.quinter.madness_table.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CharacterRulesTest {

    private static Attributes attrs(int str, int con, int siz, int dex, int pow) {
        return new Attributes(str, con, siz, dex, 50, 50, pow, 50);
    }

    @Test
    void attributesRejectEveryValueOutOfRange() {
        for (int position = 0; position < 8; position++) {
            for (int invalid : new int[] {0, 100, -5}) {
                int [] v = {50, 50, 50, 50, 50, 50, 50, 50};
                v[position] = invalid;

                assertThatThrownBy(() -> new Attributes(
                        v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7]))
                        .as("posición %d, con valor %d", position, invalid)
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Test
    void attributesAcceptTheLimits() {
        assertThat(new Attributes(1, 1, 1, 1, 1, 1, 1, 1)
                .strength()).isEqualTo(1);
        assertThat(new Attributes(99, 99, 99, 99, 99, 99, 99, 99)
                .education()).isEqualTo(99);
    }

    @ParameterizedTest(name = "CON {0} + TAM {1} -> {2} puntos de vida")
    @CsvSource({
            "50, 60, 11",
            "60, 65, 12",
            "70, 65, 13",
            "40, 40,  8",
            "15, 40,  5",
            "10, 10,  2",
            "99, 99, 19"
    })
    void hitPointsRoundDown(int constitution, int size, int expected) {
        assertThat(CharacterRules.hitPoints(attrs(50, constitution, size, 50, 50)))
                .isEqualTo(expected);
    }

    @ParameterizedTest(name = "POD {0} -> {1} puntos de magia")
    @CsvSource({
            "60, 12",
            "59, 11",
            "50, 10",
            "49,  9",
            " 5,  1",
            " 4,  0",
            "99, 19"
    })
    void magicPointsRoundDown(int power, int expected) {
        assertThat(CharacterRules.magicPoints(attrs(50, 50, 50, 50, power)))
                .isEqualTo(expected);
    }

    @ParameterizedTest(name = "FUE+TAM {0} -> bonificación {1}, corpulencia {2}")
    @CsvSource({
            "  2, -2, -2",
            " 64, -2, -2",
            " 65, -1, -1",
            " 84, -1, -1",
            " 85,  0,  0",
            "124,  0,  0",
            "125, 1d4, 1",
            "164, 1d4, 1",
            "165, 1d6, 2",
            "198, 1d6, 2"
    })
    void damageBonusAndBuildFollowTheTable(int total, String damageBonus, int build) {
        assertThat(CharacterRules.damageBonus(total)).hasToString(damageBonus);
        assertThat(CharacterRules.build(total)).isEqualTo(build);
    }

    @Test
    void damageBonusAndBuildRejectImpossibleTotals() {
        assertThatThrownBy(() -> CharacterRules.damageBonus(1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CharacterRules.build(199))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buildNeverDecreasesAsStrengthPlusSizeGrows() {
        int previous = Integer.MIN_VALUE;
        for (int total = 2; total <= 198; total++) {
            int build = CharacterRules.build(total);

            assertThat(build).as("FUE+TAM %d", total)
                    .isBetween(-2, 2)
                    .isGreaterThanOrEqualTo(previous);
            previous = build;
        }
    }

    @Test
    void damageBonusAndBuildChangeAtExactlyTheSameThresholds() {
        for (int total = 3; total <= 198; total++) {
            boolean damageChanged = !CharacterRules.damageBonus(total)
                    .equals(CharacterRules.damageBonus(total-1));
            boolean buildChanged = CharacterRules.build(total) != CharacterRules.build(total-1);

            assertThat(damageChanged).as("FUE+TAM %d", total).isEqualTo(buildChanged);
        }
    }

    @ParameterizedTest(name = "FUE {0}, TAM {1}, DES {2}, años {3} -> movimiento {4}")
    @CsvSource({
            // Ambas por debajo de TAM: 7
            "50, 60, 50, 25, 7",
            "59, 60, 59, 25, 7",
            // Ambas por encima de TAM: 9
            "70, 60, 70, 25, 9",
            "61, 60, 61, 25, 9",
            // Mezcla o empates: 8
            "70, 60, 50, 25, 8",
            "50, 60, 70, 25, 8",
            "60, 60, 50, 25, 8",
            "60, 60, 60, 25, 8",
            "60, 60, 70, 25, 8",
            // Penalización por edad (base 8)
            "60, 60, 60, 39, 8",
            "60, 60, 60, 40, 7",
            "60, 60, 60, 49, 7",
            "60, 60, 60, 50, 6",
            "60, 60, 60, 59, 6",
            "60, 60, 60, 60, 5",
            "60, 60, 60, 70, 4",
            "60, 60, 60, 80, 3",
            "60, 60, 60, 89, 3",
            // El movimiento nunca baja de 0
            "50, 60, 50, 120, 0"
    })
    void moveDependsOnStrengthDexterityAndAge(int str, int siz, int dex, int age, int expected) {
        assertThat(CharacterRules.move(attrs(str, 50, siz, dex, 50), age)).isEqualTo(expected);
    }

    @Test
    void deriveCombinesEveryValue() {
        Attributes attributes = new Attributes(60, 70, 65, 55,
                                        50, 64, 75, 80);
        DerivedStats stats = CharacterRules.derive(attributes, 39);

        assertThat(stats.maxHitPoints()).isEqualTo(13);
        assertThat(stats.maxMagicPoints()).isEqualTo(15);
        assertThat(stats.startingSanity()).isEqualTo(75);
        assertThat(stats.damageBonus()).hasToString("1d4");
        assertThat(stats.build()).isEqualTo(1);
        assertThat(stats.move()).isEqualTo(7);
    }

    @Test
    void deriveRejectsInvalidAges() {
        Attributes attributes = new Attributes(60, 70, 65, 55,
                                        50, 64, 75, 80);

        assertThatThrownBy(() -> CharacterRules.derive(attributes, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CharacterRules.derive(attributes, -6))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "dados {0}, {1}, {2} -> suerte {3}")
    @CsvSource({
            "1, 1, 1, 15",
            "3, 4, 5, 60",
            "6, 6, 6, 90"
    })
    void startingLuckIsThreeD6TimesFive(int a, int b, int c, int expected) {
        assertThat(CharacterRules.rollStartingLuck(FixedRandomSource.of(a, b, c)))
                .isEqualTo(expected);
    }

    @Test
    void startingLuckRollsExactlyThreeDice() {
        FixedRandomSource dice = FixedRandomSource.of(2, 2, 2, 5);
        assertThat(CharacterRules.rollStartingLuck(dice))
                .isEqualTo(30);
        assertThat(dice.roll(6)).isEqualTo(5);
    }

    @Test
    void realStartingLuckIsAlwaysAMultipleOfFiveBetween15And90() {
        Map<Integer, Integer> frequency = new TreeMap<>();
        int combinations = 0;

        for (int a = 1; a <= 6; a++) {
            for (int b = 1; b <= 6; b++) {
                for (int c= 1; c <= 6; c++) {
                    int luck = CharacterRules.rollStartingLuck(FixedRandomSource.of(a, b, c));

                    assertThat(luck).isEqualTo((a+b+c)*5);
                    frequency.merge(luck, 1, Integer::sum);
                    combinations++;
                }
            }
        }

        assertThat(combinations).isEqualTo(216);
        assertThat(frequency).hasSize(16);
        assertThat(frequency.keySet())
                .allMatch(luck -> luck >= 15 && luck <= 90 && luck % 5 == 0);

        assertThat(frequency.get(15)).isEqualTo(1);
        assertThat(frequency.get(90)).isEqualTo(1);
        assertThat(frequency.get(50)).isEqualTo(27);
        assertThat(frequency.get(55)).isEqualTo(27);
    }
}
