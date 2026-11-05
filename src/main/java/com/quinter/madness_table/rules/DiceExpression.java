package com.quinter.madness_table.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record DiceExpression(int count, int sides, int modifier) {

    public static final int MAX_DICE = 10;
    public static final int MAX_SIDES = 100;

    private static final Pattern DICE = Pattern.compile("^(\\d{0,3})d(\\d{1,4})([+-]\\d{1,6})?$");
    private static final Pattern CONSTANT = Pattern.compile("^[+-]?\\d{1,6}$");

    public DiceExpression {
        if (count == 0) {
            if (sides != 0) {
                throw new IllegalArgumentException("Una constante no puede tener caras");
            }
        } else {
            if (count < 0 || count > MAX_DICE) {
                throw new IllegalArgumentException("El número de daods debe estar entre 1 y " + MAX_DICE + ": " + count);
            }
            if (sides < 2 || sides > MAX_SIDES) {
                throw new IllegalArgumentException("Las caras deben estar entre 2 y " + MAX_SIDES + ": " + sides);
            }
        }
    }

    public static DiceExpression parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("La expresión de datos está vacía");
        }
        String s = text.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);

        Matcher dice = DICE.matcher(s);
        if (dice.matches()) {
            int count = dice.group(1).isEmpty() ? 1 : Integer.parseInt(dice.group(1));
            int sides = Integer.parseInt(dice.group(2));
            int modifier = dice.group(3) == null ? 0 : Integer.parseInt(dice.group(3));
            return new DiceExpression(count, sides, modifier);
        }
        if (CONSTANT.matcher(s).matches()) {
            return new DiceExpression(0, 0, Integer.parseInt(s));
        }
        throw new IllegalArgumentException("Expresión de dados no válida: " + text);
    }

    public DiceRoll roll (RandomSource random) {
        List<Integer> results = new ArrayList<>(count);
        int total = modifier;
        for (int i = 0; i < count; i++) {
            int value = random.roll(sides);
            results.add(value);
            total += value;
        }
        return new DiceRoll(results, modifier, total);
    }

    public int min() {
        return count + modifier;
    }

    public int max() {
        return count * sides + modifier;
    }

    @Override
    public String toString() {
        if (count == 0) {
            return String.valueOf(modifier);
        }
        String mod = modifier > 0 ? "+" + modifier : modifier < 0 ? String.valueOf(modifier) : "";
        return count + "d" + sides + mod;
    }
}
