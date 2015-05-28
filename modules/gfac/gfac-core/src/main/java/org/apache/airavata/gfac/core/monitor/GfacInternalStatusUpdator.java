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
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.listener.AbstractActivityListener;
import org.apache.airavata.gfac.core.monitor.state.GfacExperimentStateChangeRequest;
import org.apache.airavata.gfac.core.states.GfacExperimentState;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class GfacInternalStatusUpdator implements AbstractActivityListener, Watcher {
    private final static Logger logger = LoggerFactory.getLogger(GfacInternalStatusUpdator.class);

    private CuratorFramework curatorClient;

    private static Integer mutex = -1;

    @Subscribe
    public void updateZK(GfacExperimentStateChangeRequest statusChangeRequest) throws Exception {
        logger.info("Gfac internal state changed to: " + statusChangeRequest.getState().toString());
        MonitorID monitorID = statusChangeRequest.getMonitorID();
        String experimentNode = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
        String experimentPath = experimentNode + File.separator + ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NAME)
                + File.separator + statusChangeRequest.getMonitorID().getExperimentID();
        Stat exists = null;
        if(!(GfacExperimentState.COMPLETED.equals(statusChangeRequest.getState()) || GfacExperimentState.FAILED.equals(statusChangeRequest.getState()))) {
            exists = curatorClient.checkExists().forPath(experimentPath);
            if (exists == null) {
                logger.error("ZK path: " + experimentPath + " does not exists !!");
                return;
            }
            Stat state = curatorClient.checkExists().forPath(experimentPath + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE);
            if (state == null) {
                // state znode has to be created
                curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).
                        forPath(experimentPath + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
                                String.valueOf(statusChangeRequest.getState().getValue()).getBytes());
            } else {
                curatorClient.setData().withVersion(state.getVersion()).forPath(experimentPath + File.separator + AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
                        String.valueOf(statusChangeRequest.getState().getValue()).getBytes());
            }
        }
        switch (statusChangeRequest.getState()) {
            case COMPLETED:
                logger.info("Experiment Completed, So removing the ZK entry for the experiment" + monitorID.getExperimentID());
                logger.info("Zookeeper experiment Path: " + experimentPath);
                break;
            case FAILED:
                logger.info("Experiment Failed, So removing the ZK entry for the experiment" + monitorID.getExperimentID());
                logger.info("Zookeeper experiment Path: " + experimentPath);
                break;
            default:
        }
    }

    public void setup(Object... configurations) {
        for (Object configuration : configurations) {
            if (configuration instanceof CuratorFramework) {
                this.curatorClient = (CuratorFramework) configuration;
            }
        }
    }

    public void process(WatchedEvent watchedEvent) {
        logger.info(watchedEvent.getPath());
        synchronized (mutex) {
            Event.KeeperState state = watchedEvent.getState();
            if (state == Event.KeeperState.SyncConnected) {
                mutex.notify();
            }
        }
    }
}
