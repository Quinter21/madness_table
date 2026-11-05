package com.quinter.madness_table.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JavaRandomSourceTest {
    private final JavaRandomSource source = new JavaRandomSource();

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 6, 10, 20, 50, 100})
    void rollsStayWithinTheDiceRange(int sides) {
        for (int i = 0; i < 5000; i++) {
            assertThat(source.roll(sides)).isBetween(1, sides);
        }
    }

    @Test
    void everyFaceOfAD6Appears() {
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < 5000; i++) {
            seen.add(source.roll(6));
        }
        assertThat(seen).containsExactlyInAnyOrder(1,2,3,4,5,6);
    }

    @Test
    void aDieNeedsAtLeastOneSide() {
        assertThatThrownBy(() -> source.roll(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> source.roll(-4)).isInstanceOf(IllegalArgumentException.class);
    }
}
