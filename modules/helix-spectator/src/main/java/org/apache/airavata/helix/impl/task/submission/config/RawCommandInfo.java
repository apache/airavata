package org.apache.airavata.helix.impl.task.submission.config;

public class RawCommandInfo {

    private String rawCommand;

    public RawCommandInfo(String cmd) {
        this.rawCommand = cmd;
    }

    public String getCommand() {
        return this.rawCommand;
    }

    public String getRawCommand() {
        return rawCommand;
    }

    public void setRawCommand(String rawCommand) {
        this.rawCommand = rawCommand;
    }
}
