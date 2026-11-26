package io.github.dannsant.tickrate.core.sim;

import static io.github.dannsant.tickrate.core.sim.PlayerSlot.FIRST;
import static io.github.dannsant.tickrate.core.sim.PlayerSlot.SECOND;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SimulationDashTest {

    private static final GameRules RULES = GameRules.standard(); // dash: x2 speed for 5 ticks, every 60 ticks
    private static final PlayerInput RIGHT = new PlayerInput(1, 0);
    private static final PlayerInput DASH_RIGHT = new PlayerInput(1, 0, true);

    private final Simulation simulation = new Simulation(RULES);

    /** Players far apart (no tags), with the second player as "it". The first player is the one tested. */
    private final GameState start = TestStates.at(new Vector2(100, 450), new Vector2(1500, 100), SECOND);

    @Test
    void dashingPlayerMovesTwiceAsFar() {
        GameState next = simulation.step(start, new TickInputs(DASH_RIGHT, PlayerInput.NONE));

        assertThat(next.first().position()).isEqualTo(new Vector2(130, 450));
    }

    @Test
    void itPlayerDashesTwiceAsFarAsTheirOwnSpeed() {
        GameState next = simulation.step(start, new TickInputs(PlayerInput.NONE, new PlayerInput(0, 1, true)));

        assertThat(next.second().position()).isEqualTo(new Vector2(1500, 131.5));
    }

    @Test
    void dashLastsFiveTicksThenSpeedReturnsToNormal() {
        GameState state = simulation.step(start, new TickInputs(DASH_RIGHT, PlayerInput.NONE));
        for (int tick = 1; tick < 6; tick++) {
            state = simulation.step(state, new TickInputs(RIGHT, PlayerInput.NONE));
        }

        // 5 dashing ticks at 30 units, then 1 normal tick at 15.
        assertThat(state.first().position().x()).isEqualTo(100 + 5 * 30 + 15);
        assertThat(state.first().dashTicksRemaining()).isZero();
    }

    @Test
    void dashRequestsAreIgnoredUntilTheCooldownEnds() {
        GameState state = start;
        double[] distanceEachTick = new double[61];
        for (int tick = 0; tick < 61; tick++) {
            double before = state.first().position().x();
            // The player asks to dash on every single tick.
            state = simulation.step(state, new TickInputs(DASH_RIGHT, PlayerInput.NONE));
            distanceEachTick[tick] = state.first().position().x() - before;
        }

        for (int tick = 0; tick < 5; tick++) {
            assertThat(distanceEachTick[tick]).as("tick %d (first dash)", tick).isEqualTo(30);
        }
        for (int tick = 5; tick < 60; tick++) {
            assertThat(distanceEachTick[tick]).as("tick %d (cooling down)", tick).isEqualTo(15);
        }
        assertThat(distanceEachTick[60]).as("tick 60 (3 s later, dash ready again)").isEqualTo(30);
    }

    @Test
    void dashWithoutADirectionStillUsesTheCooldown() {
        GameState next = simulation.step(start, new TickInputs(new PlayerInput(0, 0, true), PlayerInput.NONE));

        assertThat(next.first().position()).isEqualTo(start.first().position());
        assertThat(next.first().dashCooldownTicks()).isEqualTo(RULES.dashCooldownTicks() - 1);
    }

    @Test
    void eachPlayerHasTheirOwnDashCooldown() {
        GameState next = simulation.step(start, new TickInputs(DASH_RIGHT, PlayerInput.NONE));

        assertThat(next.first().dashCooldownTicks()).isPositive();
        assertThat(next.second().dashCooldownTicks()).isZero();
        assertThat(next.player(FIRST)).isEqualTo(next.first());
    }
}
