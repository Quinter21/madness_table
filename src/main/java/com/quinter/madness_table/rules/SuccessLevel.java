package com.quinter.madness_table.rules;

public enum SuccessLevel {
    FUMBLE,
    FAILURE,
    REGULAR,
    HARD,
    EXTREME,
    CRITICAL;

    public boolean isSuccess() {
        return this.compareTo(REGULAR) >= 0;
    }

    public boolean isBetterThan(SuccessLevel other){
        return this.compareTo(other) > 0;
    }
}
