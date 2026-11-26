package io.github.dannsant.tickrate.core.sim;

import java.util.Objects;

/**
 * How a finished match ended. It's a sealed interface, so a {@code switch} over a {@code MatchResult}
 * must handle exactly these two cases, and the compiler checks that it does.
 */
public sealed interface MatchResult {

    /** One player spent less time as "it" and won. */
    record Win(PlayerSlot winner) implements MatchResult {
        public Win {
            Objects.requireNonNull(winner, "winner");
        }
    }

    /** Both players spent exactly the same time as "it". */
    record Draw() implements MatchResult {
    }
}
