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

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.orchestrator.util.OrchestratorRecoveryHandler;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.cpi.orchestrator_cpi_serviceConstants;
import org.apache.airavata.orchestrator.cpi.impl.SimpleOrchestratorImpl;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.utils.Constants.FieldConstants.TaskDetailConstants;
import org.apache.airavata.registry.cpi.utils.Constants.FieldConstants.WorkflowNodeConstants;
import org.apache.thrift.TException;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrchestratorServerHandler implements OrchestratorService.Iface, Watcher {
    private static Logger log = LoggerFactory.getLogger(OrchestratorServerHandler.class);

    private SimpleOrchestratorImpl orchestrator = null;

    private Registry registry;

    private ZooKeeper zk;

    private static Integer mutex = new Integer(-1);
    ;

    /**
     * Query orchestrator server to fetch the CPI version
     */
    public String getOrchestratorCPIVersion() throws TException {

        return orchestrator_cpi_serviceConstants.ORCHESTRATOR_CPI_VERSION;
    }


    public OrchestratorServerHandler() {
        // registering with zk
        try {
            String zkhostPort = AiravataZKUtils.getZKhostPort();
            String airavataServerHostPort = ServerSettings.getSetting(Constants.ORCHESTRATOR_SERVER_HOST)
                    + ":" + ServerSettings.getSetting(Constants.ORCHESTRATOR_SERVER_PORT);
            try {
                zk = new ZooKeeper(zkhostPort, 6000, this);   // no watcher is required, this will only use to store some data
                String OrchServer = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_ORCHESTRATOR_SERVER_NODE);
                synchronized (mutex) {
                    mutex.wait();  // waiting for the syncConnected event
                }
                Stat zkStat = zk.exists(OrchServer, false);
                if (zkStat == null) {
                    zk.create(OrchServer, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                }
                String instantNode = OrchServer + File.separator + String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
                zkStat = zk.exists(instantNode, false);
                if (zkStat == null) {
                    zk.create(instantNode,
                            airavataServerHostPort.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.EPHEMERAL);      // other component will watch these childeren creation deletion to monitor the status of the node
                }
                // creating a watch in orchestrator to monitor the gfac instances
                zk.getChildren(ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NODE, "/gfac-server"), this);
                log.info("Finished starting ZK: " + zk);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (KeeperException e) {
                e.printStackTrace();
            }
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        }
        // orchestrator init
        try {
            // first constructing the monitorManager and orchestrator, then fill the required properties
            orchestrator = new SimpleOrchestratorImpl();
            registry = RegistryFactory.getDefaultRegistry();
            orchestrator.initialize();
            orchestrator.getOrchestratorContext().setZk(this.zk);
        } catch (OrchestratorException e) {
            e.printStackTrace();
        } catch (RegistryException e) {
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
        Experiment experiment = null;
        try {
            List<String> ids = registry.getIds(RegistryModelType.WORKFLOW_NODE_DETAIL, WorkflowNodeConstants.EXPERIMENT_ID, experimentId);
            for (String workflowNodeId : ids) {
                WorkflowNodeDetails workflowNodeDetail = (WorkflowNodeDetails) registry.get(RegistryModelType.WORKFLOW_NODE_DETAIL, workflowNodeId);
                List<Object> taskDetailList = registry.get(RegistryModelType.TASK_DETAIL, TaskDetailConstants.NODE_ID, workflowNodeId);
                for (Object o : taskDetailList) {
                    TaskDetails taskID = (TaskDetails) o;
                    //iterate through all the generated tasks and performs the job submisssion+monitoring
                    experiment = (Experiment) registry.get(RegistryModelType.EXPERIMENT, experimentId);
                    if (experiment == null) {
                        log.error("Error retrieving the Experiment by the given experimentID: " + experimentId);
                        return false;
                    }
                    ExperimentStatus status = new ExperimentStatus();
                    status.setExperimentState(ExperimentState.LAUNCHED);
                    status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
                    experiment.setExperimentStatus(status);
                    registry.update(RegistryModelType.EXPERIMENT, experiment, experimentId);
                    //launching the experiment
                    orchestrator.launchExperiment(experiment, workflowNodeDetail, taskID);
                }
            }

        } catch (Exception e) {
            // Here we really do not have to do much because only potential failure can happen
            // is in gfac, if there are errors in gfac, it will handle the experiment/task/job statuses
            // We might get failures in registry access before submitting the jobs to gfac, in that case we
            // leave the status of these as created.
            ExperimentStatus status = new ExperimentStatus();
            status.setExperimentState(ExperimentState.FAILED);
            status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
            experiment.setExperimentStatus(status);
            try {
                registry.update(RegistryModelType.EXPERIMENT, experiment, experimentId);
            } catch (RegistryException e1) {
                throw new TException(e);
            }

            throw new TException(e);
        }
        return true;
    }


    /**
     * This method will validate the experiment before launching, if is failed we do not run the launch in airavata
     * thrift service (only if validation is enabled
     *
     * @param experimentId
     * @return
     * @throws TException
     */
    public boolean validateExperiment(String experimentId) throws TException, LaunchValidationException {
        //TODO: Write the Orchestrator implementaion
        try {
            List<TaskDetails> tasks = orchestrator.createTasks(experimentId);
            if (tasks.size() > 1) {
                log.info("There are multiple tasks for this experiment, So Orchestrator will launch multiple Jobs");
            }
            List<String> ids = registry.getIds(RegistryModelType.WORKFLOW_NODE_DETAIL, WorkflowNodeConstants.EXPERIMENT_ID, experimentId);
            for (String workflowNodeId : ids) {
                WorkflowNodeDetails workflowNodeDetail = (WorkflowNodeDetails) registry.get(RegistryModelType.WORKFLOW_NODE_DETAIL, workflowNodeId);
                List<Object> taskDetailList = registry.get(RegistryModelType.TASK_DETAIL, TaskDetailConstants.NODE_ID, workflowNodeId);
                for (Object o : taskDetailList) {
                    TaskDetails taskID = (TaskDetails) o;
                    //iterate through all the generated tasks and performs the job submisssion+monitoring
                    Experiment experiment = (Experiment) registry.get(RegistryModelType.EXPERIMENT, experimentId);
                    if (experiment == null) {
                        log.error("Error retrieving the Experiment by the given experimentID: " + experimentId);
                        return false;
                    }
                    return orchestrator.validateExperiment(experiment, workflowNodeDetail, taskID).isSetValidationState();
                }
            }

        } catch (OrchestratorException e) {
            throw new TException(e);
        } catch (RegistryException e) {
            throw new TException(e);
        }
        return false;
    }

    /**
     * This can be used to cancel a running experiment and store the status to terminated in registry
     *
     * @param experimentId
     * @return
     * @throws TException
     */
    public boolean terminateExperiment(String experimentId) throws TException {
        try {
            orchestrator.cancelExperiment(experimentId);
        } catch (OrchestratorException e) {
            log.error("Error canceling experiment " + experimentId, e);
            return false;
        }
        return true;
    }

    /** This method gracefully handler gfac node failures */
    synchronized public void process(WatchedEvent watchedEvent) {
        synchronized (mutex) {
            try {
                Event.KeeperState state = watchedEvent.getState();
                switch (state) {
                    case SyncConnected:
                        mutex.notify();
                        break;
                }
                if (watchedEvent.getPath() != null && watchedEvent.getPath().startsWith(ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NODE, "/gfac-server"))) {
                    List<String> children = zk.getChildren(ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NODE, "/gfac-server"), true);
                    for (String gfacNodes : children) {
                        zk.exists(ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NODE, "/gfac-server") +
                                File.separator + gfacNodes, this);
                    }
                    switch (watchedEvent.getType()) {
                        case NodeCreated:
                            mutex.notify();
                            break;
                        case NodeDeleted:
                            // here we have to handle gfac node shutdown case
                            if (children.size() == 0) {
                                log.error("There are not gfac instances to route failed jobs");
                                return;
                            }
                            // we recover one gfac node at a time
                            final WatchedEvent event = watchedEvent;
                            final OrchestratorServerHandler handler = this;
                            (new Thread() {
                                public void run() {
                                    try {
                                        (new OrchestratorRecoveryHandler(handler, event.getPath())).recover();     // run this task in a separate thread
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        log.error("error recovering the jobs for gfac-node: " + event.getPath());
                                    }
                                }
                            }).start();
                            break;
                    }
                }
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


	@Override
	public boolean launchTask(String taskId) throws TException {
		// TODO Auto-generated method stub
		return false;
	}
}
