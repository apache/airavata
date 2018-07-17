/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.helix.impl.task.submission.config.app;

import org.apache.airavata.helix.impl.task.submission.config.JobManagerConfiguration;
import org.apache.airavata.helix.impl.task.submission.config.OutputParser;
import org.apache.airavata.helix.impl.task.submission.config.RawCommandInfo;
import org.apache.airavata.model.appcatalog.computeresource.JobManagerCommand;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Map;

public class SlurmJobConfiguration implements JobManagerConfiguration {
	private final Map<JobManagerCommand, String> jMCommands;
    private String jobDescriptionTemplateName;
    private String scriptExtension;
    private String installedPath;
    private OutputParser parser;

    public SlurmJobConfiguration(String jobDescriptionTemplateName,
                                 String scriptExtension, String installedPath, Map<JobManagerCommand, String>
		                                 jobManagerCommands, OutputParser parser) {
        this.jobDescriptionTemplateName = jobDescriptionTemplateName;
        this.scriptExtension = scriptExtension;
        this.parser = parser;
	    installedPath = installedPath.trim();
        if (installedPath.endsWith("/")) {
            this.installedPath = installedPath;
        } else {
            this.installedPath = installedPath + "/";
        }
	    this.jMCommands = jobManagerCommands;
    }

    public RawCommandInfo getCancelCommand(String jobID) {
        return new RawCommandInfo(this.installedPath + jMCommands.get(JobManagerCommand.DELETION).trim() + " " + jobID);
    }

    public String getJobDescriptionTemplateName() {
        return jobDescriptionTemplateName;
    }

    public void setJobDescriptionTemplateName(String jobDescriptionTemplateName) {
        this.jobDescriptionTemplateName = jobDescriptionTemplateName;
    }

    public RawCommandInfo getMonitorCommand(String jobID) {
        return new RawCommandInfo(this.installedPath + jMCommands.get(JobManagerCommand.JOB_MONITORING).trim() + " -j " + jobID);
    }

    public String getScriptExtension() {
        return scriptExtension;
    }

    public RawCommandInfo getSubmitCommand(String workingDirectory,String pbsFilePath) {
          return new RawCommandInfo(this.installedPath + jMCommands.get(JobManagerCommand.SUBMISSION).trim() + " " +
                workingDirectory + File.separator + FilenameUtils.getName(pbsFilePath));
    }

    public String getInstalledPath() {
        return installedPath;
    }

    public void setInstalledPath(String installedPath) {
        this.installedPath = installedPath;
    }

    public OutputParser getParser() {
        return parser;
    }

    public void setParser(OutputParser parser) {
        this.parser = parser;
    }

    public RawCommandInfo getUserBasedMonitorCommand(String userName) {
        return new RawCommandInfo(this.installedPath + jMCommands.get(JobManagerCommand.JOB_MONITORING).trim() + " -u " + userName);
    }

    @Override
    public RawCommandInfo getJobIdMonitorCommand(String jobName, String userName) {
        return new RawCommandInfo(this.installedPath + jMCommands.get(JobManagerCommand.JOB_MONITORING).trim() + " -n " + jobName + " -u " + userName);
    }

    @Override
    public String getBaseCancelCommand() {
	    return jMCommands.get(JobManagerCommand.DELETION).trim();
    }

    @Override
    public String getBaseMonitorCommand() {
        return jMCommands.get(JobManagerCommand.JOB_MONITORING).trim();
    }

    @Override
    public String getBaseSubmitCommand() {
        return jMCommands.get(JobManagerCommand.SUBMISSION).trim();
    }
}
