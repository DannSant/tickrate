package io.github.dannsant.tickrate.core.sim;

import java.util.Objects;

/**
 * The game's rules as a pure function: {@code step(state, inputs)} returns the next state.
 *
 * <p>There is no clock, no thread and no I/O in here. The server's game room calls {@link #step} a
 * fixed number of times per second; tests call it in a plain loop. Given the same state and inputs it
 * always returns the same result, which is what makes the game testable and replayable.
 *
 * <p>The game is 1v1 tag. One player is "it" and chases the other. Touching the other player passes
 * "it" on. Whoever spends less time as "it" by the end of the match wins.
 */
public final class Simulation {

    private final GameRules rules;

    public Simulation(GameRules rules) {
        this.rules = Objects.requireNonNull(rules, "rules");
    }

    public GameRules rules() {
        return rules;
    }

    /**
     * The state at tick 0: both players at mid-height on opposite sides, and a random starting "it".
     *
     * @param seed decides who starts as "it". The server picks it and stores it with the match, so
     *             the match can be replayed.
     */
    public GameState initialState(long seed) {
        double y = rules.arenaHeight() / 2;
        PlayerSlot startingIt = (splitMix64(seed) & 1) == 0 ? PlayerSlot.FIRST : PlayerSlot.SECOND;
        return new GameState(
                0,
                PlayerState.at(new Vector2(rules.arenaWidth() / 8, y)),
                PlayerState.at(new Vector2(rules.arenaWidth() * 7 / 8, y)),
                startingIt,
                0);
    }

    /**
     * Advances the match by one tick. Within a tick, things happen in this order:
     * <ol>
     *   <li>both players move (the "it" player a little faster, and anyone dashing twice as fast);</li>
     *   <li>the tick is counted against whoever was "it" during it;</li>
     *   <li>if the players touch and the tag cooldown is over, "it" passes to the other player.</li>
     * </ol>
     *
     * @throws IllegalStateException if the match is already over
     */
    public GameState step(GameState state, TickInputs inputs) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(inputs, "inputs");
        if (isOver(state)) {
            throw new IllegalStateException("the match ended at tick " + state.tick());
        }

        PlayerSlot it = state.it();
        PlayerState first = advance(state.first(), inputs.first(), it == PlayerSlot.FIRST);
        PlayerState second = advance(state.second(), inputs.second(), it == PlayerSlot.SECOND);

        int cooldown = Math.max(0, state.tagCooldownTicks() - 1);
        if (cooldown == 0 && touching(first, second)) {
            it = it.other();
            cooldown = rules.tagCooldownTicks();
        }

        return new GameState(state.tick() + 1, first, second, it, cooldown);
    }

    /** True once the match has run for its full duration. */
    public boolean isOver(GameState state) {
        return state.tick() >= rules.matchDurationTicks();
    }

    /**
     * The result of a finished match: the player who spent less time as "it" wins.
     *
     * @throws IllegalStateException if the match isn't over yet
     */
    public MatchResult result(GameState state) {
        if (!isOver(state)) {
            throw new IllegalStateException("the match is still running at tick " + state.tick());
        }
        int comparison = Long.compare(state.first().ticksAsIt(), state.second().ticksAsIt());
        if (comparison == 0) {
            return new MatchResult.Draw();
        }
        return new MatchResult.Win(comparison < 0 ? PlayerSlot.FIRST : PlayerSlot.SECOND);
    }

    /**
     * Moves one player for one tick: starts a dash if they asked for one and it's ready, moves them
     * (faster while dashing), counts the tick against them if they are "it", and counts down the dash
     * timers.
     */
    private PlayerState advance(PlayerState player, PlayerInput input, boolean isIt) {
        int dashTicksRemaining = player.dashTicksRemaining();
        int dashCooldown = player.dashCooldownTicks();
        if (input.dash() && dashCooldown == 0) {
            // The cooldown starts with the dash, so dashes are at most one cooldown apart.
            dashTicksRemaining = rules.dashDurationTicks();
            dashCooldown = rules.dashCooldownTicks();
        }

        double distance = isIt ? rules.itPlayerDistancePerTick() : rules.playerDistancePerTick();
        if (dashTicksRemaining > 0) {
            distance *= rules.dash().speedMultiplier();
        }
        Vector2 position = keepInsideArena(player.position().plus(input.direction().times(distance)));
        long ticksAsIt = isIt ? player.ticksAsIt() + 1 : player.ticksAsIt();

        return new PlayerState(
                position,
                ticksAsIt,
                Math.max(0, dashTicksRemaining - 1),
                Math.max(0, dashCooldown - 1));
    }

    /** Clamps a player's center so the whole circle stays inside the arena walls. */
    private Vector2 keepInsideArena(Vector2 position) {
        double radius = rules.playerRadius();
        return new Vector2(
                Math.clamp(position.x(), radius, rules.arenaWidth() - radius),
                Math.clamp(position.y(), radius, rules.arenaHeight() - radius));
    }

    /**
     * Turns a seed into a well-mixed 64-bit number: the first output of the SplitMix64 generator.
     *
     * <p>Why not {@code new Random(seed).nextBoolean()}? With {@code java.util.Random}, seeds that are
     * close together (0, 1, 2...) produce almost the same first value: seeds 0 to 99 all gave the same
     * starting "it". SplitMix64 scrambles every bit of the seed, and it's plain integer arithmetic, so
     * it gives the same result on every platform.
     */
    private static long splitMix64(long seed) {
        long z = seed + 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /**
     * Two circles touch when the distance between their centers is at most two radii. Comparing
     * squared distances avoids the square root.
     */
    private boolean touching(PlayerState a, PlayerState b) {
        double dx = a.position().x() - b.position().x();
        double dy = a.position().y() - b.position().y();
        double reach = 2 * rules.playerRadius();
        return dx * dx + dy * dy <= reach * reach;
    }
}
