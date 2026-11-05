package com.quinter.madness_table.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DiceExpressionTest {

    @ParameterizedTest(name = "\"{0}\" -> {1} dado(s) de {2} caras, modificador {3}")
    @CsvSource({
            "1d6,      1, 6,   0",
            "d6,       1, 6,   0",
            "2d6+1,    2, 6,   1",
            "1d10-2,   1, 10, -2",
            "d100,     1, 100, 0",
            "3D6,      3, 6,   0",
            "2d6 + 1,  2, 6,   1",
            "' 1d8 ',  1, 8,   0",
            "5,        0, 0,   5",
            "0,        0, 0,   0",
            "+3,       0, 0,   3",
            "-2,       0, 0,  -2"
    })
    void parsesValidExpressions(String text, int count, int sides, int modifier) {
        assertThat(DiceExpression.parse(text))
                .isEqualTo(new DiceExpression(count, sides, modifier));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "  ", "d", "2d", "0d6", "1d0", "1d1", "2d6+", "2x6", "1d6+1d4",
            "101d6", "1d1001", "1d10000", "abc", "-", "2d6++1", "2d6-+1", "1.5d6"
    })
    void rejectsInvalidExpressions(String text) {
        assertThatThrownBy(() -> DiceExpression.parse(text))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rollSumsTheDiceAndTheModifier() {
        DiceRoll roll = DiceExpression.parse("2d6+1").roll(FixedRandomSource.of(3, 4));

        assertThat(roll.dice()).containsExactly(3, 4);
        assertThat(roll.modifier()).isEqualTo(1);
        assertThat(roll.total()).isEqualTo(8);
    }

    @Test
    void rollCanBeNegativeWithANegativeModifier() {
        DiceRoll roll = DiceExpression.parse("1d4-3").roll(FixedRandomSource.of(1));

        assertThat(roll.total()).isEqualTo(-2);
    }

    @Test
    void aConstantRollsNoDice() {
        DiceRoll roll = DiceExpression.parse("5").roll(FixedRandomSource.of());

        assertThat(roll.dice()).isEmpty();
        assertThat(roll.total()).isEqualTo(5);
    }

    @Test
    void rollRollsExactlyTheRequestedNumberOfDice() {
        DiceExpression expression = DiceExpression.parse("3d6");

        FixedRandomSource dice = FixedRandomSource.of(1, 2, 3, 4);
        expression.roll(dice);

        assertThat(dice.roll(6)).isEqualTo(4);
    }

    @ParameterizedTest
    @CsvSource({
            "2d6+1,  3, 13",
            "1d4-3, -2,  1",
            "1d100,  1, 100",
            "5,      5,  5"
    })
    void minAndMaxGivenThePossibleRange(String text, int min, int max) {
        DiceExpression expression = DiceExpression.parse(text);

        assertThat(expression.max()).isEqualTo(max);
        assertThat(expression.min()).isEqualTo(min);
    }

    @ParameterizedTest
    @CsvSource ({
            "2D6 + 1, 2d6+1",
            "d6,      1d6",
            "1d10-2,  1d10-2",
            "+3,      3"
    })
    void toStringGivesTheCanonicalForm(String text, String expected) {
        assertThat(DiceExpression.parse(text)).hasToString(expected);
    }
}
