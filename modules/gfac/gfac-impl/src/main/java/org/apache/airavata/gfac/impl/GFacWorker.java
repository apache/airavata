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

import org.apache.airavata.gfac.core.GFacEngine;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GFacWorker implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(GFacWorker.class);
	private ProcessContext processContext;
	private String experimentId;
	private String processId;
	private String gatewayId;
	private String tokenId;
	private boolean isProcessContextPopulated = false;


	/**
	 * This will be called by monitoring service.
	 * @param processContext
	 * @throws GFacException
	 */
    public   GFacWorker(ProcessContext processContext) throws GFacException {
        if (processContext == null) {
            throw new GFacException("Worker must initialize with valide processContext, Process context is null");
        }
	    this.processContext = processContext;
    }

	/**
	 * This constructor will be called when new or recovery request comes.
	 * @param experimentId
	 * @param processId
	 * @param gatewayId
	 * @param tokenId
	 * @throws GFacException
	 */
	public GFacWorker(String experimentId, String processId, String gatewayId, String tokenId) throws GFacException {
		this.experimentId = experimentId;
		this.processId = processId;
		this.gatewayId = gatewayId;
		this.tokenId = tokenId;
	}

    @Override
    public void run() {
	    try {
		    GFacEngine engine = Factory.getGFacEngine();
		    ProcessType type = getProcessType(processContext);
		    if (processContext == null) {
			    processContext = engine.populateProcessContext(experimentId, processId, gatewayId, tokenId);
			    isProcessContextPopulated = true;
		    }
		    try {
			    switch (type) {
				    case NEW:
					    engine.createTaskChain(processContext);
					    engine.executeProcess(processContext);
					    break;
				    case RECOVER:
					    // recover the process
					    engine.createTaskChain(processContext);
					    engine.recoverProcess(processContext);
					    break;
				    case OUTFLOW:
					    // run the outflow task
					    engine.runProcessOutflow(processContext);
					    break;
				    case RECOVER_OUTFLOW:
					    // recover  outflow task;
					    engine.recoverProcessOutflow(processContext);
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
		    }
	    } catch (GFacException e) {
		    log.error("GFac Worker throws an exception", e);
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
