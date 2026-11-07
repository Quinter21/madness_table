package com.quinter.madness_table.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PercentileRollTest {

    private static FixedRandomSource dice(int units, int... tens) {
        int[] values = new int[1 + tens.length];
        values[0] = units + 1;
        for (int i = 0; i < tens.length; i++){
            values[i+1] = tens[i] / 10 + 1;
        }

        return FixedRandomSource.of(values);
    }

    @ParameterizedTest(name = "unidades {0}, decenas {1} -> {2}")
    @CsvSource({
            "0,  0, 100",
            "1,  0,   1",
            "5,  0,   5",
            "9,  0,   9",
            "0, 10,  10",
            "4, 50,  54",
            "0, 90,  90",
            "9, 90,  99"
    })
    void plainRollAddsTensAndUnits(int units, int tens, int expected) {
        PercentileRoll roll = PercentileRoll.roll(dice(units, tens));

        assertThat(roll.result()).isEqualTo(expected);
        assertThat(roll.netDice()).isZero();
        assertThat(roll.tens()).containsExactly(tens);
    }

    @ParameterizedTest(name = "unidades {0}, decenas {1}/{2} -> {3}")
    @CsvSource ({
            "5, 70, 20, 25",
            "5, 20, 70, 25",
            "5, 40, 40, 45",
            "7,  0, 40,  7",
            "0,  0, 30, 30",
            "0,  0,  0, 100",
            "0, 50, 80, 50"
    })
    void bonusDieKeepsTheBestResult(int units, int tensA, int tensB, int expected) {
        PercentileRoll roll = PercentileRoll.roll(dice(units, tensA, tensB), 1, 0);

        assertThat(roll.result()).isEqualTo(expected);
        assertThat(roll.netDice()).isEqualTo(1);
        assertThat(roll.tens()).containsExactly(tensA, tensB);
    }

    @ParameterizedTest(name = "unidades {0}, decenas {1}/{2} -> {3}")
    @CsvSource ({
            "5, 70, 20, 75",
            "5, 20, 70, 75",
            "5, 40, 40, 45",
            "7,  0, 40,  47",
            "0,  0, 30, 100",
            "0, 50, 80, 80"
    })
    void penaltyDieKeepsTheWorstResult(int units, int tensA, int tensB, int expected) {
        PercentileRoll roll = PercentileRoll.roll(dice(units, tensA, tensB), 0, 1);

        assertThat(roll.result()).isEqualTo(expected);
        assertThat(roll.netDice()).isEqualTo(-1);
    }

    @Test
    void twoBonusDiceKeepTheBestOfThree() {
        PercentileRoll roll = PercentileRoll.roll(dice(3, 60, 20, 40), 2, 0);

        assertThat(roll.result()).isEqualTo(23);
        assertThat(roll.netDice()).isEqualTo(2);
    }

    @Test
    void twoPenaltyDiceKeepTheWorstOfThree() {
        PercentileRoll roll = PercentileRoll.roll(dice(3, 60, 20, 40), 0, 2);

        assertThat(roll.result()).isEqualTo(63);
        assertThat(roll.netDice()).isEqualTo(-2);
    }

    @Test
    void bonusAndPenaltyDiceCancelEachOther() {
        FixedRandomSource source = dice(5,70, 20);
        PercentileRoll roll = PercentileRoll.roll(source, 1, 1);

        assertThat(roll.result()).isEqualTo(75);
        assertThat(roll.netDice()).isEqualTo(0);
        assertThat(source.roll(10)).isEqualTo(3);
    }

    @Test
    void extraDiceAreCappedAtTwo() {
        FixedRandomSource source = dice(5, 70, 20, 10, 50);
        PercentileRoll roll = PercentileRoll.roll(source, 5, 0);

        assertThat(roll.result()).isEqualTo(15);
        assertThat(roll.tens()).hasSize(3);
        assertThat(roll.netDice()).isEqualTo(2);
        assertThat(source.roll(10)).isEqualTo(6);
    }

    @Test
    void netDiceIsComputedFromTheDifference() {
        FixedRandomSource source = dice(5, 70, 20, 10);
        PercentileRoll roll = PercentileRoll.roll(source, 3, 1);

        assertThat(roll.netDice()).isEqualTo(2);
    }

    @Test
    void negativeDiceCountsAreRejected() {
        assertThatThrownBy(() -> PercentileRoll.roll(dice(0,0), -1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PercentileRoll.roll(dice(0,0), 0, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bonusDiceLowerTheAverageAndPenaltyDiceRaiseIt() {
        JavaRandomSource random = new JavaRandomSource();
        int n= 200;
        long plain = 0;
        long bonus = 0;
        long penalty = 0;
        for (int i = 0; i < n; i++){
            plain += PercentileRoll.roll(random, 0, 0).result();
            bonus += PercentileRoll.roll(random, 1, 0).result();
            penalty += PercentileRoll.roll(random, 0, 1).result();
        }

        assertThat(bonus / (double) n).isLessThan(plain / (double) n);
        assertThat(penalty / (double) n).isGreaterThan(plain / (double) n);
    }

    @Test
    void realRollsAlwaysStayBetween1And100() {
        JavaRandomSource random = new JavaRandomSource();
        for (int i = 0; i < 10000; i++){
            assertThat(PercentileRoll.roll(random, i%4, (i/4)%4).result()).isBetween(1, 100);
        }
    }
}
