package com.quinter.madness_table.rules;

import java.util.List;

public record DiceRoll(List<Integer> dice, int modifier, int total) {

    public DiceRoll {
        dice = List.copyOf(dice);
    }
}
