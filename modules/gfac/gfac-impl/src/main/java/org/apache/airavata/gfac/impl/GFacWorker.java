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

package org.apache.airavata.gfac.impl;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.gfac.core.GFacEngine;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.monitor.JobMonitor;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

public class GFacWorker implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(GFacWorker.class);
	private GFacEngine engine;
	private ProcessContext processContext;
	private String processId;
	private String gatewayId;
	private String tokenId;
	private boolean runOutflow = false;


	/**
	 * This will be called by monitoring service.
	 */
	public GFacWorker(ProcessContext processContext) throws GFacException {
		if (processContext == null) {
			throw new GFacException("Worker must initialize with valide processContext, Process context is null");
		}
		this.processId = processContext.getProcessId();
		this.gatewayId = processContext.getGatewayId();
		this.tokenId = processContext.getTokenId();
		engine = Factory.getGFacEngine();
		this.processContext = processContext;
		runOutflow = true;
	}

	/**
	 * This constructor will be called when new or recovery request comes.
	 */
	public GFacWorker(String processId, String gatewayId, String tokenId) throws GFacException {
		this.processId = processId;
		this.gatewayId = gatewayId;
		this.tokenId = tokenId;
		engine = Factory.getGFacEngine();
		this.processContext = engine.populateProcessContext(processId, gatewayId, tokenId);
		Factory.getGfacContext().addProcess(this.processContext);
	}

	@Override
	public void run() {
		try {
			if (processContext.isInterrupted()) {
				GFacUtils.handleProcessInterrupt(processContext);
				if (processContext.isCancel()) {
					sendAck();
					Factory.getGfacContext().removeProcess(processContext.getProcessId());
				}
				return;
			}
			ProcessState processState = processContext.getProcessStatus().getState();
			switch (processState) {
				case CREATED:
				case VALIDATED:
				case STARTED:
					executeProcess();
					break;
				case PRE_PROCESSING:
				case CONFIGURING_WORKSPACE:
				case INPUT_DATA_STAGING:
				case EXECUTING:
					recoverProcess();
					break;
				case MONITORING:
					if (runOutflow) {
						runProcessOutflow();
					} else {
						monitorProcess();
					}
					break;
				case OUTPUT_DATA_STAGING:
				case POST_PROCESSING:
					recoverProcessOutflow();
					break;
				case COMPLETED:
					completeProcess();
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
				sendAck();
				Factory.getGfacContext().removeProcess(processContext.getProcessId());
			}
		} catch (GFacException e) {
			log.error("GFac Worker throws an exception", e);
			ProcessStatus status = new ProcessStatus(ProcessState.FAILED);
			status.setReason(e.getMessage());
			processContext.setProcessStatus(status);
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            ErrorModel errorModel = new ErrorModel();
            errorModel.setUserFriendlyMessage("GFac Worker throws an exception");
            errorModel.setActualErrorMessage(errors.toString());
            errorModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
			try {
				GFacUtils.saveAndPublishProcessStatus(processContext);
                GFacUtils.saveExperimentError(processContext, errorModel);
                GFacUtils.saveProcessError(processContext, errorModel);
			} catch (GFacException e1) {
				log.error("expId: {}, processId: {} :- Couldn't save and publish process status {}", processContext
						.getExperimentId(), processContext.getProcessId(), processContext.getProcessState());
			}
			sendAck();
		}
	}

	private void completeProcess() throws GFacException {
		processContext.setProcessStatus(new ProcessStatus(ProcessState.COMPLETED));
		GFacUtils.saveAndPublishProcessStatus(processContext);
		sendAck();
		Factory.getGfacContext().removeProcess(processContext.getProcessId());
	}

	private void recoverProcessOutflow() throws GFacException {
		engine.recoverProcessOutflow(processContext);
		if (processContext.isInterrupted()) {
			return;
		}
		completeProcess();
	}

	private void runProcessOutflow() throws GFacException {
		engine.runProcessOutflow(processContext);
		if (processContext.isInterrupted()) {
			return;
		}
		completeProcess();
	}

	private void recoverProcess() throws GFacException {
		engine.recoverProcess(processContext);
		if (processContext.isInterrupted()) {
			return;
		}
		monitorProcess();
	}

	private void executeProcess() throws GFacException {
		engine.executeProcess(processContext);
		if (processContext.isInterrupted()) {
			return;
		}
		monitorProcess();
	}

	private void monitorProcess() throws GFacException {
		try {
			JobMonitor monitorService = Factory.getMonitorService(processContext.getMonitorMode());
			if (monitorService != null) {
				monitorService.monitor(processContext.getJobModel().getJobId(), processContext);
				processContext.setProcessStatus(new ProcessStatus(ProcessState.MONITORING));
				GFacUtils.saveAndPublishProcessStatus(processContext);
			} else {
				// we directly invoke outflow
				runProcessOutflow();
			}
		} catch (AiravataException e) {
			throw new GFacException("Error while retrieving moniot service", e);
		}
	}

	private void sendAck() {
		try {
			long processDeliveryTag = GFacUtils.getProcessDeliveryTag(processContext.getCuratorClient(),
					processContext.getExperimentId(), processId);
			Factory.getProcessLaunchConsumer().sendAck(processDeliveryTag);
			log.info("expId: {}, procesId: {} :- Sent ack for deliveryTag {}", processContext.getExperimentId(),
					processId, processDeliveryTag);
		} catch (Exception e1) {
			String format = MessageFormat.format("expId: {0}, processId: {1} :- Couldn't send ack for deliveryTag ",
					processContext .getExperimentId(), processId);
			log.error(format, e1);
		}
	}

}
