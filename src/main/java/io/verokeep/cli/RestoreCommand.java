package io.verokeep.cli;

import io.verokeep.profile.Profile;
import io.verokeep.profile.ProfileReader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "restore", description = "Restore an environment from a YAML profile")
public class RestoreCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Profile YAML file")
    private Path profilePath;

    @Option(names = "--dry-run", description = "Only print the plan, do not execute anything")
    private boolean dryRun;

    @Option(names = "--generate-script", description = "Generate a shell script for review instead of executing")
    private boolean generateScript;

    @Override
    public Integer call() throws Exception {
        Profile profile = new ProfileReader().read(profilePath);

        System.out.println("Source distro: " + profile.source().distro() + " " + profile.source().version());
        System.out.println("Packages to install (" + profile.packages().size() + "):");
        for (String pkg : profile.packages()) {
            System.out.println("  - " + pkg);
        }

        if (dryRun) {
            System.out.println("\n[dry-run] No changes were made.");
        } else if (generateScript) {
            System.out.println("\n--generate-script is not implemented yet.");
        } else {
            System.out.println("\nApplying changes is not implemented yet.");
        }
        return 0;
    }
}
