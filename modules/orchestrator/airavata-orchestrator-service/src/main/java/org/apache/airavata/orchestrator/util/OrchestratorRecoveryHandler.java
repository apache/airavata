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
package org.apache.airavata.orchestrator.util;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.server.OrchestratorServerHandler;
import org.apache.thrift.TException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class OrchestratorRecoveryHandler implements Watcher {
    private static Logger log = LoggerFactory.getLogger(OrchestratorRecoveryHandler.class);

    private ZooKeeper zk;

    private String gfacId;

    private static Integer mutex = -1;

    private OrchestratorServerHandler serverHandler;

    public OrchestratorRecoveryHandler(OrchestratorServerHandler handler, String zkExpPath) {
        this.zk = zk;
        int index = zkExpPath.split(File.separator).length - 1;
        this.gfacId = zkExpPath.split(File.separator)[index];
        this.serverHandler = handler;
    }

    /**
     * This method return the list of experimentId
     *
     * @return
     * @throws OrchestratorException
     * @throws ApplicationSettingsException
     * @throws IOException
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void recover() throws OrchestratorException, ApplicationSettingsException, IOException, KeeperException, InterruptedException {
        String zkhostPort = AiravataZKUtils.getZKhostPort();
        zk = new ZooKeeper(zkhostPort, 6000, this);
        synchronized (mutex) {
            mutex.wait();
        }
        List<String> children = zk.getChildren(ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE)
                + File.separator + gfacId, false);
            log.info("------------------ Recovering Experiments started ----------------------------------");
        for (String expId : children) {
            log.info("Recovering Experiment: " + expId.split("\\+")[0]);
            log.info("------------------------------------------------------------------------------------");
            try {
                if(GFacUtils.isCancelled(expId.split("\\+")[0], expId.split("\\+")[1], zk)) {// during relaunching we check the operation and then launch
                    serverHandler.terminateExperiment(expId.split("\\+")[0]);
                }else {
                    serverHandler.launchExperiment(expId.split("\\+")[0], null);
                }
                // we do not move the old experiment in to new gfac node, gfac will do it
            } catch (Exception e) {       // we attempt all the experiments
                log.error(e.getMessage(), e);
            }
            log.info("------------------------------------------------------------------------------------");
        }
    }

    synchronized public void process(WatchedEvent watchedEvent) {
        synchronized (mutex) {
            Event.KeeperState state = watchedEvent.getState();
            switch (state) {
                case SyncConnected:
                    mutex.notify();
                    break;
            }
        }
    }
}
