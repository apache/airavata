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

import groovy.text.GStringTemplateEngine;
import groovy.text.TemplateEngine;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.TaskOnFailException;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.CommandObject;
import org.apache.airavata.model.appcatalog.appdeployment.SetEnvPaths;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.task.JobSubmissionTaskModel;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GroovyMapBuilder {

    private final static Logger logger = LoggerFactory.getLogger(GroovyMapBuilder.class);

    public static final String MULTIPLE_INPUTS_SPLITTER = ",";

    private TaskContext taskContext;

    public GroovyMapBuilder(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    public GroovyMapData build() throws Exception {
        GroovyMapData mapData = new GroovyMapData();

        setMailAddresses(taskContext, mapData);
        mapData.setInputDir(taskContext.getInputDir());
        mapData.setOutputDir(taskContext.getOutputDir());
        mapData.setExecutablePath(taskContext.getApplicationDeploymentDescription().getExecutablePath());
        mapData.setStdoutFile(taskContext.getStdoutLocation());
        mapData.setStderrFile(taskContext.getStderrLocation());
        mapData.setScratchLocation(taskContext.getScratchLocation());
        mapData.setGatewayId(taskContext.getGatewayId());
        mapData.setGatewayUserName(taskContext.getProcessModel().getUserName());
        mapData.setApplicationName(taskContext.getApplicationInterfaceDescription().getApplicationName());
        mapData.setQueueSpecificMacros(taskContext.getQueueSpecificMacros());
        mapData.setAccountString(taskContext.getAllocationProjectNumber());
        mapData.setReservation(taskContext.getReservation());
        mapData.setJobName("A" + String.valueOf(generateJobName()));
        mapData.setWorkingDirectory(taskContext.getWorkingDir());
        mapData.setTaskId(taskContext.getTaskId());
        mapData.setExperimentDataDir(taskContext.getProcessModel().getExperimentDataDir());

        SimpleDateFormat gmtDateFormat = new SimpleDateFormat("yyyy-MM-dd+HH:mmZ");
        gmtDateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
        mapData.setCurrentTime(gmtDateFormat.format(new Date()));

        //List<String> emails = taskContext.getUserProfile().getEmails();
        //if (emails != null && emails.size() > 0) {
        //    mapData.setGatewayUserEmail(emails.get(0));
        //}

        List<String> inputValues = getProcessInputValues(taskContext.getProcessModel().getProcessInputs(), true);
        inputValues.addAll(getProcessOutputValues(taskContext.getProcessModel().getProcessOutputs(), true));
        mapData.setInputs(inputValues);

        List<String> inputFiles = getProcessInputFiles(taskContext.getProcessModel().getProcessInputs(), false);
        mapData.setInputFiles(inputFiles);

        List<String> inputValuesAll = getProcessInputValues(taskContext.getProcessModel().getProcessInputs(), false);
        inputValuesAll.addAll(getProcessOutputValues(taskContext.getProcessModel().getProcessOutputs(), false));
        mapData.setInputsAll(inputValuesAll);

        mapData.setUserName(taskContext.getComputeResourceLoginUserName());
        mapData.setShellName("/bin/bash");

        String hostName = taskContext.getComputeResourceDescription().getHostName();
        List<String> hostAliases = taskContext.getComputeResourceDescription().getHostAliases();
        if (hostAliases != null && hostAliases.size() > 0) {
            hostName = hostAliases.get(0);
        }
        mapData.setComputeHostName(hostName);

        if (taskContext != null) {
            try {
                JobSubmissionTaskModel jobSubmissionTaskModel = ((JobSubmissionTaskModel) taskContext.getSubTaskModel());
                if (jobSubmissionTaskModel.getWallTime() > 0) {
                    mapData.setMaxWallTime(maxWallTimeCalculator(jobSubmissionTaskModel.getWallTime()));
                    // TODO fix this
                    /*if (resourceJobManager != null) {
                        if (resourceJobManager.getResourceJobManagerType().equals(ResourceJobManagerType.LSF)) {
                            groovyMap.add(Script.MAX_WALL_TIME,
                                    GFacUtils.maxWallTimeCalculatorForLSF(jobSubmissionTaskModel.getWallTime()));
                        }
                    }*/
                }
            } catch (TException e) {
                logger.error("Error while getting job submission sub task model", e);
            }
        }

        // NOTE: Give precedence to data comes with experiment
        // qos per queue
        String qoS = getQoS(taskContext.getQualityOfService(), taskContext.getQueueName());
        if (qoS != null) {
            mapData.setQualityOfService(qoS);
        }
        ComputationalResourceSchedulingModel scheduling = taskContext.getProcessModel().getProcessResourceSchedule();
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
                mapData.setMaxWallTime(maxWallTimeCalculator(scheduling.getWallTimeLimit()));

                // TODO fix this
                /*
                if (resourceJobManager != null) {
                    if (resourceJobManager.getResourceJobManagerType().equals(ResourceJobManagerType.LSF)) {
                        mapData.setMaxWallTime(maxWallTimeCalculatorForLSF(scheduling.getWallTimeLimit()));
                    }
                }
                */
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

        ApplicationDeploymentDescription appDepDescription = taskContext.getApplicationDeploymentDescription();

        List<SetEnvPaths> exportCommands = appDepDescription.getSetEnvironment();
        if (exportCommands != null) {
            List<String> exportCommandList = exportCommands.stream()
                    .sorted((e1, e2) -> e1.getEnvPathOrder() - e2.getEnvPathOrder())
                    .map(map -> map.getName() + "=" + map.getValue())
                    .collect(Collectors.toList());
            mapData.setExports(exportCommandList);
        }

        List<CommandObject> moduleCmds = appDepDescription.getModuleLoadCmds();
        if (moduleCmds != null) {
            List<String> modulesCmdCollect = moduleCmds.stream()
                    .sorted((e1, e2) -> e1.getCommandOrder() - e2.getCommandOrder())
                    .map(map -> parseCommands(map.getCommand(), mapData))
                    .collect(Collectors.toList());
            mapData.setModuleCommands(modulesCmdCollect);
        }

        List<CommandObject> preJobCommands = appDepDescription.getPreJobCommands();
        if (preJobCommands != null) {
            List<String> preJobCmdCollect = preJobCommands.stream()
                    .sorted((e1, e2) -> e1.getCommandOrder() - e2.getCommandOrder())
                    .map(map -> parseCommands(map.getCommand(), mapData))
                    .collect(Collectors.toList());
            mapData.setPreJobCommands(preJobCmdCollect);
        }

        List<CommandObject> postJobCommands = appDepDescription.getPostJobCommands();
        if (postJobCommands != null) {
            List<String> postJobCmdCollect = postJobCommands.stream()
                    .sorted((e1, e2) -> e1.getCommandOrder() - e2.getCommandOrder())
                    .map(map -> parseCommands(map.getCommand(), mapData))
                    .collect(Collectors.toList());
            mapData.setPostJobCommands(postJobCmdCollect);
        }

        ApplicationParallelismType parallelism = appDepDescription.getParallelism();
        if (parallelism != null) {
            if (parallelism != ApplicationParallelismType.SERIAL) {
                Map<ApplicationParallelismType, String> parallelismPrefix = taskContext.getResourceJobManager().getParallelismPrefix();
                if (parallelismPrefix != null){
                    String parallelismCommand = parallelismPrefix.get(parallelism);
                    if (parallelismCommand != null){
                        mapData.setJobSubmitterCommand(parallelismCommand);
                    }else {
                        throw new Exception("Parallelism prefix is not defined for given parallelism type " + parallelism + ".. Please define the parallelism prefix at App Catalog");
                    }
                }
            }
        }

        return mapData;
    }

    public static int generateJobName() {
        Random random = new Random();
        int i = random.nextInt(Integer.MAX_VALUE);
        i = i + 99999999;
        if (i < 0) {
            i = i * (-1);
        }
        return i;
    }

    private static List<String> getProcessInputValues(List<InputDataObjectType> processInputs, boolean commandLineOnly) {
        List<String> inputValues = new ArrayList<String>();
        if (processInputs != null) {

            // sort the inputs first and then build the command ListR
            Comparator<InputDataObjectType> inputOrderComparator = new Comparator<InputDataObjectType>() {
                @Override
                public int compare(InputDataObjectType inputDataObjectType, InputDataObjectType t1) {
                    return inputDataObjectType.getInputOrder() - t1.getInputOrder();
                }
            };
            Set<InputDataObjectType> sortedInputSet = new TreeSet<InputDataObjectType>(inputOrderComparator);
            for (InputDataObjectType input : processInputs) {
                sortedInputSet.add(input);
            }
            for (InputDataObjectType inputDataObjectType : sortedInputSet) {
                if (commandLineOnly && !inputDataObjectType.isRequiredToAddedToCommandLine()) {
                    continue;
                }

                if (!inputDataObjectType.isIsRequired() &&
                        (inputDataObjectType.getValue() == null || "".equals(inputDataObjectType.getValue()))) {
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
                            filePath = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1, filePath.length());
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
        List<String> inputFiles = new ArrayList<String>();
        if (processInputs != null) {

            // sort the inputs first and then build the command ListR
            Comparator<InputDataObjectType> inputOrderComparator = new Comparator<InputDataObjectType>() {
                @Override
                public int compare(InputDataObjectType inputDataObjectType, InputDataObjectType t1) {
                    return inputDataObjectType.getInputOrder() - t1.getInputOrder();
                }
            };
            Set<InputDataObjectType> sortedInputSet = new TreeSet<InputDataObjectType>(inputOrderComparator);
            for (InputDataObjectType input : processInputs) {
                sortedInputSet.add(input);
            }
            for (InputDataObjectType inputDataObjectType : sortedInputSet) {
                if (!inputDataObjectType.isIsRequired() &&
                        (inputDataObjectType.getValue() == null || "".equals(inputDataObjectType.getValue()))) {
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
                            filePath = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1, filePath.length());
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

    private static List<String> getProcessOutputValues(List<OutputDataObjectType> processOutputs, boolean commandLineOnly) {
        List<String> inputValues = new ArrayList<>();
        if (processOutputs != null) {
            for (OutputDataObjectType output : processOutputs) {
                if (output.getApplicationArgument() != null
                        && !output.getApplicationArgument().equals("")) {
                    inputValues.add(output.getApplicationArgument());
                }
                if(commandLineOnly){
                    if (output.getValue() != null && !output.getValue().equals("") && output.isRequiredToAddedToCommandLine()) {
                        if (output.getType() == DataType.URI) {
                            String filePath = output.getValue();
                            filePath = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1, filePath.length());
                            inputValues.add(filePath);
                        }
                    }
                }else{
                    if (output.getValue() != null && !output.getValue().equals("")) {
                        if (output.getType() == DataType.URI) {
                            String filePath = output.getValue();
                            filePath = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1, filePath.length());
                            inputValues.add(filePath);
                        }
                    }
                }

            }
        }
        return inputValues;
    }

    static String getQoS(String qualityOfService, String preferredBatchQueue) {
        if(preferredBatchQueue == null  || preferredBatchQueue.isEmpty()
                ||  qualityOfService == null  || qualityOfService.isEmpty()) return null;
        final String qos = "qos";
        Pattern pattern = Pattern.compile(preferredBatchQueue + "=(?<" + qos + ">[^,]*)");
        Matcher matcher = pattern.matcher(qualityOfService);
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
        TemplateEngine templateEngine = new GStringTemplateEngine();
        try {
            return templateEngine.createTemplate(value).make(bindMap.toImmutableMap()).toString();
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalArgumentException("Error while parsing command " + value
                    + " , Invalid command or incomplete bind map");
        }
    }

    private static void setMailAddresses(TaskContext taskContext, GroovyMapData groovyMap) throws Exception {

        ProcessModel processModel =  taskContext.getProcessModel();
        String emailIds = null;
        if (isEmailBasedJobMonitor(taskContext)) {
            emailIds = ServerSettings.getEmailBasedMonitorAddress();
        }
        if (ServerSettings.getSetting(ServerSettings.JOB_NOTIFICATION_ENABLE).equalsIgnoreCase("true")) {
            String userJobNotifEmailIds = ServerSettings.getSetting(ServerSettings.JOB_NOTIFICATION_EMAILIDS);
            if (userJobNotifEmailIds != null && !userJobNotifEmailIds.isEmpty()) {
                if (emailIds != null && !emailIds.isEmpty()) {
                    emailIds += ("," + userJobNotifEmailIds);
                } else {
                    emailIds = userJobNotifEmailIds;
                }
            }
            if (processModel.isEnableEmailNotification()) {
                List<String> emailList = processModel.getEmailAddresses();
                if (emailList == null) {
                    throw new TaskOnFailException("At least one email should be provided as the email notification is turned on", false, null);
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

    public static boolean isEmailBasedJobMonitor(TaskContext taskContext) throws Exception {
        JobSubmissionProtocol jobSubmissionProtocol = taskContext.getPreferredJobSubmissionProtocol();
        JobSubmissionInterface jobSubmissionInterface = taskContext.getPreferredJobSubmissionInterface();
        if (jobSubmissionProtocol == JobSubmissionProtocol.SSH) {
            String jobSubmissionInterfaceId = jobSubmissionInterface.getJobSubmissionInterfaceId();
            SSHJobSubmission sshJobSubmission = taskContext.getRegistryClient().getSSHJobSubmission(jobSubmissionInterfaceId);
            MonitorMode monitorMode = sshJobSubmission.getMonitorMode();
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
        if (listOfStrings.size() > 0) {
            sb.append(listOfStrings.get(listOfStrings.size() - 1));
        }

        return sb.toString();
    }

}
