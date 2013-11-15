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

package org.apache.airavata.client.impl;

import org.apache.airavata.client.AiravataClient;
import org.apache.airavata.client.api.*;
import org.apache.airavata.client.api.ExperimentAdvanceOptions;
import org.apache.airavata.client.api.HPCSettings;
import org.apache.airavata.client.api.HostSchedulingSettings;
import org.apache.airavata.client.api.NodeSettings;
import org.apache.airavata.client.api.OutputDataSettings;
import org.apache.airavata.client.api.SecuritySettings;
import org.apache.airavata.client.api.WorkflowOutputDataSettings;
import org.apache.airavata.client.api.WorkflowSchedulingSettings;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.tools.NameValuePairType;
import org.apache.airavata.registry.api.ExecutionErrors;
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.apache.airavata.ws.monitor.*;
import org.apache.airavata.interpreter.service.client.ExecutionClient;
import org.apache.thrift.TException;

import java.net.URISyntaxException;
import java.util.*;

public class ExecutionManagerThriftImpl implements ExecutionManager {
    private AiravataClient client;

    public ExecutionManagerThriftImpl() {
    }

    public ExecutionManagerThriftImpl(AiravataClient client) {
        setClient(client);
    }

    public AiravataClient getClient() {
        return client;
    }

    public void setClient(AiravataClient client) {
        this.client = client;
    }

    public String runExperiment(String workflowTemplateId, List<WorkflowInput> inputs) throws AiravataAPIInvocationException {
        ExperimentAdvanceOptions options = createExperimentAdvanceOptions(workflowTemplateId + "_" + Calendar.getInstance().getTime().toString(), getClient().getCurrentUser(), null);
        return runExperimentGeneral(workflowTemplateId, inputs, options, null);

    }

    public String runExperiment(String workflow, List<WorkflowInput> inputs, ExperimentAdvanceOptions options) throws AiravataAPIInvocationException {
        return runExperimentGeneral(workflow, inputs, options, null);
    }

    public String runExperiment(String workflow, List<WorkflowInput> inputs, ExperimentAdvanceOptions options, EventDataListener listener) throws AiravataAPIInvocationException {
        return runExperimentGeneral(workflow, inputs, options, listener);
    }

    public String runExperiment(Workflow workflow, List<WorkflowInput> inputs, ExperimentAdvanceOptions options) throws AiravataAPIInvocationException {
        return runExperimentGeneral(workflow.getName(), inputs, options, null);
    }

    private String runExperimentGeneral(String wfname, List<WorkflowInput> inputs, ExperimentAdvanceOptions options, EventDataListener listener) throws AiravataAPIInvocationException {
        Workflow workflowObj = null;
        try {
            workflowObj = extractWorkflow(wfname);
            String experimentID = options.getCustomExperimentId();
            String workflowTemplateName = workflowObj.getName();
            if (experimentID == null || experimentID.isEmpty()) {
                experimentID = workflowTemplateName + "_" + UUID.randomUUID();
            }
            options.setCustomExperimentId(experimentID);
            getClient().getProvenanceManager().setWorkflowInstanceTemplateName(experimentID, workflowTemplateName);

            String submissionUser = getClient().getUserManager().getAiravataUser();
            String executionUser=options.getExperimentExecutionUser();
            if (executionUser==null){
                executionUser=submissionUser;
            }
            options.setExperimentExecutionUser(executionUser);
            runPreWorkflowExecutionTasks(experimentID, executionUser, options.getExperimentMetadata(), options.getExperimentName());

            String workflowContent = extractWorkflowContent(wfname);
            Map<String, String> workflowInputs = new HashMap<String, String>();
            for (WorkflowInput workflowInput : inputs){
                String name = workflowInput.getName();
                String value = (String)workflowInput.getValue();
                workflowInputs.put(name, value);
            }
            if (listener!=null){
                getExperimentMonitor(experimentID, listener).startMonitoring();
            }
            org.apache.airavata.experiment.execution.ExperimentAdvanceOptions experimentAdvanceOptions = generateAdvancedOptions(options);
            return getExecutionClient().runExperiment(workflowContent, workflowInputs, experimentAdvanceOptions);
        } catch (AiravataAPIInvocationException e) {
            throw new AiravataAPIInvocationException("Error occured while running the workflow", e);
        } catch (TException e) {
            throw new AiravataAPIInvocationException("Error occured while running the workflow", e);
        }
    }

