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
package org.apache.airavata.gfac.impl;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.gfac.core.GFacEngine;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.List;

public class GFacWorker implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(GFacWorker.class);
	private GFacEngine engine;
	private ProcessContext processContext;
	private String processId;
	private String gatewayId;
	private String tokenId;
	private boolean continueTaskFlow = false;


	/**
	 * This will be called by monitoring service.
	 */
	public GFacWorker(ProcessContext processContext) throws GFacException {
		if (processContext == null) {
			throw new GFacException("Worker must initialize with valid processContext, Process context is null");
		}
		this.processId = processContext.getProcessId();
		this.gatewayId = processContext.getGatewayId();
		this.tokenId = processContext.getTokenId();
		engine = Factory.getGFacEngine();
		this.processContext = processContext;
		continueTaskFlow = true;
	}

	/**
	 * This constructor will be called when new or recovery request comes.
	 */
	public GFacWorker(String processId, String gatewayId, String tokenId) throws GFacException,
			CredentialStoreException, TException {
		this.processId = processId;
		this.gatewayId = gatewayId;
		this.tokenId = tokenId;
		engine = Factory.getGFacEngine();
		this.processContext = engine.populateProcessContext(processId, gatewayId, tokenId);
		Factory.getGfacContext().addProcess(this.processContext);
	}

	@Override
	public void run() {
		RegistryService.Client registryClient = Factory.getRegistryServiceClient();
		try {
			ProcessState processState = processContext.getProcessState();
			switch (processState) {
				case CREATED:
				case VALIDATED:
				case STARTED:
					executeProcess(registryClient);
					break;
				case PRE_PROCESSING:
				case CONFIGURING_WORKSPACE:
				case INPUT_DATA_STAGING:
				case EXECUTING:
				case MONITORING:
                case OUTPUT_DATA_STAGING:
                case POST_PROCESSING:
                    if (continueTaskFlow) {
                        continueTaskExecution(registryClient);
                    } else {
                        recoverProcess(registryClient);
                    }
                    break;
				case COMPLETED:
					completeProcess(registryClient);
					break;
				case CANCELLING:
					cancelProcess(registryClient);
					break;
				case CANCELED:
					// TODO - implement cancel scenario
					break;
				case FAILED:
					// TODO - implement failed scenario
					break;
				default:
					throw new GFacException("process Id : " + processId + " Couldn't identify process type");
			}
			if (processContext.isCancel()) {
				processState = processContext.getProcessState();
				switch (processState) {
					case MONITORING: case EXECUTING:
						// don't send ack if the process is in MONITORING or EXECUTING states, wait until cancel email comes to airavata
						break;
					case CANCELLING:
						cancelProcess(registryClient);
						break;
					default:
						sendAck();
						Factory.getGfacContext().removeProcess(processContext.getProcessId());
						break;
				}
			}
		} catch (GFacException e) {
			log.error("GFac Worker throws an exception", e);
			ProcessStatus status = new ProcessStatus(ProcessState.FAILED);
			status.setReason(e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
			processContext.setProcessStatus(status);
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            ErrorModel errorModel = new ErrorModel();
            errorModel.setUserFriendlyMessage("GFac Worker throws an exception");
            errorModel.setActualErrorMessage(errors.toString());
            errorModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
			try {
				GFacUtils.saveAndPublishProcessStatus(processContext, registryClient);
                GFacUtils.saveExperimentError(processContext, registryClient, errorModel);
                GFacUtils.saveProcessError(processContext, registryClient, errorModel);
			} catch (GFacException|TException e1) {
				log.error("expId: {}, processId: {} :- Couldn't save and publish process status {}", processContext
						.getExperimentId(), processContext.getProcessId(), processContext.getProcessState());
			}
			sendAck();
		} finally {
			if (registryClient != null) {
				ThriftUtils.close(registryClient);
			}
		}
	}

	private void cancelProcess(RegistryService.Client registryClient) throws GFacException {
		// do cleanup works before cancel the process.
		ProcessStatus processStatus = new ProcessStatus(ProcessState.CANCELED);
		processStatus.setReason("Process cancellation has been triggered");
		processContext.setProcessStatus(processStatus);
		GFacUtils.saveAndPublishProcessStatus(processContext, registryClient);
		sendAck();
		Factory.getGfacContext().removeProcess(processContext.getProcessId());
	}

	private void completeProcess(RegistryService.Client registryClient) throws GFacException {
		ProcessStatus status = new ProcessStatus(ProcessState.COMPLETED);
        status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        processContext.setProcessStatus(status);
		GFacUtils.saveAndPublishProcessStatus(processContext, registryClient);
		sendAck();
		Factory.getGfacContext().removeProcess(processContext.getProcessId());
	}

	private void continueTaskExecution(RegistryService.Client registryClient) throws GFacException {
        // checkpoint
        if (processContext.isInterrupted()) {
            return;
        }
        processContext.setPauseTaskExecution(false);
        List<String> taskExecutionOrder = processContext.getTaskExecutionOrder();
        String currentExecutingTaskId = processContext.getCurrentExecutingTaskId();
        boolean found = false;
        String nextTaskId = null;
        for (String taskId : taskExecutionOrder) {
            if (!found) {
                if (taskId.equalsIgnoreCase(currentExecutingTaskId)) {
                    found = true;
                }
                continue;
            } else {
                nextTaskId = taskId;
                break;
            }
        }
        if (nextTaskId != null) {
            engine.continueProcess(processContext, nextTaskId);
        }else {
			processContext.setComplete(true);
		}
        // checkpoint
        if (processContext.isInterrupted()) {
            return;
        }

        if (processContext.isComplete()) {
            completeProcess(registryClient);
        }
    }

	private void recoverProcess(RegistryService.Client registryClient) throws GFacException {
        engine.recoverProcess(processContext);
        if (processContext.isInterrupted()) {
            return;
        }

        if (processContext.isComplete()) {
            completeProcess(registryClient);
        }
    }

	private void executeProcess(RegistryService.Client registryClient) throws GFacException {
		// checkpoint
		if (processContext.isInterrupted()) {
			return;
		}

		engine.executeProcess(processContext);
		// checkpoint
		if (processContext.isInterrupted()) {
			return;
		}

        if (processContext.isComplete()) {
            completeProcess(registryClient);
        }
	}

	private void sendAck() {
		// this ensure, gfac doesn't send ack more than once for a process. which cause to remove gfac rabbitmq consumer from rabbitmq server.
		if (!processContext.isAcknowledge()) {
			try {
                long processDeliveryTag = GFacUtils.getProcessDeliveryTag(processContext.getCuratorClient(),
                        processContext.getExperimentId(), processId);
                Factory.getProcessLaunchSubscriber().sendAck(processDeliveryTag);
                processContext.setAcknowledge(true);
                log.info("expId: {}, processId: {} :- Sent ack for deliveryTag {}", processContext.getExperimentId(),
                        processId, processDeliveryTag);
            } catch (Exception e1) {
                processContext.setAcknowledge(false);
                String format = MessageFormat.format("expId: {0}, processId: {1} :- Couldn't send ack for deliveryTag ",
                        processContext .getExperimentId(), processId);
                log.error(format, e1);
            }
		} else {
			log.info("expId: {}, processId: {} :- already acknowledged ", processContext.getExperimentId(), processId);
		}
	}

}
