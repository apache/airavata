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
 *
*/

package org.apache.airavata.testsuite.multitenantedairavata;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.MessagingConstants;
import org.apache.airavata.messaging.core.impl.RabbitMQStatusConsumer;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.testsuite.multitenantedairavata.utils.FrameworkUtils;
import org.apache.airavata.testsuite.multitenantedairavata.utils.PropertyFileType;
import org.apache.airavata.testsuite.multitenantedairavata.utils.PropertyReader;
import org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExperimentExecution {
    private Airavata.Client airavata;
    private final static Logger logger = LoggerFactory.getLogger(ExperimentExecution.class);
    private Map<String, String> experimentsWithTokens;
    private Map<String, String> experimentsWithGateway;
    private Map<String, String> csTokens;
    private Map<String, Map<String, String>> appInterfaceMap;
    private Map<String, List<Project>> projectsMap;
    private PropertyReader propertyReader;
    private PrintWriter resultWriter;
    private String testUser;
    private List<String> gatewaysToAvoid;
    private TestFrameworkProps properties;
    private AuthzToken authzToken;

    public ExperimentExecution(Airavata.Client airavata,
                               Map<String, String> tokenMap,
                               TestFrameworkProps props) throws Exception {
        this.airavata = airavata;
        this.csTokens = tokenMap;
        authzToken = new AuthzToken("emptyToken");
        this.appInterfaceMap = getApplicationMap(tokenMap);
        this.propertyReader = new PropertyReader();
        this.properties = props;
        FrameworkUtils frameworkUtils = FrameworkUtils.getInstance();
        testUser = props.getTestUserName();
        gatewaysToAvoid = frameworkUtils.getGatewayListToAvoid(properties.getSkippedGateways());
        this.projectsMap = getProjects(tokenMap);
        this.experimentsWithTokens = new HashMap<String, String>();
        this.experimentsWithGateway = new HashMap<String, String>();
        String resultFileLocation = properties.getResultFileLoc();
        String resultFileName = resultFileLocation + getResultFileName();

        File resultFolder = new File(resultFileLocation);
        if (!resultFolder.exists()){
            resultFolder.mkdir();
        }
        File resultFile = new File(resultFileName);
        resultWriter = new PrintWriter(resultFile, "UTF-8");
        resultWriter.println("Test Framework Results");
        resultWriter.println("========================================");
    }

    public PrintWriter getResultWriter() {
        return resultWriter;
    }

    public void setResultWriter(PrintWriter resultWriter) {
        this.resultWriter = resultWriter;
    }

    protected Map<String, Map<String, String>> getApplicationMap (Map<String, String> tokenMap) throws  Exception{
        appInterfaceMap = new HashMap<String, Map<String, String>>();
        try {
            if (tokenMap != null && !tokenMap.isEmpty()){
                for (String gatewayId : tokenMap.keySet()){
                    Map<String, String> allApplicationInterfaceNames = airavata.getAllApplicationInterfaceNames(authzToken, gatewayId);
                    appInterfaceMap.put(gatewayId, allApplicationInterfaceNames);
                }
            }
        } catch (AiravataSystemException e) {
            logger.error("Error while getting application interfaces", e);
            throw new Exception("Error while getting application interfaces", e);
        } catch (InvalidRequestException e) {
            logger.error("Error while getting application interfaces", e);
            throw new Exception("Error while getting application interfaces", e);
        } catch (AiravataClientException e) {
            logger.error("Error while getting application interfaces", e);
            throw new Exception("Error while getting application interfaces", e);
        } catch (TException e) {
            logger.error("Error while getting application interfaces", e);
            throw new Exception("Error while getting application interfaces", e);
        }
        return appInterfaceMap;
    }

    protected Map<String, List<Project>> getProjects (Map<String, String> tokenMap) throws Exception{
        projectsMap = new HashMap<String, List<Project>>();
        try {
            if (tokenMap != null && !tokenMap.isEmpty()){
                for (String gatewayId : tokenMap.keySet()){
                    boolean isgatewayValid = true;
                    for (String ovoidGateway : gatewaysToAvoid){
                        if (gatewayId.equals(ovoidGateway)){
                            isgatewayValid = false;
                            break;
                        }
                    }
                    if (isgatewayValid){
                        List<Project> allUserProjects = airavata.getUserProjects(authzToken, gatewayId, testUser, 5, 0);
                        projectsMap.put(gatewayId, allUserProjects);
                    }
                }
            }
        } catch (AiravataSystemException e) {
            logger.error("Error while getting all user projects", e);
            throw new Exception("Error while getting all user projects", e);
        } catch (InvalidRequestException e) {
            logger.error("Error while getting all user projects", e);
            throw new Exception("Error while getting all user projects", e);
        } catch (AiravataClientException e) {
            logger.error("Error while getting all user projects", e);
            throw new Exception("Error while getting all user projects", e);
        } catch (TException e) {
            logger.error("Error while getting all user projects", e);
            throw new Exception("Error while getting all user projects", e);
        }
        return projectsMap;
    }

    public void launchExperiments () throws Exception {
        try {
            for (String expId : experimentsWithTokens.keySet()){
                airavata.launchExperiment(authzToken, expId, experimentsWithTokens.get(expId));
            }
        }catch (Exception e){
            logger.error("Error while launching experiment", e);
            throw new Exception("Error while launching experiment", e);
        }
    }

    public void monitorExperiments () throws Exception {

        String brokerUrl = propertyReader.readProperty(TestFrameworkConstants.AiravataClientConstants.RABBIT_BROKER_URL, PropertyFileType.AIRAVATA_CLIENT);
        System.out.println("broker url " + brokerUrl);
        final String exchangeName = propertyReader.readProperty(TestFrameworkConstants.AiravataClientConstants.RABBIT_EXCHANGE_NAME, PropertyFileType.AIRAVATA_CLIENT);
        RabbitMQStatusConsumer consumer = new RabbitMQStatusConsumer(brokerUrl, exchangeName);

        consumer.listen(new MessageHandler() {
            @Override
            public Map<String, Object> getProperties() {
                Map<String, Object> props = new HashMap<String, Object>();
                List<String> routingKeys = new ArrayList<String>();
                for (String expId : experimentsWithGateway.keySet()) {
                    String gatewayId = experimentsWithGateway.get(expId);
                    System.out.println("experiment Id : " + expId + " gateway Id : " + gatewayId);

                    routingKeys.add(gatewayId);
                    routingKeys.add(gatewayId + "." + expId);
                    routingKeys.add(gatewayId + "." + expId + ".*");
                    routingKeys.add(gatewayId + "." + expId + ".*.*");
                    routingKeys.add(gatewayId + "." + expId + ".*.*.*");
                }
                props.put(MessagingConstants.RABBIT_ROUTING_KEY, routingKeys);
                return props;
            }

            @Override
            public void onMessage(MessageContext message) {

                if (message.getType().equals(MessageType.EXPERIMENT)) {
                    try {
                        ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
                        TBase messageEvent = message.getEvent();
                        byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                        ThriftUtils.createThriftFromBytes(bytes, event);
                        ExperimentState expState = event.getState();
                        String expId = event.getExperimentId();
                        String gatewayId = event.getGatewayId();

                        if (expState.equals(ExperimentState.COMPLETED)) {
                            resultWriter.println("Results for experiment : " + expId + " of gateway Id : " + gatewayId);
                            resultWriter.println("=====================================================================");
                            resultWriter.println("Status : " + ExperimentState.COMPLETED.toString());
                            // check file transfers
                            List<OutputDataObjectType> experimentOutputs = airavata.getExperimentOutputs(authzToken, expId);
                            int i = 1;
                            for (OutputDataObjectType output : experimentOutputs) {
                                System.out.println("################ Experiment : " + expId + " COMPLETES ###################");
                                System.out.println("Output " + i + " : " + output.getValue());
                                resultWriter.println("Output " + i + " : " + output.getValue());
                                i++;
                            }
                            resultWriter.println("End of Results for Experiment : " + expId );
                            resultWriter.println("=====================================================================");
                        } else if (expState.equals(ExperimentState.FAILED)) {
                            resultWriter.println("Results for experiment : " + expId + " of gateway Id : " + gatewayId);
                            resultWriter.println("=====================================================================");
                            int j = 1;
                            resultWriter.println("Status : " + ExperimentState.FAILED.toString());
                            System.out.println("################ Experiment : " + expId + " FAILED ###################");
                            ExperimentModel experiment = airavata.getExperiment(authzToken, expId);
                            List<ErrorModel> errors = experiment.getErrors();
                            if (errors != null && !errors.isEmpty()){
                                for (ErrorModel errorDetails : errors) {
                                    System.out.println(errorDetails.getActualErrorMessage());
                                    resultWriter.println("Actual Error : " + j + " : " + errorDetails.getActualErrorMessage());
                                    resultWriter.println("User Friendly Message : " + j + " : " + errorDetails.getUserFriendlyMessage());
                                }
                            }

                            resultWriter.println("End of Results for Experiment : " + expId );
                            resultWriter.println("=====================================================================");
                        }
//                        System.out.println(" Experiment Id : '" + expId
//                                + "' with state : '" + event.getState().toString() +
//                                " for Gateway " + event.getGatewayId());
                    } catch (TException e) {
                        logger.error(e.getMessage(), e);
                    }
                } else if (message.getType().equals(MessageType.JOB)) {
                    try {
                        JobStatusChangeEvent event = new JobStatusChangeEvent();
                        TBase messageEvent = message.getEvent();
                        byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                        ThriftUtils.createThriftFromBytes(bytes, event);
//                        System.out.println(" Job ID : '" + event.getJobIdentity().getJobId()
//                                + "' with state : '" + event.getState().toString() +
//                                " for Gateway " + event.getJobIdentity().getGatewayId());
//                        resultWriter.println("Job Status : " + event.getState().toString());

                    } catch (TException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            resultWriter.flush();
            }
        });
    }

    private String getResultFileName (){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    public void createAmberWithErrorInputs (String gatewayId,
                                            String token,
                                            String projectId,
                                            String hostId,
                                            String appId) throws Exception {
        try {
            List<InputDataObjectType> applicationInputs = airavata.getApplicationInputs(authzToken, appId);
            List<OutputDataObjectType> appOutputs = airavata.getApplicationOutputs(authzToken, appId);
            TestFrameworkProps.Error[] errors = properties.getErrors();
            for (TestFrameworkProps.Error error : errors) {
                String name = error.getName();
                String hostName = error.getResoureName();
                if (name.equals(TestFrameworkConstants.ErrorTypeConstants.BADINPUTS)) {
                    if (error.getApplication().equals(TestFrameworkConstants.AppcatalogConstants.AMBER_APP_NAME)) {
                        Map<String, String> userGivenErrorInputs = error.getErrorFeeds();
                        for (String inputName : userGivenErrorInputs.keySet()) {
                            for (InputDataObjectType inputDataObjectType : applicationInputs) {
                                if (inputDataObjectType.getName().equalsIgnoreCase(inputName)) {
                                    inputDataObjectType.setValue(userGivenErrorInputs.get(inputName));
                                }
                            }
                        }
                        ExperimentModel simpleExperiment =
                                ExperimentModelUtil.createSimpleExperiment(gatewayId, projectId, testUser, "AmberErrorInputs", "Amber Experiment run", appId, applicationInputs);
                        simpleExperiment.setExperimentOutputs(appOutputs);
                        String experimentId;
                        if (hostName.equals(TestFrameworkConstants.AppcatalogConstants.TRESTLES_RESOURCE_NAME)) {
                            ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(hostId, 4, 1, 1, "normal", 20, 0);
                            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                            userConfigurationData.setAiravataAutoSchedule(false);
                            userConfigurationData.setOverrideManualScheduledParams(false);
                            userConfigurationData.setComputationalResourceScheduling(scheduling);
                            simpleExperiment.setUserConfigurationData(userConfigurationData);
                            experimentId = airavata.createExperiment(authzToken, gatewayId, simpleExperiment);
                            experimentsWithTokens.put(experimentId, token);
                            experimentsWithGateway.put(experimentId, gatewayId);
                        } else if (hostName.equals(TestFrameworkConstants.AppcatalogConstants.STAMPEDE_RESOURCE_NAME)) {
                            ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(hostId, 4, 1, 1, "normal", 20, 0);
                            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                            userConfigurationData.setAiravataAutoSchedule(false);
                            userConfigurationData.setOverrideManualScheduledParams(false);
                            userConfigurationData.setComputationalResourceScheduling(scheduling);
                            simpleExperiment.setUserConfigurationData(userConfigurationData);
                            experimentId = airavata.createExperiment(authzToken, gatewayId, simpleExperiment);
                            experimentsWithTokens.put(experimentId, token);
                            experimentsWithGateway.put(experimentId, gatewayId);
                        } else if (hostName.equals(TestFrameworkConstants.AppcatalogConstants.BR2_RESOURCE_NAME)) {
                            ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(hostId, 4, 1, 1, "cpu", 20, 0);
                            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                            userConfigurationData.setAiravataAutoSchedule(false);
                            userConfigurationData.setOverrideManualScheduledParams(false);
                            userConfigurationData.setComputationalResourceScheduling(scheduling);
                            simpleExperiment.setUserConfigurationData(userConfigurationData);
                            experimentId = airavata.createExperiment(authzToken, gatewayId, simpleExperiment);
                            experimentsWithTokens.put(experimentId, token);
                            experimentsWithGateway.put(experimentId, gatewayId);
                        }

                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error occured while creating amber experiment with bad inputs", e);
            throw new Exception("Error occured while creating amber experiment with bad inputs", e);
        }
    }

    public void createAmberWithErrorUserConfig (String gatewayId,
                                            String token,
                                            String projectId,
                                            String hostId,
                                            String appId) throws Exception {
        try {

            TestFrameworkProps.Error[] errors = properties.getErrors();
            for (TestFrameworkProps.Error error : errors) {
                String name = error.getName();
                String hostName = error.getResoureName();
                if (name.equals(TestFrameworkConstants.ErrorTypeConstants.ERROR_CONFIG)) {
                    if (error.getApplication().equals(TestFrameworkConstants.AppcatalogConstants.AMBER_APP_NAME)) {
                        List<InputDataObjectType> applicationInputs = airavata.getApplicationInputs(authzToken, appId);
                        List<OutputDataObjectType> appOutputs = airavata.getApplicationOutputs(authzToken, appId);
                        TestFrameworkProps.Application[] applications = properties.getApplications();
                        Map<String, String> userGivenAmberInputs = new HashMap<>();
                        for (TestFrameworkProps.Application application : applications) {
                            if (application.getName().equals(TestFrameworkConstants.AppcatalogConstants.AMBER_APP_NAME)) {
                                userGivenAmberInputs = application.getInputs();
                            }
                        }
                        for (String inputName : userGivenAmberInputs.keySet()) {
                            for (InputDataObjectType inputDataObjectType : applicationInputs) {
                                if (inputDataObjectType.getName().equalsIgnoreCase(inputName)) {
                                    inputDataObjectType.setValue(userGivenAmberInputs.get(inputName));
                                }
                            }
                        }
                        Map<String, String> errorConfigs = error.getErrorFeeds();
                        String allocationProject = null;
                        String queueName = null;
                        Integer walltime = 0;
                        String host = null;
                        for (String configName : errorConfigs.keySet()) {
                            if (configName.equals(TestFrameworkConstants.ErrorTypeConstants.ALLOCATION_PROJECT)) {
                                allocationProject = errorConfigs.get(configName);
                            } else if (configName.equals(TestFrameworkConstants.ErrorTypeConstants.QUEUE_NAME)) {
                                queueName = errorConfigs.get(configName);
                            } else if (configName.equals(TestFrameworkConstants.ErrorTypeConstants.WALLTIME)) {
                                walltime = Integer.valueOf(errorConfigs.get(configName));
                            } else if (configName.equals(TestFrameworkConstants.ErrorTypeConstants.HOST_NAME)) {
                                host = errorConfigs.get(configName);
                            }
                        }

                        ExperimentModel simpleExperiment =
                                ExperimentModelUtil.createSimpleExperiment(gatewayId, projectId, testUser, "AmberErrorConfigs", "Amber Experiment run", appId, applicationInputs);
                        simpleExperiment.setExperimentOutputs(appOutputs);
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(hostId, 4, 1, 1, queueName, walltime, 0);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);

                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        String experimentId = airavata.createExperiment(authzToken, gatewayId, simpleExperiment);
                        experimentsWithTokens.put(experimentId, token);
                        experimentsWithGateway.put(experimentId, gatewayId);

                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error occured while creating amber experiment with bad inputs", e);
            throw new Exception("Error occured while creating amber experiment with bad inputs", e);
        }
    }

    public void createAmberExperiment () throws Exception{
        try {
            TestFrameworkProps.Application[] applications = properties.getApplications();
            Map<String, String> userGivenAmberInputs = new HashMap<>();
            for (TestFrameworkProps.Application application : applications){
                if (application.getName().equals(TestFrameworkConstants.AppcatalogConstants.AMBER_APP_NAME)){
                    userGivenAmberInputs = application.getInputs();
                }
            }

            for (String gatewayId : csTokens.keySet()){
                String token = csTokens.get(gatewayId);
                Map<String, String> appsWithNames = appInterfaceMap.get(gatewayId);
                for (String appId : appsWithNames.keySet()){
                    String appName = appsWithNames.get(appId);
                    if (appName.equals(TestFrameworkConstants.AppcatalogConstants.AMBER_APP_NAME)){
                        List<InputDataObjectType> applicationInputs = airavata.getApplicationInputs(authzToken, appId);
                        List<OutputDataObjectType> appOutputs = airavata.getApplicationOutputs(authzToken, appId);
                        for (String inputName : userGivenAmberInputs.keySet()){
                            for (InputDataObjectType inputDataObjectType : applicationInputs) {
                                if (inputDataObjectType.getName().equalsIgnoreCase(inputName)) {
                                    inputDataObjectType.setValue(userGivenAmberInputs.get(inputName));
                                }
                            }
                        }
                        List<Project> projectsPerGateway = projectsMap.get(gatewayId);
                        String projectID = null;
                        if (projectsPerGateway != null && !projectsPerGateway.isEmpty()){
                            projectID = projectsPerGateway.get(0).getProjectID();
                        }
                        ExperimentModel simpleExperiment =
                                ExperimentModelUtil.createSimpleExperiment(gatewayId, projectID, testUser, "Amber Experiment", "Amber Experiment run", appId, applicationInputs);
                        simpleExperiment.setExperimentOutputs(appOutputs);
                        String experimentId;
                        Map<String, String> computeResources = airavata.getAvailableAppInterfaceComputeResources(authzToken, appId);
                        if (computeResources != null && computeResources.size() != 0) {
                            for (String id : computeResources.keySet()) {
                                String resourceName = computeResources.get(id);
                                if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.TRESTLES_RESOURCE_NAME)) {
                                    ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 20, 0);
                                    UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                                    userConfigurationData.setAiravataAutoSchedule(false);
                                    userConfigurationData.setOverrideManualScheduledParams(false);
                                    userConfigurationData.setComputationalResourceScheduling(scheduling);
                                    simpleExperiment.setUserConfigurationData(userConfigurationData);
                                    experimentId = airavata.createExperiment(authzToken, gatewayId, simpleExperiment);
                                    experimentsWithTokens.put(experimentId, token);
                                    experimentsWithGateway.put(experimentId, gatewayId);
                                }else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.STAMPEDE_RESOURCE_NAME)) {
                                    ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 20, 0);
                                    UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                                    userConfigurationData.setAiravataAutoSchedule(false);
                                    userConfigurationData.setOverrideManualScheduledParams(false);
                                    userConfigurationData.setComputationalResourceScheduling(scheduling);
                                    simpleExperiment.setUserConfigurationData(userConfigurationData);
                                    experimentId = airavata.createExperiment(authzToken, gatewayId, simpleExperiment);
                                    experimentsWithTokens.put(experimentId, token);
                                    experimentsWithGateway.put(experimentId, gatewayId);
                                    createAmberWithErrorInputs(gatewayId, token, projectID, id, appId);
                                    createAmberWithErrorUserConfig(gatewayId, token, projectID, id, appId);
                                } else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.BR2_RESOURCE_NAME)) {
                                    ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "cpu", 20, 0);
                                    UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                                    userConfigurationData.setAiravataAutoSchedule(false);
                                    userConfigurationData.setOverrideManualScheduledParams(false);
                                    userConfigurationData.setComputationalResourceScheduling(scheduling);
                                    simpleExperiment.setUserConfigurationData(userConfigurationData);
                                    experimentId = airavata.createExperiment(authzToken, gatewayId, simpleExperiment);
                                    experimentsWithTokens.put(experimentId, token);
                                    experimentsWithGateway.put(experimentId, gatewayId);
                                }
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            logger.error("Error while creating AMBEr experiment", e);
            throw new Exception("Error while creating AMBER experiment", e);
        }
    }

    public void createUltrascanExperiment () throws Exception{
        try {
            TestFrameworkProps.Application[] applications = properties.getApplications();
            int numberOfIterations = properties.getNumberOfIterations();
            Map<String, String> userGivenAmberInputs = new HashMap<>();
            for (TestFrameworkProps.Application application : applications){
                if (application.getName().equals(TestFrameworkConstants.AppcatalogConstants.ULTRASCAN)){
                    userGivenAmberInputs = application.getInputs();
                }
            }

            for (int i=0; i < numberOfIterations; i++){
                for (String gatewayId : csTokens.keySet()){
                    String token = csTokens.get(gatewayId);
                    Map<String, String> appsWithNames = appInterfaceMap.get(gatewayId);
                    for (String appId : appsWithNames.keySet()){
                        String appName = appsWithNames.get(appId);
                        if (appName.equals(TestFrameworkConstants.AppcatalogConstants.ULTRASCAN)){
                            List<InputDataObjectType> applicationInputs = airavata.getApplicationInputs(authzToken, appId);
                            List<OutputDataObjectType> appOutputs = airavata.getApplicationOutputs(authzToken, appId);
                            for (String inputName : userGivenAmberInputs.keySet()){
                                for (InputDataObjectType inputDataObjectType : applicationInputs) {
                                    if (inputDataObjectType.getName().equalsIgnoreCase(inputName)) {
                                        inputDataObjectType.setValue(userGivenAmberInputs.get(inputName));
                                    }
                                }
                            }
                            List<Project> projectsPerGateway = projectsMap.get(gatewayId);
                            String projectID = null;
                            if (projectsPerGateway != null && !projectsPerGateway.isEmpty()){
                                projectID = projectsPerGateway.get(0).getProjectID();
                            }
                            ExperimentModel simpleExperiment =
                                    ExperimentModelUtil.createSimpleExperiment(gatewayId, projectID, testUser, "TestFR_Ultrascan_Experiment", "Ultrascan Experiment run", appId, applicationInputs);
                            simpleExperiment.setExperimentOutputs(appOutputs);
                            String experimentId;
                            Map<String, String> computeResources = airavata.getAvailableAppInterfaceComputeResources(authzToken, appId);
                            if (computeResources != null && computeResources.size() != 0) {
                                for (String id : computeResources.keySet()) {
                                    String resourceName = computeResources.get(id);
                                    if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.STAMPEDE_RESOURCE_NAME)) {
                                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 30, 0);
                                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                                        userConfigurationData.setAiravataAutoSchedule(false);
                                        userConfigurationData.setOverrideManualScheduledParams(false);
                                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                                        experimentId = airavata.createExperiment(authzToken, gatewayId, simpleExperiment);
                                        experimentsWithTokens.put(experimentId, token);
                                        experimentsWithGateway.put(experimentId, gatewayId);
                                    }else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.ALAMO_RESOURCE_NAME)) {
                                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "batch", 30, 0);
                                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                                        userConfigurationData.setAiravataAutoSchedule(false);
                                        userConfigurationData.setOverrideManualScheduledParams(false);
                                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                                        experimentId = airavata.createExperiment(authzToken, gatewayId, simpleExperiment);
                                        experimentsWithTokens.put(experimentId, token);
                                        experimentsWithGateway.put(experimentId, gatewayId);
                                    }else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.GORDEN_RESOURCE_NAME)) {
                                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 30, 0);
                                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                                        userConfigurationData.setAiravataAutoSchedule(false);
                                        userConfigurationData.setOverrideManualScheduledParams(false);
                                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                                        experimentId = airavata.createExperiment(authzToken, gatewayId, simpleExperiment);
                                        experimentsWithTokens.put(experimentId, token);
                                        experimentsWithGateway.put(experimentId, gatewayId);
                                    }else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.COMET_RESOURCE_NAME)) {
                                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "compute", 30, 0);
                                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                                        userConfigurationData.setAiravataAutoSchedule(false);
                                        userConfigurationData.setOverrideManualScheduledParams(false);
                                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                                        experimentId = airavata.createExperiment(authzToken,gatewayId, simpleExperiment);
                                        experimentsWithTokens.put(experimentId, token);
                                        experimentsWithGateway.put(experimentId, gatewayId);
                                    }else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.LONESTAR_RESOURCE_NAME)) {
                                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 30, 0);
                                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                                        userConfigurationData.setAiravataAutoSchedule(false);
                                        userConfigurationData.setOverrideManualScheduledParams(false);
                                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                                        experimentId = airavata.createExperiment(authzToken, gatewayId, simpleExperiment);
                                        experimentsWithTokens.put(experimentId, token);
                                        experimentsWithGateway.put(experimentId, gatewayId);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }catch (Exception e){
            logger.error("Error while creating Ultrascan experiment", e);
            throw new Exception("Error while creating Ultrascan experiment", e);
        }
    }


    public void createEchoExperiment () throws Exception{
        try {
            for (String gatewayId : csTokens.keySet()) {
                    boolean isgatewayValid = true;
                    for (String ovoidGateway : gatewaysToAvoid){
                        if (gatewayId.equals(ovoidGateway)){
                            isgatewayValid = false;
                            break;
                        }
                    }
                    if (isgatewayValid) {
                        String token = csTokens.get(gatewayId);
                        Map<String, String> appsWithNames = appInterfaceMap.get(gatewayId);
                        for (String appId : appsWithNames.keySet()) {
                            String appName = appsWithNames.get(appId);
                            if (appName.equals(TestFrameworkConstants.AppcatalogConstants.ECHO_NAME)) {
                                List<InputDataObjectType> applicationInputs = airavata.getApplicationInputs(authzToken, appId);
                                List<OutputDataObjectType> appOutputs = airavata.getApplicationOutputs(authzToken, appId);
                                for (InputDataObjectType inputDataObjectType : applicationInputs) {
                                    if (inputDataObjectType.getName().equalsIgnoreCase("input_to_Echo")) {
                                        inputDataObjectType.setValue("Hello World !!!");
                                    }
                                }

                                List<Project> projectsPerGateway = projectsMap.get(gatewayId);
                                String projectID = null;
                                if (projectsPerGateway != null && !projectsPerGateway.isEmpty()) {
                                    projectID = projectsPerGateway.get(0).getProjectID();
                                }
                                ExperimentModel simpleExperiment =
                                        ExperimentModelUtil.createSimpleExperiment(gatewayId, projectID, "admin", "Echo Experiment", "Echo Experiment run", appId, applicationInputs);
                                simpleExperiment.setExperimentOutputs(appOutputs);
                                String experimentId;
                                Map<String, String> computeResources = airavata.getAvailableAppInterfaceComputeResources(authzToken, appId);
                                if (computeResources != null && computeResources.size() != 0) {
                                    for (String id : computeResources.keySet()) {
                                        String resourceName = computeResources.get(id);
                                        if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.TRESTLES_RESOURCE_NAME)) {
                                            ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 20, 0);
                                            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                                            userConfigurationData.setAiravataAutoSchedule(false);
                                            userConfigurationData.setOverrideManualScheduledParams(false);
                                            userConfigurationData.setComputationalResourceScheduling(scheduling);
                                            simpleExperiment.setUserConfigurationData(userConfigurationData);
                                            experimentId = airavata.createExperiment(authzToken, gatewayId, simpleExperiment);
                                            experimentsWithTokens.put(experimentId, token);
                                            experimentsWithGateway.put(experimentId, gatewayId);
                                        } else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.STAMPEDE_RESOURCE_NAME)) {
                                            ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 20, 0);
                                            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                                            userConfigurationData.setAiravataAutoSchedule(false);
                                            userConfigurationData.setOverrideManualScheduledParams(false);
                                            userConfigurationData.setComputationalResourceScheduling(scheduling);
                                            simpleExperiment.setUserConfigurationData(userConfigurationData);
                                            experimentId = airavata.createExperiment(authzToken, gatewayId, simpleExperiment);
                                            experimentsWithTokens.put(experimentId, token);
                                            experimentsWithGateway.put(experimentId, gatewayId);
                                        } else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.BR2_RESOURCE_NAME)) {
                                            ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "cpu", 20, 0);
                                            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                                            userConfigurationData.setAiravataAutoSchedule(false);
                                            userConfigurationData.setOverrideManualScheduledParams(false);
                                            userConfigurationData.setComputationalResourceScheduling(scheduling);
                                            simpleExperiment.setUserConfigurationData(userConfigurationData);
                                            experimentId = airavata.createExperiment(authzToken, gatewayId, simpleExperiment);
                                            experimentsWithTokens.put(experimentId, token);
                                            experimentsWithGateway.put(experimentId, gatewayId);
                                        }
                                    }
                                }
                            }
                        }
                    }

            }
        }catch (Exception e){
            logger.error("Error while creating Echo experiment", e);
            throw new Exception("Error while creating Echo experiment", e);
        }
    }
}
