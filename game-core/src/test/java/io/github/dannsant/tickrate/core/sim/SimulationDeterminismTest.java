package io.github.dannsant.tickrate.core.sim;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Determinism: the same seed and the same inputs must always produce the same match, tick for tick.
 * This is what makes replays and server-side verification of a match possible.
 */
class SimulationDeterminismTest {

    private static final long MATCH_SEED = 2026;
    private static final long INPUT_SEED = 7;

    private final Simulation simulation = new Simulation(GameRules.standard());

    @Test
    void replayingTheSameSeedAndInputsProducesTheSameMatch() {
        List<GameState> firstRun = playFullMatch();
        List<GameState> replay = playFullMatch();

        assertThat(replay).isEqualTo(firstRun);
    }

    @Test
    void everyTickIsCountedAgainstExactlyOnePlayer() {
        GameState end = playFullMatch().getLast();

        assertThat(end.first().ticksAsIt() + end.second().ticksAsIt())
                .isEqualTo(simulation.rules().matchDurationTicks());
    }

    /**
     * A golden-value test: the final state of this scripted match was recorded once and is checked on
     * every build. CI runs on Linux and development happens on Windows, so passing in both places
     * shows the simulation gives bit-identical results across platforms.
     *
     * <p>If a rule change is intended to change the outcome, re-record the expected state and say so in
     * the commit message.
     */
    @Test
    void scriptedMatchEndsInTheRecordedState() {
        GameState end = playFullMatch().getLast();

        // Exact doubles, not "close to": any difference in floating-point behavior would show up here.
        assertThat(end).isEqualTo(new GameState(
                1200,
                new PlayerState(new Vector2(1564.1020797896356, 335.8998188013207), 586, 0, 0),
                new PlayerState(new Vector2(1559.007935154069, 286.60231848952856), 614, 0, 0),
                PlayerSlot.FIRST,
                17));
    }

    /** Plays a whole match where both players follow a simple chase-and-flee script with random noise. */
    private List<GameState> playFullMatch() {
        Random inputNoise = new Random(INPUT_SEED);
        List<GameState> states = new ArrayList<>();
        GameState state = simulation.initialState(MATCH_SEED);
        states.add(state);
        while (!simulation.isOver(state)) {
            state = simulation.step(state, scriptedInputs(state, inputNoise));
            states.add(state);
        }
        return states;
    }

    /** The chaser ("it") heads for the runner and the runner flees, each with some random noise. */
    private static TickInputs scriptedInputs(GameState state, Random noise) {
        PlayerSlot chaser = state.it();
        Vector2 chaserPosition = state.player(chaser).position();
        Vector2 runnerPosition = state.player(chaser.other()).position();

        Vector2 offset = runnerPosition.plus(chaserPosition.times(-1));
        double length = offset.length();
        Vector2 towardRunner = length == 0 ? Vector2.ZERO : offset.times(1 / length);

        // Moving toward the runner (chaser) and moving away from the chaser (runner) is the same direction.
        PlayerInput chaserInput = withNoise(towardRunner, noise);
        PlayerInput runnerInput = withNoise(towardRunner, noise);
        return chaser == PlayerSlot.FIRST
                ? new TickInputs(chaserInput, runnerInput)
                : new TickInputs(runnerInput, chaserInput);
    }

    private static PlayerInput withNoise(Vector2 direction, Random noise) {
        return new PlayerInput(
                direction.x() + 0.8 * (2 * noise.nextDouble() - 1),
                direction.y() + 0.8 * (2 * noise.nextDouble() - 1));
    }
}
