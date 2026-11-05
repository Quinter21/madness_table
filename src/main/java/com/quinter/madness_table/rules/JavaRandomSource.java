package com.quinter.madness_table.rules;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

public class JavaRandomSource implements RandomSource{

    private final RandomGenerator generator;

    public JavaRandomSource() {
        this(new SecureRandom());
    }

    public JavaRandomSource(RandomGenerator generator) {
        this.generator = generator;
    }


    @Override
    public int roll(int sides) {
        if (sides < 1) {
            throw new IllegalArgumentException("Un dado necesita al menos 1 cara: " + sides);
        }
        return generator.nextInt(sides) +1;
    }
}
