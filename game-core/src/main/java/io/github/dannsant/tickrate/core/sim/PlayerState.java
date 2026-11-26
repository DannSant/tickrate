package io.github.dannsant.tickrate.core.sim;

import java.util.Objects;

/**
 * Everything the simulation knows about one player at a given tick.
 *
 * @param position           the center of the player's circle, in arena units
 * @param ticksAsIt          how many ticks this player has spent as "it" so far; the lower, the better
 * @param dashTicksRemaining how many more ticks the current dash lasts; 0 when not dashing
 * @param dashCooldownTicks  how many ticks until the player can dash again; 0 when a dash is ready
 */
public record PlayerState(Vector2 position, long ticksAsIt, int dashTicksRemaining, int dashCooldownTicks) {

    public PlayerState {
        Objects.requireNonNull(position, "position");
        if (ticksAsIt < 0) {
            throw new IllegalArgumentException("ticksAsIt must not be negative, was " + ticksAsIt);
        }
        if (dashTicksRemaining < 0) {
            throw new IllegalArgumentException("dashTicksRemaining must not be negative, was " + dashTicksRemaining);
        }
        if (dashCooldownTicks < 0) {
            throw new IllegalArgumentException("dashCooldownTicks must not be negative, was " + dashCooldownTicks);
        }
    }

    /** A player at the given position who hasn't been "it" yet and has a dash ready. */
    public static PlayerState at(Vector2 position) {
        return new PlayerState(position, 0, 0, 0);
    }
}