    private void runPreWorkflowExecutionTasks(String experimentId, String user,
                                              String metadata, String experimentName) throws AiravataAPIInvocationException {
        if (user != null) {
            getClient().getProvenanceManager().setExperimentUser(experimentId, user);
        }
        if (metadata != null) {
            getClient().getProvenanceManager().setExperimentMetadata(experimentId, metadata);
        }
        if (experimentName == null) {
            experimentName = experimentId;
        }
        getClient().getProvenanceManager().setExperimentName(experimentId, experimentName);
    }

    public Monitor getExperimentMonitor(String experimentId) throws AiravataAPIInvocationException {
        return getExperimentMonitor(experimentId,null);
    }

    public Monitor getExperimentMonitor(String experimentId, EventDataListener listener) throws AiravataAPIInvocationException {
        MonitorConfiguration monitorConfiguration;
        try {
            monitorConfiguration = new MonitorConfiguration(
                    getClient().getClientConfiguration().getMessagebrokerURL().toURI(), experimentId,
                    true, getClient().getClientConfiguration().getMessageboxURL().toURI());
            final Monitor monitor = new Monitor(monitorConfiguration);
            monitor.printRawMessage(false);
            if (listener!=null) {
                monitor.getEventDataRepository().registerEventListener(listener);
                listener.setExperimentMonitor(monitor);
            }
            if (!monitor.getExperimentId().equals(">")){
                monitor.getEventDataRepository().registerEventListener(new EventDataListenerAdapter() {
                    public void notify(EventDataRepository eventDataRepo, EventData eventData) {
                        if (eventData.getType()== MonitorUtil.EventType.WORKFLOW_TERMINATED || eventData.getType()== MonitorUtil.EventType.SENDING_FAULT){
                            monitor.stopMonitoring();
                        }
                    }
                });
            }
            return monitor;
        } catch (URISyntaxException e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    public ExperimentAdvanceOptions createExperimentAdvanceOptions() throws AiravataAPIInvocationException {
        return new ExperimentAdvanceOptions();
    }

    public ExperimentAdvanceOptions createExperimentAdvanceOptions(String experimentName, String experimentUser, String experimentMetadata) throws AiravataAPIInvocationException {
        ExperimentAdvanceOptions options = createExperimentAdvanceOptions();
        options.setExperimentName(experimentName);
        options.setExperimentCustomMetadata(experimentMetadata);
        options.setExperimentExecutionUser(experimentUser);
        return options;
    }

    public void waitForExperimentTermination(String experimentId) throws AiravataAPIInvocationException {
        Monitor experimentMonitor = getExperimentMonitor(experimentId, new EventDataListenerAdapter() {
            public void notify(EventDataRepository eventDataRepo,
                               EventData eventData) {
                if (eventData.getType()== MonitorUtil.EventType.WORKFLOW_TERMINATED){
                    getMonitor().stopMonitoring();
                }
            }
        });
        experimentMonitor.startMonitoring();
        try {
            WorkflowExecutionStatus workflowInstanceStatus = getClient().getProvenanceManager().getWorkflowInstanceStatus(experimentId, experimentId);
            if (workflowInstanceStatus.getExecutionStatus()== WorkflowExecutionStatus.State.FINISHED || workflowInstanceStatus.getExecutionStatus()== WorkflowExecutionStatus.State.FAILED){
                experimentMonitor.stopMonitoring();
                return;
            }
        } catch (AiravataAPIInvocationException e) {
            //Workflow may not have started yet. Best to use the monitor to follow the progress
        }
        experimentMonitor.waitForCompletion();

    }

    public List<ExperimentExecutionError> getExperimentExecutionErrors(String experimentId) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().getExperimentExecutionErrors(experimentId);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    public List<WorkflowExecutionError> getWorkflowExecutionErrors(String experimentId, String workflowInstanceId) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().getWorkflowExecutionErrors(experimentId,
                    workflowInstanceId);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    public List<NodeExecutionError> getNodeExecutionErrors(String experimentId, String workflowInstanceId, String nodeId) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().getNodeExecutionErrors(experimentId,
                    workflowInstanceId, nodeId);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    public List<ApplicationJobExecutionError> getApplicationJobErrors(String experimentId, String workflowInstanceId, String nodeId, String gfacJobId) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().getApplicationJobErrors(experimentId,
                    workflowInstanceId, nodeId, gfacJobId);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    public List<ApplicationJobExecutionError> getApplicationJobErrors(String gfacJobId) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().getApplicationJobErrors(gfacJobId);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    public List<ExecutionError> getExecutionErrors(String experimentId, String workflowInstanceId, String nodeId, String gfacJobId, ExecutionErrors.Source... filterBy) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().getExecutionErrors(experimentId,
                    workflowInstanceId, nodeId, gfacJobId, filterBy);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    public int addExperimentError(ExperimentExecutionError error) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().addExperimentError(error);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    public int addWorkflowExecutionError(WorkflowExecutionError error) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().addWorkflowExecutionError(error);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    public int addNodeExecutionError(NodeExecutionError error) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().addNodeExecutionError(error);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    public int addApplicationJobExecutionError(ApplicationJobExecutionError error) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().addApplicationJobExecutionError(error);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    public org.apache.airavata.experiment.execution.InterpreterService.Client getExecutionClient (){
        ExecutionClient executionClient = new ExecutionClient();
        return executionClient.getInterpreterServiceClient();

    }


    private String extractWorkflowContent(String workflowName) throws AiravataAPIInvocationException {
        if(workflowName.contains("http://airavata.apache.org/xbaya/xwf")){//(getClient().getWorkflowManager().isWorkflowExists(workflowName)) {
            return workflowName;
        }else {
            return getClient().getWorkflowManager().getWorkflowAsString(workflowName);
        }
    }

    private Workflow extractWorkflow(String workflowName) throws AiravataAPIInvocationException {
        Workflow workflowObj = null;
        //FIXME - There should be a better way to figure-out if the passed string is a name or an xml
        if(!workflowName.contains("http://airavata.apache.org/xbaya/xwf")){//(getClient().getWorkflowManager().isWorkflowExists(workflowName)) {
            workflowObj = getClient().getWorkflowManager().getWorkflow(workflowName);
        }else {
            try{
                workflowObj = getClient().getWorkflowManager().getWorkflowFromString(workflowName);
            }catch (AiravataAPIInvocationException e){
                getClient().getWorkflowManager().getWorkflow(workflowName);
            }
        }
        return workflowObj;
    }

    private org.apache.airavata.experiment.execution.ExperimentAdvanceOptions generateAdvancedOptions(org.apache.airavata.client.api.ExperimentAdvanceOptions exAdOpt){
        try {
            org.apache.airavata.experiment.execution.ExperimentAdvanceOptions advanceOptions = new org.apache.airavata.experiment.execution.ExperimentAdvanceOptions();
            advanceOptions.setExperimentName(exAdOpt.getExperimentName());
            advanceOptions.setCustomExperimentId(exAdOpt.getCustomExperimentId());
            advanceOptions.setExecutionUser(exAdOpt.getExperimentExecutionUser());
            advanceOptions.setMetadata(exAdOpt.getExperimentMetadata());
            SecuritySettings customSecuritySettings = exAdOpt.getCustomSecuritySettings();
            if (customSecuritySettings != null){
                advanceOptions.setSecuritySettings(generateSecuritySettingsObj(customSecuritySettings));
            }

            WorkflowOutputDataSettings outputDataSettings = exAdOpt.getCustomWorkflowOutputDataSettings();
            List<org.apache.airavata.experiment.execution.OutputDataSettings> dataSettingsList = new ArrayList<org.apache.airavata.experiment.execution.OutputDataSettings>();
            if (outputDataSettings != null){
                OutputDataSettings[] outputDataSettingsList = outputDataSettings.getOutputDataSettingsList();
                for (OutputDataSettings opds : outputDataSettingsList){
                    org.apache.airavata.experiment.execution.OutputDataSettings dataSettings = generateOutputDataObject(opds);
                    dataSettingsList.add(dataSettings);
                }
                org.apache.airavata.experiment.execution.WorkflowOutputDataSettings wfOpDSettings = new org.apache.airavata.experiment.execution.WorkflowOutputDataSettings();
                wfOpDSettings.setOutputDataSettingsList(dataSettingsList);
                advanceOptions.setWorkflowOutputDataSettings(wfOpDSettings);
            }
            WorkflowSchedulingSettings schedulingSettings = exAdOpt.getCustomWorkflowSchedulingSettings();
            if (schedulingSettings != null){
                org.apache.airavata.experiment.execution.WorkflowSchedulingSettings settings = generateShedulingSettingsObject(schedulingSettings);
                advanceOptions.setWorkflowSchedulingSettings(settings);
            }
            return advanceOptions;
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();
        }
      return null;
    }

    private org.apache.airavata.experiment.execution.SecuritySettings generateSecuritySettingsObj(org.apache.airavata.client.api.SecuritySettings secSettings) {
        try {
            org.apache.airavata.experiment.execution.SecuritySettings settings = new org.apache.airavata.experiment.execution.SecuritySettings();
            org.apache.airavata.experiment.execution.AmazonWebServicesSettings amWSSettings = new org.apache.airavata.experiment.execution.AmazonWebServicesSettings();
            org.apache.airavata.client.api.AmazonWebServicesSettings amazonWSSettings = secSettings.getAmazonWSSettings();
            if (amazonWSSettings != null){
                amWSSettings.setAccessKey(amazonWSSettings.getSecretAccessKey());
                amWSSettings.setAmiID(amazonWSSettings.getAMIId());
                amWSSettings.setInstanceID(amazonWSSettings.getInstanceId());
                amWSSettings.setSecretAccessKey(amazonWSSettings.getSecretAccessKey());
                amWSSettings.setUsername(amazonWSSettings.getUsername());
                settings.setAmazonWSSettings(amWSSettings);
            }

            org.apache.airavata.experiment.execution.CredentialStoreSecuritySettings credSettings = new org.apache.airavata.experiment.execution.CredentialStoreSecuritySettings();
            org.apache.airavata.client.api.CredentialStoreSecuritySettings credStoreSecSettings = secSettings.getCredentialStoreSecuritySettings();
            if (credStoreSecSettings != null){
                credSettings.setGatewayID(credStoreSecSettings.getGatewayId());
                credSettings.setPortalUser(credStoreSecSettings.getPortalUser());
                credSettings.setTokenId(credStoreSecSettings.getTokenId());
                settings.setCredentialStoreSettings(credSettings);
            }

//            org.apache.airavata.experiment.execution.MyProxyRepositorySettings myProxySettings = new org.apache.airavata.experiment.execution.MyProxyRepositorySettings();
//            org.apache.airavata.client.api.GridMyProxyRepositorySettings proxyRepositorySettings = secSettings.getGridMyProxyRepositorySettings();
//            if (proxyRepositorySettings != null){
//                myProxySettings.setLifetime(proxyRepositorySettings.getLifeTime());
//                myProxySettings.setMyproxyServer(proxyRepositorySettings.getMyProxyServer());
//                myProxySettings.setPassword(proxyRepositorySettings.getPassword());
//                myProxySettings.setUserName(proxyRepositorySettings.getUsername());
//                settings.setMyproxySettings(myProxySettings);
//            }
//
//            org.apache.airavata.experiment.execution.SSHAuthenticationSettings authSettings = new org.apache.airavata.experiment.execution.SSHAuthenticationSettings();
//            org.apache.airavata.client.api.SSHAuthenticationSettings sshAuthenticationSettings = secSettings.getSSHAuthenticationSettings();
//            if (sshAuthenticationSettings != null){
//                authSettings.setAccessKeyID(sshAuthenticationSettings.getAccessKeyId());
//                authSettings.setSecretAccessKey(sshAuthenticationSettings.getSecretAccessKey());
//                settings.setSshAuthSettings(authSettings);
//            }
            return settings;

        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private org.apache.airavata.experiment.execution.OutputDataSettings generateOutputDataObject(OutputDataSettings opDataSettings){
        org.apache.airavata.experiment.execution.OutputDataSettings dataSettings = new org.apache.airavata.experiment.execution.OutputDataSettings();
        dataSettings.setDataRegURL(opDataSettings.getDataRegistryUrl());
        dataSettings.setIsdataPersisted(opDataSettings.isDataPersistent());
        dataSettings.setNodeID(opDataSettings.getNodeId());
        dataSettings.setOutputdataDir(opDataSettings.getOutputDataDirectory());
        return dataSettings;
    }

    private org.apache.airavata.experiment.execution.WorkflowSchedulingSettings generateShedulingSettingsObject (WorkflowSchedulingSettings wfschSettings){
        org.apache.airavata.experiment.execution.WorkflowSchedulingSettings schedulingSettings = new org.apache.airavata.experiment.execution.WorkflowSchedulingSettings();
        NodeSettings[] list = wfschSettings.getNodeSettingsList();
        List<org.apache.airavata.experiment.execution.NodeSettings> nodes = new ArrayList<org.apache.airavata.experiment.execution.NodeSettings>();
        if (list != null){
            for (NodeSettings ns : list){
                org.apache.airavata.experiment.execution.NodeSettings nodeSettings = generateNodeSettingsObject(ns);
                nodes.add(nodeSettings);
            }
        }
        schedulingSettings.setNodeSettingsList(nodes);
        return schedulingSettings;
    }

    private org.apache.airavata.experiment.execution.NodeSettings generateNodeSettingsObject (NodeSettings settings){
        org.apache.airavata.experiment.execution.NodeSettings nsettings = new org.apache.airavata.experiment.execution.NodeSettings();
        nsettings.setNodeId(settings.getNodeId());
        nsettings.setServiceId(settings.getServiceId());
        nsettings.setHostSchedulingSettings(generateHostSchSettings(settings.getHostSettings()));
        nsettings.setHpcSettings(generateHPCSettingsObject(settings.getHPCSettings()));

        List<NameValuePairType> nameValuePair = settings.getNameValuePair();
        List<org.apache.airavata.experiment.execution.NameValuePairType> typeList = new ArrayList<org.apache.airavata.experiment.execution.NameValuePairType>();
        if (nameValuePair != null){
            for (NameValuePairType nvPair : nameValuePair){
                org.apache.airavata.experiment.execution.NameValuePairType type = generateNVPairObject(nvPair);
                typeList.add(type);
            }
        }
        nsettings.setNameValuePairList(typeList);
        return nsettings;
    }

    private org.apache.airavata.experiment.execution.HostSchedulingSettings generateHostSchSettings (HostSchedulingSettings settings){
        org.apache.airavata.experiment.execution.HostSchedulingSettings hscheduleSettings = new org.apache.airavata.experiment.execution.HostSchedulingSettings();
        hscheduleSettings.setGatekeeperEPR(settings.getGatekeeperEPR());
        hscheduleSettings.setHostID(settings.getHostId());
        hscheduleSettings.setIsWSGramPreferred(settings.isWSGRAMPreffered());
        return hscheduleSettings;
    }

    private org.apache.airavata.experiment.execution.HPCSettings generateHPCSettingsObject (HPCSettings settings){
        org.apache.airavata.experiment.execution.HPCSettings hsettings = new org.apache.airavata.experiment.execution.HPCSettings();
        hsettings.setCpuCount(settings.getCPUCount());
        hsettings.setJobManager(settings.getJobManager());
        hsettings.setMaxWalltime(settings.getMaxWallTime());
        hsettings.setNodeCount(settings.getNodeCount());
        hsettings.setQueueName(settings.getQueueName());
        return hsettings;
    }

    private org.apache.airavata.experiment.execution.NameValuePairType generateNVPairObject (org.apache.airavata.client.tools.NameValuePairType settings){
        org.apache.airavata.experiment.execution.NameValuePairType nvType = new org.apache.airavata.experiment.execution.NameValuePairType();
        nvType.setName(settings.getName());
        nvType.setDescription(settings.getDescription());
        nvType.setValue(settings.getValue());
        return nvType;
    }
}


