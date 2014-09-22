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
package org.apache.airavata.gfac.core.utils;

import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.cpi.GFac;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.core.monitor.TaskIdentity;
import org.apache.airavata.gfac.core.monitor.state.TaskStatusChangeRequest;
import org.apache.airavata.model.workspace.experiment.TaskState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutHandlerWorker implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(OutHandlerWorker.class);

    private GFac gfac;

    private MonitorID monitorID;

    private MonitorPublisher monitorPublisher;

    public OutHandlerWorker(GFac gfac, MonitorID monitorID,MonitorPublisher monitorPublisher) {
        this.gfac = gfac;
        this.monitorID = monitorID;
        this.monitorPublisher = monitorPublisher;
    }

    @Override
    public void run() {
        try {
            gfac.invokeOutFlowHandlers(monitorID.getJobExecutionContext());
        } catch (GFacException e) {
            monitorPublisher.publish(new TaskStatusChangeRequest(new TaskIdentity(monitorID.getExperimentID(), monitorID.getWorkflowNodeID(),
                    monitorID.getTaskID()), TaskState.FAILED));
            //FIXME this is a case where the output retrieving fails even if the job execution was a success. Thus updating the task status
            logger.info(e.getLocalizedMessage(), e);
        }
        monitorPublisher.publish(monitorID.getStatus());
    }
}
