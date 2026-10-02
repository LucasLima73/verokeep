package io.verokeep.collector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DotfilesCollector implements Collector {

    /**
     * Never exported, even if explicitly requested: SSH/GPG keys, cloud and
     * tool credentials. This is a hard block, not just a default.
     */
    public static final List<String> SECURITY_EXCLUDED = List.of(
            ".ssh",
            ".gnupg",
            ".gpg",
            ".netrc",
            ".pgpass",
            ".aws/credentials",
            ".aws/config",
            ".docker/config.json",
            ".kube/config",
            ".npmrc",
            ".git-credentials"
    );

    private final Path home;
    private final List<String> requestedPaths;
    private final List<String> excludedPatterns;

    public DotfilesCollector(List<String> requestedPaths) {
        this(Path.of(System.getProperty("user.home")), requestedPaths, SECURITY_EXCLUDED);
    }

    public DotfilesCollector(Path home, List<String> requestedPaths, List<String> excludedPatterns) {
        this.home = home;
        this.requestedPaths = requestedPaths;
        this.excludedPatterns = excludedPatterns;
    }

    @Override
    public CollectorResult collect() {
        List<String> accepted = new ArrayList<>();
        for (String requested : requestedPaths) {
            String normalized = normalize(requested);

            if (isExcluded(normalized)) {
                System.err.println("Skipping " + requested + ": excluded for security reasons (keys/credentials).");
                continue;
            }
            if (!Files.exists(home.resolve(normalized))) {
                System.err.println("Skipping " + requested + ": not found.");
                continue;
            }
            accepted.add("~/" + normalized);
        }
        return new CollectorResult.Dotfiles(accepted);
    }

    private String normalize(String requested) {
        String value = requested.trim();
        if (value.startsWith("~/")) {
            value = value.substring(2);
        } else if (value.equals("~")) {
            value = "";
        }
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        return value;
    }

    private boolean isExcluded(String normalizedPath) {
        for (String pattern : excludedPatterns) {
            if (normalizedPath.equals(pattern) || normalizedPath.startsWith(pattern + "/")) {
                return true;
            }
        }
        return false;
    }
}
