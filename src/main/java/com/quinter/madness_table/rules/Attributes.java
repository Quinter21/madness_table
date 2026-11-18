package com.quinter.madness_table.rules;

public record Attributes(int strength, int constitution, int size, int dexterity,
                         int appearance, int intelligence, int power, int education) {

    public static final int MIN = 1;
    public static final int MAX = 99;

    public Attributes {
        check("strength", strength);
        check("constitution",constitution);
        check("size", size);
        check("dexterity", dexterity);
        check("appearance", appearance);
        check("intelligence", intelligence);
        check("power", power);
        check("education", education);
    }

    private static void check(String name, int value) {
        if (value < MIN || value > MAX) {
            throw new IllegalArgumentException(
                    name + "debe estar entre " + MIN + " y " + MAX + ": " + value
            );
        }
    }
}
