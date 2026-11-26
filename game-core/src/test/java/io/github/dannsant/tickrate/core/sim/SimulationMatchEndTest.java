package io.github.dannsant.tickrate.core.sim;

import static io.github.dannsant.tickrate.core.sim.PlayerSlot.FIRST;
import static io.github.dannsant.tickrate.core.sim.PlayerSlot.SECOND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class SimulationMatchEndTest {

    /** Standard rules, but a one-second (20-tick) match so tests run it to the end quickly. */
    private static final GameRules ONE_SECOND_MATCH =
            new GameRules(1600, 900, 24, 300, 315, 20, Duration.ofSeconds(1), Duration.ofSeconds(1));

    private final Simulation simulation = new Simulation(ONE_SECOND_MATCH);

    @Test
    void matchIsOverAfterItsFullDuration() {
        GameState state = simulation.initialState(1);

        for (int tick = 0; tick < 19; tick++) {
            state = simulation.step(state, TickInputs.NONE);
        }
        assertThat(simulation.isOver(state)).isFalse();

        state = simulation.step(state, TickInputs.NONE);
        assertThat(state.tick()).isEqualTo(20);
        assertThat(simulation.isOver(state)).isTrue();
    }

    @Test
    void steppingAFinishedMatchFails() {
        GameState finished = finishedMatch(5, 15);

        assertThatIllegalStateException().isThrownBy(() -> simulation.step(finished, TickInputs.NONE));
    }

    @Test
    void askingForTheResultOfARunningMatchFails() {
        GameState running = simulation.initialState(1);

        assertThatIllegalStateException().isThrownBy(() -> simulation.result(running));
    }

    @Test
    void playerWhoSpentLessTimeAsItWins() {
        assertThat(simulation.result(finishedMatch(5, 15))).isEqualTo(new MatchResult.Win(FIRST));
        assertThat(simulation.result(finishedMatch(12, 8))).isEqualTo(new MatchResult.Win(SECOND));
    }

    @Test
    void equalTimeAsItIsADraw() {
        assertThat(simulation.result(finishedMatch(10, 10))).isEqualTo(new MatchResult.Draw());
    }

    private static GameState finishedMatch(long firstTicksAsIt, long secondTicksAsIt) {
        return new GameState(
                20,
                new PlayerState(new Vector2(200, 450), firstTicksAsIt),
                new PlayerState(new Vector2(1400, 450), secondTicksAsIt),
                FIRST,
                0);
    }
}
