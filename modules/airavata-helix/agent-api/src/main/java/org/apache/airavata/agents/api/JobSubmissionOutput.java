package org.apache.airavata.agents.api;

public class JobSubmissionOutput {
    private int exitCode = Integer.MIN_VALUE;
    private String stdOut;
    private String stdErr;
    private String command;
    private String jobId;
    private boolean isJobSubmissionFailed;
    private String failureReason;
    private String description;

    public int getExitCode() {
        return exitCode;
    }

    public JobSubmissionOutput setExitCode(int exitCode) {
        this.exitCode = exitCode;
        return this;
    }

    public String getStdOut() {
        return stdOut;
    }

    public JobSubmissionOutput setStdOut(String stdOut) {
        this.stdOut = stdOut;
        return this;
    }

    public String getStdErr() {
        return stdErr;
    }

    public JobSubmissionOutput setStdErr(String stdErr) {
        this.stdErr = stdErr;
        return this;
    }

    public String getCommand() {
        return command;
    }

    public JobSubmissionOutput setCommand(String command) {
        this.command = command;
        return this;
    }

    public String getJobId() {
        return jobId;
    }

    public JobSubmissionOutput setJobId(String jobId) {
        this.jobId = jobId;
        return this;
    }

    public boolean isJobSubmissionFailed() {
        return isJobSubmissionFailed;
    }

    public JobSubmissionOutput setJobSubmissionFailed(boolean jobSubmissionFailed) {
        isJobSubmissionFailed = jobSubmissionFailed;
        return this;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public JobSubmissionOutput setFailureReason(String failureReason) {
        this.failureReason = failureReason;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
