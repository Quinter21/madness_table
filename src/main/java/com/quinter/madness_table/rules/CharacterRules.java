package com.quinter.madness_table.rules;

public final class CharacterRules {

    private static final int MIN_STR_PLUS_SIZ = Attributes.MIN * 2; //2
    private static final int MAX_STR_PLUS_SIZ = Attributes.MAX * 2; //198

    private static final int[] LIMITS = {64, 84, 124, 164, MAX_STR_PLUS_SIZ};
    private static final DiceExpression[] DAMAGE_BONUS = {
            new DiceExpression(0, 0, -2),
            new DiceExpression(0, 0, -1),
            new DiceExpression(0, 0, 0),
            new DiceExpression(1, 4, 0),
            new DiceExpression(1, 6, 0),
    };
    private static final int[] BUILD = {-2, -1, 0, 1, 2};

    private static final DiceExpression STARTING_LUCK = new DiceExpression(3, 6, 0);

    private CharacterRules() {}

    public static DerivedStats derive(Attributes attributes, int age) {
        if (age < 1) {
            throw new IllegalArgumentException("La edad debe ser positiva: " + age);
        }
        int strPlusSiz = attributes.strength() + attributes.size();
        return new DerivedStats(
                hitPoints(attributes),
                magicPoints(attributes),
                attributes.power(),
                damageBonus(strPlusSiz),
                build(strPlusSiz),
                move(attributes, age)
        );
    }

    public static int hitPoints(Attributes a) {
        return (a.size() + a.constitution()) / 10;
    }

    public static int magicPoints(Attributes a) {
        return a.power() / 5;
    }

    public static DiceExpression damageBonus(int strPlusSiz) {
        return DAMAGE_BONUS[selectLimit(strPlusSiz)];
    }

    public static int build(int strPlusSiz) {
        return BUILD[selectLimit(strPlusSiz)];
    }

    public static int move(Attributes a, int age) {
        int base;
        if (a.strength() < a.size() && a.dexterity() < a.size()){
            base = 7;
        } else if (a.strength() > a.size() && a.dexterity() > a.size() ) {
            base = 9;
        } else {
            base = 8;
        }

        int agePenalty = age >= 40 ? (age -30) / 10 : 0;
        return (Math.max(base - agePenalty, 0));
    }

    public static int rollStartingLuck(RandomSource random) {
        return STARTING_LUCK.roll(random).total() * 5;
    }


    private static int selectLimit(int strPlusSiz) {
        if (strPlusSiz < MIN_STR_PLUS_SIZ || strPlusSiz > MAX_STR_PLUS_SIZ) {
            throw new IllegalArgumentException("FUE + TAM debe estar entre " + MIN_STR_PLUS_SIZ + " y " + MAX_STR_PLUS_SIZ);
        }

        int i = 0;
        while (strPlusSiz > LIMITS[i]) {
            i++;
        }
        return i;
    }

}
