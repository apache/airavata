/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.task.submission;

import java.io.File;
import org.apache.airavata.task.submission.parser.AiravataCustomCommandOutputParser;

/**
 * A Job Manager Configuration for executing jobs directly on a cloud VM via SSH
 */
public class CloudJobManagerConfiguration implements JobManagerConfiguration {

    private final String jobDescriptionTemplateName;

    public CloudJobManagerConfiguration(String jobDescriptionTemplateName) {
        this.jobDescriptionTemplateName = jobDescriptionTemplateName;
    }

    @Override
    public RawCommandInfo getCancelCommand(String jobID) {
        // The jobID is the Process ID (PID)
        return new RawCommandInfo("kill -9 " + jobID);
    }

    @Override
    public String getJobDescriptionTemplateName() {
        return jobDescriptionTemplateName;
    }

    @Override
    public RawCommandInfo getMonitorCommand(String jobID) {
        // The jobID is the PID
        return new RawCommandInfo("ps -p " + jobID + " -o stat=");
    }

    @Override
    public RawCommandInfo getUserBasedMonitorCommand(String userName) {
        return new RawCommandInfo("ps -u " + userName);
    }

    @Override
    public RawCommandInfo getJobIdMonitorCommand(String jobName, String userName) {
        return new RawCommandInfo("pgrep -u " + userName + " -f " + jobName);
    }

    @Override
    public String getScriptExtension() {
        return ".sh";
    }

    @Override
    public RawCommandInfo getSubmitCommand(String workingDirectory, String filePath) {
        String remoteScriptPath = workingDirectory
                + File.separator
                + java.nio.file.Path.of(filePath).getFileName().toString();
        return new RawCommandInfo("/bin/bash " + remoteScriptPath);
    }

    @Override
    public OutputParser getParser() {
        return new AiravataCustomCommandOutputParser();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Not applicable for CloudJobManagerConfiguration as jobs are executed directly
     * on cloud VMs via SSH, not through a job scheduler that requires an installed path.
     *
     * @throws UnsupportedOperationException always, as this operation is not applicable
     */
    @Override
    public String getInstalledPath() {
        throw new UnsupportedOperationException("Installed path is not applicable for direct executions on cloud VMs");
    }

    @Override
    public String getBaseCancelCommand() {
        return "kill";
    }

    @Override
    public String getBaseMonitorCommand() {
        return "ps";
    }

    @Override
    public String getBaseSubmitCommand() {
        return "/bin/bash";
    }

    /**
     * {@inheritDoc}
     *
     * <p>Liveness checks are not supported for cloud job managers as jobs run directly
     * on VMs without a queue/partition system. Job status is monitored via process status
     * checks instead.
     *
     * @param queueName queue name (not used)
     * @param partition partition name (not used)
     * @throws UnsupportedOperationException always, as this operation is not applicable
     */
    @Override
    public String getLivenessCheckCommand(String queueName, String partition) {
        throw new UnsupportedOperationException(
                "Liveness check is not supported for cloud job managers - jobs run directly on VMs");
    }
}
