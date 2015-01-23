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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.gfac.client.GFACInstance;
import org.apache.airavata.gfac.client.GFacClientFactory;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.cpi.GfacService;
import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.thrift.TException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * this class is responsible for submitting a job to gfac in service mode,
 * it will select a gfac instance based on the incoming request and submit to that
 * gfac instance.
 */
public class GFACServiceJobSubmitter implements JobSubmitter, Watcher {
	private final static Logger logger = LoggerFactory.getLogger(GFACServiceJobSubmitter.class);
	public static final String IP = "ip";

	private OrchestratorContext orchestratorContext;

	private static Integer mutex = -1;

	public void initialize(OrchestratorContext orchestratorContext) throws OrchestratorException {
		this.orchestratorContext = orchestratorContext;
	}

	public GFACInstance selectGFACInstance() throws OrchestratorException {
		// currently we only support one instance but future we have to pick an
		// instance
		return null;
	}

	public boolean submit(String experimentID, String taskID) throws OrchestratorException {
		return this.submit(experimentID, taskID, null);
	}

	public boolean submit(String experimentID, String taskID, String tokenId) throws OrchestratorException {
		ZooKeeper zk = orchestratorContext.getZk();
        GfacService.Client gfacClient = null;
		try {
			if (zk == null || !zk.getState().isConnected()) {
				String zkhostPort = AiravataZKUtils.getZKhostPort();
				zk = new ZooKeeper(zkhostPort, 6000, this);
				synchronized (mutex) {
					mutex.wait();
				}
			}
			String gfacServer = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NODE, "/gfac-server");
			String experimentNode = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
			List<String> children = zk.getChildren(gfacServer, this);
			
			if (children.size() == 0) {
                // Zookeeper data need cleaning
                throw new OrchestratorException("There is no active GFac instance to route the request");
            } else {
				String pickedChild = children.get(new Random().nextInt(Integer.MAX_VALUE) % children.size());
				// here we are not using an index because the getChildren does not return the same order everytime
				String gfacNodeData = new String(zk.getData(gfacServer + File.separator + pickedChild, false, null));
				logger.info("GFAC instance node data: " + gfacNodeData);
				String[] split = gfacNodeData.split(":");
				gfacClient = GFacClientFactory.createGFacClient(split[0], Integer.parseInt(split[1]));
				if (zk.exists(gfacServer + File.separator + pickedChild, false) != null) {
					// before submitting the job we check again the state of the node
					if (GFacUtils.createExperimentEntry(experimentID, taskID, zk, experimentNode, pickedChild, tokenId)) {
						 String gatewayId = null;
                    	 CredentialReader credentialReader = GFacUtils.getCredentialReader();
                         if (credentialReader != null) {
                             try {
                            	 gatewayId = credentialReader.getGatewayID(tokenId);
                             } catch (Exception e) {
                                 logger.error(e.getLocalizedMessage());
                             }
                         }
                        if(gatewayId == null || gatewayId.isEmpty()){
                         gatewayId = ServerSettings.getDefaultUserGateway();
                        }
						return gfacClient.submitJob(experimentID, taskID, gatewayId);
					}
				}
			}
		} catch (TException e) {
            logger.error(e.getMessage(), e);
			throw new OrchestratorException(e);
		} catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new OrchestratorException(e);
		} catch (KeeperException e) {
            logger.error(e.getMessage(), e);
            throw new OrchestratorException(e);
		} catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new OrchestratorException(e);
		} catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new OrchestratorException(e);
		} catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new OrchestratorException(e);
		}finally {
            gfacClient.getOutputProtocol().getTransport().close();
        }
        return false;
	}

    public boolean terminate(String experimentID, String taskID) throws OrchestratorException {
        ZooKeeper zk = orchestratorContext.getZk();
        GfacService.Client localhost = null;
        try {
            if (zk == null || !zk.getState().isConnected()) {
                String zkhostPort = AiravataZKUtils.getZKhostPort();
                zk = new ZooKeeper(zkhostPort, 6000, this);
                synchronized (mutex) {
                    mutex.wait();
                }
            }
            String gfacServer = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NODE, "/gfac-server");
            String experimentNode = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
            List<String> children = zk.getChildren(gfacServer, this);

            if (children.size() == 0) {
                // Zookeeper data need cleaning
                throw new OrchestratorException("There is no active GFac instance to route the request");
            } else {
                String pickedChild = children.get(new Random().nextInt(Integer.MAX_VALUE) % children.size());
                // here we are not using an index because the getChildren does not return the same order everytime
                String gfacNodeData = new String(zk.getData(gfacServer + File.separator + pickedChild, false, null));
                logger.info("GFAC instance node data: " + gfacNodeData);
                String[] split = gfacNodeData.split(":");
                localhost = GFacClientFactory.createGFacClient(split[0], Integer.parseInt(split[1]));
                if (zk.exists(gfacServer + File.separator + pickedChild, false) != null) {
                    // before submitting the job we check again the state of the node
                    if (GFacUtils.createExperimentEntry(experimentID, taskID, zk, experimentNode, pickedChild, null)) {
                        return localhost.cancelJob(experimentID, taskID);
                    }
                }
            }
        } catch (TException e) {
            logger.error(e.getMessage(), e);
            throw new OrchestratorException(e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new OrchestratorException(e);
        } catch (KeeperException e) {
            logger.error(e.getMessage(), e);
            throw new OrchestratorException(e);
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new OrchestratorException(e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new OrchestratorException(e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new OrchestratorException(e);
        }finally {

        }
        return false;
    }

    synchronized public void process(WatchedEvent event) {
		synchronized (mutex) {
			switch (event.getState()) {
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
