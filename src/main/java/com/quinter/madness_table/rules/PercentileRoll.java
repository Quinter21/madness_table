package com.quinter.madness_table.rules;

import java.util.ArrayList;
import java.util.List;

public record PercentileRoll(int units, List<Integer> tens, int netDice, int result) {

    public static final int MAX_EXTRA_DICE = 2;

    public PercentileRoll {
        tens = List.copyOf(tens);
    }

    public static PercentileRoll roll (RandomSource random) {
        return roll(random, 0, 0);
    }

    public static PercentileRoll roll (RandomSource random, int bonusDice, int penaltyDice) {
        if (bonusDice < 0 || penaltyDice < 0) {
            throw new IllegalArgumentException("Los dados de bonificación/penalización no pueden ser negativos");
        }

        int net = bonusDice - penaltyDice;
        int extra = Math.min(Math.abs(net), MAX_EXTRA_DICE);
        int netDice = Integer.signum(net) * extra;

        int units = random.roll(10) - 1;
        List<Integer> tens = new ArrayList<>();
        for (int i = 0; i <= extra; i++) {
            tens.add((random.roll(10) - 1) * 10);
        }

        int result = toResult(tens.get(0), units);
        for (int t : tens) {
            int candidate = toResult(t, units);
            if (netDice > 0) {
                result = Math.min(result, candidate);
            }
            if (netDice < 0) {
                result = Math.max(result, candidate);
            }
        }

        return new PercentileRoll(units, tens, netDice, result);
    }

    private static int toResult(int tens, int units) {
        int value = tens + units;
        return value == 0 ? 100 : value;
    }
}
