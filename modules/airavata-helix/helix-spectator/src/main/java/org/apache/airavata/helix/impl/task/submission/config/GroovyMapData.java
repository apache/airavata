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
package org.apache.airavata.helix.impl.task.submission.config;

import groovy.lang.Writable;
import groovy.text.GStringTemplateEngine;
import groovy.text.TemplateEngine;
import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroovyMapData {

    private final static Logger logger = LoggerFactory.getLogger(GroovyMapData.class);

    @ScriptTag(name = "inputDir")
    private String inputDir;

    @ScriptTag(name = "outputDir")
    private String outputDir;

    @ScriptTag(name = "executablePath")
    private String executablePath;

    @ScriptTag(name = "standardOutFile")
    private String stdoutFile;

    @ScriptTag(name = "standardErrorFile")
    private String stderrFile;

    @ScriptTag(name = "scratchLocation")
    private String scratchLocation;

    @ScriptTag(name = "gatewayId")
    private String gatewayId;

    @ScriptTag(name = "gatewayUserName")
    private String gatewayUserName;

    @ScriptTag(name = "gatewayUserEmail")
    private String gatewayUserEmail;

    @ScriptTag(name = "applicationName")
    private String applicationName;

    @ScriptTag(name = "queueSpecificMacros")
    private List<String> queueSpecificMacros;

    @ScriptTag(name = "accountString")
    private String accountString;

    @ScriptTag(name = "reservation")
    private String reservation;

    @ScriptTag(name = "jobName")
    private String jobName;

    @ScriptTag(name = "jobId")
    private String jobId;

    @ScriptTag(name = "workingDirectory")
    private String workingDirectory;

    @ScriptTag(name = "inputs")
    private List<String> inputs;

    @ScriptTag(name = "inputFiles")
    private List<String> inputFiles;

    @ScriptTag(name = "inputsAll")
    private List<String> inputsAll;

    // This is username of the airavata tries to talk to compute resources
    @ScriptTag(name = "userName")
    private String userName;

    @ScriptTag(name = "currentTime")
    private String currentTime;

    @ScriptTag(name = "shellName")
    private String shellName;

    @ScriptTag(name = "maxWallTime")
    private String maxWallTime;

    @ScriptTag(name = "qualityOfService")
    private String qualityOfService;

    @ScriptTag(name = "queueName")
    private String queueName;

    @ScriptTag(name = "nodes")
    private Integer nodes;

    @ScriptTag(name = "processPerNode")
    private Integer processPerNode;

    @ScriptTag(name = "cpuCount")
    private Integer cpuCount;

    @ScriptTag(name = "usedMem")
    private Integer usedMem;

    @ScriptTag(name = "mailAddress")
    private String mailAddress;

    @ScriptTag(name = "exports")
    private List<String> exports;

    @ScriptTag(name = "moduleCommands")
    private List<String> moduleCommands;

    @ScriptTag(name = "preJobCommands")
    private List<String> preJobCommands;

    @ScriptTag(name = "postJobCommands")
    private List<String> postJobCommands;

    @ScriptTag(name = "jobSubmitterCommand")
    private String jobSubmitterCommand;

    @ScriptTag(name = "chassisName")
    private String chassisName;

    @ScriptTag(name = "taskId")
    private String taskId;

    @ScriptTag(name = "experimentDataDir")
    private String experimentDataDir;

    @ScriptTag(name = "computeHostName")
    private String computeHostName;


    public Map<String, Object> getMap() {

        Map<String, Object> map = new HashMap<>();
        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            ScriptTag scriptTag = field.getAnnotation(ScriptTag.class);
            if (scriptTag != null) {
                field.setAccessible(true);
                try {
                    map.put(scriptTag.name(), field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    // ignore silently
                }
            }
        }

        return map;
    }

    public String getInputDir() {
        return inputDir;
    }

    public GroovyMapData setInputDir(String inputDir) {
        this.inputDir = inputDir;
        return this;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public GroovyMapData setOutputDir(String outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public GroovyMapData setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
        return this;
    }

    public String getStdoutFile() {
        return stdoutFile;
    }

    public GroovyMapData setStdoutFile(String stdoutFile) {
        this.stdoutFile = stdoutFile;
        return this;
    }

    public String getStderrFile() {
        return stderrFile;
    }

    public GroovyMapData setStderrFile(String stderrFile) {
        this.stderrFile = stderrFile;
        return this;
    }

    public String getScratchLocation() {
        return scratchLocation;
    }

    public GroovyMapData setScratchLocation(String scratchLocation) {
        this.scratchLocation = scratchLocation;
        return this;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public GroovyMapData setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
        return this;
    }

    public String getGatewayUserName() {
        return gatewayUserName;
    }

    public GroovyMapData setGatewayUserName(String gatewayUserName) {
        this.gatewayUserName = gatewayUserName;
        return this;
    }

    public String getGatewayUserEmail() {
        return gatewayUserEmail;
    }

    public void setGatewayUserEmail(String gatewayUserEmail) {
        this.gatewayUserEmail = gatewayUserEmail;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public GroovyMapData setApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    public List<String> getQueueSpecificMacros() {
        return queueSpecificMacros;
    }

    public void setQueueSpecificMacros(List<String> queueSpecificMacros) {
        this.queueSpecificMacros = queueSpecificMacros;
    }

    public String getAccountString() {
        return accountString;
    }

    public GroovyMapData setAccountString(String accountString) {
        this.accountString = accountString;
        return this;
    }

    public String getReservation() {
        return reservation;
    }

    public GroovyMapData setReservation(String reservation) {
        this.reservation = reservation;
        return this;
    }

    public String getJobName() {
        return jobName;
    }

    public GroovyMapData setJobName(String jobName) {
        this.jobName = jobName;
        return this;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public GroovyMapData setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public List<String> getInputs() {
        return inputs;
    }

    public GroovyMapData setInputs(List<String> inputs) {
        this.inputs = inputs;
        return this;
    }

    public List<String> getInputFiles() {
        return inputFiles;
    }

    public GroovyMapData setInputFiles(List<String> inputFiles) {
        this.inputFiles = inputFiles;
        return this;
    }

    public List<String> getInputsAll() {
        return inputsAll;
    }

    public GroovyMapData setInputsAll(List<String> inputsAll) {
        this.inputsAll = inputsAll;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public GroovyMapData setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getShellName() {
        return shellName;
    }

    public GroovyMapData setShellName(String shellName) {
        this.shellName = shellName;
        return this;
    }

    public String getMaxWallTime() {
        return maxWallTime;
    }

    public GroovyMapData setMaxWallTime(String maxWallTime) {
        this.maxWallTime = maxWallTime;
        return this;
    }

    public String getQualityOfService() {
        return qualityOfService;
    }

    public GroovyMapData setQualityOfService(String qualityOfService) {
        this.qualityOfService = qualityOfService;
        return this;
    }

    public String getQueueName() {
        return queueName;
    }

    public GroovyMapData setQueueName(String queueName) {
        this.queueName = queueName;
        return this;
    }

    public Integer getNodes() {
        return nodes;
    }

    public GroovyMapData setNodes(Integer nodes) {
        this.nodes = nodes;
        return this;
    }

    public Integer getProcessPerNode() {
        return processPerNode;
    }

    public GroovyMapData setProcessPerNode(Integer processPerNode) {
        this.processPerNode = processPerNode;
        return this;
    }

    public Integer getCpuCount() {
        return cpuCount;
    }

    public GroovyMapData setCpuCount(Integer cpuCount) {
        this.cpuCount = cpuCount;
        return this;
    }

    public Integer getUsedMem() {
        return usedMem;
    }

    public GroovyMapData setUsedMem(Integer usedMem) {
        this.usedMem = usedMem;
        return this;
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public GroovyMapData setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
        return this;
    }

    public List<String> getExports() {
        return exports;
    }

    public GroovyMapData setExports(List<String> exports) {
        this.exports = exports;
        return this;
    }

    public List<String> getModuleCommands() {
        return moduleCommands;
    }

    public GroovyMapData setModuleCommands(List<String> moduleCommands) {
        this.moduleCommands = moduleCommands;
        return this;
    }

    public List<String> getPreJobCommands() {
        return preJobCommands;
    }

    public GroovyMapData setPreJobCommands(List<String> preJobCommands) {
        this.preJobCommands = preJobCommands;
        return this;
    }

    public List<String> getPostJobCommands() {
        return postJobCommands;
    }

    public GroovyMapData setPostJobCommands(List<String> postJobCommands) {
        this.postJobCommands = postJobCommands;
        return this;
    }

    public String getJobSubmitterCommand() {
        return jobSubmitterCommand;
    }

    public GroovyMapData setJobSubmitterCommand(String jobSubmitterCommand) {
        this.jobSubmitterCommand = jobSubmitterCommand;
        return this;
    }

    public String getChassisName() {
        return chassisName;
    }

    public GroovyMapData setChassisName(String chassisName) {
        this.chassisName = chassisName;
        return this;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getExperimentDataDir() {
        return experimentDataDir;
    }

    public void setExperimentDataDir(String experimentDataDir) {
        this.experimentDataDir = experimentDataDir;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getComputeHostName() {
        return computeHostName;
    }

    public void setComputeHostName(String computeHostName) {
        this.computeHostName = computeHostName;
    }

    public Map toImmutableMap() {

        Map<String, Object> dataMap = new HashMap<>();
        Field[] declaredFields = this.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            if (field.getAnnotation(ScriptTag.class) != null) {
                try {
                    dataMap.put(field.getAnnotation(ScriptTag.class).name(), field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return dataMap;
    }

    public String loadFromString(String templateStr) throws Exception {
        TemplateEngine engine = new GStringTemplateEngine();
        Writable make;
        try {
            make = engine.createTemplate(templateStr).make(toImmutableMap());
            //String intermediateOut = make.toString();
            //make = engine.createTemplate(intermediateOut).make(toImmutableMap()); // Parsing through the map to resolve parameters in the map values (AIRAVATA-3391)
        } catch (Exception e) {
            throw new Exception("Error while generating script using groovy map for string " + templateStr, e);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Groovy map as string for template string " + templateStr);
            logger.trace(make.toString());
        }
        return make.toString();
    }

    public String loadFromFile(String templateName) throws Exception {
        URL templateUrl = ApplicationSettings.loadFile(templateName);
        if (templateUrl == null) {
            String error = "Template file '" + templateName + "' not found";
            logger.error(error);
            throw new Exception(error);
        }

        try {
            String templateStr = IOUtils.toString(templateUrl.openStream(), Charset.defaultCharset());
            return loadFromString(templateStr);
        } catch (Exception e) {
            throw new Exception("Error while generating script using groovy map for template " + templateUrl.getPath(), e);
        }
    }
}
