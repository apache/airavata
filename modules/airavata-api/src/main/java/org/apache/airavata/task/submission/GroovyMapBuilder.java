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

import groovy.text.GStringTemplateEngine;
import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.apache.airavata.common.model.ApplicationParallelismType;
import org.apache.airavata.common.model.CommandObject;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.DataType;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.common.model.JobSubmissionProtocol;
import org.apache.airavata.common.model.JobSubmissionTaskModel;
import org.apache.airavata.common.model.MonitorMode;
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.common.model.ResourceJobManager;
import org.apache.airavata.common.model.ResourceJobManagerType;
import org.apache.airavata.common.model.SetEnvPaths;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.conditional.ConditionalOnParticipant;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.task.base.TaskContext;
import org.apache.airavata.task.base.TaskOnFailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnParticipant
public class GroovyMapBuilder {

    private final RegistryService registryService;
    private final AiravataServerProperties properties;

    public GroovyMapBuilder(RegistryService registryService, AiravataServerProperties properties) {
        this.registryService = registryService;
        this.properties = properties;
    }

    private static final Logger logger = LoggerFactory.getLogger(GroovyMapBuilder.class);

    public static final String MULTIPLE_INPUTS_SPLITTER = ",";

    public GroovyMapData build(TaskContext taskContext) throws Exception {
        var mapData = new GroovyMapData();

        setMailAddresses(taskContext, mapData);
        mapData.setInputDir(taskContext.getInputDir());
        mapData.setOutputDir(taskContext.getOutputDir());
        mapData.setExecutablePath(
                taskContext.getApplicationDeploymentDescription().getExecutablePath());
        mapData.setStdoutFile(taskContext.getStdoutLocation());
        mapData.setStderrFile(taskContext.getStderrLocation());
        mapData.setScratchLocation(taskContext.getScratchLocation());
        mapData.setGatewayId(taskContext.getGatewayId());
        mapData.setGatewayUserName(taskContext.getProcessModel().getUserName());
        mapData.setApplicationName(
                taskContext.getApplicationInterfaceDescription().getApplicationName());
        mapData.setQueueSpecificMacros(taskContext.getQueueSpecificMacros());
        mapData.setAccountString(taskContext.getAllocationProjectNumber());
        mapData.setReservation(taskContext.getReservation());
        mapData.setJobName("A" + generateJobName());
        mapData.setWorkingDirectory(taskContext.getWorkingDir());
        mapData.setTaskId(taskContext.getTaskId());
        mapData.setExperimentDataDir(taskContext.getProcessModel().getExperimentDataDir());
        mapData.setExperimentId(taskContext.getExperimentId());

        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mmZ").withZone(ZoneId.of("America/New_York"));
        mapData.setCurrentTime(
                formatter.format(AiravataUtils.getUniqueTimestamp().toInstant()));

        // List<String> emails = taskContext.getUserProfile().getEmails();
        // if (emails != null && emails.size() > 0) {
        //    mapData.setGatewayUserEmail(emails.get(0));
        // }

        List<String> inputValues =
                getProcessInputValues(taskContext.getProcessModel().getProcessInputs(), true);
        inputValues.addAll(getProcessOutputValues(taskContext.getProcessModel().getProcessOutputs(), true));
        mapData.setInputs(inputValues);

        List<String> inputFiles =
                getProcessInputFiles(taskContext.getProcessModel().getProcessInputs(), false);
        mapData.setInputFiles(inputFiles);

        List<String> inputValuesAll =
                getProcessInputValues(taskContext.getProcessModel().getProcessInputs(), false);
        inputValuesAll.addAll(
                getProcessOutputValues(taskContext.getProcessModel().getProcessOutputs(), false));
        mapData.setInputsAll(inputValuesAll);

        mapData.setUserName(taskContext.getComputeResourceLoginUserName());
        mapData.setShellName("/bin/bash");

        String hostName = taskContext.getComputeResourceDescription().getHostName();
        List<String> hostAliases = taskContext.getComputeResourceDescription().getHostAliases();
        if (hostAliases != null && !hostAliases.isEmpty()) {
            hostName = hostAliases.get(0);
        }
        mapData.setComputeHostName(hostName);

        if (taskContext != null) {
            try {
                JobSubmissionTaskModel jobSubmissionTaskModel =
                        ((JobSubmissionTaskModel) taskContext.getSubTaskModel());
                if (jobSubmissionTaskModel.getWallTime() > 0) {
                    String wallTime = maxWallTimeCalculator(jobSubmissionTaskModel.getWallTime());
                    // Use LSF-specific format if ResourceJobManager is LSF
                    try {
                        ResourceJobManager resourceJobManager = taskContext.getResourceJobManager();
                        if (resourceJobManager != null
                                && resourceJobManager.getResourceJobManagerType() == ResourceJobManagerType.LSF) {
                            wallTime = maxWallTimeCalculatorForLSF(jobSubmissionTaskModel.getWallTime());
                        }
                    } catch (Exception e) {
                        logger.debug("Could not get ResourceJobManager for LSF wall time check: {}", e.getMessage());
                    }
                    mapData.setMaxWallTime(wallTime);
                    mapData.setWallTimeInSeconds(jobSubmissionTaskModel.getWallTime() * 60);
                }
            } catch (Exception e) {
                logger.error("Error while getting job submission sub task model", e);
            }
        }

        // NOTE: Give precedence to data comes with experiment
        // qos per queue
        String qoS = getQoS(taskContext.getQualityOfService(), taskContext.getQueueName());
        if (qoS != null) {
            mapData.setQualityOfService(qoS);
        }
        ComputationalResourceSchedulingModel scheduling =
                taskContext.getProcessModel().getProcessResourceSchedule();
        if (scheduling != null) {
            int totalNodeCount = scheduling.getNodeCount();
            int totalCPUCount = scheduling.getTotalCPUCount();

            if (isValid(scheduling.getQueueName())) {
                mapData.setQueueName(scheduling.getQueueName());
            }
            if (totalNodeCount > 0) {
                mapData.setNodes(totalNodeCount);
            }
            if (totalCPUCount > 0) {
                int ppn = totalCPUCount / totalNodeCount;
                mapData.setProcessPerNode(ppn);
                mapData.setCpuCount(totalCPUCount);
            }
            // max wall time may be set before this level if jobsubmission task has wall time configured to this job,
            // if so we ignore scheduling configuration.
            if (scheduling.getWallTimeLimit() > 0 && mapData.getMaxWallTime() == null) {
                String wallTime = maxWallTimeCalculator(scheduling.getWallTimeLimit());
                // Use LSF-specific format if ResourceJobManager is LSF
                try {
                    ResourceJobManager resourceJobManager = taskContext.getResourceJobManager();
                    if (resourceJobManager != null
                            && resourceJobManager.getResourceJobManagerType() == ResourceJobManagerType.LSF) {
                        wallTime = maxWallTimeCalculatorForLSF(scheduling.getWallTimeLimit());
                    }
                } catch (Exception e) {
                    logger.debug("Could not get ResourceJobManager for LSF wall time check: {}", e.getMessage());
                }
                mapData.setMaxWallTime(wallTime);
                mapData.setWallTimeInSeconds(scheduling.getWallTimeLimit() * 60);
            }
            if (scheduling.getTotalPhysicalMemory() > 0) {
                mapData.setUsedMem(scheduling.getTotalPhysicalMemory());
            }
            if (isValid(scheduling.getOverrideLoginUserName())) {
                mapData.setUserName(scheduling.getOverrideLoginUserName());
            }
            if (isValid(scheduling.getOverrideAllocationProjectNumber())) {
                mapData.setAccountString(scheduling.getOverrideAllocationProjectNumber());
            }
            if (isValid(scheduling.getStaticWorkingDir())) {
                mapData.setWorkingDirectory(scheduling.getStaticWorkingDir());
            }
        } else {
            logger.error("Task scheduling cannot be null at this point..");
        }

        var appDepDescription = taskContext.getApplicationDeploymentDescription();

        List<SetEnvPaths> exportCommands = appDepDescription.getSetEnvironment();
        if (exportCommands != null) {
            List<String> exportCommandList = exportCommands.stream()
                    .sorted((e1, e2) -> e1.getEnvPathOrder() - e2.getEnvPathOrder())
                    .map(map -> map.getName() + "=" + map.getValue())
                    .toList();
            mapData.setExports(exportCommandList);
        }

        List<CommandObject> moduleCmds = appDepDescription.getModuleLoadCmds();
        if (moduleCmds != null) {
            List<String> modulesCmdCollect = moduleCmds.stream()
                    .sorted((e1, e2) -> e1.getCommandOrder() - e2.getCommandOrder())
                    .map(map -> parseCommands(map.getCommand(), mapData))
                    .toList();
            mapData.setModuleCommands(modulesCmdCollect);
        }

        List<CommandObject> preJobCommands = appDepDescription.getPreJobCommands();
        if (preJobCommands != null) {
            List<String> preJobCmdCollect = preJobCommands.stream()
                    .sorted((e1, e2) -> e1.getCommandOrder() - e2.getCommandOrder())
                    .map(map -> parseCommands(map.getCommand(), mapData))
                    .toList();
            mapData.setPreJobCommands(preJobCmdCollect);
        }

        List<CommandObject> postJobCommands = appDepDescription.getPostJobCommands();
        if (postJobCommands != null) {
            List<String> postJobCmdCollect = postJobCommands.stream()
                    .sorted((e1, e2) -> e1.getCommandOrder() - e2.getCommandOrder())
                    .map(map -> parseCommands(map.getCommand(), mapData))
                    .toList();
            mapData.setPostJobCommands(postJobCmdCollect);
        }

        var parallelism = appDepDescription.getParallelism();
        if (parallelism != null) {
            if (parallelism != ApplicationParallelismType.SERIAL) {
                Map<ApplicationParallelismType, String> parallelismPrefix =
                        taskContext.getResourceJobManager().getParallelismPrefix();
                if (parallelismPrefix != null) {
                    String parallelismCommand = parallelismPrefix.get(parallelism);
                    if (parallelismCommand != null) {
                        mapData.setJobSubmitterCommand(parallelismCommand);
                    } else {
                        throw new Exception("Parallelism prefix is not defined for given parallelism type "
                                + parallelism + ".. Please define the parallelism prefix at App Catalog");
                    }
                }
            }
        }

        return mapData;
    }

