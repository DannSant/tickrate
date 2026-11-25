package io.github.dannsant.tickrate.core.rating;

import java.util.List;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/**
 * The Glicko-2 rating system, following Mark Glickman's paper "Example of the Glicko-2 system"
 * (http://www.glicko.net/glicko/glicko2.pdf). The step numbers in the comments match the paper.
 *
 * <p>Glicko-2 is designed to update ratings once per "rating period" (for example, a day of games).
 * Like many online games, Tickrate treats every match as its own rating period, so ratings change
 * right after each match. That simplification is deliberate; see {@link #rateMatch}.
 *
 * <p>This class is immutable and stateless apart from its configuration, so it is thread-safe.
 */
public final class Glicko2 {

    /** Converts between the public scale (1500 &plusmn; 350) and Glicko-2's internal scale. From the paper. */
    private static final double SCALE = 173.7178;

    /** How precisely Step 5 solves for the new volatility. The paper suggests 0.000001. */
    private static final double CONVERGENCE_TOLERANCE = 0.000001;

    private final double tau;

    /**
     * @param tau the system constant that limits how fast volatility can change. The paper suggests a
     *            value between 0.3 and 1.2; smaller values suit games where upsets are rare.
     */
    public Glicko2(double tau) {
        if (!(tau > 0) || !Double.isFinite(tau)) {
            throw new IllegalArgumentException("tau must be a positive finite number, was " + tau);
        }
        this.tau = tau;
    }

    /**
     * Rates a 1v1 match and returns both players' new ratings.
     *
     * <p>Both players are rated against the other's rating from <em>before</em> the match. Updating the
     * first player and then rating the second against that new value would be a subtle bug: the
     * second player's change would depend on the order of the arguments.
     */
    public MatchRatings rateMatch(Rating first, Rating second, MatchOutcome firstOutcome) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        Objects.requireNonNull(firstOutcome, "firstOutcome");

        Rating newFirst = update(first, List.of(new GameResult(second, firstOutcome)));
        Rating newSecond = update(second, List.of(new GameResult(first, firstOutcome.opposite())));
        return new MatchRatings(newFirst, newSecond);
    }

    /**
     * Applies one rating period to a player: every game they played in it, rated together.
     *
     * @param player  the player's rating at the start of the period
     * @param results the games played in the period; empty if the player didn't play
     * @return the player's rating at the end of the period
     */
    public Rating update(Rating player, List<GameResult> results) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(results, "results");

        // Step 2: convert to the Glicko-2 scale.
        double mu = toGlicko2Scale(player.rating());
        double phi = player.deviation() / SCALE;
        double sigma = player.volatility();

        if (results.isEmpty()) {
            // A player who didn't play keeps their rating, but we become less sure of it (Step 6 only).
            double phiStar = Math.sqrt(phi * phi + sigma * sigma);
            return new Rating(player.rating(), phiStar * SCALE, sigma);
        }

        // Steps 3 and 4: the estimated variance v of the player's rating based only on these games,
        // and the sum that gives the estimated improvement delta.
        double varianceInverse = 0;
        double weightedSurprise = 0;
        for (GameResult result : results) {
            double opponentMu = toGlicko2Scale(result.opponent().rating());
            double opponentPhi = result.opponent().deviation() / SCALE;
            double g = g(opponentPhi);
            double expected = expectedScore(mu, opponentMu, g);

            varianceInverse += g * g * expected * (1 - expected);
            weightedSurprise += g * (result.outcome().score() - expected);
        }
        double v = 1 / varianceInverse;
        double delta = v * weightedSurprise;

        // Step 5: the new volatility.
        double newSigma = newVolatility(phi, sigma, v, delta);

        // Step 6: widen the deviation by the new volatility.
        double phiStar = Math.sqrt(phi * phi + newSigma * newSigma);

        // Step 7: the new deviation and rating.
        double newPhi = 1 / Math.sqrt(1 / (phiStar * phiStar) + 1 / v);
        double newMu = mu + newPhi * newPhi * weightedSurprise;

        // Step 8: convert back to the public scale.
        return new Rating(newMu * SCALE + Rating.DEFAULT_RATING, newPhi * SCALE, newSigma);
    }

    /**
     * Step 5: finds the new volatility by solving f(x) = 0, where x = ln(sigma'^2), with the
     * Illinois algorithm (a faster variant of the regula falsi root-finding method).
     */
    private double newVolatility(double phi, double sigma, double v, double delta) {
        double phiSquared = phi * phi;
        double deltaSquared = delta * delta;
        double a = Math.log(sigma * sigma);

        // Step 5.1: the function whose root we need.
        DoubleUnaryOperator f = x -> {
            double ex = Math.exp(x);
            double denominatorBase = phiSquared + v + ex;
            return ex * (deltaSquared - phiSquared - v - ex) / (2 * denominatorBase * denominatorBase)
                    - (x - a) / (tau * tau);
        };

        // Step 5.2: two starting points A and B on opposite sides of the root.
        double pointA = a;
        double pointB;
        if (deltaSquared > phiSquared + v) {
            pointB = Math.log(deltaSquared - phiSquared - v);
        } else {
            int k = 1;
            while (f.applyAsDouble(a - k * tau) < 0) {
                k++;
            }
            pointB = a - k * tau;
        }

        // Steps 5.3 and 5.4: narrow the interval until A and B are close enough.
        double fA = f.applyAsDouble(pointA);
        double fB = f.applyAsDouble(pointB);
        while (Math.abs(pointB - pointA) > CONVERGENCE_TOLERANCE) {
            double pointC = pointA + (pointA - pointB) * fA / (fB - fA);
            double fC = f.applyAsDouble(pointC);
            if (fC * fB <= 0) {
                pointA = pointB;
                fA = fB;
            } else {
                // The Illinois tweak: halving fA stops the method from stalling on one side.
                fA = fA / 2;
            }
            pointB = pointC;
            fB = fC;
        }

        // Step 5.5
        return Math.exp(pointA / 2);
    }

    /** Down-weights a game against an opponent whose rating is uncertain (high deviation). */
    private static double g(double phi) {
        return 1 / Math.sqrt(1 + 3 * phi * phi / (Math.PI * Math.PI));
    }

    /** The probability that a player with rating mu beats the opponent: 0.5 for equal ratings. */
    private static double expectedScore(double mu, double opponentMu, double opponentG) {
        return 1 / (1 + Math.exp(-opponentG * (mu - opponentMu)));
    }

    private static double toGlicko2Scale(double rating) {
        return (rating - Rating.DEFAULT_RATING) / SCALE;
    }
}
