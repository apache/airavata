package org.apache.airavata.helix.impl.task.submission.config;

public interface JobManagerConfiguration {

    public RawCommandInfo getCancelCommand(String jobID);

    public String getJobDescriptionTemplateName();

    public RawCommandInfo getMonitorCommand(String jobID);

    public RawCommandInfo getUserBasedMonitorCommand(String userName);

    public RawCommandInfo getJobIdMonitorCommand(String jobName , String userName);

    public String getScriptExtension();

    public RawCommandInfo getSubmitCommand(String workingDirectory, String pbsFilePath);

    public OutputParser getParser();

    public String getInstalledPath();

    public String getBaseCancelCommand();

    public String getBaseMonitorCommand();

    public String getBaseSubmitCommand();

}