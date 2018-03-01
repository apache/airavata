/**
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
 */
package org.apache.airavata.gfac.server;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.AiravataStartupException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.logging.MDCConstants;
import org.apache.airavata.common.logging.MDCUtil;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.common.utils.ZkConstants;
import org.apache.airavata.common.utils.listener.AbstractActivityListener;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.gfac.core.GFacConstants;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.cpi.GfacService;
import org.apache.airavata.gfac.cpi.gfac_cpiConstants;
import org.apache.airavata.gfac.impl.Factory;
import org.apache.airavata.gfac.impl.GFacWorker;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Subscriber;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.ProcessIdentifier;
import org.apache.airavata.model.messaging.event.ProcessStatusChangeEvent;
import org.apache.airavata.model.messaging.event.ProcessSubmitEvent;
import org.apache.airavata.model.messaging.event.TaskSubmitEvent;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.PrintWriter;
import java.io.StringWriter;
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
        Factory.initPrcessLaunchSubscriber(new ProcessLaunchMessageHandler());
        processLaunchSubscriber = Factory.getProcessLaunchSubscriber();
        // init status publisher
	    statusPublisher = Factory.getStatusPublisher();
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
        MDC.put(MDCConstants.PROCESS_ID, processId);
        MDC.put(MDCConstants.GATEWAY_ID, gatewayId);
        MDC.put(MDCConstants.TOKEN_ID, tokenId);
        try {
            executorService.execute(MDCUtil.wrapWithMDC(new GFacWorker(processId, gatewayId, tokenId)));
        } catch (GFacException e) {
            log.error("Failed to submit process", e);
            throw new TException("Failed to submit process", e);
        } catch (CredentialStoreException e) {
            log.error("Failed to submit process due to credential issue, " +
                    "make sure you are passing a valid credentials");
            throw new TException("Failed to submit process due to credential issue, " +
                    "make sure you are passing a valid credential token", e);
        } catch (Exception e) {
            log.error("Error creating zookeeper nodes", e);
            throw new TException("Error creating zookeeper nodes", e);
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


        public void onMessage(MessageContext messageContext) {
            MDC.put(MDCConstants.GATEWAY_ID, messageContext.getGatewayId());
            log.info("Message Received with message id {} and with message type: {}" + messageContext.getMessageId(), messageContext.getType());

            if (messageContext.getType().equals(MessageType.LAUNCHPROCESS)) {
                ProcessStatus status = new ProcessStatus();
                status.setState(ProcessState.STARTED);
                RegistryService.Client registryClient = Factory.getRegistryServiceClient();
                try {
                    ProcessSubmitEvent event = new ProcessSubmitEvent();
                    TBase messageEvent = messageContext.getEvent();
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);

                    if (messageContext.isRedeliver()) {
                        log.debug("Message " + messageContext.getMessageId() + " is a redeliver one");
                        // check the process is already active in this instance.
                        if (Factory.getGfacContext().getProcess(event.getProcessId()) != null) {
                            // update deliver tag
                            try {
                                updateDeliveryTag(curatorClient, gfacServerName, event, messageContext );
                                log.debug("Updated delivery tag for message" + messageContext.getMessageId());
                                return;
                            } catch (Exception e) {
                                log.error("Error while updating delivery tag for redelivery message , messageId : " +
                                        messageContext.getMessageId(), e);
                                processLaunchSubscriber.sendAck(messageContext.getDeliveryTag());
                                return;
                            }
                        } else {
                            // read process status from registry
                            ProcessStatus processStatus = registryClient.getProcessStatus(event.getProcessId());
                            status.setState(processStatus.getState());
                            // write server name to zookeeper , this is happen inside createProcessZKNode(...) method
                        }
                    }
                    // update process status
                    status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
                    registryClient.updateProcessStatus(status, event
                            .getProcessId());
                    publishProcessStatus(event, status);
                    MDC.put(MDCConstants.EXPERIMENT_ID, event.getExperimentId());

                    try {
                        createProcessZKNode(curatorClient, gfacServerName, event, messageContext);
                        boolean isCancel = setCancelWatcher(curatorClient, event.getExperimentId(), event.getProcessId());
                        if (isCancel) {
                            log.info("Staring to cancel the process " + event.getProcessId() + " of experiment " + event.getExperimentId());
                            if (status.getState() == ProcessState.STARTED) {
                                status.setState(ProcessState.CANCELLING);
                                status.setReason("Process Cancel is triggered");
                                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                                registryClient.updateProcessStatus(status, event.getProcessId());
                                publishProcessStatus(event, status);

                                // do cancel operation here

                                status.setState(ProcessState.CANCELED);
                                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                                registryClient.updateProcessStatus(status, event.getProcessId());
                                publishProcessStatus(event, status);
                                processLaunchSubscriber.sendAck(messageContext.getDeliveryTag());
                                return;

                            } else {
                                setCancelData(event.getExperimentId(),event.getProcessId());
                            }
                        }

                        try {
                            log.info("Submitting process " + event.getProcessId() + " of experiment " + event.getExperimentId());
                            submitProcess(event.getProcessId(), event.getGatewayId(), event.getTokenId());
                            log.info("Process " + event.getProcessId() + " of experiment " + event.getExperimentId() + " successfully submitted");

                        } catch (TException e) {
                            log.error("Submission of process " + event.getProcessId() + " failed", e);
                            try {
                                submissionErrorHandling(status, event, e);
                            } catch (Exception ex) {
                                // ignore silently as we have nothing to do in this stage other than printing the log
                                log.error("Failed to submit error of process " + event.getProcessId(), ex);
                            } finally {
                                processLaunchSubscriber.sendAck(messageContext.getDeliveryTag());
                            }
                        }

                    } catch (Exception e) {
                        log.error("Failed to prepare the process " + event.getProcessId() + " for submission", e);
                        processLaunchSubscriber.sendAck(messageContext.getDeliveryTag());
                    }

                } catch (Exception e) {
                    log.error("Unknown error while handling the meassage" , e); //nobody is listening so nothing to throw
                    processLaunchSubscriber.sendAck(messageContext.getDeliveryTag());

                } finally {
                    if (registryClient != null) {
                        ThriftUtils.close(registryClient);
                    }
                    MDC.clear();
                }
            }
        }
    }

    private void submissionErrorHandling(ProcessStatus status, ProcessSubmitEvent event, TException e) throws  AiravataException, TException {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        ErrorModel errorModel = new ErrorModel();
        errorModel.setUserFriendlyMessage("Process execution failed");
        errorModel.setActualErrorMessage(errors.toString());
        errorModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
        RegistryService.Client registryClient = Factory.getRegistryServiceClient();

        try {
            errorModel.setErrorId(AiravataUtils.getId("PROCESS_ERROR"));
            registryClient.addErrors(GFacConstants.PROCESS_ERROR, errorModel, event.getProcessId());

            errorModel.setErrorId(AiravataUtils.getId("EXP_ERROR"));
            registryClient.addErrors(GFacConstants.EXPERIMENT_ERROR, errorModel, event.getExperimentId());

            status.setState(ProcessState.FAILED);
            status.setReason("Process execution failed");
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            registryClient
                    .updateProcessStatus(status, event.getProcessId());
            publishProcessStatus(event, status);
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
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
