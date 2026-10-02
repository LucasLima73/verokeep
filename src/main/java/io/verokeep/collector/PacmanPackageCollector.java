package io.verokeep.collector;

import io.verokeep.exec.CommandRunner;

import java.util.Arrays;
import java.util.List;

public class PacmanPackageCollector implements Collector {

    private final CommandRunner commandRunner;

    public PacmanPackageCollector() {
        this(new CommandRunner(false));
    }

    public PacmanPackageCollector(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    @Override
    public CollectorResult collect() {
        CommandRunner.CommandResult result = commandRunner.run(List.of("pacman", "-Qqe"));
        if (!result.isSuccess()) {
            throw new IllegalStateException("pacman -Qqe failed: " + result.stderr());
        }
        List<String> packages = parse(result.stdout());
        return new CollectorResult.Packages(packages);
    }

    private List<String> parse(String stdout) {
        return Arrays.stream(stdout.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .sorted()
                .toList();
    }
}
