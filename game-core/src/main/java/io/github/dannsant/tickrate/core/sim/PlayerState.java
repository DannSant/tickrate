package io.github.dannsant.tickrate.core.sim;

import java.util.Objects;

/**
 * Everything the simulation knows about one player at a given tick.
 *
 * @param position   the center of the player's circle, in arena units
 * @param ticksAsIt  how many ticks this player has spent as "it" so far; the lower, the better
 */
public record PlayerState(Vector2 position, long ticksAsIt) {

    public PlayerState {
        Objects.requireNonNull(position, "position");
        if (ticksAsIt < 0) {
            throw new IllegalArgumentException("ticksAsIt must not be negative, was " + ticksAsIt);
        }
    }

    /** A player at the given position who hasn't been "it" yet. */
    public static PlayerState at(Vector2 position) {
        return new PlayerState(position, 0);
    }
}
