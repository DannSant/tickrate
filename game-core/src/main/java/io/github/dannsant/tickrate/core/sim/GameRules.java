package io.github.dannsant.tickrate.core.sim;

import java.time.Duration;
import java.util.Objects;

/**
 * The tunable numbers of the game. Keeping them in one record makes them easy to test with
 * (for example, a short match) and easy to show in the README.
 *
 * @param arenaWidth     arena width in arena units; the arena spans x = 0 to arenaWidth
 * @param arenaHeight    arena height in arena units; the arena spans y = 0 to arenaHeight
 * @param playerRadius   radius of a player's circle
 * @param playerSpeed    top speed of the player being chased, in arena units per second
 * @param itPlayerSpeed  top speed of the player who is "it". A little faster, so chases can end
 * @param ticksPerSecond how many simulation steps run per second
 * @param matchDuration  how long a match lasts
 * @param tagCooldown    after a tag, how long until the next tag is allowed (no instant tag-backs)
 */
public record GameRules(
        double arenaWidth,
        double arenaHeight,
        double playerRadius,
        double playerSpeed,
        double itPlayerSpeed,
        int ticksPerSecond,
        Duration matchDuration,
        Duration tagCooldown) {

    public GameRules {
        requirePositive(arenaWidth, "arenaWidth");
        requirePositive(arenaHeight, "arenaHeight");
        requirePositive(playerRadius, "playerRadius");
        requirePositive(playerSpeed, "playerSpeed");
        requirePositive(itPlayerSpeed, "itPlayerSpeed");
        if (ticksPerSecond <= 0) {
            throw new IllegalArgumentException("ticksPerSecond must be positive, was " + ticksPerSecond);
        }
        if (2 * playerRadius >= Math.min(arenaWidth, arenaHeight)) {
            throw new IllegalArgumentException("the arena is too small for a player of radius " + playerRadius);
        }
        Objects.requireNonNull(matchDuration, "matchDuration");
        Objects.requireNonNull(tagCooldown, "tagCooldown");
        if (toTicks(matchDuration, ticksPerSecond) <= 0) {
            throw new IllegalArgumentException("matchDuration must last at least one tick, was " + matchDuration);
        }
        if (tagCooldown.isNegative()) {
            throw new IllegalArgumentException("tagCooldown must not be negative, was " + tagCooldown);
        }
    }

    /** The rules Tickrate is played with: a 16:9 arena, 20 ticks per second and 60-second matches. */
    public static GameRules standard() {
        return new GameRules(1600, 900, 24, 300, 315, 20, Duration.ofSeconds(60), Duration.ofSeconds(1));
    }

    /**
     * How far the chased player moves in one tick at full speed. Computed as speed / ticks rather
     * than speed * (1 / ticks): 1/20 has no exact binary representation, while 300 / 20 is exactly 15.
     */
    public double playerDistancePerTick() {
        return playerSpeed / ticksPerSecond;
    }

    /** How far the "it" player moves in one tick at full speed (315 / 20 = 15.75 with standard rules). */
    public double itPlayerDistancePerTick() {
        return itPlayerSpeed / ticksPerSecond;
    }

    /** The match length in ticks (1200 with standard rules). */
    public long matchDurationTicks() {
        return toTicks(matchDuration, ticksPerSecond);
    }

    /** The tag cooldown in ticks (20 with standard rules). */
    public int tagCooldownTicks() {
        return Math.toIntExact(toTicks(tagCooldown, ticksPerSecond));
    }

    /** Converts a duration to whole ticks, rounding down. Integer math, so no rounding surprises. */
    private static long toTicks(Duration duration, int ticksPerSecond) {
        return duration.toMillis() * ticksPerSecond / 1000;
    }

    private static void requirePositive(double value, String name) {
        if (!(value > 0) || !Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be a positive finite number, was " + value);
        }
    }
}
