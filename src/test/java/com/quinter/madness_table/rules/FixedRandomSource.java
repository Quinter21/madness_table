package com.quinter.madness_table.rules;

import java.util.ArrayDeque;
import java.util.Deque;

public class FixedRandomSource implements RandomSource{

    private final Deque<Integer> values = new ArrayDeque<>();

    private FixedRandomSource(int... rolls) {
        for (int r : rolls) {
            values.add(r);
        }
    }

    public static FixedRandomSource of(int... rolls) {
        return new FixedRandomSource(rolls);
    }

    @Override
    public int roll(int sides) {
        Integer next = values.poll();
        if (next == null) {
            throw new IllegalStateException("El test pidió más tiradas de las preparadas");
        }
        if (next < 1 || next > sides) {
            throw new IllegalStateException("Valor trucado " + next + "imposible en un d" + sides);
        }
        return next;
    }
}

