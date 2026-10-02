package io.verokeep;

import io.verokeep.cli.ExportCommand;
import io.verokeep.cli.RestoreCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "verokeep",
        mixinStandardHelpOptions = true,
        version = "verokeep 0.1.0",
        subcommands = {ExportCommand.class, RestoreCommand.class},
        description = "Capture your Linux environment and recreate it on another distro."
)
public class VerokeepApp {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new VerokeepApp()).execute(args);
        System.exit(exitCode);
    }
}
