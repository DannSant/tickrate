package io.github.dannsant.tickrate.core.rating;

/**
 * A player's Glicko-2 rating, expressed on the familiar public scale where new players start at 1500.
 *
 * @param rating     estimated skill; higher is better
 * @param deviation  rating deviation (RD): how uncertain the estimate is. About 350 for a brand-new
 *                   player, and it shrinks as the player plays more games
 * @param volatility how erratic the player's results are (sigma). It stays near 0.06 for players
 *                   whose results are consistent, and rises after a streak of surprises
 */
public record Rating(double rating, double deviation, double volatility) {

    public static final double DEFAULT_RATING = 1500.0;
    public static final double DEFAULT_DEVIATION = 350.0;
    public static final double DEFAULT_VOLATILITY = 0.06;

    public Rating {
        if (!Double.isFinite(rating)) {
            throw new IllegalArgumentException("rating must be a finite number, was " + rating);
        }
        requirePositiveFinite(deviation, "deviation");
        requirePositiveFinite(volatility, "volatility");
    }

    /** The rating every new player starts with: 1500, with maximum uncertainty. */
    public static Rating initial() {
        return new Rating(DEFAULT_RATING, DEFAULT_DEVIATION, DEFAULT_VOLATILITY);
    }

    /**
     * A cautious estimate of skill: {@code rating - 2 * deviation}. The player's true skill is very
     * likely (about 95%) above this value. Leaderboards rank by it, so a new player who wins one lucky
     * game (high rating, but also high deviation) doesn't jump to the top.
     */
    public double conservativeEstimate() {
        return rating - 2 * deviation;
    }

    private static void requirePositiveFinite(double value, String name) {
        // "!(value > 0)" is also true for NaN, which "value <= 0" would let through.
        if (!(value > 0) || !Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be a positive finite number, was " + value);
        }
    }
}
