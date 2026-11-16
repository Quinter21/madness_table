package com.quinter.madness_table.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RulesEdgeCasesTest {

    @ParameterizedTest(name = "{0}d{1}{2} es imposible")
    @CsvSource({
            "0,    6, 0",   // una constante no puede tener caras
            "-1,   6, 0",
            "11,  6, 0",
            "1,    1, 0",   // un dado necesita al menos 2 caras
            "1,    0, 0",
            "1, 101, 0"
    })
    void diceExpressionConstructorRejectsImpossibleValue(int count, int sides, int modifier) {
        assertThatThrownBy(() -> new DiceExpression(count, sides, modifier))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void diceExpresionAcceptsItsLimits() {
        DiceExpression biggest = new DiceExpression(
                DiceExpression.MAX_DICE, DiceExpression.MAX_SIDES, 0
        );

        assertThat(biggest.max()).isEqualTo(1000);
        assertThat(new DiceExpression(1, 2, 0).min()).isEqualTo(1);
        assertThat(new DiceExpression(0, 0, -5).min()).isEqualTo(-5);
    }



    @Test
    void diceRollKeepsItsOwnCopyOfTheDice() {
        List<Integer> source = new ArrayList<>(List.of(3, 4));
        DiceRoll roll = new DiceRoll(source, 0, 7);

        source.add(6);

        assertThat(roll.dice()).containsExactly(3, 4);
        assertThatThrownBy(() -> roll.dice().add(1))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void percentileRollKeepsItsOwnCopyOfTheTens() {
        List<Integer> source = new ArrayList<>(List.of(30));
        PercentileRoll roll = new PercentileRoll(5, source, 0, 35);

        source.add(60);

        assertThat(roll.tens()).containsExactly(30);
        assertThatThrownBy(() -> roll.tens().add(10))
                .isInstanceOf(UnsupportedOperationException.class);
    }


    @ParameterizedTest(name = "tirada {0} con habilidad {1} -> {2}")
    @CsvSource({
            "  1, 150, CRITICAL",
            " 30, 150, EXTREME",
            " 31, 150, HARD",
            " 75, 150, HARD",
            " 76, 150, REGULAR",
            " 99, 150, REGULAR",
            "100, 150, FUMBLE"     // el 100 siempre es pifia
    })
    void skillsAbove100StillFumbleOnA100(int roll, int skill, SuccessLevel expected) {
        assertThat(SkillCheck.evaluate(roll, skill)).isEqualTo(expected);
    }


    @Test
    void aOneSidedDiceAlwaysGivesOne() {
        JavaRandomSource source = new JavaRandomSource();

        for (int i = 0; i < 100; i++) {
            assertThat(source.roll(1)).isEqualTo(1);
        }
    }

    @Test
    void sameSeedGivesTheSameRolls() {
        JavaRandomSource a = new JavaRandomSource(new Random(42));
        JavaRandomSource b = new JavaRandomSource(new Random(42));

        for (int i = 0; i < 100; i++) {
            assertThat(a.roll(100)).isEqualTo(b.roll(100));
        }
    }

    @Test
    void fixedSourceReturnsTheValuesInOrderAndThenRunsOut() {
        FixedRandomSource source = FixedRandomSource.of(3, 6);

        assertThat(source.roll(6)).isEqualTo(3);
        assertThat(source.roll(6)).isEqualTo(6);
        assertThatThrownBy(() -> source.roll(6)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fixedSourceRejectsValuesThatTheDiceCannotGive() {
        assertThatThrownBy(() -> FixedRandomSource.of(7).roll(6))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> FixedRandomSource.of(0).roll(6))
                .isInstanceOf(IllegalStateException.class);
    }


}
