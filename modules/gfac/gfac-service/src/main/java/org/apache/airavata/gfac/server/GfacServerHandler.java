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

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.AiravataStartupException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.LocalEventPublisher;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.common.utils.listener.AbstractActivityListener;
import org.apache.airavata.gfac.core.GFacConstants;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.cpi.GfacService;
import org.apache.airavata.gfac.cpi.gfac_cpi_serviceConstants;
import org.apache.airavata.gfac.impl.Factory;
import org.apache.airavata.gfac.impl.GFacWorker;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.MessagingConstants;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.PublisherFactory;
import org.apache.airavata.messaging.core.impl.RabbitMQTaskLaunchConsumer;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.TaskSubmitEvent;
import org.apache.airavata.model.messaging.event.TaskTerminateEvent;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GfacServerHandler implements GfacService.Iface {
    private final static Logger log = LoggerFactory.getLogger(GfacServerHandler.class);
    private RabbitMQTaskLaunchConsumer rabbitMQTaskLaunchConsumer;
    private static int requestCount=0;
    private ExperimentCatalog experimentCatalog;
    private AppCatalog appCatalog;
    private String airavataUserName;
    private CuratorFramework curatorClient;
    private LocalEventPublisher localEventPublisher;
    private String airavataServerHostPort;
    private BlockingQueue<TaskSubmitEvent> taskSubmitEvents;
    private static List<AbstractActivityListener> activityListeners = new ArrayList<AbstractActivityListener>();
    private ExecutorService executorService;

    public GfacServerHandler() throws AiravataStartupException {
        try {
            startCuratorClient();
            initZkDataStructure();
            initAMQPClient();
	        executorService = Executors.newFixedThreadPool(ServerSettings.getGFacThreadPoolSize());
            startStatusUpdators(experimentCatalog, curatorClient, localEventPublisher, rabbitMQTaskLaunchConsumer);
        } catch (Exception e) {
            throw new AiravataStartupException("Gfac Server Initialization error ", e);
        }
    }

    private void initAMQPClient() throws AiravataException {
        rabbitMQTaskLaunchConsumer = new RabbitMQTaskLaunchConsumer();
        rabbitMQTaskLaunchConsumer.listen(new TaskLaunchMessageHandler());
    }

    private void startCuratorClient() throws ApplicationSettingsException {
		curatorClient = Factory.getCuratorClient();
        curatorClient.start();
    }

    private void initZkDataStructure() throws Exception {
        /*
        *|/servers
        *    - /gfac
        *        - /gfac-node0 (localhost:2181)
        *|/experiments
         */
        airavataServerHostPort = ServerSettings.getGfacServerHost() + ":" + ServerSettings.getGFacServerPort();
        // create PERSISTENT nodes
        ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), GFacUtils.getZKGfacServersParentPath());
        ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), GFacConstants.ZOOKEEPER_EXPERIMENT_NODE);
        // create EPHEMERAL server name node
        String gfacName = ServerSettings.getGFacServerName();
        if (curatorClient.checkExists().forPath(ZKPaths.makePath(GFacUtils.getZKGfacServersParentPath() ,gfacName)) == null) {
	        curatorClient.create().withMode(CreateMode.EPHEMERAL).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
			        .forPath(ZKPaths.makePath(GFacUtils.getZKGfacServersParentPath(), gfacName));

        }
	    curatorClient.setData().withVersion(-1).forPath(ZKPaths.makePath(GFacUtils.getZKGfacServersParentPath(),
			    gfacName), airavataServerHostPort.getBytes());

    }

    public String getGFACServiceVersion() throws TException {
        return gfac_cpi_serviceConstants.GFAC_CPI_VERSION;
    }

    /**
     * * After creating the experiment Data and Task Data in the orchestrator
     * * Orchestrator has to invoke this operation for each Task per experiment to run
     * * the actual Job related actions.
     * *
     * * @param experimentID
     * * @param taskID
     * * @param gatewayId:
     * *  The GatewayId is inferred from security context and passed onto gfac.
     * * @return sucess/failure
     * *
     * *
     *
     * @param experimentId - ExperimentModel id in registry
     * @param processId - processModel id in registry
     * @param gatewayId - gateway Identification
     */
    public boolean submitJob(String experimentId, String processId, String gatewayId, String tokenId) throws
            TException {
        requestCount++;
        log.info("-----------------------------------" + requestCount + "-----------------------------------------");
        log.info(experimentId, "GFac Received submit job request for the Experiment: {} process: {}", experimentId,
                processId);

        try {
	        executorService.execute(new GFacWorker(experimentId, processId, gatewayId, tokenId));
        } catch (GFacException e) {
            log.error("Failed to submit process", e);
            return false;
        } catch (Exception e) {
	        log.error("Error creating zookeeper nodes");
        }
	    return true;
    }

    public boolean cancelJob(String experimentId, String taskId, String gatewayId, String tokenId) throws TException {
    /*    log.info(experimentId, "GFac Received cancel job request for Experiment: {} TaskId: {} ", experimentId, taskId);
        try {
            if (BetterGfacImpl.getInstance().cancel(experimentId, taskId, gatewayId, tokenId)) {
                log.debug(experimentId, "Successfully cancelled job, experiment {} , task {}", experimentId, taskId);
                return true;
            } else {
                log.error(experimentId, "Job cancellation failed, experiment {} , task {}", experimentId, taskId);
                return false;
            }
        } catch (Exception e) {
            log.error(experimentId, "Error cancelling the experiment {}.", experimentId);
            throw new TException("Error cancelling the experiment : " + e.getMessage(), e);
        }*/
	    return false;
    }




    public static void startStatusUpdators(ExperimentCatalog experimentCatalog, CuratorFramework curatorClient, LocalEventPublisher publisher,

                                           RabbitMQTaskLaunchConsumer rabbitMQTaskLaunchConsumer) {
       /* try {
            String[] listenerClassList = ServerSettings.getActivityListeners();
            Publisher rabbitMQPublisher = PublisherFactory.createActivityPublisher();
            for (String listenerClass : listenerClassList) {
                Class<? extends AbstractActivityListener> aClass = Class.forName(listenerClass).asSubclass(AbstractActivityListener.class);
                AbstractActivityListener abstractActivityListener = aClass.newInstance();
                activityListeners.add(abstractActivityListener);
                abstractActivityListener.setup(publisher, experimentCatalog, curatorClient, rabbitMQPublisher, rabbitMQTaskLaunchConsumer);
                log.info("Registering listener: " + listenerClass);
                publisher.registerListener(abstractActivityListener);
            }
        } catch (Exception e) {
            log.error("Error loading the listener classes configured in airavata-server.properties", e);
        }*/
    }

    private class TaskLaunchMessageHandler implements MessageHandler {
        private String experimentNode;
        private String gfacServerName;

        public TaskLaunchMessageHandler() throws ApplicationSettingsException {
            experimentNode = GFacConstants.ZOOKEEPER_EXPERIMENT_NODE;
            gfacServerName = ServerSettings.getGFacServerName();
        }

        public Map<String, Object> getProperties() {
            Map<String, Object> props = new HashMap<String, Object>();
            ArrayList<String> keys = new ArrayList<String>();
            keys.add(ServerSettings.getLaunchQueueName());
            keys.add(ServerSettings.getCancelQueueName());
            props.put(MessagingConstants.RABBIT_ROUTING_KEY, keys);
            props.put(MessagingConstants.RABBIT_QUEUE, ServerSettings.getLaunchQueueName());
            return props;
        }

        public void onMessage(MessageContext message) {
            System.out.println(" Message Received with message id '" + message.getMessageId()
                    + "' and with message type '" + message.getType());
            if (message.getType().equals(MessageType.LAUNCHTASK)) {
                try {
                    TaskSubmitEvent event = new TaskSubmitEvent();
                    TBase messageEvent = message.getEvent();
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
                    // update experiment status to executing
                    ExperimentStatus status = new ExperimentStatus();
                    status.setState(ExperimentState.EXECUTING);
                    status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
                    Factory.getDefaultExpCatalog().update(ExperimentCatalogModelType.EXPERIMENT_STATUS, status, event.getExperimentId());
                    try {
	                    GFacUtils.createExperimentNode(curatorClient, gfacServerName, event.getExperimentId(), message.getDeliveryTag(),
			                    event.getTokenId());
                        submitJob(event.getExperimentId(), event.getTaskId(), event.getGatewayId(), event.getTokenId());
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        rabbitMQTaskLaunchConsumer.sendAck(message.getDeliveryTag());
                    }
                } catch (TException e) {
                    log.error(e.getMessage(), e); //nobody is listening so nothing to throw
                } catch (RegistryException e) {
                    log.error("Error while updating experiment status", e);
                }
            } else if (message.getType().equals(MessageType.TERMINATETASK)) {
                TaskTerminateEvent event = new TaskTerminateEvent();
                TBase messageEvent = message.getEvent();
                try {
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
	                boolean success = GFacUtils.setExperimentCancelRequest(event.getExperimentId(), curatorClient,
			                message.getDeliveryTag());
	                if (success) {
		                log.info("expId:{} - Experiment cancel request save successfully", event.getExperimentId());
	                }
                } catch (Exception e) {
	                log.error("expId:" + event.getExperimentId() + " - Experiment cancel reqeust failed", e);
                }finally {
	                try {
		                if (!rabbitMQTaskLaunchConsumer.isOpen()) {
			                rabbitMQTaskLaunchConsumer.reconnect();
		                }
		                rabbitMQTaskLaunchConsumer.sendAck(message.getDeliveryTag());
	                } catch (AiravataException e) {
		                log.error("expId: " + event.getExperimentId() + " - Failed to send acknowledgement back to cancel request.", e);
	                }
                }
            }
        }
    }
}
