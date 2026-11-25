package io.github.dannsant.tickrate.core.sim;

import java.util.Objects;

/**
 * The game's rules as a pure function: {@code step(state, inputs)} returns the next state.
 *
 * <p>There is no clock, no thread and no I/O in here. The server's game room calls {@link #step} a
 * fixed number of times per second; tests call it in a plain loop. Given the same state and inputs it
 * always returns the same result, which is what makes the game testable and replayable.
 */
public final class Simulation {

    private final GameRules rules;

    public Simulation(GameRules rules) {
        this.rules = Objects.requireNonNull(rules, "rules");
    }

    public GameRules rules() {
        return rules;
    }

    /** The state at tick 0: both players at mid-height, facing each other from opposite sides. */
    public GameState initialState() {
        double y = rules.arenaHeight() / 2;
        return new GameState(
                0,
                new PlayerState(new Vector2(rules.arenaWidth() / 8, y)),
                new PlayerState(new Vector2(rules.arenaWidth() * 7 / 8, y)));
    }

    /** Advances the match by one tick. */
    public GameState step(GameState state, TickInputs inputs) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(inputs, "inputs");

        return new GameState(
                state.tick() + 1,
                move(state.first(), inputs.first()),
                move(state.second(), inputs.second()));
    }

    private PlayerState move(PlayerState player, PlayerInput input) {
        Vector2 displacement = input.direction().times(rules.maxDistancePerTick());
        return new PlayerState(keepInsideArena(player.position().plus(displacement)));
    }

    /** Clamps a player's center so the whole circle stays inside the arena walls. */
    private Vector2 keepInsideArena(Vector2 position) {
        double radius = rules.playerRadius();
        return new Vector2(
                Math.clamp(position.x(), radius, rules.arenaWidth() - radius),
                Math.clamp(position.y(), radius, rules.arenaHeight() - radius));
    }
}
