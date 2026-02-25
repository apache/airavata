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
package org.apache.airavata.compute.resource.submission;

import groovy.text.GStringTemplateEngine;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data container used by JobSubmissionDataBuilder to carry job submission parameters.
 * These are passed into Groovy script templates to generate the final job script.
 */
public class JobSubmissionData {

    private static final Logger logger = LoggerFactory.getLogger(JobSubmissionData.class);

    private String jobName;
    private String jobId;
    private String workingDirectory;
    private String taskId;
    private String processId;
    private String userName;
    private String shellName = "/bin/bash";
    private String queueName;
    private int nodeCount;
    private int cpuCount;
    private int wallTimeLimit;
    private String totalPhysicalMemory;
    private String accountString;
    private String reservationId;
    private String qualityOfService;
    private String applicationInputs;
    private String executablePath;
    private String standardInputFile;
    private String standardOutputFile;
    private String standardErrorFile;
    private List<String> modulesToLoad;
    private List<String> preJobCommands;
    private List<String> postJobCommands;
    private List<String> exportRequirements;
    private List<String> emailIds;
    private String emailOnStart;
    private String emailOnEnd;
    private String emailOnFail;
    private String stdoutFile;
    private String stderrFile;
    private boolean overrideLoginUserName;
    private boolean overrideScratchLocation;
    private boolean overrideAllocationProjectNumber;
    private boolean generateJobscript;
    private boolean hasOptionalFileInputs;
    private String gatewayId;
    private String gatewayUserName;
    private String languageCode;
    private String scriptExtension;
    private String partition;
    private String groupForJob;
    private boolean exclusiveExecution;

    public JobSubmissionData() {}

