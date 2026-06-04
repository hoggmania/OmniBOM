/*
 * MIT License
 *
 * Copyright (c) 2025 James Holland
 */
package org.hoggmania;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** CommandRunner backed by java.lang.ProcessBuilder. */
public class ProcessBuilderCommandRunner implements CommandRunner {
    @Override
    public CommandResult run(String command, List<String> args) throws IOException, InterruptedException {
        List<String> fullCommand = new ArrayList<>();
        fullCommand.add(command);
        fullCommand.addAll(args);

        Process process = new ProcessBuilder(fullCommand).start();
        byte[] stdout = process.getInputStream().readAllBytes();
        byte[] stderr = process.getErrorStream().readAllBytes();
        int exitCode = process.waitFor();

        return new CommandResult(
            exitCode,
            new String(stdout, StandardCharsets.UTF_8),
            new String(stderr, StandardCharsets.UTF_8));
    }
}
