package io.github.dannsant.tickrate.core.sim;

/**
 * Identifies one of the two players in a match. The simulation only knows slots; the server maps
 * each slot to a real player account.
 */
public enum PlayerSlot {
    FIRST,
    SECOND;

    public PlayerSlot other() {
        return switch (this) {
            case FIRST -> SECOND;
            case SECOND -> FIRST;
        };
    }
}
