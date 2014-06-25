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
package org.apache.airavata.orchestrator.core.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.cpi.GfacService;
import org.apache.airavata.gfac.core.states.GfacExperimentState;
import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.gfac.GFACInstance;
import org.apache.airavata.orchestrator.core.gfac.GFacClientFactory;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.thrift.TException;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/*
 * this class is responsible for submitting a job to gfac in service mode,
 * it will select a gfac instance based on the incoming request and submit to that
 * gfac instance.
 */
public class GFACServiceJobSubmitter implements JobSubmitter,Watcher{
    private final static Logger logger = LoggerFactory.getLogger(GFACServiceJobSubmitter.class);
    public static final String IP = "ip";

    private OrchestratorContext orchestratorContext;

    private static Integer mutex = -1;

    public void initialize(OrchestratorContext orchestratorContext) throws OrchestratorException {
        this.orchestratorContext = orchestratorContext;
    }

    public GFACInstance selectGFACInstance() throws OrchestratorException {
        // currently we only support one instance but future we have to pick an instance
        return null;
    }

    public boolean submit(String experimentID, String taskID) throws OrchestratorException {
        ZooKeeper zk = orchestratorContext.getZk();
        int retryCount = 0;
        try {
            if (!zk.getState().isConnected()) {
                String zkhostPort = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_SERVER_HOST)
                        + ":" + ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_SERVER_PORT);
                zk = new ZooKeeper(zkhostPort, 6000, this);
                synchronized (mutex){
                    mutex.wait();
                }
            }
            String gfacServer = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NODE, "/gfac-server");
            String experimentNode = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
            List<String> children = zk.getChildren(gfacServer, this);

            String pickedChild = children.get(new Random().nextInt(Integer.MAX_VALUE) % children.size());
            // here we are not using an index because the getChildren does not return the same order everytime

            String gfacNodeData = new String(zk.getData(gfacServer + File.separator + pickedChild, false, null));
            logger.info("GFAC instance node data: " + gfacNodeData);
            String[] split = gfacNodeData.split(":");
            GfacService.Client localhost = GFacClientFactory.createGFacClient(split[0], Integer.parseInt(split[1]));
            if (zk.exists(gfacServer + File.separator + pickedChild, false) != null) {      // before submitting the job we check again the state of the node
                if(GFacUtils.createExperimentEntry(experimentID, taskID, zk, experimentNode, pickedChild)) {
                    return localhost.submitJob(experimentID, taskID);
                }
            }
        } catch (TException e) {
            throw new OrchestratorException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }



    synchronized public void process(WatchedEvent event) {
        synchronized (mutex) {
            switch (event.getState()){
                case SyncConnected:
                    mutex.notify();
            }
            switch (event.getType()) {
                case NodeCreated:
                    mutex.notify();
                    break;
            }
        }
    }
}
