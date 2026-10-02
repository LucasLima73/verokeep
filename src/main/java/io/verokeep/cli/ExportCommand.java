package io.verokeep.cli;

import io.verokeep.collector.AptPackageCollector;
import io.verokeep.collector.Collector;
import io.verokeep.collector.CollectorResult;
import io.verokeep.collector.DotfilesCollector;
import io.verokeep.collector.PacmanPackageCollector;
import io.verokeep.detector.Distro;
import io.verokeep.detector.DistroDetector;
import io.verokeep.detector.PackageManager;
import io.verokeep.profile.Profile;
import io.verokeep.profile.ProfileWriter;
import io.verokeep.profile.SourceInfo;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "export", description = "Export the current environment (packages, dotfiles, flatpaks) to a YAML profile")
public class ExportCommand implements Callable<Integer> {

    private static final List<String> DEFAULT_DOTFILES = List.of(
            "~/.bashrc",
            "~/.zshrc",
            "~/.profile",
            "~/.vimrc",
            "~/.gitconfig",
            "~/.tmux.conf",
            "~/.config/nvim",
            "~/.config/alacritty",
            "~/.config/kitty"
    );

    @Option(names = {"-o", "--output"}, description = "Output YAML file", required = true)
    private Path output;

    @Option(names = "--dotfiles", description = "Dotfiles/config paths to include (default: a curated common list). "
            + "~/.ssh, ~/.gnupg and other credential paths are never included.")
    private List<String> dotfiles = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        Distro distro = new DistroDetector().detect();

        List<String> packages = collectPackages(distro);
        List<String> dotfilePaths = collectDotfiles();

        Profile profile = new Profile(
                new SourceInfo(distro.id(), distro.version(), distro.packageManager().name().toLowerCase()),
                packages,
                List.of(),
                dotfilePaths
        );

        new ProfileWriter().write(profile, output);
        System.out.println("Profile written to " + output);
        return 0;
    }

    private List<String> collectDotfiles() {
        List<String> requested = dotfiles.isEmpty() ? DEFAULT_DOTFILES : dotfiles;
        CollectorResult.Dotfiles result = (CollectorResult.Dotfiles) new DotfilesCollector(requested).collect();
        return result.paths();
    }

    private List<String> collectPackages(Distro distro) {
        Collector collector = switch (distro.packageManager()) {
            case APT -> new AptPackageCollector();
            case PACMAN -> new PacmanPackageCollector();
            case DNF, ZYPPER, UNKNOWN -> null;
        };

        if (collector == null) {
            System.err.println("Package manager " + distro.packageManager() + " is not supported yet.");
            return List.of();
        }

        CollectorResult.Packages result = (CollectorResult.Packages) collector.collect();
        return result.names();
    }
}
