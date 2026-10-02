package io.verokeep.profile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfileReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void defaultsMissingListsToEmpty() throws IOException {
        Path profilePath = tempDir.resolve("profile.yaml");
        Files.writeString(profilePath, """
                source:
                  distro: ubuntu
                  version: "24.04"
                  packageManager: apt
                packages:
                  - git
                """);

        Profile profile = new ProfileReader().read(profilePath);

        assertEquals(List.of("git"), profile.packages());
        assertEquals(List.of(), profile.flatpaks());
        assertEquals(List.of(), profile.dotfiles());
    }
}
