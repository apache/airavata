/**
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
package org.apache.airavata.testsuite.multitenantedairavata;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.common.logging.MDCConstants;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.*;
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
import org.apache.airavata.model.status.ExperimentStatus;
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
import org.slf4j.MDC;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants.LocalEchoProperties.LOCAL_ECHO_EXPERIMENT_INPUT;
import static org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants.LocalEchoProperties.LocalApplication.INPUT_NAME;

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
    private TestFrameworkProps properties;
    private AuthzToken authzToken;

    public ExperimentExecution(Airavata.Client airavata,
                               Map<String, String> tokenMap,
                               TestFrameworkProps props) throws Exception {
        this.airavata = airavata;
        this.csTokens = tokenMap;
        authzToken = new AuthzToken("emptyToken");
        authzToken.setClaimsMap(new HashMap<>());
        this.appInterfaceMap = getApplicationMap(tokenMap);
        this.propertyReader = new PropertyReader();
        this.properties = props;
        authzToken.getClaimsMap().put(org.apache.airavata.common.utils.Constants.USER_NAME, props.getTestUserName());
        authzToken.getClaimsMap().put(org.apache.airavata.common.utils.Constants.GATEWAY_ID, props.getGname());
        FrameworkUtils frameworkUtils = FrameworkUtils.getInstance();
        testUser = props.getTestUserName();
        this.projectsMap = getProjects(tokenMap);
        this.experimentsWithTokens = new HashMap<String, String>();
        this.experimentsWithGateway = new HashMap<String, String>();
        String resultFileLocation = properties.getResultFileLoc();
        String resultFileName = resultFileLocation + getResultFileName();

        File resultFolder = new File(resultFileLocation);
        if (!resultFolder.exists()) {
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

    protected Map<String, Map<String, String>> getApplicationMap(Map<String, String> tokenMap) throws Exception {
        appInterfaceMap = new HashMap<String, Map<String, String>>();
        try {
            if (tokenMap != null && !tokenMap.isEmpty()) {
                for (String gatewayId : tokenMap.keySet()) {
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

    protected Map<String, List<Project>> getProjects(Map<String, String> tokenMap) throws Exception {
        projectsMap = new HashMap<String, List<Project>>();
        try {
            if (tokenMap != null && !tokenMap.isEmpty()) {
                for (String gatewayId : tokenMap.keySet()) {

                    List<Project> allUserProjects = airavata.getUserProjects(authzToken, gatewayId, testUser, 5, 0);
                    projectsMap.put(gatewayId, allUserProjects);

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


    public void launchExperiments(String gatewayId, String experimentId) throws Exception {
        try {
            airavata.launchExperiment(authzToken, experimentId, gatewayId);

        } catch (Exception e) {
            logger.error("Error while launching experiment", e);
            throw new Exception("Error while launching experiment", e);
        }
    }

    public void monitorExperiments() throws Exception {

        String brokerUrl = propertyReader.readProperty(TestFrameworkConstants.AiravataClientConstants.RABBIT_BROKER_URL, PropertyFileType.AIRAVATA_SERVER);
        System.out.println("broker url " + brokerUrl);
        final String exchangeName = propertyReader.readProperty(TestFrameworkConstants.AiravataClientConstants.RABBIT_EXCHANGE_NAME, PropertyFileType.AIRAVATA_SERVER);
        //Subscriber statusSubscriber = MessagingFactory.getSubscriber(this::processMessage, getRoutingKeys(), Type.STATUS);
        Subscriber statusSubscriber = MessagingFactory.getSubscriber(new ExperimentHandler(), getRoutingKeys(), Type.STATUS);

    }

    private List<String> getRoutingKeys() {
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
        return routingKeys;
    }

    private class ExperimentHandler implements MessageHandler {

        @Override
        public void onMessage(MessageContext messageContext) {
            MDC.put(MDCConstants.GATEWAY_ID, messageContext.getGatewayId());
            if (messageContext.getType().equals(MessageType.EXPERIMENT)) {
                try {
                    ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
                    TBase messageEvent = messageContext.getEvent();
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
                        resultWriter.println("End of Results for Experiment : " + expId);
                        resultWriter.println("=====================================================================");
                    } else if (expState.equals(ExperimentState.FAILED)) {
                        resultWriter.println("Results for experiment : " + expId + " of gateway Id : " + gatewayId);
                        resultWriter.println("=====================================================================");
                        int j = 1;
                        resultWriter.println("Status : " + ExperimentState.FAILED.toString());
                        System.out.println("################ Experiment : " + expId + " FAILED ###################");
                        ExperimentModel experiment = airavata.getExperiment(authzToken, expId);
                        List<ErrorModel> errors = experiment.getErrors();
                        if (errors != null && !errors.isEmpty()) {
                            for (ErrorModel errorDetails : errors) {
                                System.out.println(errorDetails.getActualErrorMessage());
                                resultWriter.println("Actual Error : " + j + " : " + errorDetails.getActualErrorMessage());
                                resultWriter.println("User Friendly Message : " + j + " : " + errorDetails.getUserFriendlyMessage());
                            }
                        }

                        resultWriter.println("End of Results for Experiment : " + expId);
                        resultWriter.println("=====================================================================");
                    }
                } catch (TException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            resultWriter.flush();
            MDC.clear();
        }

        private void cancelExperiment(MessageContext messageContext) {
//            try {
//                byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());
//                ExperimentSubmitEvent expEvent = new ExperimentSubmitEvent();
//                ThriftUtils.createThriftFromBytes(bytes, expEvent);
//                log.info("Cancelling experiment with experimentId: {} gateway Id: {}", expEvent.getExperimentId(), expEvent.getGatewayId());
//                terminateExperiment(expEvent.getExperimentId(), expEvent.getGatewayId());
//            } catch (TException e) {
//                log.error("Experiment cancellation failed due to Thrift conversion error", e);
//            }finally {
//                experimentSubscriber.sendAck(messageContext.getDeliveryTag());
//            }

        }
    }

    private void processMessage(MessageContext message) {
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
                    resultWriter.println("End of Results for Experiment : " + expId);
                    resultWriter.println("=====================================================================");
                } else if (expState.equals(ExperimentState.FAILED)) {
                    resultWriter.println("Results for experiment : " + expId + " of gateway Id : " + gatewayId);
                    resultWriter.println("=====================================================================");
                    int j = 1;
                    resultWriter.println("Status : " + ExperimentState.FAILED.toString());
                    System.out.println("################ Experiment : " + expId + " FAILED ###################");
                    ExperimentModel experiment = airavata.getExperiment(authzToken, expId);
                    List<ErrorModel> errors = experiment.getErrors();
                    if (errors != null && !errors.isEmpty()) {
                        for (ErrorModel errorDetails : errors) {
                            System.out.println(errorDetails.getActualErrorMessage());
                            resultWriter.println("Actual Error : " + j + " : " + errorDetails.getActualErrorMessage());
                            resultWriter.println("User Friendly Message : " + j + " : " + errorDetails.getUserFriendlyMessage());
                        }
                    }

                    resultWriter.println("End of Results for Experiment : " + expId);
                    resultWriter.println("=====================================================================");
                }
            } catch (TException e) {
                logger.error(e.getMessage(), e);
            }
        } else if (message.getType().equals(MessageType.JOB)) {
            try {
                JobStatusChangeEvent event = new JobStatusChangeEvent();
                TBase messageEvent = message.getEvent();
                byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                ThriftUtils.createThriftFromBytes(bytes, event);

            } catch (TException e) {
                logger.error(e.getMessage(), e);
            }
        }
        resultWriter.flush();
    }

    private String getResultFileName() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    public String createLocalEchoExperiment(String gatewayId, String applicationInterfaceId, String storageId, String computeResourceId) throws Exception {
        String experimentId = null;
        try {
            List<InputDataObjectType> applicationInputs = airavata.getApplicationInputs(authzToken, applicationInterfaceId);
            List<OutputDataObjectType> appOutputs = airavata.getApplicationOutputs(authzToken, applicationInterfaceId);
            for (InputDataObjectType inputDataObjectType : applicationInputs) {
                if (inputDataObjectType.getName().equalsIgnoreCase(INPUT_NAME)) {
                    inputDataObjectType.setValue(LOCAL_ECHO_EXPERIMENT_INPUT);
                }
            }
            List<Project> projectsPerGateway = projectsMap.get(gatewayId);
            String projectID = null;
            if (projectsPerGateway != null && !projectsPerGateway.isEmpty()) {
                projectID = projectsPerGateway.get(0).getProjectID();
            }
            ExperimentModel simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(gatewayId, projectID, properties.getTestUserName(), "Local Echo Experiment", "Local Echo Experiment run", applicationInterfaceId, applicationInputs);
            simpleExperiment.setExperimentOutputs(appOutputs);

            ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(computeResourceId, 4, 1, 1, "cpu", 20, 0);
            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);
            userConfigurationData.setStorageId(storageId);
            userConfigurationData.setExperimentDataDir(TestFrameworkConstants.STORAGE_LOCATION);
            simpleExperiment.setUserConfigurationData(userConfigurationData);

            experimentId = airavata.createExperiment(authzToken, gatewayId, simpleExperiment);
            experimentsWithGateway.put(experimentId, gatewayId);

        } catch (Exception e) {
            logger.error("Error while creating Echo experiment", e);
            throw new Exception("Error while creating Echo experiment", e);
        }
        return experimentId;
    }

    public ExperimentModel getExperimentModel(String experimentId){
        ExperimentModel experimentModel = null;
        try {
            experimentModel = airavata.getExperiment(authzToken, experimentId);
        } catch (TException e) {
            logger.error("Error fetching experiment model", e);
        }
        return experimentModel;
    }


    public ExperimentStatus getExperimentStatus(String experimentId){
        ExperimentStatus experimentStatus = null;
        try {
            experimentStatus = airavata.getExperimentStatus(authzToken, experimentId);
        } catch (TException e) {
            logger.error("Error fetching experiment status", e);
        }
        return experimentStatus;
    }

}
