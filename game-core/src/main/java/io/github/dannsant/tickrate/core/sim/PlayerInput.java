package io.github.dannsant.tickrate.core.sim;

/**
 * What a player wants to do during one tick: the direction they want to move in.
 *
 * <p>This comes from the network, so it can't be trusted. The constructor turns any values into a safe
 * direction, so every {@code PlayerInput} that exists is valid:
 * <ul>
 *   <li>{@code NaN} becomes "don't move";</li>
 *   <li>each component is clamped to [-1, 1], so huge values can't overflow;</li>
 *   <li>a direction longer than 1 (for example a diagonal (1, 1)) is shortened to length 1.</li>
 * </ul>
 * The result: no input can make a player faster than the rules allow. That's the server being
 * authoritative. The client asks; the simulation decides.
 *
 * @param moveX horizontal direction, -1 (left) to 1 (right)
 * @param moveY vertical direction, -1 (up) to 1 (down), matching screen coordinates
 */
public record PlayerInput(double moveX, double moveY) {

    public static final PlayerInput NONE = new PlayerInput(0, 0);

    public PlayerInput {
        if (Double.isNaN(moveX) || Double.isNaN(moveY)) {
            moveX = 0;
            moveY = 0;
        }
        moveX = Math.clamp(moveX, -1.0, 1.0);
        moveY = Math.clamp(moveY, -1.0, 1.0);

        double length = Math.sqrt(moveX * moveX + moveY * moveY);
        if (length > 1) {
            moveX /= length;
            moveY /= length;
        }
    }

    /** The movement direction as a vector of length 0 to 1. */
    public Vector2 direction() {
        return new Vector2(moveX, moveY);
    }
}
