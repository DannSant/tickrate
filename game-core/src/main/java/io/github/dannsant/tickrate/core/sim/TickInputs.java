package io.github.dannsant.tickrate.core.sim;

import java.util.Objects;

/**
 * Both players' inputs for one tick.
 *
 * <p>The simulation doesn't remember inputs between ticks. Whoever runs it (the game room on the
 * server) decides what to pass in, for example the latest input each player sent, so a held key keeps
 * the player moving.
 */
public record TickInputs(PlayerInput first, PlayerInput second) {

    public static final TickInputs NONE = new TickInputs(PlayerInput.NONE, PlayerInput.NONE);

    public TickInputs {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
    }
}
