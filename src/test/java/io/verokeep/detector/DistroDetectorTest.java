package io.verokeep.detector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DistroDetectorTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsUbuntuAsApt() throws IOException {
        Path osRelease = writeOsRelease("""
                ID=ubuntu
                ID_LIKE=debian
                VERSION_ID="24.04"
                """);

        Distro distro = new DistroDetector(osRelease).detect();

        assertEquals("ubuntu", distro.id());
        assertEquals("24.04", distro.version());
        assertEquals(PackageManager.APT, distro.packageManager());
    }

    @Test
    void detectsArchAsPacman() throws IOException {
        Path osRelease = writeOsRelease("""
                ID=arch
                VERSION_ID=rolling
                """);

        Distro distro = new DistroDetector(osRelease).detect();

        assertEquals(PackageManager.PACMAN, distro.packageManager());
    }

    @Test
    void detectsFedoraAsDnf() throws IOException {
        Path osRelease = writeOsRelease("""
                ID=fedora
                VERSION_ID=40
                """);

        Distro distro = new DistroDetector(osRelease).detect();

        assertEquals(PackageManager.DNF, distro.packageManager());
    }

    @Test
    void unknownDistroWhenFileMissing() {
        Distro distro = new DistroDetector(tempDir.resolve("missing")).detect();

        assertEquals(PackageManager.UNKNOWN, distro.packageManager());
    }

    private Path writeOsRelease(String content) throws IOException {
        Path file = tempDir.resolve("os-release");
        Files.writeString(file, content);
        return file;
    }
}
