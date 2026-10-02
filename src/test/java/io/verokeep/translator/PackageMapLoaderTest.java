package io.verokeep.translator;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageMapLoaderTest {

    @Test
    void loadsBundledPackageMap() {
        Map<String, Map<String, String>> map = new PackageMapLoader().loadDefault();

        assertFalse(map.isEmpty());
        assertTrue(map.get("build-tools").containsKey("apt"));
        assertTrue(map.get("build-tools").containsKey("pacman"));
    }
}
