package io.verokeep.collector;

import io.verokeep.exec.CommandRunner;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PacmanPackageCollectorTest {

    @Test
    void parsesAndSortsExplicitlyInstalledPackages() {
        CommandRunner fakeRunner = new CommandRunner(false) {
            @Override
            public CommandResult run(List<String> command) {
                return new CommandResult(0, "vlc\ngit\n\nbase-devel\n", "");
            }
        };

        CollectorResult.Packages result = (CollectorResult.Packages) new PacmanPackageCollector(fakeRunner).collect();

        assertEquals(List.of("base-devel", "git", "vlc"), result.names());
    }

    @Test
    void failsWhenCommandFails() {
        CommandRunner failingRunner = new CommandRunner(false) {
            @Override
            public CommandResult run(List<String> command) {
                return new CommandResult(1, "", "pacman: command not found");
            }
        };

        try {
            new PacmanPackageCollector(failingRunner).collect();
            throw new AssertionError("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // expected
        }
    }
}
