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
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.cpi.GFac;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.model.messaging.event.TaskIdentifier;
import org.apache.airavata.model.messaging.event.TaskStatusChangeRequestEvent;
import org.apache.airavata.model.workspace.experiment.CorrectiveAction;
import org.apache.airavata.model.workspace.experiment.ErrorCategory;
import org.apache.airavata.model.workspace.experiment.TaskState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class OutHandlerWorker implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(OutHandlerWorker.class);

    private GFac gfac;

    private MonitorID monitorID;

    private MonitorPublisher monitorPublisher;
    private JobExecutionContext jEC;

    public OutHandlerWorker(GFac gfac, MonitorID monitorID,MonitorPublisher monitorPublisher) {
        this.gfac = gfac;
        this.monitorID = monitorID;
        this.monitorPublisher = monitorPublisher;
        this.jEC = monitorID.getJobExecutionContext();
    }

    public OutHandlerWorker(JobExecutionContext jEC, MonitorPublisher monitorPublisher) {
        this.jEC = jEC;
        this.gfac = jEC.getGfac();
        this.monitorPublisher = monitorPublisher;
    }

    @Override
    public void run() {
        try {
//            gfac.invokeOutFlowHandlers(monitorID.getJobExecutionContext());
            gfac.invokeOutFlowHandlers(jEC);
        } catch (Exception e) {
            TaskIdentifier taskIdentifier = new TaskIdentifier(monitorID.getTaskID(), monitorID.getWorkflowNodeID(),monitorID.getExperimentID(), monitorID.getJobExecutionContext().getGatewayID());
            //FIXME this is a case where the output retrieving fails even if the job execution was a success. Thus updating the task status
            monitorPublisher.publish(new TaskStatusChangeRequestEvent(TaskState.FAILED, taskIdentifier));
            try {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                GFacUtils.saveErrorDetails(monitorID.getJobExecutionContext(),  errors.toString(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
            } catch (GFacException e1) {
                logger.error("Error while persisting error details", e);
            }
            logger.info(e.getLocalizedMessage(), e);
            // Save error details to registry

        }
//        monitorPublisher.publish(monitorID.getStatus());
        monitorPublisher.publish(jEC.getJobDetails().getJobStatus());

    }
}
