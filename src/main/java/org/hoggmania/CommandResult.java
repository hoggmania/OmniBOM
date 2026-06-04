/*
 * MIT License
 *
 * Copyright (c) 2025 James Holland
 */
package org.hoggmania;

/** Result of invoking an external command. */
public record CommandResult(int exitCode, String stdout, String stderr) {
}
