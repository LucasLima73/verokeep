package io.verokeep.detector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DistroDetector {

    private static final Set<String> ARCH_LIKE = Set.of("arch", "manjaro", "endeavouros");
    private static final Set<String> FEDORA_LIKE = Set.of("fedora", "rhel", "centos", "rocky", "almalinux");
    private static final Set<String> DEBIAN_LIKE = Set.of("ubuntu", "debian", "linuxmint", "pop");
    private static final Set<String> SUSE_LIKE = Set.of("opensuse", "opensuse-leap", "opensuse-tumbleweed", "sles");

    private final Path osReleasePath;

    public DistroDetector() {
        this(Path.of("/etc/os-release"));
    }

    public DistroDetector(Path osReleasePath) {
        this.osReleasePath = osReleasePath;
    }

    public Distro detect() {
        Map<String, String> fields = readOsRelease();
        String id = fields.getOrDefault("ID", "unknown").toLowerCase();
        String version = fields.getOrDefault("VERSION_ID", "unknown");
        Set<String> idLike = idLikeSet(fields, id);

        PackageManager packageManager = resolvePackageManager(id, idLike);

        return new Distro(id, version, packageManager);
    }

    private Set<String> idLikeSet(Map<String, String> fields, String id) {
        String idLikeRaw = fields.getOrDefault("ID_LIKE", "");
        Set<String> result = new java.util.HashSet<>(Set.of(idLikeRaw.split("\\s+")));
        result.add(id);
        result.removeIf(String::isBlank);
        return result;
    }

    private PackageManager resolvePackageManager(String id, Set<String> idLike) {
        if (containsAny(idLike, id, DEBIAN_LIKE)) {
            return PackageManager.APT;
        }
        if (containsAny(idLike, id, FEDORA_LIKE)) {
            return PackageManager.DNF;
        }
        if (containsAny(idLike, id, ARCH_LIKE)) {
            return PackageManager.PACMAN;
        }
        if (containsAny(idLike, id, SUSE_LIKE)) {
            return PackageManager.ZYPPER;
        }
        return PackageManager.UNKNOWN;
    }

    private boolean containsAny(Set<String> idLike, String id, Set<String> family) {
        return family.contains(id) || idLike.stream().anyMatch(family::contains);
    }

    private Map<String, String> readOsRelease() {
        Map<String, String> fields = new HashMap<>();
        if (!Files.isReadable(osReleasePath)) {
            return fields;
        }
        try {
            for (String line : Files.readAllLines(osReleasePath)) {
                int separator = line.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                    value = value.substring(1, value.length() - 1);
                }
                fields.put(key, value);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + osReleasePath, e);
        }
        return fields;
    }
}
