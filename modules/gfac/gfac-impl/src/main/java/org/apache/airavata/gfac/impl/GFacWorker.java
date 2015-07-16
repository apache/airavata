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
import org.apache.airavata.gfac.core.GFac;
import org.apache.airavata.gfac.core.GFacEngine;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.monitor.JobMonitor;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public class GFacWorker implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(GFacWorker.class);
	private ProcessContext processContext;
	private String processId;
	private String gatewayId;
	private String tokenId;
	private boolean isProcessContextPopulated = false;


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
		this.processContext = processContext;
	}

	/**
	 * This constructor will be called when new or recovery request comes.
	 */
	public GFacWorker(String processId, String gatewayId, String tokenId) throws GFacException {
		this.processId = processId;
		this.gatewayId = gatewayId;
		this.tokenId = tokenId;
	}

	@Override
	public void run() {
		try {
			GFacEngine engine = Factory.getGFacEngine();
			if (processContext == null) {
				processContext = engine.populateProcessContext(processId, gatewayId, tokenId);
				isProcessContextPopulated = true;
			}
			ProcessType type = getProcessType(processContext);
			try {
				switch (type) {
					case NEW:
						exectuteProcess(engine);
						break;
					case RECOVER:
						recoverProcess(engine);
						break;
					case OUTFLOW:
						// run the outflow task
						engine.runProcessOutflow(processContext);
						processContext.setProcessStatus(new ProcessStatus(ProcessState.COMPLETED));
						GFacUtils.saveAndPublishProcessStatus(processContext);
						sendAck();
						break;
					case RECOVER_OUTFLOW:
						// recover  outflow task;
						engine.recoverProcessOutflow(processContext);
						processContext.setProcessStatus(new ProcessStatus(ProcessState.COMPLETED));
						GFacUtils.saveAndPublishProcessStatus(processContext);
						sendAck();
						break;
					default:
						throw new GFacException("process Id : " + processId + " Couldn't identify process type");
				}
			} catch (GFacException e) {
				switch (type) {
					case NEW:
						log.error("Process execution error", e);
						break;
					case RECOVER:
						log.error("Process recover error ", e);
						break;
					case OUTFLOW:
						log.error("Process outflow execution error", e);
						break;
					case RECOVER_OUTFLOW:
						log.error("Process outflow recover error", e);
						break;
				}
				throw e;
			}
		} catch (GFacException e) {
			log.error("GFac Worker throws an exception", e);
			processContext.setProcessStatus(new ProcessStatus(ProcessState.FAILED));
			try {
				GFacUtils.saveAndPublishProcessStatus(processContext);
			} catch (GFacException e1) {
				log.error("expId: {}, processId: {} :- Couldn't save and publish process status {}", processContext
						.getExperimentId(), processContext.getProcessId(), processContext.getProcessState());
			}
			sendAck();
		}
	}

	private void recoverProcess(GFacEngine engine) throws GFacException {
		// recover the process
		//	engine.recoverProcess(processContext);
		exectuteProcess(engine); // TODO - implement recover process.
	}

	private void exectuteProcess(GFacEngine engine) throws GFacException {
		engine.executeProcess(processContext);
		if (processContext.getMonitorMode() == null) {
			engine.runProcessOutflow(processContext);
		} else {
			try {
				JobMonitor monitorService = Factory.getMonitorService(processContext.getMonitorMode());
				if (monitorService != null) {
					monitorService.monitor(processContext.getJobModel().getJobId(), processContext);
					processContext.setProcessStatus(new ProcessStatus(ProcessState.MONITORING));
				} else {
					// we directly invoke outflow
					engine.runProcessOutflow(processContext);
				}
			} catch (AiravataException e) {
				throw new GFacException("Error while retrieving moniot service", e);
			}
		}
	}

	private void sendAck() {
		try {
			long processDeliveryTag = GFacUtils.getProcessDeliveryTag(processContext.getCuratorClient(), processId);
			Factory.getProcessLaunchConsumer().sendAck(processDeliveryTag);
			log.info("expId: {}, procesId: {} :- Sent ack for deliveryTag {}", processContext.getExperimentId(),
					processId, processDeliveryTag);
		} catch (Exception e1) {
			String format = MessageFormat.format("expId: {0}, processId: {1} :- Couldn't send ack for deliveryTag ",
					processContext .getExperimentId(), processId);
			log.error(format, e1);
		}
	}

	private ProcessType getProcessType(ProcessContext processContext) {
		// check the status and return correct type of process.
		switch (processContext.getProcessState()) {
			case CREATED:
			case VALIDATED:
				return ProcessType.NEW;
			case PRE_PROCESSING:
			case CONFIGURING_WORKSPACE:
			case INPUT_DATA_STAGING:
			case EXECUTING:
				return ProcessType.RECOVER;
			case MONITORING:
				if (isProcessContextPopulated) {
					return ProcessType.RECOVER; // hand over to monitor task
				} else {
					return ProcessType.OUTFLOW; // execute outflow
				}
			case OUTPUT_DATA_STAGING:
			case POST_PROCESSING:
				return ProcessType.RECOVER_OUTFLOW;
			case COMPLETED:
			case CANCELED:
			case FAILED:
				return ProcessType.COMPLETED;
			//case CANCELLING: // TODO: handle this
			default:
				// this will never hit as we have handle all states in cases.
				return ProcessType.NEW;
		}
	}


	private enum ProcessType {
		NEW,
		RECOVER,
		OUTFLOW,
		RECOVER_OUTFLOW,
		COMPLETED
	}
}
