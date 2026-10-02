package io.verokeep.profile;

import java.util.List;

public record Profile(
        SourceInfo source,
        List<String> packages,
        List<String> flatpaks,
        List<String> dotfiles
) {
}
