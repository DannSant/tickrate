package io.github.dannsant.tickrate.core.rating;

/** The result of one game from one player's point of view. */
public enum MatchOutcome {
    WIN(1.0),
    DRAW(0.5),
    LOSS(0.0);

    private final double score;

    MatchOutcome(double score) {
        this.score = score;
    }

    /** The score Glicko-2 uses for this outcome: 1 for a win, 0.5 for a draw, 0 for a loss. */
    public double score() {
        return score;
    }

    /** The same game seen from the opponent's side. */
    public MatchOutcome opposite() {
        return switch (this) {
            case WIN -> LOSS;
            case LOSS -> WIN;
            case DRAW -> DRAW;
        };
    }
}
