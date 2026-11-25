package io.github.dannsant.tickrate.core.sim;

/**
 * An immutable 2D vector, used for positions and directions in the arena.
 *
 * <p>Lengths use {@code Math.sqrt(x * x + y * y)} rather than {@code Math.hypot}. The JVM must return
 * the exact same result for {@code sqrt} and for basic arithmetic on every platform, while functions
 * such as {@code hypot}, {@code sin} or {@code exp} may differ slightly between CPUs. Sticking to exact
 * operations keeps the simulation deterministic.
 */
public record Vector2(double x, double y) {

    public static final Vector2 ZERO = new Vector2(0, 0);

    public Vector2 plus(Vector2 other) {
        return new Vector2(x + other.x, y + other.y);
    }

    public Vector2 times(double factor) {
        return new Vector2(x * factor, y * factor);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }
}
