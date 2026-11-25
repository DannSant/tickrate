package io.github.dannsant.tickrate.core.rating;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MatchOutcomeTest {

    @Test
    void scoresFollowGlicko2Convention() {
        assertThat(MatchOutcome.WIN.score()).isEqualTo(1.0);
        assertThat(MatchOutcome.DRAW.score()).isEqualTo(0.5);
        assertThat(MatchOutcome.LOSS.score()).isEqualTo(0.0);
    }

    @Test
    void oppositeIsTheSameGameFromTheOtherSide() {
        assertThat(MatchOutcome.WIN.opposite()).isEqualTo(MatchOutcome.LOSS);
        assertThat(MatchOutcome.LOSS.opposite()).isEqualTo(MatchOutcome.WIN);
        assertThat(MatchOutcome.DRAW.opposite()).isEqualTo(MatchOutcome.DRAW);
    }
}