    /**
     * Load and render a Groovy template, returning the resulting script content.
     * The template is loaded as a classpath resource.
     *
     * @param templateName the classpath template name (e.g. "templates/SLURM_Groovy.template")
     * @return rendered job script content
     * @throws Exception if template loading or rendering fails
     */
    public String loadFromFile(String templateName) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(templateName)) {
            if (is == null) {
                logger.warn("Template file '{}' not found on classpath; returning empty script", templateName);
                return "";
            }
            try (var reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                var engine = new GStringTemplateEngine();
                var binding = buildBinding();
                var template = engine.createTemplate(reader);
                return template.make(binding).toString();
            }
        } catch (IOException e) {
            logger.error("Failed to load template '{}'", templateName, e);
            throw new Exception("Failed to load template: " + templateName, e);
        } catch (Exception e) {
            logger.error("Failed to render template '{}'", templateName, e);
            throw new Exception("Failed to render template: " + templateName, e);
        }
    }

    private Map<String, Object> buildBinding() {
        var map = new HashMap<String, Object>();
        map.put("jobName", jobName);
        map.put("jobId", jobId);
        map.put("workingDirectory", workingDirectory);
        map.put("taskId", taskId);
        map.put("processId", processId);
        map.put("userName", userName);
        map.put("shellName", shellName);
        map.put("queueName", queueName);
        map.put("nodeCount", nodeCount);
        map.put("cpuCount", cpuCount);
        map.put("wallTimeLimit", wallTimeLimit);
        map.put("totalPhysicalMemory", totalPhysicalMemory);
        map.put("accountString", accountString);
        map.put("reservationId", reservationId);
        map.put("qualityOfService", qualityOfService);
        map.put("applicationInputs", applicationInputs);
        map.put("executablePath", executablePath);
        map.put("standardInputFile", standardInputFile);
        map.put("standardOutputFile", standardOutputFile);
        map.put("standardErrorFile", standardErrorFile);
        map.put("modulesToLoad", modulesToLoad != null ? modulesToLoad : new ArrayList<>());
        map.put("preJobCommands", preJobCommands != null ? preJobCommands : new ArrayList<>());
        map.put("postJobCommands", postJobCommands != null ? postJobCommands : new ArrayList<>());
        map.put("exportRequirements", exportRequirements != null ? exportRequirements : new ArrayList<>());
        map.put("emailIds", emailIds != null ? emailIds : new ArrayList<>());
        map.put("emailOnStart", emailOnStart);
        map.put("emailOnEnd", emailOnEnd);
        map.put("emailOnFail", emailOnFail);
        map.put("stdoutFile", stdoutFile);
        map.put("stderrFile", stderrFile);
        map.put("partition", partition);
        map.put("groupForJob", groupForJob);
        map.put("exclusiveExecution", exclusiveExecution);
        map.put("gatewayId", gatewayId);
        map.put("gatewayUserName", gatewayUserName);
        // Pass self as map entry so templates can call mapData.xyz
        map.put("mapData", this);
        return map;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDirectory = workingDir;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getShellName() {
        return shellName;
    }

    public void setShellName(String shellName) {
        this.shellName = shellName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public int getWallTimeLimit() {
        return wallTimeLimit;
    }

    public void setWallTimeLimit(int wallTimeLimit) {
        this.wallTimeLimit = wallTimeLimit;
    }

    public String getTotalPhysicalMemory() {
        return totalPhysicalMemory;
    }

    public void setTotalPhysicalMemory(String totalPhysicalMemory) {
        this.totalPhysicalMemory = totalPhysicalMemory;
    }

    public String getAccountString() {
        return accountString;
    }

    public void setAccountString(String accountString) {
        this.accountString = accountString;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getQualityOfService() {
        return qualityOfService;
    }

    public void setQualityOfService(String qualityOfService) {
        this.qualityOfService = qualityOfService;
    }

    public String getApplicationInputs() {
        return applicationInputs;
    }

    public void setApplicationInputs(String applicationInputs) {
        this.applicationInputs = applicationInputs;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public String getStandardInputFile() {
        return standardInputFile;
    }

    public void setStandardInputFile(String standardInputFile) {
        this.standardInputFile = standardInputFile;
    }

    public String getStandardOutputFile() {
        return standardOutputFile;
    }

    public void setStandardOutputFile(String standardOutputFile) {
        this.standardOutputFile = standardOutputFile;
    }

    public String getStandardErrorFile() {
        return standardErrorFile;
    }

    public void setStandardErrorFile(String standardErrorFile) {
        this.standardErrorFile = standardErrorFile;
    }

    public List<String> getModulesToLoad() {
        return modulesToLoad;
    }

    public void setModulesToLoad(List<String> modulesToLoad) {
        this.modulesToLoad = modulesToLoad;
    }

    public List<String> getPreJobCommands() {
        return preJobCommands;
    }

    public void setPreJobCommands(List<String> preJobCommands) {
        this.preJobCommands = preJobCommands;
    }

    public List<String> getPostJobCommands() {
        return postJobCommands;
    }

    public void setPostJobCommands(List<String> postJobCommands) {
        this.postJobCommands = postJobCommands;
    }

    public List<String> getExportRequirements() {
        return exportRequirements;
    }

    public void setExportRequirements(List<String> exportRequirements) {
        this.exportRequirements = exportRequirements;
    }

    public List<String> getEmailIds() {
        return emailIds;
    }

    public void setEmailIds(List<String> emailIds) {
        this.emailIds = emailIds;
    }

    public String getEmailOnStart() {
        return emailOnStart;
    }

    public void setEmailOnStart(String emailOnStart) {
        this.emailOnStart = emailOnStart;
    }

    public String getEmailOnEnd() {
        return emailOnEnd;
    }

    public void setEmailOnEnd(String emailOnEnd) {
        this.emailOnEnd = emailOnEnd;
    }

    public String getEmailOnFail() {
        return emailOnFail;
    }

    public void setEmailOnFail(String emailOnFail) {
        this.emailOnFail = emailOnFail;
    }

    public String getStdoutFile() {
        return stdoutFile;
    }

    public void setStdoutFile(String stdoutFile) {
        this.stdoutFile = stdoutFile;
    }

    public String getStderrFile() {
        return stderrFile;
    }

    public void setStderrFile(String stderrFile) {
        this.stderrFile = stderrFile;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public String getGroupForJob() {
        return groupForJob;
    }

    public void setGroupForJob(String groupForJob) {
        this.groupForJob = groupForJob;
    }

    public boolean isExclusiveExecution() {
        return exclusiveExecution;
    }

    public void setExclusiveExecution(boolean exclusiveExecution) {
        this.exclusiveExecution = exclusiveExecution;
    }

    public boolean isGenerateJobscript() {
        return generateJobscript;
    }

    public void setGenerateJobscript(boolean generateJobscript) {
        this.generateJobscript = generateJobscript;
    }

    public boolean isHasOptionalFileInputs() {
        return hasOptionalFileInputs;
    }

    public void setHasOptionalFileInputs(boolean hasOptionalFileInputs) {
        this.hasOptionalFileInputs = hasOptionalFileInputs;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getGatewayUserName() {
        return gatewayUserName;
    }

    public void setGatewayUserName(String gatewayUserName) {
        this.gatewayUserName = gatewayUserName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getScriptExtension() {
        return scriptExtension;
    }

    public void setScriptExtension(String scriptExtension) {
        this.scriptExtension = scriptExtension;
    }
}
