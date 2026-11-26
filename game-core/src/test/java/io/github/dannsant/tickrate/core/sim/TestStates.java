package io.github.dannsant.tickrate.core.sim;

/** Builds game states for tests, so each test can set up exactly the situation it checks. */
final class TestStates {

    private TestStates() {
    }

    /** A state at tick 0 with the players at the given positions and no tag cooldown. */
    static GameState at(Vector2 first, Vector2 second, PlayerSlot it) {
        return new GameState(0, PlayerState.at(first), PlayerState.at(second), it, 0);
    }
}
