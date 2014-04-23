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

package org.apache.airavata.orchestrator.server;

import java.util.Arrays;
import java.util.List;

import org.apache.airavata.gsi.ssh.api.authentication.GSIAuthenticationInfo;
import org.apache.airavata.gfac.monitor.MonitorID;
import org.apache.airavata.gfac.monitor.MonitorManager;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeDetails;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.cpi.orchestrator_cpi_serviceConstants;
import org.apache.airavata.orchestrator.cpi.impl.SimpleOrchestratorImpl;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.DataType;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.utils.Constants.FieldConstants.TaskDetailConstants;
import org.apache.airavata.registry.cpi.utils.Constants.FieldConstants.WorkflowNodeConstants;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrchestratorServerHandler implements OrchestratorService.Iface {
    private static Logger log = LoggerFactory.getLogger(OrchestratorServerHandler.class);

    private MonitorManager monitorManager = null;

    private SimpleOrchestratorImpl orchestrator = null;

    private Registry registry;

    GSIAuthenticationInfo authenticationInfo = null;

    /**
     * Query orchestrator server to fetch the CPI version
     */
    public String getOrchestratorCPIVersion() throws TException {

        return orchestrator_cpi_serviceConstants.ORCHESTRATOR_CPI_VERSION;
    }


    public OrchestratorServerHandler() {
        try {
            // first constructing the monitorManager and orchestrator, then fill the required properties
            orchestrator = new SimpleOrchestratorImpl();
            registry = RegistryFactory.getDefaultRegistry();
            orchestrator.initialize();
        }catch (OrchestratorException e) {
            e.printStackTrace();
        }
    }

    /**
     * * After creating the experiment Data user have the
     * * experimentID as the handler to the experiment, during the launchExperiment
     * * We just have to give the experimentID
     * *
     * * @param experimentID
     * * @return sucess/failure
     * *
     * *
     *
     * @param experimentId
     */
    public boolean launchExperiment(String experimentId) throws TException {
        //TODO: Write the Orchestrator implementaion
        try {
            List<TaskDetails> tasks = orchestrator.createTasks(experimentId);
            MonitorID monitorID = null;
            if (tasks.size() > 1) {
                log.info("There are multiple tasks for this experiment, So Orchestrator will launch multiple Jobs");
            }
            List<String> ids = registry.getIds(DataType.WORKFLOW_NODE_DETAIL,WorkflowNodeConstants.EXPERIMENT_ID,experimentId);
            for (String workflowNodeId : ids) {
                WorkflowNodeDetails workflowNodeDetail = (WorkflowNodeDetails)registry.get(DataType.WORKFLOW_NODE_DETAIL, workflowNodeId);
                List<Object> taskDetailList = registry.get(DataType.TASK_DETAIL, TaskDetailConstants.NODE_ID, workflowNodeId);
                for (Object o : taskDetailList) {
                    TaskDetails taskID = (TaskDetails) o;
                    //iterate through all the generated tasks and performs the job submisssion+monitoring
                    Experiment experiment = (Experiment) registry.get(DataType.EXPERIMENT, experimentId);
                    if (experiment == null) {
                        log.error("Error retrieving the Experiment by the given experimentID: " + experimentId);
                        return false;
                    }
                    orchestrator.launchExperiment(experiment, workflowNodeDetail, taskID);
                }
            }

        } catch (Exception e) {
            throw new TException(e);
        }
        return true;
    }

    public MonitorManager getMonitorManager() {
        return monitorManager;
    }

    public void setMonitorManager(MonitorManager monitorManager) {
        this.monitorManager = monitorManager;
    }

    public boolean terminateExperiment(String experimentId) throws TException {
    	try {
			orchestrator.cancelExperiment(experimentId);
		} catch (OrchestratorException e) {
			log.error("Error canceling experiment "+experimentId,e);
			return false;
		}
        return true;
    }
}
