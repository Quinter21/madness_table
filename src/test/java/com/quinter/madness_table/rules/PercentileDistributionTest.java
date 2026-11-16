package com.quinter.madness_table.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntConsumer;

import static org.assertj.core.api.Assertions.assertThat;

public class PercentileDistributionTest {

    private static void forEveryDiceCombination(int bonus, int penalty, IntConsumer consumer) {
        int extra = Math.min(Math.abs(bonus - penalty), PercentileRoll.MAX_EXTRA_DICE);
        int[] faces = new int[2 + extra];
        Arrays.fill(faces, 1);

        do {
            consumer.accept(
                    PercentileRoll.roll(FixedRandomSource.of(faces), bonus, penalty).result()
            );
        } while (next(faces));
    }

    private static boolean next(int[] faces) {
        for (int i = faces.length - 1; i >= 0; i--) {
            if(faces[i] < 10) {
                faces[i]++;
                return true;
            }
            faces[i] = 1;
        }
        return false;
    }

    @Test
    void aPlainRollGivesEachResultFrom1To100ExactlyOnce() {
        Set<Integer> seen = new HashSet<>();
        int[] combinations = {0};

        forEveryDiceCombination(0, 0, result -> {
            seen.add(result);
            combinations[0]++;
        });

        assertThat(combinations[0]).isEqualTo(100);
        assertThat(seen).hasSize(100).allMatch(r -> r >= 1 && r <= 100);
    }

    @ParameterizedTest(name = "bonif. {0}, penal. {1}: {2} combinaciones, {3} con resultado <= 50")
    @CsvSource({
            "0, 0,   100,   50",
            "1, 0,  1000,  750",
            "0, 1,  1000,  250",
            "2, 0, 10000, 8750",
            "0, 2, 10000, 1250",
            "1, 1,   100,   50",   // se cancelan
            "3, 0, 10000, 8750",   // el máximo son 2 dados extra
            "3, 1, 10000, 8750"    // 3 - 1 = 2 netos
    })
    void distributionMatchesTheTheory(int bonus, int penalty, int combinations, int atMost50) {
        int[] total = {0};
        int[] low = {0};

        forEveryDiceCombination(bonus, penalty, result -> {
            total[0]++;
            if (result <= 50) {
                low[0]++;
            }
        });

        assertThat(total[0]).isEqualTo(combinations);
        assertThat(low[0]).isEqualTo(atMost50);
    }
}
