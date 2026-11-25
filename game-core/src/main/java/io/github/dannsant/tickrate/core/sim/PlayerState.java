package io.github.dannsant.tickrate.core.sim;

import java.util.Objects;

/**
 * Everything the simulation knows about one player at a given tick.
 *
 * @param position the center of the player's circle, in arena units
 */
public record PlayerState(Vector2 position) {

    public PlayerState {
        Objects.requireNonNull(position, "position");
    }
}
