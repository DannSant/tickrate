/**
 * Framework-free game logic for Tickrate.
 *
 * <p>Everything in this module is plain Java: no Spring, no I/O, no system clock. Callers pass in
 * the current time and random seeds, so given the same inputs the code always produces the same
 * outputs. That makes the simulation, rating and matchmaking rules testable with fast unit tests,
 * and lets the server replay a match exactly.
 */
package io.github.dannsant.tickrate.core;
