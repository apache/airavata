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
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GFacWorker implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(GFacWorker.class);
	private final ProcessContext processContext;

    public   GFacWorker(ProcessContext processContext) throws AiravataException {
        if (processContext == null) {
            throw new AiravataException("Worker must initialize with valide processContext, Process context is null");
        }
	    this.processContext = processContext;
    }

    @Override
    public void run() {
	    ProcessType type = getProcessType(processContext);
	    try {
		    switch (type){
			    case NEW:
				    GFacUtils.populateProcessContext(processContext);
				    GFacEngine.createTaskChain(processContext);
				    GFacEngine.executeProcess(processContext);
				    break;
			    case RECOVER:
				    // recover the process
				    GFacEngine.recoverProcess(processContext);
				    break;
			    case OUTFLOW:
				    // run the outflow task
				    GFacEngine.runProcessOutflow(processContext);
				    break;
			    case RECOVER_OUTFLOW:
				    // recover  outflow task;
				    GFacEngine.recoverProcessOutflow(processContext);
		    }
	    } catch (GFacException e) {
		    switch (type) {
			    case NEW: log.error("Process execution error", e); break;
			    case RECOVER: log.error("Process recover error ", e); break;
			    case OUTFLOW: log.error("Process outflow execution error",e); break;
			    case RECOVER_OUTFLOW: log.error("Process outflow recover error",e); break;
		    }
	    }
    }

	private ProcessType getProcessType(ProcessContext processContext) {
		// check the status and return correct type of process.
		return ProcessType.NEW;
	}


	private enum ProcessType {
		NEW,
		RECOVER,
		OUTFLOW,
		RECOVER_OUTFLOW
	}
}
