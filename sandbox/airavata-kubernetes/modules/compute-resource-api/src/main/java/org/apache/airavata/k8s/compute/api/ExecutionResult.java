package org.apache.airavata.k8s.compute.api;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ExecutionResult {

    private String stdOut;
    private String stdErr;
    private int exitStatus = -1;

    public String getStdOut() {
        return stdOut;
    }

    public ExecutionResult setStdOut(String stdOut) {
        this.stdOut = stdOut;
        return this;
    }

    public String getStdErr() {
        return stdErr;
    }

    public ExecutionResult setStdErr(String stdErr) {
        this.stdErr = stdErr;
        return this;
    }

    public int getExitStatus() {
        return exitStatus;
    }

    public ExecutionResult setExitStatus(int exitStatus) {
        this.exitStatus = exitStatus;
        return this;
    }
}
