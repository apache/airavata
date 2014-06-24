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
package org.apache.airavata.gfac.core.monitor;

import com.google.common.eventbus.Subscribe;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.core.monitor.state.GfacExperimentStateChangeRequest;
import org.apache.airavata.gfac.workspace.experiment.GfacExperimentState;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class GfacInternalStatusUpdator implements AbstractActivityListener, Watcher {
    private final static Logger logger = LoggerFactory.getLogger(AiravataWorkflowNodeStatusUpdator.class);

    private ZooKeeper zk;

    private Integer mutex = -1;

    @Subscribe
    public void updateZK(GfacExperimentStateChangeRequest statusChangeRequest) throws KeeperException, InterruptedException, ApplicationSettingsException {
        logger.info("Gfac internal state changed to: " + statusChangeRequest.getState().toString());
        MonitorID monitorID = statusChangeRequest.getMonitorID();
        String experimentPath = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments") +
                File.separator + ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NAME) + File.separator + statusChangeRequest.getMonitorID().getExperimentID() + "+" + monitorID.getTaskID();
        Stat exists = null;
        try {
            if (!zk.getState().isConnected()) {
                String zkhostPort = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_SERVER_HOST)
                        + ":" + ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_SERVER_PORT);
                zk = new ZooKeeper(zkhostPort, 6000, this);
                synchronized (mutex){
                    mutex.wait();
                }
            }
            exists = zk.exists(experimentPath, false);// this znode is created by orchestrator so it has to exist at this level
            if (exists == null) {
                logger.error("ZK path: " + experimentPath + " does not exists !!");
                logger.error("Zookeeper is in an inconsistent state !!! ");
                return;
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        switch (statusChangeRequest.getState()) {
            case COMPLETED:
                zk.delete(experimentPath, exists.getVersion());
                break;
            case FAILED:
                zk.delete(experimentPath, exists.getVersion());
                break;
            default:
                zk.setData(experimentPath, (statusChangeRequest.getMonitorID().getJobID() +
                        "," + statusChangeRequest.getMonitorID().getWorkflowNodeID()).getBytes(), exists.getVersion());
        }
    }

    public void setup(Object... configurations) {
        for (Object configuration : configurations) {
            if (configuration instanceof ZooKeeper) {
                this.zk = (ZooKeeper) configuration;
            }
        }
    }

    public void process(WatchedEvent watchedEvent) {
        synchronized (mutex) {
            Event.KeeperState state = watchedEvent.getState();
            if (state == Event.KeeperState.SyncConnected) {
                mutex.notify();
            }
        }
    }
}
