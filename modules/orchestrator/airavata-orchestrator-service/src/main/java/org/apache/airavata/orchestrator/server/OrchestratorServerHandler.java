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

import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.job.monitor.MonitorManager;
import org.apache.airavata.job.monitor.core.Monitor;
import org.apache.airavata.job.monitor.core.PullMonitor;
import org.apache.airavata.job.monitor.core.PushMonitor;
import org.apache.airavata.job.monitor.exception.AiravataMonitorException;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.cpi.impl.SimpleOrchestratorImpl;
import org.apache.airavata.orchestrator.cpi.orchestrator_cpi_serviceConstants;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryImpl;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class OrchestratorServerHandler implements OrchestratorService.Iface {
    private static Logger log = LoggerFactory.getLogger(OrchestratorServerHandler.class);

    private MonitorManager monitorManager = null;

    private SimpleOrchestratorImpl orchestrator = null;

    private Registry registry;

    /**
     * Query orchestrator server to fetch the CPI version
     */
    @Override
    public String getOrchestratorCPIVersion() throws TException {

        return orchestrator_cpi_serviceConstants.ORCHESTRATOR_CPI_VERSION;
    }


    public OrchestratorServerHandler() {
        URL monitorUrl = OrchestratorServerHandler.class.getClassLoader().getResource(Constants.MONITOR_PROPERTIES);
        Properties properties = new Properties();
        try {
            // first constructing the monitorManager and orchestrator, then fill the required properties
            monitorManager = new MonitorManager();
            orchestrator = new SimpleOrchestratorImpl();
            registry = new RegistryImpl();

            // Filling monitorManager properties
            properties.load(monitorUrl.openStream());
            String primaryMonitor = properties.getProperty("primaryMonitor");
            String secondaryMonitor = properties.getProperty("secondaryMonitor");
            if (primaryMonitor == null) {
                log.error("Error loading primaryMonitor and there has to be a primary monitor");
            } else {
                Class<? extends Monitor> aClass = Class.forName(primaryMonitor).asSubclass(Monitor.class);
                Monitor monitor = aClass.newInstance();
                if (monitor instanceof PullMonitor) {
                    monitorManager.addPullMonitor((PullMonitor) monitor);
                } else if (monitor instanceof PushMonitor) {
                    monitorManager.addPushMonitor((PushMonitor) monitor);
                } else {
                    log.error("Wrong class is given to primary Monitor");
                }
            }
            if (secondaryMonitor == null) {
                log.info("No secondary Monitor has configured !!!!");
            } else {
                // todo we do not support a secondary Monitor at this point
            }

            // Now Monitor Manager is properly configured, now we have to start the monitoring system.
            // This will initialize all the required threads and required queues
            monitorManager.launchMonitor();
        } catch (OrchestratorException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (AiravataMonitorException e) {
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
    @Override
    public boolean launchExperiment(String experimentId) throws TException {
        //TODO: Write the Orchestrator implementaion

        /*
        Use the registry to take the Experiment Model object
         check the ExperimentModel object to check airavataAutoSchedule property
         if its set give an error telling that we do not support it
         else create a Task and save to the registry
         This should return the task ID
         if monitoring is in push mode, add the job to monitor queue, i hope by this time the host has finalized
         Get the task ID and invoke GFAC
         GFac will submit the job and return the jobID
         submit the job to minitor to monitoring queue after the submission if the monitoring is in pull mode
         RETURN;
        */
        try {
            orchestrator.launchExperiment(experimentId);
        } catch (OrchestratorException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }

    @Override
    public boolean terminateExperiment(String experimentId) throws TException {
        return false;
    }
}
