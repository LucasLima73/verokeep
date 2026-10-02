package io.verokeep.cli;

import io.verokeep.detector.Distro;
import io.verokeep.detector.DistroDetector;
import io.verokeep.detector.PackageManager;
import io.verokeep.exec.CommandRunner;
import io.verokeep.profile.Profile;
import io.verokeep.profile.ProfileReader;
import io.verokeep.translator.PackageMapLoader;
import io.verokeep.translator.PackageTranslator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "restore", description = "Restore an environment from a YAML profile")
public class RestoreCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Profile YAML file")
    private Path profilePath;

    @Option(names = "--dry-run", description = "Only print the plan, do not execute anything")
    private boolean dryRun;

    @Option(names = "--generate-script", description = "Generate a shell script for review instead of executing")
    private boolean generateScript;

    @Option(names = "--script-output", description = "Where to write the generated script (default: verokeep-restore.sh)")
    private Path scriptOutput = Path.of("verokeep-restore.sh");

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

        System.out.println("\nDotfiles to restore (" + profile.dotfiles().size() + "):");
        for (String dotfile : profile.dotfiles()) {
            System.out.println("  - " + dotfile);
        }
        System.out.println("(Verokeep does not copy dotfile contents yet -- it only tracks these paths."
                + " Restore them from your own dotfiles repo or backup.)");

        List<String> installCommand = buildInstallCommand(targetPm, translation.packages());

        if (dryRun) {
            System.out.println("\n[dry-run] No changes were made.");
            return 0;
        }

        if (generateScript) {
            writeScript(installCommand, profile);
            return 0;
        }

        if (installCommand == null) {
            System.err.println("\nDon't know how to install packages for " + targetPm + " yet.");
            return 1;
        }

        if (installCommand.isEmpty()) {
            System.out.println("\nNo packages to install.");
            return 0;
        }

        System.out.println("\nThis will run as root:");
        System.out.println("  " + String.join(" ", installCommand));

        if (!confirm()) {
            System.out.println("Aborted. Nothing was changed.");
            return 1;
        }

        int exitCode = new CommandRunner(false).runInteractive(installCommand);
        if (exitCode != 0) {
            System.err.println("Package installation failed (exit " + exitCode + ").");
            return exitCode;
        }
        System.out.println("Packages installed.");
        return 0;
    }

    private List<String> buildInstallCommand(PackageManager pm, List<String> packages) {
        if (packages.isEmpty()) {
            return List.of();
        }
        List<String> command = new ArrayList<>();
        command.add("sudo");
        switch (pm) {
            case APT -> {
                command.add("apt-get");
                command.add("install");
            }
            case DNF -> {
                command.add("dnf");
                command.add("install");
            }
            case PACMAN -> {
                command.add("pacman");
                command.add("-S");
                command.add("--needed");
            }
            case ZYPPER -> {
                command.add("zypper");
                command.add("install");
            }
            case UNKNOWN -> {
                return null;
            }
        }
        command.addAll(packages);
        return command;
    }

    /**
     * Reads one line directly off file descriptor 0, bypassing System.in.
     * System.in is a BufferedInputStream set up by the JVM at startup: even
     * reading it one byte at a time pulls a whole block from the underlying
     * pipe into that buffer, silently stealing bytes meant for the package
     * manager's own confirmation prompt right after (it inherits this same
     * stdin via {@link CommandRunner#runInteractive}). A fresh,
     * unbuffered FileInputStream over FileDescriptor.in reads only what it's
     * asked for, leaving the rest in the pipe for the child process.
     */
    private boolean confirm() {
        System.out.print("Type 'yes' to continue: ");
        System.out.flush();
        StringBuilder line = new StringBuilder();
        try {
            java.io.InputStream rawStdin = new java.io.FileInputStream(java.io.FileDescriptor.in);
            int b;
            while ((b = rawStdin.read()) != -1 && b != '\n') {
                if (b != '\r') {
                    line.append((char) b);
                }
            }
        } catch (IOException e) {
            return false;
        }
        return line.toString().trim().equalsIgnoreCase("yes");
    }

    private void writeScript(List<String> installCommand, Profile profile) throws java.io.IOException {
        StringBuilder script = new StringBuilder();
        script.append("#!/usr/bin/env bash\n");
        script.append("# Generated by `verokeep restore --generate-script`.\n");
        script.append("# Source: ").append(profile.source().distro()).append(' ')
                .append(profile.source().version()).append('\n');
        script.append("# Review this script before running it. It will prompt for your sudo password.\n");
        script.append("set -euo pipefail\n\n");

        if (installCommand == null) {
            script.append("# No known way to install packages for this target package manager.\n");
        } else if (installCommand.isEmpty()) {
            script.append("# No packages to install.\n");
        } else {
            script.append(String.join(" ", installCommand)).append('\n');
        }

        script.append("\n# Dotfiles (not copied automatically -- restore from your own backup/dotfiles repo):\n");
        for (String dotfile : profile.dotfiles()) {
            script.append("#   ").append(dotfile).append('\n');
        }

        Files.writeString(scriptOutput, script.toString());
        scriptOutput.toFile().setExecutable(true);
        System.out.println("\nScript written to " + scriptOutput + ". Review it, then run it yourself.");
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
