package io.verokeep.collector;

import io.verokeep.exec.CommandRunner;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AptPackageCollectorTest {

    @Test
    void parsesAndSortsManuallyInstalledPackages() {
        CommandRunner fakeRunner = new CommandRunner(false) {
            @Override
            public CommandResult run(List<String> command) {
                return new CommandResult(0, "vlc\ngit\n\nbuild-essential\n", "");
            }
        };

        CollectorResult.Packages result = (CollectorResult.Packages) new AptPackageCollector(fakeRunner).collect();

        assertEquals(List.of("build-essential", "git", "vlc"), result.names());
    }

    @Test
    void failsWhenCommandFails() {
        CommandRunner failingRunner = new CommandRunner(false) {
            @Override
            public CommandResult run(List<String> command) {
                return new CommandResult(1, "", "apt-mark: command not found");
            }
        };

        try {
            new AptPackageCollector(failingRunner).collect();
            throw new AssertionError("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // expected
        }
    }
}
