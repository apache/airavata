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
package org.apache.airavata.gfac.server;

import com.google.common.eventbus.EventBus;
import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.cpi.BetterGfacImpl;
import org.apache.airavata.gfac.core.cpi.GFac;
import org.apache.airavata.gfac.core.cpi.GFacImpl;
import org.apache.airavata.gfac.core.notification.MonitorPublisher;
import org.apache.airavata.gfac.cpi.GfacService;
import org.apache.airavata.gfac.cpi.gfac_cpi_serviceConstants;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.api.AiravataRegistryFactory;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.exception.RegException;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Random;


public class GfacServerHandler implements GfacService.Iface, Watcher{
    private final static Logger logger = LoggerFactory.getLogger(GfacServerHandler.class);

    private Registry registry;

    private String registryURL;

    private String gatewayName;

    private String airavataUserName;

    private ZooKeeper zk;

    private boolean connected = false;

    private static Integer mutex = new Integer(-1);

    private MonitorPublisher publisher;


    public GfacServerHandler() {
        // registering with zk
        try {
            String zkhostPort = AiravataZKUtils.getZKhostPort();
            String airavataServerHostPort = ServerSettings.getSetting(Constants.GFAC_SERVER_HOST)
                    + ":" + ServerSettings.getSetting(Constants.GFAC_SERVER_PORT);
            try {
                zk = new ZooKeeper(zkhostPort, 6000, this);   // no watcher is required, this will only use to store some data
                String gfacServer = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NODE,"/gfac-server");
                String gfacExperiments = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE,"/gfac-experiments");
                synchronized(mutex){
                    mutex.wait();  // waiting for the syncConnected event
                }
                Stat zkStat = zk.exists(gfacServer, false);
                if (zkStat == null) {
                    zk.create(gfacServer, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                }
                String instanceId = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NAME);
                String instantNode = gfacServer + File.separator + instanceId;
                zkStat = zk.exists(instantNode, false);
                if (zkStat == null) {
                    zk.create(instantNode,
                            airavataServerHostPort.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.EPHEMERAL);      // other component will watch these childeren creation deletion to monitor the status of the node
                }
                zkStat = zk.exists(gfacExperiments, false);
                if (zkStat == null) {
                    zk.create(gfacExperiments,
                            airavataServerHostPort.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                }
                zkStat = zk.exists(gfacExperiments + File.separator + instanceId, false);
                if (zkStat == null) {
                    zk.create(gfacExperiments + File.separator + instanceId,
                            airavataServerHostPort.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                }else{
                    logger.error(" Zookeeper is inconsistent state  !!!!!");
                }
                logger.info("Finished starting ZK: " + zk);
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
        try {
            publisher = new MonitorPublisher(new EventBus());
            BetterGfacImpl.setMonitorPublisher(publisher);
            registry = RegistryFactory.getDefaultRegistry();
            setGatewayProperties();
            BetterGfacImpl.startDaemonHandlers();
            BetterGfacImpl.startStatusUpdators(registry,zk,publisher);
        }catch (Exception e){
           logger.error("Error initialising GFAC",e);
        }
    }

    synchronized public void process(WatchedEvent watchedEvent) {
        synchronized (mutex) {
            Event.KeeperState state = watchedEvent.getState();
            if (state == Event.KeeperState.SyncConnected) {
                mutex.notify();
                connected = true;
            } else if(state == Event.KeeperState.Expired ||
                    state == Event.KeeperState.Disconnected){
                try {
                    zk = new ZooKeeper(AiravataZKUtils.getZKhostPort(),6000,this);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ApplicationSettingsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getGFACServiceVersion() throws TException {
        return gfac_cpi_serviceConstants.GFAC_CPI_VERSION;
    }

    public boolean submitJob(String experimentId, String taskId) throws TException {
        GFac gfac = getGfac();
        try {
            return gfac.submitJob(experimentId, taskId);
        } catch (GFacException e) {
            throw new TException("Error launching the experiment : " + e.getMessage(), e);
        }
    }

    public boolean cancelJob(String experimentId, String taskId) throws TException {
        throw new TException("Operation not supported");
    }


    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public String getRegistryURL() {
        return registryURL;
    }

    public void setRegistryURL(String registryURL) {
        this.registryURL = registryURL;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getAiravataUserName() {
        return airavataUserName;
    }

    public void setAiravataUserName(String airavataUserName) {
        this.airavataUserName = airavataUserName;
    }
    protected void setGatewayProperties() throws ApplicationSettingsException {
         setAiravataUserName(ServerSettings.getProperties().getProperty("system.user"));
         setGatewayName(ServerSettings.getProperties().getProperty("system.gateway"));
         setRegistryURL(ServerSettings.getProperties().getProperty("airavata.server.url"));
     }

    private GFac getGfac()throws TException{
        try {
            return new BetterGfacImpl(registry, null,
                                AiravataRegistryFactory.getRegistry(new Gateway(getGatewayName()),
                                        new AiravataUser(getAiravataUserName())),zk,publisher);
        } catch (RegException e) {
            throw new TException("Error initializing gfac instance",e);
        } catch (AiravataConfigurationException e) {
            throw new TException("Error initializing gfac instance",e);
        }
    }
}
