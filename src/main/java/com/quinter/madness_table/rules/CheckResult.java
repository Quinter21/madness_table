package com.quinter.madness_table.rules;

public record CheckResult(PercentileRoll roll, int skill, SuccessLevel level) {

    public int result() {
        return roll.result();
    }
}
