package io.github.dannsant.tickrate.core.sim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class PlayerInputTest {

    @Test
    void keepsDirectionsThatAreAlreadyValid() {
        PlayerInput input = new PlayerInput(0.5, -0.25);

        assertThat(input.moveX()).isEqualTo(0.5);
        assertThat(input.moveY()).isEqualTo(-0.25);
    }

    @Test
    void shortensDiagonalToLengthOne() {
        PlayerInput input = new PlayerInput(1, 1);

        assertThat(input.direction().length()).isCloseTo(1.0, within(1e-12));
        assertThat(input.moveX()).isCloseTo(input.moveY(), within(1e-12));
    }

    @Test
    void clampsHugeValuesInsteadOfOverflowing() {
        PlayerInput input = new PlayerInput(Double.MAX_VALUE, Double.NEGATIVE_INFINITY);

        assertThat(input.direction().length()).isCloseTo(1.0, within(1e-12));
        assertThat(input.moveX()).isPositive();
        assertThat(input.moveY()).isNegative();
    }

    @Test
    void keepsTheDashRequestAndDefaultsToNoDash() {
        assertThat(new PlayerInput(1, 0, true).dash()).isTrue();
        assertThat(new PlayerInput(1, 0).dash()).isFalse();
    }

    @Test
    void treatsNaNAsNoMovement() {
        assertThat(new PlayerInput(Double.NaN, 1)).isEqualTo(PlayerInput.NONE);
        assertThat(new PlayerInput(1, Double.NaN)).isEqualTo(PlayerInput.NONE);
    }
}
