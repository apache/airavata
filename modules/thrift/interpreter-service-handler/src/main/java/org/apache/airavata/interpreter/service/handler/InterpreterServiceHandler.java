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

package org.apache.airavata.interpreter.service.handler;


import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.AiravataAPIUtils;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.experiment.execution.ExperimentAdvanceOptions;
import org.apache.airavata.experiment.execution.InterpreterService;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.ws.monitor.*;
import org.apache.airavata.xbaya.interpretor.WorkflowInterpretorSkeleton;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;

public class InterpreterServiceHandler implements InterpreterService.Iface{
    private static final Logger log = LoggerFactory.getLogger(InterpreterServiceHandler.class);
    private WorkflowInterpretorSkeleton interpreterService;
    private AiravataAPI airavataAPI;

    public String runExperiment(String workflowTemplateName, Map<String, String> workflowInputs, ExperimentAdvanceOptions experimentAdOptions) throws TException {
        String user =  getAiravataAPI().getUserManager().getAiravataUser();
        String gatewayId = getAiravataAPI().getGateway();
        String experimentID;
        Workflow workflowObj;
        try {
            workflowObj = extractWorkflow(workflowTemplateName);
            experimentID = experimentAdOptions.getCustomExperimentId();
            workflowTemplateName = workflowObj.getName();
            if (experimentID == null || experimentID.isEmpty()) {
                experimentID = workflowTemplateName + "_" + UUID.randomUUID();
            }
            experimentAdOptions.setCustomExperimentId(experimentID);
            getAiravataAPI().getProvenanceManager().setWorkflowInstanceTemplateName(experimentID, workflowTemplateName);

            String submissionUser = getAiravataAPI().getUserManager().getAiravataUser();
            String executionUser=experimentAdOptions.getExecutionUser();
            if (executionUser==null){
                executionUser=submissionUser;
            }
            experimentAdOptions.setExecutionUser(executionUser);
            runPreWorkflowExecutionTasks(experimentID, executionUser, experimentAdOptions.getMetadata(), experimentAdOptions.getExperimentName());

            EventDataListener listener = new EventDataListenerAdapter() {
                @Override
                public void notify(EventDataRepository eventDataRepo, EventData eventData) {

                }
            };

            getExperimentMonitor(experimentID, listener).startMonitoring();
            String workflowContent = extractWorkflowContent(workflowTemplateName);

            return getInterpreterService().setupAndLaunch(workflowContent,
                    experimentID,
                    gatewayId,
                    user,
                    workflowInputs,
                    true,
                    AiravataAPIUtils.createWorkflowContextHeaderBuilder(MappingUtils.getExperimentOptionsObject(experimentAdOptions), experimentAdOptions.getExecutionUser(), user));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Monitor getExperimentMonitor(String experimentId, EventDataListener listener) throws AiravataAPIInvocationException {
        MonitorConfiguration monitorConfiguration;
        try {
            monitorConfiguration = new MonitorConfiguration(
                    getAiravataAPI().getAiravataManager().getMessageBoxServiceURL(), experimentId,
                    true, getAiravataAPI().getAiravataManager().getMessageBoxServiceURL());
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
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    private void runPreWorkflowExecutionTasks(String experimentId, String user,
                                              String metadata, String experimentName) throws AiravataAPIInvocationException {
        if (user != null) {
            getAiravataAPI().getProvenanceManager().setExperimentUser(experimentId, user);
        }
        if (metadata != null) {
            getAiravataAPI().getProvenanceManager().setExperimentMetadata(experimentId, metadata);
        }
        if (experimentName == null) {
            experimentName = experimentId;
        }
        getAiravataAPI().getProvenanceManager().setExperimentName(experimentId, experimentName);
    }

    private Workflow extractWorkflow(String workflowName) throws AiravataAPIInvocationException {
        Workflow workflowObj = null;
        //FIXME - There should be a better way to figure-out if the passed string is a name or an xml
        if(!workflowName.contains("http://airavata.apache.org/xbaya/xwf")){//(getClient().getWorkflowManager().isWorkflowExists(workflowName)) {
            workflowObj = getAiravataAPI().getWorkflowManager().getWorkflow(workflowName);
        }else {
            try{
                workflowObj = getAiravataAPI().getWorkflowManager().getWorkflowFromString(workflowName);
            }catch (AiravataAPIInvocationException e){
                getAiravataAPI().getWorkflowManager().getWorkflow(workflowName);
            }
        }
        return workflowObj;
    }

    private AiravataAPI getAiravataAPI(){
        if (airavataAPI==null) {
            try {
                String systemUserName = ServerSettings.getSystemUser();
                String gateway = ServerSettings.getSystemUserGateway();
                airavataAPI = AiravataAPIFactory.getAPI(gateway, systemUserName);
            } catch (ApplicationSettingsException e) {
                log.error("Unable to read the properties file", e);
            } catch (AiravataAPIInvocationException e) {
                log.error("Unable to create Airavata API", e);
            }
        }
        return airavataAPI;
    }

    private String extractWorkflowContent(String workflowName) throws AiravataAPIInvocationException {
        if(workflowName.contains("http://airavata.apache.org/xbaya/xwf")){//(getClient().getWorkflowManager().isWorkflowExists(workflowName)) {
            return workflowName;
        }else {
            return getAiravataAPI().getWorkflowManager().getWorkflowAsString(workflowName);
        }
    }

    public void cancelExperiment(String experimentID) throws TException {
        try {
            getInterpreterService().haltWorkflow(experimentID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void suspendExperiment(String experimentID) throws TException {
        try {
            getInterpreterService().suspendWorkflow(experimentID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resumeExperiment(String experimentID) throws TException {
        try {
            getInterpreterService().resumeWorkflow(experimentID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WorkflowInterpretorSkeleton getInterpreterService() {
        if (interpreterService==null){
            interpreterService=new WorkflowInterpretorSkeleton();
        }
        return interpreterService;
    }
}
