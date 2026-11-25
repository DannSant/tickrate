package io.github.dannsant.tickrate.core.rating;

import java.util.Objects;

/** Both players' new ratings after a 1v1 match, in the same order the players were passed in. */
public record MatchRatings(Rating first, Rating second) {

    public MatchRatings {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
    }
}
