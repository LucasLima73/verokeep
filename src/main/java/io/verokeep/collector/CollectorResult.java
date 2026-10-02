package io.verokeep.collector;

import java.util.List;

public sealed interface CollectorResult {

    record Packages(List<String> names) implements CollectorResult {
    }

    record Flatpaks(List<String> applicationIds) implements CollectorResult {
    }

    record Dotfiles(List<String> paths) implements CollectorResult {
    }
}
