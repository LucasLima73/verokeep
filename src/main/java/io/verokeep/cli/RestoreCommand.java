package io.verokeep.cli;

import io.verokeep.detector.Distro;
import io.verokeep.detector.DistroDetector;
import io.verokeep.detector.PackageManager;
import io.verokeep.profile.Profile;
import io.verokeep.profile.ProfileReader;
import io.verokeep.translator.PackageMapLoader;
import io.verokeep.translator.PackageTranslator;
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
        Distro targetDistro = new DistroDetector().detect();

        PackageManager sourcePm = parsePackageManager(profile.source().packageManager());
        PackageManager targetPm = targetDistro.packageManager();

        System.out.println("Source distro: " + profile.source().distro() + " " + profile.source().version()
                + " (" + sourcePm + ")");
        System.out.println("Target distro: " + targetDistro.id() + " " + targetDistro.version()
                + " (" + targetPm + ")");

        PackageTranslator translator = new PackageTranslator(new PackageMapLoader().loadDefault());
        PackageTranslator.TranslationResult translation = translator.translate(profile.packages(), sourcePm, targetPm);

        System.out.println("\nPackages to install (" + translation.packages().size() + "):");
        for (String pkg : translation.packages()) {
            System.out.println("  - " + pkg);
        }

        if (!translation.unmapped().isEmpty()) {
            System.out.println("\nNo mapping found for these packages in package-map.yaml;"
                    + " keeping the original name as-is (it may not exist on the target distro):");
            for (String pkg : translation.unmapped()) {
                System.out.println("  - " + pkg);
            }
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

    private PackageManager parsePackageManager(String value) {
        if (value == null) {
            return PackageManager.UNKNOWN;
        }
        try {
            return PackageManager.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PackageManager.UNKNOWN;
        }
    }
}