    public static int generateJobName() {
        return java.util.concurrent.ThreadLocalRandom.current().nextInt(99999999, Integer.MAX_VALUE);
    }

    private static List<String> getProcessInputValues(
            List<InputDataObjectType> processInputs, boolean commandLineOnly) {
        List<String> inputValues = new ArrayList<>();
        if (processInputs != null) {

            // sort the inputs first and then build the command ListR
            Set<InputDataObjectType> sortedInputSet =
                    new TreeSet<>(Comparator.comparingInt(InputDataObjectType::getInputOrder));
            sortedInputSet.addAll(processInputs);
            for (InputDataObjectType inputDataObjectType : sortedInputSet) {
                if (commandLineOnly && !inputDataObjectType.getRequiredToAddedToCommandLine()) {
                    continue;
                }

                if (!inputDataObjectType.getIsRequired()
                        && (inputDataObjectType.getValue() == null || "".equals(inputDataObjectType.getValue()))) {
                    // For URI/ Collection non required inputs, if the value is empty, ignore it. Fix for airavata-3276
                    continue;
                }

                if (inputDataObjectType.getApplicationArgument() != null
                        && !inputDataObjectType.getApplicationArgument().equals("")) {
                    inputValues.add(inputDataObjectType.getApplicationArgument());
                }

                if (inputDataObjectType.getValue() != null
                        && !inputDataObjectType.getValue().equals("")) {
                    if (inputDataObjectType.getType() == DataType.URI) {
                        if (inputDataObjectType.getOverrideFilename() != null) {
                            inputValues.add(inputDataObjectType.getOverrideFilename());
                        } else {
                            // set only the relative path
                            String filePath = inputDataObjectType.getValue();
                            filePath =
                                    filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1, filePath.length());
                            inputValues.add(filePath);
                        }
                    } else if (inputDataObjectType.getType() == DataType.URI_COLLECTION) {
                        String filePaths = inputDataObjectType.getValue();
                        String[] paths = filePaths.split(MULTIPLE_INPUTS_SPLITTER);

                        for (int i = 0; i < paths.length; i++) {
                            paths[i] = paths[i].substring(paths[i].lastIndexOf(File.separatorChar) + 1);
                        }

                        inputValues.add(String.join(" ", paths));
                    } else {
                        inputValues.add(inputDataObjectType.getValue());
                    }
                }
            }
        }
        return inputValues;
    }

    private static List<String> getProcessInputFiles(List<InputDataObjectType> processInputs, boolean commandLineOnly) {
        List<String> inputFiles = new ArrayList<>();
        if (processInputs != null) {

            // sort the inputs first and then build the command ListR
            Set<InputDataObjectType> sortedInputSet =
                    new TreeSet<>(Comparator.comparingInt(InputDataObjectType::getInputOrder));
            sortedInputSet.addAll(processInputs);
            for (InputDataObjectType inputDataObjectType : sortedInputSet) {
                if (!inputDataObjectType.getIsRequired()
                        && (inputDataObjectType.getValue() == null || "".equals(inputDataObjectType.getValue()))) {
                    // For URI/ Collection non required inputs, if the value is empty, ignore it. Fix for airavata-3276
                    continue;
                }

                if (inputDataObjectType.getValue() != null
                        && !inputDataObjectType.getValue().equals("")) {
                    if (inputDataObjectType.getType() == DataType.URI) {
                        if (inputDataObjectType.getOverrideFilename() != null) {
                            inputFiles.add(inputDataObjectType.getOverrideFilename());
                        } else {
                            // set only the relative path
                            String filePath = inputDataObjectType.getValue();
                            filePath =
                                    filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1, filePath.length());
                            inputFiles.add(filePath);
                        }
                    } else if (inputDataObjectType.getType() == DataType.URI_COLLECTION) {
                        String filePaths = inputDataObjectType.getValue();
                        String[] paths = filePaths.split(MULTIPLE_INPUTS_SPLITTER);

                        for (int i = 0; i < paths.length; i++) {
                            paths[i] = paths[i].substring(paths[i].lastIndexOf(File.separatorChar) + 1);
                        }

                        inputFiles.add(String.join(" ", paths));
                    }
                }
            }
        }
        return inputFiles;
    }

    private static List<String> getProcessOutputValues(
            List<OutputDataObjectType> processOutputs, boolean commandLineOnly) {
        List<String> inputValues = new ArrayList<>();
        if (processOutputs != null) {
            for (OutputDataObjectType output : processOutputs) {
                if (output.getApplicationArgument() != null
                        && !output.getApplicationArgument().equals("")) {
                    inputValues.add(output.getApplicationArgument());
                }
                if (commandLineOnly) {
                    if (output.getValue() != null
                            && !output.getValue().equals("")
                            && output.getRequiredToAddedToCommandLine()) {
                        if (output.getType() == DataType.URI) {
                            String filePath = output.getValue();
                            filePath =
                                    filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1, filePath.length());
                            inputValues.add(filePath);
                        }
                    }
                } else {
                    if (output.getValue() != null && !output.getValue().equals("")) {
                        if (output.getType() == DataType.URI) {
                            String filePath = output.getValue();
                            filePath =
                                    filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1, filePath.length());
                            inputValues.add(filePath);
                        }
                    }
                }
            }
        }
        return inputValues;
    }

    static String getQoS(String qualityOfService, String preferredBatchQueue) {
        if (preferredBatchQueue == null
                || preferredBatchQueue.isEmpty()
                || qualityOfService == null
                || qualityOfService.isEmpty()) return null;
        final String qos = "qos";
        var pattern = Pattern.compile(preferredBatchQueue + "=(?<" + qos + ">[^,]*)");
        var matcher = pattern.matcher(qualityOfService);
        if (matcher.find()) {
            return matcher.group(qos);
        }
        return null;
    }

    public static String maxWallTimeCalculator(int maxWalltime) {
        if (maxWalltime < 60) {
            return "00:" + maxWalltime + ":00";
        } else {
            int minutes = maxWalltime % 60;
            int hours = maxWalltime / 60;
            return hours + ":" + minutes + ":00";
        }
    }

    public static String maxWallTimeCalculatorForLSF(int maxWalltime) {
        if (maxWalltime < 60) {
            return "00:" + maxWalltime;
        } else {
            int minutes = maxWalltime % 60;
            int hours = maxWalltime / 60;
            return hours + ":" + minutes;
        }
    }

    private static boolean isValid(String str) {
        return str != null && !str.isEmpty();
    }

    static String parseCommands(String value, GroovyMapData bindMap) {
        var templateEngine = new GStringTemplateEngine();
        try {
            return templateEngine
                    .createTemplate(value)
                    .make(bindMap.toImmutableMap())
                    .toString();
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalArgumentException(
                    "Error while parsing command " + value + " , Invalid command or incomplete bind map");
        }
    }

    private void setMailAddresses(TaskContext taskContext, GroovyMapData groovyMap) throws Exception {
        var processModel = taskContext.getProcessModel();
        String emailIds = null;

        var props = this.properties;

        if (isEmailBasedJobMonitor(taskContext) && props != null) {
            emailIds = props.services().monitor().email().address();
        }
        if (props != null && props.services().monitor().compute().enabled()) {
            String userJobNotifEmailIds =
                    props.services().monitor().compute().notification().emailIds();
            if (userJobNotifEmailIds != null && !userJobNotifEmailIds.isEmpty()) {
                if (emailIds != null && !emailIds.isEmpty()) {
                    emailIds += ("," + userJobNotifEmailIds);
                } else {
                    emailIds = userJobNotifEmailIds;
                }
            }
            if (processModel.getEnableEmailNotification()) {
                List<String> emailList = processModel.getEmailAddresses();
                if (emailList == null) {
                    throw new TaskOnFailException(
                            "At least one email should be provided as the email notification is turned on",
                            false,
                            null);
                }
                String elist = listToCsv(emailList, ',');
                if (elist != null && !elist.isEmpty()) {
                    if (emailIds != null && !emailIds.isEmpty()) {
                        emailIds = emailIds + "," + elist;
                    } else {
                        emailIds = elist;
                    }
                }
            }
        }
        if (emailIds != null && !emailIds.isEmpty()) {
            logger.info("Email list: " + emailIds);
            groovyMap.setMailAddress(emailIds);
        }
    }

    public boolean isEmailBasedJobMonitor(TaskContext taskContext) throws Exception {
        var jobSubmissionProtocol = taskContext.getPreferredJobSubmissionProtocol();
        var jobSubmissionInterface = taskContext.getPreferredJobSubmissionInterface();
        if (jobSubmissionProtocol == JobSubmissionProtocol.SSH) {
            var jobSubmissionInterfaceId = jobSubmissionInterface.getJobSubmissionInterfaceId();
            var sshJobSubmission = registryService.getSSHJobSubmission(jobSubmissionInterfaceId);
            var monitorMode = sshJobSubmission.getMonitorMode();
            return monitorMode != null && monitorMode == MonitorMode.JOB_EMAIL_NOTIFICATION_MONITOR;
        } else {
            return false;
        }
    }

    public static String listToCsv(List<String> listOfStrings, char separator) {
        StringBuilder sb = new StringBuilder();

        // all but last
        for (int i = 0; i < listOfStrings.size() - 1; i++) {
            sb.append(listOfStrings.get(i));
            sb.append(separator);
        }

        // last string, no separator
        if (!listOfStrings.isEmpty()) {
            sb.append(listOfStrings.getLast());
        }

        return sb.toString();
    }
}
