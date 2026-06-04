/*
 * MIT License
 *
 * Copyright (c) 2025 James Holland
 */
package org.hoggmania;

import java.io.IOException;
import java.util.List;

/** Abstraction for external process execution so library detection can be tested safely. */
public interface CommandRunner {
    CommandResult run(String command, List<String> args) throws IOException, InterruptedException;
}
