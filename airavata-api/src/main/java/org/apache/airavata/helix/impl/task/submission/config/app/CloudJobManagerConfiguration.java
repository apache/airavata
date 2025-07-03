/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.helix.impl.task.submission.config.app;

import org.apache.airavata.helix.impl.task.submission.config.JobManagerConfiguration;
import org.apache.airavata.helix.impl.task.submission.config.OutputParser;
import org.apache.airavata.helix.impl.task.submission.config.RawCommandInfo;
import org.apache.airavata.helix.impl.task.submission.config.app.parser.AiravataCustomCommandOutputParser;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

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
        String remoteScriptPath = workingDirectory + File.separator + FilenameUtils.getName(filePath);
        return new RawCommandInfo("/bin/bash " + remoteScriptPath);
    }

    @Override
    public OutputParser getParser() {
        return new AiravataCustomCommandOutputParser();
    }

    @Override
    public String getInstalledPath() {
        throw new UnsupportedOperationException("Installed path is not applicable for direct executions");
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

    @Override
    public String getLivenessCheckCommand(String queueName, String partition) {
        throw new UnsupportedOperationException("Liveness check is not supported for cloud job managers");
    }
}