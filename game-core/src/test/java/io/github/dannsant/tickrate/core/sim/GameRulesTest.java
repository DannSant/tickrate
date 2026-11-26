package io.github.dannsant.tickrate.core.sim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class GameRulesTest {

    private static final Duration MINUTE = Duration.ofSeconds(60);
    private static final Duration SECOND = Duration.ofSeconds(1);

    @Test
    void standardRulesConvertToExactPerTickValues() {
        GameRules rules = GameRules.standard();

        assertThat(rules.playerDistancePerTick()).isEqualTo(15.0);
        assertThat(rules.itPlayerDistancePerTick()).isEqualTo(15.75);
        assertThat(rules.matchDurationTicks()).isEqualTo(1200);
        assertThat(rules.tagCooldownTicks()).isEqualTo(20);
    }

    @Test
    void rejectsInvalidNumbers() {
        assertThatIllegalArgumentException().isThrownBy(() -> new GameRules(0, 900, 24, 300, 315, 20, MINUTE, SECOND));
        assertThatIllegalArgumentException().isThrownBy(() -> new GameRules(1600, 900, 24, Double.NaN, 315, 20, MINUTE, SECOND));
        assertThatIllegalArgumentException().isThrownBy(() -> new GameRules(1600, 900, 24, 300, 315, 0, MINUTE, SECOND));
        assertThatIllegalArgumentException().isThrownBy(() -> new GameRules(1600, 900, 24, 300, 315, 20, MINUTE, Duration.ofSeconds(-1)));
    }

    @Test
    void rejectsMatchShorterThanOneTick() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new GameRules(1600, 900, 24, 300, 315, 20, Duration.ofMillis(10), SECOND));
    }

    @Test
    void rejectsArenaTooSmallForAPlayer() {
        assertThatIllegalArgumentException().isThrownBy(() -> new GameRules(40, 900, 24, 300, 315, 20, MINUTE, SECOND));
    }
}
