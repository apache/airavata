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
package org.apache.airavata.compute.provider.local;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import org.apache.airavata.compute.resource.model.JobManagerCommand;
import org.apache.airavata.compute.resource.submission.JobManagerSpec;
import org.apache.airavata.compute.resource.submission.JobOutputParser;
import org.apache.airavata.compute.resource.submission.RawCommandInfo;

public class LocalJobSpec implements JobManagerSpec {
    private final Map<JobManagerCommand, String> jobManagerCommands;
    private final String jobDescriptionTemplateName;
    private final String scriptExtension;
    private final String installedPath;
    private final JobOutputParser parser;

    public LocalJobSpec(
            String jobDescriptionTemplateName,
            String scriptExtension,
            String installedPath,
            Map<JobManagerCommand, String> jobManagerCommands,
            JobOutputParser parser) {
        this.jobDescriptionTemplateName = jobDescriptionTemplateName;
        this.scriptExtension = scriptExtension;
        this.parser = parser;
        this.jobManagerCommands = jobManagerCommands;
        installedPath = installedPath.trim();
        if (installedPath.isEmpty() || installedPath.endsWith("/")) {
            this.installedPath = installedPath;
        } else {
            this.installedPath = installedPath + "/";
        }
    }

    @Override
    public RawCommandInfo getCancelCommand(String jobID) {
        return new RawCommandInfo(this.installedPath
                + jobManagerCommands.get(JobManagerCommand.DELETION).trim() + " " + jobID);
    }

    @Override
    public String getJobDescriptionTemplateName() {
        return jobDescriptionTemplateName;
    }

    @Override
    public Optional<RawCommandInfo> getMonitorCommand(String jobID) {
        return Optional.empty();
    }

    @Override
    public Optional<RawCommandInfo> getJobIdMonitorCommand(String jobName, String userName) {
        return Optional.empty();
    }

    @Override
    public String getScriptExtension() {
        return scriptExtension;
    }

    @Override
    public RawCommandInfo getSubmitCommand(String workingDirectory, String scriptFilePath) {
        return new RawCommandInfo(this.installedPath
                + jobManagerCommands.get(JobManagerCommand.SUBMISSION).trim() + " " + workingDirectory + File.separator
                + java.nio.file.Path.of(scriptFilePath).getFileName().toString());
    }

    @Override
    public JobOutputParser getParser() {
        return parser;
    }
}
