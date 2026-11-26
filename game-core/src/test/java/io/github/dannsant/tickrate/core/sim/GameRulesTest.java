package io.github.dannsant.tickrate.core.sim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class GameRulesTest {

    private static final Duration MINUTE = Duration.ofSeconds(60);
    private static final Duration SECOND = Duration.ofSeconds(1);
    private static final DashRules DASH = DashRules.standard();

    @Test
    void standardRulesConvertToExactPerTickValues() {
        GameRules rules = GameRules.standard();

        assertThat(rules.playerDistancePerTick()).isEqualTo(15.0);
        assertThat(rules.itPlayerDistancePerTick()).isEqualTo(15.75);
        assertThat(rules.matchDurationTicks()).isEqualTo(1200);
        assertThat(rules.tagCooldownTicks()).isEqualTo(20);
        assertThat(rules.dashDurationTicks()).isEqualTo(5);
        assertThat(rules.dashCooldownTicks()).isEqualTo(60);
    }

    @Test
    void withMatchDurationChangesOnlyTheMatchLength() {
        GameRules shortMatch = GameRules.standard().withMatchDuration(SECOND);

        assertThat(shortMatch.matchDurationTicks()).isEqualTo(20);
        assertThat(shortMatch.withMatchDuration(MINUTE)).isEqualTo(GameRules.standard());
    }

    @Test
    void rejectsInvalidNumbers() {
        assertThatIllegalArgumentException().isThrownBy(() -> new GameRules(0, 900, 24, 300, 315, 20, MINUTE, SECOND, DASH));
        assertThatIllegalArgumentException().isThrownBy(() -> new GameRules(1600, 900, 24, Double.NaN, 315, 20, MINUTE, SECOND, DASH));
        assertThatIllegalArgumentException().isThrownBy(() -> new GameRules(1600, 900, 24, 300, 315, 0, MINUTE, SECOND, DASH));
        assertThatIllegalArgumentException().isThrownBy(() -> new GameRules(1600, 900, 24, 300, 315, 20, MINUTE, Duration.ofSeconds(-1), DASH));
    }

    @Test
    void rejectsMatchOrDashShorterThanOneTick() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new GameRules(1600, 900, 24, 300, 315, 20, Duration.ofMillis(10), SECOND, DASH));
        DashRules tooShortDash = new DashRules(2, Duration.ofMillis(10), SECOND);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new GameRules(1600, 900, 24, 300, 315, 20, MINUTE, SECOND, tooShortDash));
    }

    @Test
    void rejectsArenaTooSmallForAPlayer() {
        assertThatIllegalArgumentException().isThrownBy(() -> new GameRules(40, 900, 24, 300, 315, 20, MINUTE, SECOND, DASH));
    }

    @Test
    void rejectsInvalidDashSettings() {
        assertThatIllegalArgumentException().isThrownBy(() -> new DashRules(0.5, Duration.ofMillis(250), SECOND));
        assertThatIllegalArgumentException().isThrownBy(() -> new DashRules(2, Duration.ZERO, SECOND));
        assertThatIllegalArgumentException().isThrownBy(() -> new DashRules(2, SECOND, Duration.ofMillis(500)));
    }
}
