package io.github.dannsant.tickrate.core.rating;

import java.util.Objects;

/**
 * One game a player took part in, as Glicko-2 needs it: who they played and how it ended for them.
 *
 * @param opponent the opponent's rating before the game
 * @param outcome  the result from the rated player's point of view
 */
public record GameResult(Rating opponent, MatchOutcome outcome) {

    public GameResult {
        Objects.requireNonNull(opponent, "opponent");
        Objects.requireNonNull(outcome, "outcome");
    }
}
