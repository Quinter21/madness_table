package com.quinter.madness_table.investigator;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Characteristics {

    @Column(name = "strength", nullable = false)
    private int strength;
    @Column(name = "constitution", nullable = false)
    private int constitution;
    @Column(name = "size", nullable = false)
    private int size;
    @Column(name = "dexterity", nullable = false)
    private int dexterity;
    @Column(name = "appearance", nullable = false)
    private int appearance;
    @Column(name = "intelligence", nullable = false)
    private int intelligence;
    @Column(name = "power", nullable = false)
    private int power;
    @Column(name = "education", nullable = false)
    private int education;

    protected Characteristics() {
    }

    public Characteristics(int strength, int constitution, int size, int dexterity,
                           int appearance, int intelligence, int power, int education) {
        this.strength = strength;
        this.constitution = constitution;
        this.size = size;
        this.dexterity = dexterity;
        this.appearance = appearance;
        this.intelligence = intelligence;
        this.power = power;
        this.education = education;
    }

    public int getStrength() {
        return strength;
    }

    public int getConstitution() {
        return constitution;
    }

    public int getSize() {
        return size;
    }

    public int getDexterity() {
        return dexterity;
    }

    public int getAppearance() {
        return appearance;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public int getPower() {
        return power;
    }

    public int getEducation() {
        return education;
    }
}
