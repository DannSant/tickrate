package io.github.dannsant.tickrate.core.sim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class GameRulesTest {

    @Test
    void standardRulesMoveExactly15UnitsPerTick() {
        assertThat(GameRules.standard().maxDistancePerTick()).isEqualTo(15.0);
    }

    @Test
    void rejectsInvalidNumbers() {
        assertThatIllegalArgumentException().isThrownBy(() -> new GameRules(0, 900, 24, 300, 20));
        assertThatIllegalArgumentException().isThrownBy(() -> new GameRules(1600, 900, 24, Double.NaN, 20));
        assertThatIllegalArgumentException().isThrownBy(() -> new GameRules(1600, 900, 24, 300, 0));
    }

    @Test
    void rejectsArenaTooSmallForAPlayer() {
        assertThatIllegalArgumentException().isThrownBy(() -> new GameRules(40, 900, 24, 300, 20));
    }
}
