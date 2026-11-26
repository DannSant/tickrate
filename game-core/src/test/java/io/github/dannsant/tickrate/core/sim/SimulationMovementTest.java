package io.github.dannsant.tickrate.core.sim;

import static io.github.dannsant.tickrate.core.sim.PlayerSlot.SECOND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class SimulationMovementTest {

    private static final GameRules RULES = GameRules.standard(); // 1600x900, radius 24, 15 or 15.75 units per tick
    private static final PlayerInput RIGHT = new PlayerInput(1, 0);
    private static final PlayerInput LEFT = new PlayerInput(-1, 0);
    private static final PlayerInput UP = new PlayerInput(0, -1);

    private final Simulation simulation = new Simulation(RULES);

    /** Players far apart, with the second player as "it". */
    private final GameState start = TestStates.at(new Vector2(200, 450), new Vector2(1400, 450), SECOND);

    @Test
    void initialStatePlacesPlayersOnOppositeSidesAtTickZero() {
        GameState state = simulation.initialState(42);

        assertThat(state.tick()).isZero();
        assertThat(state.first()).isEqualTo(PlayerState.at(new Vector2(200, 450)));
        assertThat(state.second()).isEqualTo(PlayerState.at(new Vector2(1400, 450)));
        assertThat(state.tagCooldownTicks()).isZero();
    }

    @Test
    void eachStepAdvancesTheTickByOne() {
        GameState next = simulation.step(simulation.step(start, TickInputs.NONE), TickInputs.NONE);

        assertThat(next.tick()).isEqualTo(2);
    }

    @Test
    void chasedPlayerMoves15AndItPlayerMoves15Point75UnitsPerTick() {
        GameState next = simulation.step(start, new TickInputs(RIGHT, UP));

        assertThat(next.first().position()).isEqualTo(new Vector2(215, 450));
        assertThat(next.second().position()).isEqualTo(new Vector2(1400, 434.25));
    }

    @Test
    void playerWithoutInputStaysStill() {
        GameState next = simulation.step(start, TickInputs.NONE);

        assertThat(next.first().position()).isEqualTo(start.first().position());
        assertThat(next.second().position()).isEqualTo(start.second().position());
    }

    @Test
    void movingDiagonallyIsNotFasterThanMovingStraight() {
        GameState next = simulation.step(start, new TickInputs(new PlayerInput(1, 1), PlayerInput.NONE));

        double distance = distanceBetween(start.first().position(), next.first().position());
        assertThat(distance).isCloseTo(RULES.playerDistancePerTick(), within(1e-9));
    }

    @Test
    void exaggeratedInputCannotExceedTopSpeed() {
        GameState next = simulation.step(start, new TickInputs(new PlayerInput(1_000_000, 0), PlayerInput.NONE));

        assertThat(next.first().position()).isEqualTo(new Vector2(215, 450));
    }

    @Test
    void playerStopsAtTheArenaWallWithTheWholeCircleInside() {
        GameState state = start;
        TickInputs bothRunIntoWalls = new TickInputs(LEFT, RIGHT);

        for (int tick = 0; tick < 200; tick++) {
            state = simulation.step(state, bothRunIntoWalls);
        }

        assertThat(state.first().position().x()).isEqualTo(RULES.playerRadius());
        assertThat(state.second().position().x()).isEqualTo(RULES.arenaWidth() - RULES.playerRadius());
    }

    @Test
    void stepNeverChangesTheStateItWasGiven() {
        GameState copy = new GameState(start.tick(), start.first(), start.second(), start.it(), start.tagCooldownTicks());

        simulation.step(start, new TickInputs(RIGHT, LEFT));

        assertThat(start).isEqualTo(copy);
    }

    private static double distanceBetween(Vector2 from, Vector2 to) {
        return to.plus(from.times(-1)).length();
    }
}
