package org.hoggmania;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FakeCommandRunner implements CommandRunner {
    private final Map<String, CommandResult> responses = new HashMap<>();
    private final Map<String, IOException> failures = new HashMap<>();
    private final List<String> calls = new ArrayList<>();

    FakeCommandRunner when(String command, List<String> args, int exitCode, String stdout, String stderr) {
        responses.put(key(command, args), new CommandResult(exitCode, stdout, stderr));
        return this;
    }

    FakeCommandRunner missing(String command, List<String> args) {
        failures.put(key(command, args), new IOException("Cannot run program \"" + command + "\""));
        return this;
    }

    @Override
    public CommandResult run(String command, List<String> args) throws IOException, InterruptedException {
        calls.add(command);
        IOException failure = failures.get(key(command, args));
        if (failure != null) {
            throw failure;
        }
        CommandResult result = responses.get(key(command, args));
        if (result == null) {
            throw new AssertionError("Unexpected command: " + command + " " + args);
        }
        return result;
    }

    int callsTo(String command) {
        return (int) calls.stream().filter(command::equals).count();
    }

    private static String key(String command, List<String> args) {
        return command + "\0" + String.join("\0", args);
    }
}
