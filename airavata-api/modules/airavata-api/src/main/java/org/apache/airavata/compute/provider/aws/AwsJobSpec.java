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
package org.apache.airavata.compute.provider.aws;

import java.io.File;
import java.util.Optional;
import org.apache.airavata.compute.resource.submission.CustomCommandOutputParser;
import org.apache.airavata.compute.resource.submission.JobManagerSpec;
import org.apache.airavata.compute.resource.submission.JobOutputParser;
import org.apache.airavata.compute.resource.submission.RawCommandInfo;

/**
 * Job manager for executing jobs directly on a cloud VM via SSH.
 * Monitors processes via PID-based ps commands rather than a batch scheduler.
 */
public class AwsJobSpec implements JobManagerSpec {

    private final String jobDescriptionTemplateName;

    public AwsJobSpec(String jobDescriptionTemplateName) {
        this.jobDescriptionTemplateName = jobDescriptionTemplateName;
    }

    @Override
    public RawCommandInfo getCancelCommand(String jobID) {
        return new RawCommandInfo("kill -9 " + jobID);
    }

    @Override
    public String getJobDescriptionTemplateName() {
        return jobDescriptionTemplateName;
    }

    @Override
    public Optional<RawCommandInfo> getMonitorCommand(String jobID) {
        return Optional.of(new RawCommandInfo("ps -p " + jobID + " -o stat="));
    }

    @Override
    public Optional<RawCommandInfo> getJobIdMonitorCommand(String jobName, String userName) {
        return Optional.empty();
    }

    @Override
    public String getScriptExtension() {
        return ".sh";
    }

    @Override
    public RawCommandInfo getSubmitCommand(String workingDirectory, String scriptFilePath) {
        String remoteScriptPath = workingDirectory
                + File.separator
                + java.nio.file.Path.of(scriptFilePath).getFileName().toString();
        return new RawCommandInfo("/bin/bash " + remoteScriptPath);
    }

    @Override
    public JobOutputParser getParser() {
        return new CustomCommandOutputParser();
    }
}
