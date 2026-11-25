package io.github.dannsant.tickrate.core.rating;

import static io.github.dannsant.tickrate.core.rating.MatchOutcome.DRAW;
import static io.github.dannsant.tickrate.core.rating.MatchOutcome.LOSS;
import static io.github.dannsant.tickrate.core.rating.MatchOutcome.WIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import org.junit.jupiter.api.Test;

class Glicko2Test {

    private final Glicko2 glicko2 = new Glicko2(0.5);

    @Test
    void reproducesTheWorkedExampleFromGlickmansPaper() {
        // The example in "Example of the Glicko-2 system": a 1500 player (RD 200) beats a 1400 player,
        // then loses to a 1550 and a 1700 player, with tau = 0.5.
        Rating player = new Rating(1500, 200, 0.06);
        List<GameResult> games = List.of(
                new GameResult(new Rating(1400, 30, 0.06), WIN),
                new GameResult(new Rating(1550, 100, 0.06), LOSS),
                new GameResult(new Rating(1700, 300, 0.06), LOSS));

        Rating updated = glicko2.update(player, games);

        // The paper publishes r' = 1464.06, RD' = 151.52 and sigma' = 0.05999. It rounds every
        // intermediate value to 4 decimals, so full double precision lands within 0.01 of those figures.
        assertThat(updated.rating()).isCloseTo(1464.06, within(0.01));
        assertThat(updated.deviation()).isCloseTo(151.52, within(0.01));
        assertThat(updated.volatility()).isCloseTo(0.05999, within(0.00001));
    }

    @Test
    void playerWhoDidNotPlayKeepsTheirRatingButBecomesMoreUncertain() {
        Rating player = new Rating(1500, 200, 0.06);

        Rating updated = glicko2.update(player, List.of());

        assertThat(updated.rating()).isEqualTo(1500);
        assertThat(updated.deviation()).isCloseTo(200.27, within(0.01));
        assertThat(updated.volatility()).isEqualTo(0.06);
    }

    @Test
    void winnerGainsRatingAndLoserLosesIt() {
        Rating first = Rating.initial();
        Rating second = Rating.initial();

        MatchRatings result = glicko2.rateMatch(first, second, WIN);

        assertThat(result.first().rating()).isGreaterThan(first.rating());
        assertThat(result.second().rating()).isLessThan(second.rating());
    }

    @Test
    void drawBetweenEqualPlayersKeepsRatingsButReducesUncertainty() {
        MatchRatings result = glicko2.rateMatch(Rating.initial(), Rating.initial(), DRAW);

        assertThat(result.first().rating()).isCloseTo(1500, within(1e-9));
        assertThat(result.second().rating()).isCloseTo(1500, within(1e-9));
        assertThat(result.first().deviation()).isLessThan(Rating.DEFAULT_DEVIATION);
        assertThat(result.second().deviation()).isLessThan(Rating.DEFAULT_DEVIATION);
    }

    @Test
    void upsetMovesRatingsMoreThanAnExpectedResult() {
        Rating favorite = new Rating(1700, 80, 0.06);
        Rating underdog = new Rating(1300, 80, 0.06);

        double expectedWinGain = glicko2.rateMatch(favorite, underdog, WIN).first().rating() - favorite.rating();
        double upsetGain = glicko2.rateMatch(underdog, favorite, WIN).first().rating() - underdog.rating();

        assertThat(upsetGain).isGreaterThan(expectedWinGain * 5);
    }

    @Test
    void uncertainPlayerMovesMoreThanAnEstablishedOne() {
        Rating opponent = new Rating(1500, 80, 0.06);
        Rating newcomer = new Rating(1500, 350, 0.06);
        Rating veteran = new Rating(1500, 50, 0.06);

        double newcomerGain = glicko2.rateMatch(newcomer, opponent, WIN).first().rating() - 1500;
        double veteranGain = glicko2.rateMatch(veteran, opponent, WIN).first().rating() - 1500;

        assertThat(newcomerGain).isGreaterThan(veteranGain * 3);
    }

    @Test
    void bothPlayersAreRatedAgainstTheirOpponentsPreMatchRating() {
        Rating first = new Rating(1620, 120, 0.06);
        Rating second = new Rating(1480, 90, 0.06);

        MatchRatings result = glicko2.rateMatch(first, second, LOSS);

        assertThat(result.first()).isEqualTo(glicko2.update(first, List.of(new GameResult(second, LOSS))));
        assertThat(result.second()).isEqualTo(glicko2.update(second, List.of(new GameResult(first, WIN))));
    }

    @Test
    void rejectsNonPositiveTau() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Glicko2(0));
        assertThatIllegalArgumentException().isThrownBy(() -> new Glicko2(Double.NaN));
    }
}
