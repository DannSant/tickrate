package io.github.dannsant.tickrate.core.sim;

import java.time.Duration;
import java.util.Objects;

/**
 * The settings for the dash: a short burst of extra speed with a cooldown.
 *
 * @param speedMultiplier how much faster a dashing player moves (2 means twice as fast)
 * @param duration        how long a dash lasts
 * @param cooldown        how long after a dash starts until the player can dash again
 */
public record DashRules(double speedMultiplier, Duration duration, Duration cooldown) {

    public DashRules {
        if (!(speedMultiplier >= 1) || !Double.isFinite(speedMultiplier)) {
            throw new IllegalArgumentException("speedMultiplier must be a finite number of at least 1, was " + speedMultiplier);
        }
        Objects.requireNonNull(duration, "duration");
        Objects.requireNonNull(cooldown, "cooldown");
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("duration must be positive, was " + duration);
        }
        if (cooldown.compareTo(duration) < 0) {
            throw new IllegalArgumentException("cooldown must not be shorter than the dash itself, was " + cooldown);
        }
    }

    /** A quarter-second dash at double speed, available every 3 seconds. */
    public static DashRules standard() {
        return new DashRules(2.0, Duration.ofMillis(250), Duration.ofSeconds(3));
    }
}
