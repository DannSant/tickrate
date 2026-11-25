package io.github.dannsant.tickrate.core.rating;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RatingTest {

    @Test
    void newPlayersStartAt1500WithMaximumUncertainty() {
        Rating initial = Rating.initial();

        assertThat(initial.rating()).isEqualTo(1500);
        assertThat(initial.deviation()).isEqualTo(350);
        assertThat(initial.volatility()).isEqualTo(0.06);
    }

    @Test
    void conservativeEstimateIsRatingMinusTwoDeviations() {
        assertThat(new Rating(1800, 60, 0.06).conservativeEstimate()).isEqualTo(1680);
    }

    @Test
    void luckyNewcomerRanksBelowAProvenPlayerWithALowerRating() {
        Rating luckyNewcomer = new Rating(1650, 300, 0.06);
        Rating provenPlayer = new Rating(1600, 50, 0.06);

        assertThat(provenPlayer.conservativeEstimate()).isGreaterThan(luckyNewcomer.conservativeEstimate());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY})
    void rejectsInvalidDeviation(double deviation) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Rating(1500, deviation, 0.06));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -0.01, Double.NaN})
    void rejectsInvalidVolatility(double volatility) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Rating(1500, 200, volatility));
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.NaN, Double.NEGATIVE_INFINITY})
    void rejectsNonFiniteRating(double rating) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Rating(rating, 200, 0.06));
    }
}
