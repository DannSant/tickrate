package io.github.dannsant.tickrate.core.sim;

import static io.github.dannsant.tickrate.core.sim.PlayerSlot.FIRST;
import static io.github.dannsant.tickrate.core.sim.PlayerSlot.SECOND;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SimulationTagTest {

    private static final GameRules RULES = GameRules.standard(); // radius 24, so players touch at distance 48
    private final Simulation simulation = new Simulation(RULES);

    @Test
    void startingItDependsOnlyOnTheSeed() {
        Set<PlayerSlot> startingIts = EnumSet.noneOf(PlayerSlot.class);
        for (long seed = 0; seed < 100; seed++) {
            PlayerSlot it = simulation.initialState(seed).it();
            assertThat(simulation.initialState(seed).it()).isEqualTo(it);
            startingIts.add(it);
        }

        assertThat(startingIts).containsExactlyInAnyOrder(FIRST, SECOND);
    }

    @Test
    void everyTickCountsAgainstThePlayerWhoIsIt() {
        GameState state = TestStates.at(new Vector2(200, 450), new Vector2(1400, 450), FIRST);

        for (int tick = 0; tick < 10; tick++) {
            state = simulation.step(state, TickInputs.NONE);
        }

        assertThat(state.first().ticksAsIt()).isEqualTo(10);
        assertThat(state.second().ticksAsIt()).isZero();
    }

    @Test
    void touchingPassesItToTheOtherPlayer() {
        GameState touching = TestStates.at(new Vector2(500, 450), new Vector2(540, 450), FIRST);

        GameState next = simulation.step(touching, TickInputs.NONE);

        assertThat(next.it()).isEqualTo(SECOND);
        assertThat(next.tagCooldownTicks()).isEqualTo(RULES.tagCooldownTicks());
        // The tick in which the tag happened was still spent as "it" by the first player.
        assertThat(next.first().ticksAsIt()).isEqualTo(1);
        assertThat(next.second().ticksAsIt()).isZero();
    }

    @Test
    void circlesExactlyTwoRadiiApartAreTouching() {
        GameState justTouching = TestStates.at(new Vector2(500, 450), new Vector2(548, 450), FIRST);

        assertThat(simulation.step(justTouching, TickInputs.NONE).it()).isEqualTo(SECOND);
    }

    @Test
    void playersJustOutOfReachDoNotTag() {
        GameState almostTouching = TestStates.at(new Vector2(500, 450), new Vector2(549, 450), FIRST);

        assertThat(simulation.step(almostTouching, TickInputs.NONE).it()).isEqualTo(FIRST);
    }

    @Test
    void newlyTaggedPlayerCannotTagBackForOneSecond() {
        GameState state = TestStates.at(new Vector2(500, 450), new Vector2(540, 450), FIRST);
        state = simulation.step(state, TickInputs.NONE);
        assertThat(state.it()).isEqualTo(SECOND);

        // Still touching, but the cooldown holds for the rest of the second (20 ticks in total).
        for (int tick = 1; tick < RULES.tagCooldownTicks(); tick++) {
            state = simulation.step(state, TickInputs.NONE);
            assertThat(state.it()).as("it after %d ticks", tick).isEqualTo(SECOND);
        }

        state = simulation.step(state, TickInputs.NONE);
        assertThat(state.it()).isEqualTo(FIRST);
    }
}
