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
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.common.utils.ZkConstants;
import org.apache.airavata.common.utils.listener.AbstractActivityListener;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.cpi.GfacService;
import org.apache.airavata.gfac.cpi.gfac_cpiConstants;
import org.apache.airavata.gfac.impl.Factory;
import org.apache.airavata.gfac.impl.GFacWorker;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Subscriber;
import org.apache.airavata.messaging.core.impl.RabbitMQStatusPublisher;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.ProcessIdentifier;
import org.apache.airavata.model.messaging.event.ProcessStatusChangeEvent;
import org.apache.airavata.model.messaging.event.ProcessSubmitEvent;
import org.apache.airavata.model.messaging.event.TaskSubmitEvent;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
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
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GfacServerHandler implements GfacService.Iface {
    private final static Logger log = LoggerFactory.getLogger(GfacServerHandler.class);
    private Subscriber processLaunchSubscriber;
    private static int requestCount=0;
    private ExperimentCatalog experimentCatalog;
    private AppCatalog appCatalog;
    private String airavataUserName;
    private CuratorFramework curatorClient;
    private Publisher statusPublisher;
    private String airavataServerHostPort;
    private BlockingQueue<TaskSubmitEvent> taskSubmitEvents;
    private static List<AbstractActivityListener> activityListeners = new ArrayList<AbstractActivityListener>();
    private ExecutorService executorService;

    public GfacServerHandler() throws AiravataStartupException {
        try {
	        Factory.loadConfiguration();
            startCuratorClient();
            initZkDataStructure();
            initAMQPClient();
	        executorService = Executors.newFixedThreadPool(ServerSettings.getGFacThreadPoolSize());
        } catch (Exception e) {
            throw new AiravataStartupException("Gfac Server Initialization error ", e);
        }
    }

    private void initAMQPClient() throws AiravataException {
	    // init process consumer
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(ServerSettings.getRabbitmqProcessLaunchQueueName());
        processLaunchSubscriber = MessagingFactory.getSubscriber(new ProcessLaunchMessageHandler(),routingKeys, Subscriber.Type.PROCESS_LAUNCH);
        // init status publisher
	    statusPublisher = new RabbitMQStatusPublisher();
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
        ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), ZkConstants.ZOOKEEPER_EXPERIMENT_NODE);
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
        return gfac_cpiConstants.GFAC_CPI_VERSION;
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
     * @param processId - processModel id in registry
     * @param gatewayId - gateway Identification
     */
    public boolean submitProcess(String processId, String gatewayId, String tokenId) throws
            TException {
        requestCount++;
        log.info("-----------------------------------" + requestCount + "-----------------------------------------");
        log.info(processId, "GFac Received submit job request for the Process: {} process: {}", processId,
                processId);

        try {
	        executorService.execute(new GFacWorker(processId, gatewayId, tokenId));
        } catch (GFacException e) {
            log.error("Failed to submit process", e);
            return false;
        } catch (Exception e) {
	        log.error("Error creating zookeeper nodes");
        }
	    return true;
    }

    @Override
    public boolean cancelProcess(String processId, String gatewayId, String tokenId) throws TException {
        return false;
    }

    private class ProcessLaunchMessageHandler implements MessageHandler {
        private String experimentNode;
        private String gfacServerName;

        public ProcessLaunchMessageHandler() throws ApplicationSettingsException {
            experimentNode = ZkConstants.ZOOKEEPER_EXPERIMENT_NODE;
            gfacServerName = ServerSettings.getGFacServerName();
        }


        public void onMessage(MessageContext message) {
            log.info(" Message Received with message id '" + message.getMessageId()
		            + "' and with message type '" + message.getType());
            if (message.getType().equals(MessageType.LAUNCHPROCESS)) {
	            ProcessStatus status = new ProcessStatus();
	            status.setState(ProcessState.STARTED);
                try {
                    ProcessSubmitEvent event = new ProcessSubmitEvent();
                    TBase messageEvent = message.getEvent();
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
	                if (message.isRedeliver()) {
		                // check the process is already active in this instance.
		                if (Factory.getGfacContext().getProcess(event.getProcessId()) != null) {
			                // update deliver tag
			                try {
				                updateDeliveryTag(curatorClient, gfacServerName, event, message );
				                return;
			                } catch (Exception e) {
				                log.error("Error while updating delivery tag for redelivery message , messageId : " +
						                message.getMessageId(), e);
				                processLaunchSubscriber.sendAck(message.getDeliveryTag());
			                }
		                } else {
			                // read process status from registry
			                ProcessStatus processStatus = ((ProcessStatus) Factory.getDefaultExpCatalog().get
					                (ExperimentCatalogModelType.PROCESS_STATUS, event.getProcessId()));
			                status.setState(processStatus.getState());
			                // write server name to zookeeper , this is happen inside createProcessZKNode(...) method 
		                }
	                }
                    // update process status
	                status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
	                Factory.getDefaultExpCatalog().update(ExperimentCatalogModelType.PROCESS_STATUS, status, event
			                .getProcessId());
	                publishProcessStatus(event, status);
                    try {
                        createProcessZKNode(curatorClient, gfacServerName, event, message);
                        boolean isCancel = setCancelWatcher(curatorClient, event.getExperimentId(), event.getProcessId());
                        if (isCancel) {
                            if (status.getState() == ProcessState.STARTED) {
                                status.setState(ProcessState.CANCELLING);
                                status.setReason("Process Cancel is triggered");
                                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                                Factory.getDefaultExpCatalog().update(ExperimentCatalogModelType.PROCESS_STATUS, status, event.getProcessId());
                                publishProcessStatus(event, status);

                                // do cancel operation here

                                status.setState(ProcessState.CANCELED);
                                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                                Factory.getDefaultExpCatalog().update(ExperimentCatalogModelType.PROCESS_STATUS, status, event.getProcessId());
                                publishProcessStatus(event, status);
                                processLaunchSubscriber.sendAck(message.getDeliveryTag());
                                return;
                            } else {
                                setCancelData(event.getExperimentId(),event.getProcessId());
                            }
                        }
                        submitProcess(event.getProcessId(), event.getGatewayId(), event.getTokenId());
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        processLaunchSubscriber.sendAck(message.getDeliveryTag());
                    }
                } catch (TException e) {
                    log.error(e.getMessage(), e); //nobody is listening so nothing to throw
                } catch (RegistryException e) {
                    log.error("Error while updating experiment status", e);
                } catch (AiravataException e) {
	                log.error("Error while publishing process status", e);
                }
            }
        }
    }

    private void setCancelData(String experimentId, String processId) throws Exception {
        String processCancelNodePath = ZKPaths.makePath(ZKPaths.makePath(ZKPaths.makePath(
                ZkConstants.ZOOKEEPER_EXPERIMENT_NODE, experimentId), processId), ZkConstants.ZOOKEEPER_CANCEL_LISTENER_NODE);
        log.info("expId: {}, processId: {}, set process cancel data to zookeeper node {}", experimentId, processId, processCancelNodePath);
        curatorClient.setData().withVersion(-1).forPath(processCancelNodePath, ZkConstants.ZOOKEEPER_CANCEL_REQEUST
                .getBytes());
    }

    private boolean setCancelWatcher(CuratorFramework curatorClient,
                                     String experimentId,
                                     String processId) throws Exception {

        String experimentNodePath = GFacUtils.getExperimentNodePath(experimentId);
        // /experiments/{experimentId}/cancelListener, set watcher for data changes
        String experimentCancelNode = ZKPaths.makePath(experimentNodePath, ZkConstants.ZOOKEEPER_CANCEL_LISTENER_NODE);
        byte[] bytes = curatorClient.getData().forPath(experimentCancelNode);
        if (bytes != null && new String(bytes).equalsIgnoreCase(ZkConstants.ZOOKEEPER_CANCEL_REQEUST)) {
            return true;
        } else {
            bytes = curatorClient.getData().usingWatcher(Factory.getCancelRequestWatcher(experimentId, processId)).forPath(experimentCancelNode);
            return bytes != null && new String(bytes).equalsIgnoreCase(ZkConstants.ZOOKEEPER_CANCEL_REQEUST);
        }

    }

    private void publishProcessStatus(ProcessSubmitEvent event, ProcessStatus status) throws AiravataException {
		ProcessIdentifier identifier = new ProcessIdentifier(event.getProcessId(),
				event.getExperimentId(),
				event.getGatewayId());
		ProcessStatusChangeEvent processStatusChangeEvent = new ProcessStatusChangeEvent(status.getState(), identifier);
		MessageContext msgCtx = new MessageContext(processStatusChangeEvent, MessageType.PROCESS,
				AiravataUtils.getId(MessageType.PROCESS.name()), event.getGatewayId());
		msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
		statusPublisher.publish(msgCtx);
	}

	private void createProcessZKNode(CuratorFramework curatorClient, String gfacServerName,ProcessSubmitEvent event
			,MessageContext messageContext) throws Exception {
		String processId  = event.getProcessId();
		String token = event.getTokenId();
		String experimentId = event.getExperimentId();
		long deliveryTag = messageContext.getDeliveryTag();

		// create /experiments//{experimentId}{processId} node and set data - serverName, add redelivery listener
		String experimentNodePath = GFacUtils.getExperimentNodePath(experimentId);
		String zkProcessNodePath = ZKPaths.makePath(experimentNodePath, processId);
		ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), zkProcessNodePath);
		curatorClient.setData().withVersion(-1).forPath(zkProcessNodePath, gfacServerName.getBytes());
		curatorClient.getData().usingWatcher(Factory.getRedeliveryReqeustWatcher(experimentId, processId)).forPath(zkProcessNodePath);

        // create /experiments//{experimentId}{processId}/cancelListener
        String zkProcessCancelPath = ZKPaths.makePath(zkProcessNodePath, ZkConstants.ZOOKEEPER_CANCEL_LISTENER_NODE);
        ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), zkProcessCancelPath);

		// create /experiments/{experimentId}/{processId}/deliveryTag node and set data - deliveryTag
		String deliveryTagPath = ZKPaths.makePath(zkProcessNodePath, ZkConstants.ZOOKEEPER_DELIVERYTAG_NODE);
		ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), deliveryTagPath);
		curatorClient.setData().withVersion(-1).forPath(deliveryTagPath, GFacUtils.longToBytes(deliveryTag));

		// create /experiments/{experimentId}/{processId}/token node and set data - token
		String tokenNodePath = ZKPaths.makePath(zkProcessNodePath, ZkConstants.ZOOKEEPER_TOKEN_NODE);
		ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), tokenNodePath);
		curatorClient.setData().withVersion(-1).forPath(tokenNodePath, token.getBytes());

	}

	private void updateDeliveryTag(CuratorFramework curatorClient, String gfacServerName, ProcessSubmitEvent event,
	                               MessageContext messageContext) throws Exception {
		String experimentId = event.getExperimentId();
		String processId = event.getProcessId();
		long deliveryTag = messageContext.getDeliveryTag();
		String processNodePath = ZKPaths.makePath(GFacUtils.getExperimentNodePath(experimentId), processId);
		Stat stat = curatorClient.checkExists().forPath(processNodePath);
		if (stat != null) {
			// create /experiments/{processId}/deliveryTag node and set data - deliveryTag
			String deliveryTagPath = ZKPaths.makePath(processNodePath, ZkConstants.ZOOKEEPER_DELIVERYTAG_NODE);
			curatorClient.setData().withVersion(-1).forPath(deliveryTagPath, GFacUtils.longToBytes(deliveryTag));
		}
	}

}
