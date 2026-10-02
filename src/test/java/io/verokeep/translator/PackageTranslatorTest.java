package io.verokeep.translator;

import io.verokeep.detector.PackageManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageTranslatorTest {

    private final Map<String, Map<String, String>> map = Map.of(
            "build-tools", Map.of(
                    "apt", "build-essential",
                    "pacman", "base-devel",
                    "dnf", "@development-tools"
            )
    );

    @Test
    void translatesMappedPackageBetweenManagers() {
        PackageTranslator translator = new PackageTranslator(map);

        PackageTranslator.TranslationResult result =
                translator.translate(List.of("build-essential"), PackageManager.APT, PackageManager.PACMAN);

        assertEquals(List.of("base-devel"), result.packages());
        assertTrue(result.unmapped().isEmpty());
    }

    @Test
    void passesThroughUnknownPackageUnchanged() {
        PackageTranslator translator = new PackageTranslator(map);

        PackageTranslator.TranslationResult result =
                translator.translate(List.of("git"), PackageManager.APT, PackageManager.PACMAN);

        assertEquals(List.of("git"), result.packages());
        assertEquals(List.of("git"), result.unmapped());
    }

    @Test
    void passesThroughWhenTargetHasNoEntryForMappedPackage() {
        PackageTranslator translator = new PackageTranslator(map);

        PackageTranslator.TranslationResult result =
                translator.translate(List.of("build-essential"), PackageManager.APT, PackageManager.ZYPPER);

        assertEquals(List.of("build-essential"), result.packages());
        assertEquals(List.of("build-essential"), result.unmapped());
    }
}
