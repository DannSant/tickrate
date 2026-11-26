package io.github.dannsant.tickrate.core.sim;

import java.util.Objects;

/**
 * A complete, immutable snapshot of a match at one tick. The simulation never changes a
 * {@code GameState}; each step returns a new one.
 *
 * @param tick             how many ticks have been simulated since the match started
 * @param first            the first player's state
 * @param second           the second player's state
 * @param it               which player is currently "it" (the chaser)
 * @param tagCooldownTicks ticks left before another tag is allowed; this stops an instant tag-back
 */
public record GameState(long tick, PlayerState first, PlayerState second, PlayerSlot it, int tagCooldownTicks) {

    public GameState {
        if (tick < 0) {
            throw new IllegalArgumentException("tick must not be negative, was " + tick);
        }
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        Objects.requireNonNull(it, "it");
        if (tagCooldownTicks < 0) {
            throw new IllegalArgumentException("tagCooldownTicks must not be negative, was " + tagCooldownTicks);
        }
    }

    public PlayerState player(PlayerSlot slot) {
        return switch (slot) {
            case FIRST -> first;
            case SECOND -> second;
        };
    }
}
