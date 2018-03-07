package org.apache.airavata.helix.impl.task;

public class TaskOnFailException extends Exception {

    private String reason;
    private boolean critical;
    private Throwable e;

    public TaskOnFailException(String reason, boolean critical, Throwable e) {
        super(reason, e);
        this.reason = reason;
        this.critical = critical;
        this.e = e;
    }

    public String getReason() {
        return reason;
    }

    public boolean isCritical() {
        return critical;
    }

    public Throwable getError() {
        return e;
    }
}
