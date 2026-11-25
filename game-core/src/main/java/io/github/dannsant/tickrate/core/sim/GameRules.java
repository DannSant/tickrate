package io.github.dannsant.tickrate.core.sim;

/**
 * The tunable numbers of the game. Keeping them in one record makes them easy to test with
 * (for example, a tiny arena) and easy to show in the README.
 *
 * @param arenaWidth     arena width in arena units; the arena spans x = 0 to arenaWidth
 * @param arenaHeight    arena height in arena units; the arena spans y = 0 to arenaHeight
 * @param playerRadius   radius of a player's circle
 * @param playerSpeed    top speed, in arena units per second
 * @param ticksPerSecond how many simulation steps run per second
 */
public record GameRules(
        double arenaWidth,
        double arenaHeight,
        double playerRadius,
        double playerSpeed,
        int ticksPerSecond) {

    public GameRules {
        requirePositive(arenaWidth, "arenaWidth");
        requirePositive(arenaHeight, "arenaHeight");
        requirePositive(playerRadius, "playerRadius");
        requirePositive(playerSpeed, "playerSpeed");
        if (ticksPerSecond <= 0) {
            throw new IllegalArgumentException("ticksPerSecond must be positive, was " + ticksPerSecond);
        }
        if (2 * playerRadius >= Math.min(arenaWidth, arenaHeight)) {
            throw new IllegalArgumentException("the arena is too small for a player of radius " + playerRadius);
        }
    }

    /** The rules Tickrate is played with: a 16:9 arena and 20 ticks per second. */
    public static GameRules standard() {
        return new GameRules(1600, 900, 24, 300, 20);
    }

    /**
     * How far a player moves in one tick at full speed. Computed as speed / ticks rather than
     * speed * (1 / ticks): 1/20 has no exact binary representation, while 300 / 20 is exactly 15.
     */
    public double maxDistancePerTick() {
        return playerSpeed / ticksPerSecond;
    }

    private static void requirePositive(double value, String name) {
        if (!(value > 0) || !Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be a positive finite number, was " + value);
        }
    }
}
