package io.github.dannsant.tickrate.core.sim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class SimulationTest {

    private static final GameRules RULES = GameRules.standard(); // 1600x900, radius 24, 15 units per tick
    private static final PlayerInput RIGHT = new PlayerInput(1, 0);
    private static final PlayerInput LEFT = new PlayerInput(-1, 0);
    private static final PlayerInput UP = new PlayerInput(0, -1);

    private final Simulation simulation = new Simulation(RULES);

    @Test
    void initialStatePlacesPlayersOnOppositeSidesAtTickZero() {
        GameState state = simulation.initialState();

        assertThat(state.tick()).isZero();
        assertThat(state.first().position()).isEqualTo(new Vector2(200, 450));
        assertThat(state.second().position()).isEqualTo(new Vector2(1400, 450));
    }

    @Test
    void eachStepAdvancesTheTickByOne() {
        GameState state = simulation.initialState();

        GameState next = simulation.step(simulation.step(state, TickInputs.NONE), TickInputs.NONE);

        assertThat(next.tick()).isEqualTo(2);
    }

    @Test
    void playerMovesMaxDistancePerTickInTheInputDirection() {
        GameState state = simulation.initialState();

        GameState next = simulation.step(state, new TickInputs(RIGHT, UP));

        assertThat(next.first().position()).isEqualTo(new Vector2(215, 450));
        assertThat(next.second().position()).isEqualTo(new Vector2(1400, 435));
    }

    @Test
    void playerWithoutInputStaysStill() {
        GameState state = simulation.initialState();

        GameState next = simulation.step(state, TickInputs.NONE);

        assertThat(next.first()).isEqualTo(state.first());
        assertThat(next.second()).isEqualTo(state.second());
    }

    @Test
    void movingDiagonallyIsNotFasterThanMovingStraight() {
        GameState state = simulation.initialState();

        GameState next = simulation.step(state, new TickInputs(new PlayerInput(1, 1), PlayerInput.NONE));

        double distance = distanceBetween(state.first().position(), next.first().position());
        assertThat(distance).isCloseTo(RULES.maxDistancePerTick(), within(1e-9));
    }

    @Test
    void exaggeratedInputCannotExceedTopSpeed() {
        GameState state = simulation.initialState();

        GameState next = simulation.step(state, new TickInputs(new PlayerInput(1_000_000, 0), PlayerInput.NONE));

        assertThat(next.first().position()).isEqualTo(new Vector2(215, 450));
    }

    @Test
    void playerStopsAtTheArenaWallWithTheWholeCircleInside() {
        GameState state = simulation.initialState();
        TickInputs bothRunIntoWalls = new TickInputs(LEFT, RIGHT);

        for (int tick = 0; tick < 200; tick++) {
            state = simulation.step(state, bothRunIntoWalls);
        }

        assertThat(state.first().position().x()).isEqualTo(RULES.playerRadius());
        assertThat(state.second().position().x()).isEqualTo(RULES.arenaWidth() - RULES.playerRadius());
    }

    @Test
    void stepNeverChangesTheStateItWasGiven() {
        GameState state = simulation.initialState();
        GameState copy = new GameState(state.tick(), state.first(), state.second());

        simulation.step(state, new TickInputs(RIGHT, LEFT));

        assertThat(state).isEqualTo(copy);
    }

    private static double distanceBetween(Vector2 from, Vector2 to) {
        return to.plus(from.times(-1)).length();
    }
}
