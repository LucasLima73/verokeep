package io.verokeep.exec;

import java.io.IOException;
import java.util.List;

public class CommandRunner {

    private final boolean dryRun;

    public CommandRunner(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public CommandResult run(List<String> command) {
        if (dryRun) {
            System.out.println("[dry-run] " + String.join(" ", command));
            return new CommandResult(0, "", "");
        }
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(false)
                    .start();
            String stdout = new String(process.getInputStream().readAllBytes());
            String stderr = new String(process.getErrorStream().readAllBytes());
            int exitCode = process.waitFor();
            return new CommandResult(exitCode, stdout, stderr);
        } catch (IOException e) {
            throw new CommandExecutionException("Failed to run command: " + command, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandExecutionException("Interrupted while running command: " + command, e);
        }
    }

    /**
     * Runs a command with stdin/stdout/stderr connected straight to the
     * terminal, so a sudo password prompt or a package manager's own
     * native confirmation prompt reaches the user directly.
     */
    public int runInteractive(List<String> command) {
        if (dryRun) {
            System.out.println("[dry-run] " + String.join(" ", command));
            return 0;
        }
        try {
            return new ProcessBuilder(command).inheritIO().start().waitFor();
        } catch (IOException e) {
            throw new CommandExecutionException("Failed to run command: " + command, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandExecutionException("Interrupted while running command: " + command, e);
        }
    }

    public record CommandResult(int exitCode, String stdout, String stderr) {
        public boolean isSuccess() {
            return exitCode == 0;
        }
    }

    public static class CommandExecutionException extends RuntimeException {
        public CommandExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
