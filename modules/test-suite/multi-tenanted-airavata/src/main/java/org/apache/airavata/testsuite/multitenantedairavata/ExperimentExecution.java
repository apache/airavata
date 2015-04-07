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
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.workspace.experiment.ComputationalResourceScheduling;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.ExperimentState;
import org.apache.airavata.model.workspace.experiment.UserConfigurationData;
import org.apache.airavata.testsuite.multitenantedairavata.utils.PropertyFileType;
import org.apache.airavata.testsuite.multitenantedairavata.utils.PropertyReader;
import org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentExecution {
    private Airavata.Client airavata;
    private final static Logger logger = LoggerFactory.getLogger(ExperimentExecution.class);
    private Map<String, String> experimentsWithTokens;
    private Map<String, String> experimentsWithGateway;
    private Map<String, String> csTokens;
    private Map<String, Map<String, String>> appInterfaceMap;
    private Map<String, List<Project>> projectsMap;
    private PropertyReader propertyReader;

    public ExperimentExecution(Airavata.Client airavata,
                               Map<String, String> tokenMap ) {
        this.airavata = airavata;
        this.csTokens = tokenMap;
        this.appInterfaceMap = getApplicationMap(tokenMap);
        this.propertyReader = new PropertyReader();
        this.projectsMap = getProjects(tokenMap);
        this.experimentsWithTokens = new HashMap<String, String>();
        this.experimentsWithGateway = new HashMap<String, String>();
    }

    protected Map<String, Map<String, String>> getApplicationMap (Map<String, String> tokenMap){
        appInterfaceMap = new HashMap<String, Map<String, String>>();
        try {
            if (tokenMap != null && !tokenMap.isEmpty()){
                for (String gatewayId : tokenMap.keySet()){
                    Map<String, String> allApplicationInterfaceNames = airavata.getAllApplicationInterfaceNames(gatewayId);
                    appInterfaceMap.put(gatewayId, allApplicationInterfaceNames);
                }
            }
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return appInterfaceMap;
    }

    protected Map<String, List<Project>> getProjects (Map<String, String> tokenMap){
        projectsMap = new HashMap<String, List<Project>>();
        try {
            if (tokenMap != null && !tokenMap.isEmpty()){
                for (String gatewayId : tokenMap.keySet()){
                    if (!gatewayId.equals("php_reference_gateway")){
                        String userName = "testUser_" + gatewayId;
                        List<Project> allUserProjects = airavata.getAllUserProjects(gatewayId, userName);
                        projectsMap.put(gatewayId, allUserProjects);
                    }
                }
            }
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return projectsMap;
    }

    public void launchExperiments () throws Exception {
        try {
            for (String expId : experimentsWithTokens.keySet()){
                airavata.launchExperiment(expId, experimentsWithTokens.get(expId));
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
        for (final String expId : experimentsWithGateway.keySet()){
            final String gatewayId = experimentsWithGateway.get(expId);
            consumer.listen(new MessageHandler() {
                @Override
                public Map<String, Object> getProperties() {
                    Map<String, Object> props = new HashMap<String, Object>();
                    List<String> routingKeys = new ArrayList<String>();
                    routingKeys.add(gatewayId);
                    routingKeys.add(gatewayId + "." + expId);
                    routingKeys.add(gatewayId + "." + expId + ".*");
                    routingKeys.add(gatewayId + "." + expId + ".*.*");
                    routingKeys.add(gatewayId + "." + expId + ".*.*.*");

                    props.put(MessagingConstants.RABBIT_ROUTING_KEY, routingKeys);
                    return props;
                }

                @Override
                public void onMessage(MessageContext message) {

                    if (message.getType().equals(MessageType.EXPERIMENT)){
                        try {
                            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
                            TBase messageEvent = message.getEvent();
                            byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                            ThriftUtils.createThriftFromBytes(bytes, event);
                            ExperimentState expState = event.getState();
                            if (expState.equals(ExperimentState.COMPLETED)){
                                // check file transfers
                                List<OutputDataObjectType> experimentOutputs = airavata.getExperimentOutputs(expId);
                                int i = 1;
                                for (OutputDataObjectType output : experimentOutputs){
                                    logger.info("################ Experiment : " + expId + " COMPLETES ###################");
                                    logger.info("Output " + i + " : "  + output.getValue());
                                    i++;
                                }
                            }
                            logger.info(" Experiment Id : '" + expId
                                    + "' with state : '" + event.getState().toString() +
                                    " for Gateway " + event.getGatewayId());
                        } catch (TException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }else if (message.getType().equals(MessageType.JOB)){
                        try {
                            JobStatusChangeEvent event = new JobStatusChangeEvent();
                            TBase messageEvent = message.getEvent();
                            byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                            ThriftUtils.createThriftFromBytes(bytes, event);
                            logger.info(" Job ID : '" + event.getJobIdentity().getJobId()
                                    + "' with state : '" + event.getState().toString() +
                                    " for Gateway " + event.getJobIdentity().getGatewayId());
                        } catch (TException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            });
        }
    }

    public void createAmberExperiment () throws Exception{
        try {
            for (String gatewayId : csTokens.keySet()){
                String token = csTokens.get(gatewayId);
                Map<String, String> appsWithNames = appInterfaceMap.get(gatewayId);
                for (String appId : appsWithNames.keySet()){
                    List<InputDataObjectType> applicationInputs = airavata.getApplicationInputs(appId);
                    List<OutputDataObjectType> appOutputs = airavata.getApplicationOutputs(appId);
                    String appName = appsWithNames.get(appId);
                    if (appName.equals(TestFrameworkConstants.AppcatalogConstants.AMBER_APP_NAME)){
                        String heatRSTFile = propertyReader.readProperty(TestFrameworkConstants.AppcatalogConstants.AMBER_HEAT_RST_LOCATION, PropertyFileType.TEST_FRAMEWORK);
                        String prodInFile = propertyReader.readProperty(TestFrameworkConstants.AppcatalogConstants.AMBER_PROD_IN_LOCATION, PropertyFileType.TEST_FRAMEWORK);
                        String prmTopFile = propertyReader.readProperty(TestFrameworkConstants.AppcatalogConstants.AMBER_PRMTOP_LOCATION, PropertyFileType.TEST_FRAMEWORK);

                        for (InputDataObjectType inputDataObjectType : applicationInputs) {
                            if (inputDataObjectType.getName().equalsIgnoreCase("Heat_Restart_File")) {
                                inputDataObjectType.setValue(heatRSTFile);
                            } else if (inputDataObjectType.getName().equalsIgnoreCase("Production_Control_File")) {
                                inputDataObjectType.setValue(prodInFile);
                            } else if (inputDataObjectType.getName().equalsIgnoreCase("Parameter_Topology_File")) {
                                inputDataObjectType.setValue(prmTopFile);
                            }
                        }

                        List<Project> projectsPerGateway = projectsMap.get(gatewayId);
                        String projectID = null;
                        if (projectsPerGateway != null && !projectsPerGateway.isEmpty()){
                            projectID = projectsPerGateway.get(0).getProjectID();
                        }
                        String userName = "testUser_" + gatewayId;
                        Experiment simpleExperiment =
                                ExperimentModelUtil.createSimpleExperiment(projectID, userName, "Amber Experiment", "Amber Experiment run", appId, applicationInputs);
                        simpleExperiment.setExperimentOutputs(appOutputs);
                        String experimentId;
                        Map<String, String> computeResources = airavata.getAvailableAppInterfaceComputeResources(appId);
                        if (computeResources != null && computeResources.size() != 0) {
                            for (String id : computeResources.keySet()) {
                                String resourceName = computeResources.get(id);
                                if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.TRESTLES_RESOURCE_NAME)) {
                                    ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 20, 0, 1, null);
                                    UserConfigurationData userConfigurationData = new UserConfigurationData();
                                    userConfigurationData.setAiravataAutoSchedule(false);
                                    userConfigurationData.setOverrideManualScheduledParams(false);
                                    userConfigurationData.setComputationalResourceScheduling(scheduling);
                                    simpleExperiment.setUserConfigurationData(userConfigurationData);
                                    experimentId = airavata.createExperiment(gatewayId, simpleExperiment);
                                    experimentsWithTokens.put(experimentId, token);
                                    experimentsWithGateway.put(experimentId, gatewayId);
                                }else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.STAMPEDE_RESOURCE_NAME)) {
                                    ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 20, 0, 1, null);
                                    UserConfigurationData userConfigurationData = new UserConfigurationData();
                                    userConfigurationData.setAiravataAutoSchedule(false);
                                    userConfigurationData.setOverrideManualScheduledParams(false);
                                    userConfigurationData.setComputationalResourceScheduling(scheduling);
                                    simpleExperiment.setUserConfigurationData(userConfigurationData);
                                    experimentId = airavata.createExperiment(gatewayId, simpleExperiment);
                                    experimentsWithTokens.put(experimentId, token);
                                    experimentsWithGateway.put(experimentId, gatewayId);
                                }else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.BR2_RESOURCE_NAME)) {
                                    ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 20, 0, 1, null);
                                    UserConfigurationData userConfigurationData = new UserConfigurationData();
                                    userConfigurationData.setAiravataAutoSchedule(false);
                                    userConfigurationData.setOverrideManualScheduledParams(false);
                                    userConfigurationData.setComputationalResourceScheduling(scheduling);
                                    simpleExperiment.setUserConfigurationData(userConfigurationData);
                                    experimentId = airavata.createExperiment(gatewayId, simpleExperiment);
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

    public void createEchoExperiment () throws Exception{
        try {
            for (String gatewayId : csTokens.keySet()) {
                if (!gatewayId.equals("php_reference_gateway")){
                    String token = csTokens.get(gatewayId);
                    Map<String, String> appsWithNames = appInterfaceMap.get(gatewayId);
                    for (String appId : appsWithNames.keySet()) {
                        String appName = appsWithNames.get(appId);
                        if (appName.equals(TestFrameworkConstants.AppcatalogConstants.ECHO_NAME)) {
                            List<InputDataObjectType> applicationInputs = airavata.getApplicationInputs(appId);
                            List<OutputDataObjectType> appOutputs = airavata.getApplicationOutputs(appId);
                            for (InputDataObjectType inputDataObjectType : applicationInputs) {
                                if (inputDataObjectType.getName().equalsIgnoreCase("input_to_Echo")) {
                                    inputDataObjectType.setValue("Hello World !!!");
                                }
                            }

                            List<Project> projectsPerGateway = projectsMap.get(gatewayId);
                            String projectID = null;
                            if (projectsPerGateway != null && !projectsPerGateway.isEmpty()){
                                projectID = projectsPerGateway.get(0).getProjectID();
                            }
                            Experiment simpleExperiment =
                                    ExperimentModelUtil.createSimpleExperiment(projectID, "admin", "Echo Experiment", "Echo Experiment run", appId, applicationInputs);
                            simpleExperiment.setExperimentOutputs(appOutputs);
                            String experimentId;
                            Map<String, String> computeResources = airavata.getAvailableAppInterfaceComputeResources(appId);
                            if (computeResources != null && computeResources.size() != 0) {
                                for (String id : computeResources.keySet()) {
                                    String resourceName = computeResources.get(id);
                                    if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.TRESTLES_RESOURCE_NAME)) {
                                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 20, 0, 1, null);
                                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                                        userConfigurationData.setAiravataAutoSchedule(false);
                                        userConfigurationData.setOverrideManualScheduledParams(false);
                                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                                        experimentId = airavata.createExperiment(gatewayId, simpleExperiment);
                                        experimentsWithTokens.put(experimentId, token);
                                        experimentsWithGateway.put(experimentId, gatewayId);
                                    } else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.STAMPEDE_RESOURCE_NAME)) {
                                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 20, 0, 1, null);
                                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                                        userConfigurationData.setAiravataAutoSchedule(false);
                                        userConfigurationData.setOverrideManualScheduledParams(false);
                                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                                        experimentId = airavata.createExperiment(gatewayId, simpleExperiment);
                                        experimentsWithTokens.put(experimentId, token);
                                        experimentsWithGateway.put(experimentId, gatewayId);
                                    } else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.BR2_RESOURCE_NAME)) {
                                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 20, 0, 1, null);
                                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                                        userConfigurationData.setAiravataAutoSchedule(false);
                                        userConfigurationData.setOverrideManualScheduledParams(false);
                                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                                        experimentId = airavata.createExperiment(gatewayId, simpleExperiment);
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
