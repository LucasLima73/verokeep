package io.verokeep.cli;

import io.verokeep.exec.CommandRunner;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "backup", description = "Optional: back up a directory with rsync (orchestrates rsync, "
        + "does not reimplement it; no partition/disk operations). Defaults to dry-run.")
public class BackupCommand implements Callable<Integer> {

    @Option(names = "--source", description = "Directory to back up", required = true)
    private Path source;

    @Option(names = "--dest", description = "Destination directory", required = true)
    private Path dest;

    @Option(names = "--apply", description = "Actually run rsync instead of only printing the command")
    private boolean apply;

    @Override
    public Integer call() {
        List<String> command = List.of(
                "rsync", "-a", "--itemize-changes",
                source.toString() + "/",
                dest.toString() + "/"
        );

        CommandRunner runner = new CommandRunner(!apply);
        CommandRunner.CommandResult result = runner.run(command);

        if (apply) {
            if (result.isSuccess()) {
                System.out.println("Backup completed: " + source + " -> " + dest);
            } else {
                System.err.println("Backup failed: " + result.stderr());
            }
        } else {
            System.out.println("\n[dry-run] No changes were made. Re-run with --apply to actually sync.");
        }

        return result.isSuccess() ? 0 : 1;
    }
}
