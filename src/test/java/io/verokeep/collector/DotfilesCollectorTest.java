package io.verokeep.collector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DotfilesCollectorTest {

    @TempDir
    Path home;

    @Test
    void acceptsExistingRequestedDotfiles() throws IOException {
        Files.createFile(home.resolve(".bashrc"));
        Files.createDirectories(home.resolve(".config/nvim"));

        CollectorResult.Dotfiles result = (CollectorResult.Dotfiles) new DotfilesCollector(
                home, List.of("~/.bashrc", "~/.config/nvim"), DotfilesCollector.SECURITY_EXCLUDED
        ).collect();

        assertEquals(List.of("~/.bashrc", "~/.config/nvim"), result.paths());
    }

    @Test
    void skipsMissingPaths() {
        CollectorResult.Dotfiles result = (CollectorResult.Dotfiles) new DotfilesCollector(
                home, List.of("~/.doesnotexist"), DotfilesCollector.SECURITY_EXCLUDED
        ).collect();

        assertTrue(result.paths().isEmpty());
    }

    @Test
    void neverIncludesSshEvenWhenExplicitlyRequestedAndPresent() throws IOException {
        Files.createDirectories(home.resolve(".ssh"));
        Files.createFile(home.resolve(".ssh/id_ed25519"));

        CollectorResult.Dotfiles result = (CollectorResult.Dotfiles) new DotfilesCollector(
                home, List.of("~/.ssh", "~/.ssh/id_ed25519"), DotfilesCollector.SECURITY_EXCLUDED
        ).collect();

        assertFalse(result.paths().contains("~/.ssh"));
        assertFalse(result.paths().contains("~/.ssh/id_ed25519"));
    }

    @Test
    void neverIncludesGnupgEvenWhenExplicitlyRequestedAndPresent() throws IOException {
        Files.createDirectories(home.resolve(".gnupg"));

        CollectorResult.Dotfiles result = (CollectorResult.Dotfiles) new DotfilesCollector(
                home, List.of("~/.gnupg"), DotfilesCollector.SECURITY_EXCLUDED
        ).collect();

        assertTrue(result.paths().isEmpty());
    }

    @Test
    void neverIncludesAwsCredentialsEvenWhenExplicitlyRequestedAndPresent() throws IOException {
        Files.createDirectories(home.resolve(".aws"));
        Files.createFile(home.resolve(".aws/credentials"));

        CollectorResult.Dotfiles result = (CollectorResult.Dotfiles) new DotfilesCollector(
                home, List.of("~/.aws/credentials"), DotfilesCollector.SECURITY_EXCLUDED
        ).collect();

        assertTrue(result.paths().isEmpty());
    }
}
