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

package org.apache.airavata.workflow.tracking.calder;

import org.apache.airavata.commons.LeadCrosscutParametersUtil;
import org.apache.airavata.workflow.tracking.GenericNotifier;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;

/**
 * Convinience interface that groups all methods used to send Lead notification messages from an application (script or
 * service). This extends the generic, provenance, and performance Notifier interfaces. Workflow Notifier interface is
 * kept separate. A typical sequence of usage of this interface would be as follows:
 * 
 * <pre>
 * 
 * [WF1[S1]]
 * 
 *        -- initialize components --
 * WF1    workflowInitialized(baseId, ...)
 * S1     serviceInitialized(baseId, ...)
 * ...
 *        -- invoke workflow --
 * WF1    workflowInvoked(myID, invokerID, input, ...)
 * ...
 *        -- invoke service --
 * WF1    invokingService(myID, invokeeID, input, ...)
 * S1         serviceInvoked(myID, invokerID, input, ...)
 * WF1    invokingServiceSucceeded|Failed(myID, invokeeID, [error], ...)
 * ...
 *            -- perform invocation task --
 * S1         info(...)
 * S1         fileReceiveStarted(F1)
 * S1         -- do gridftp get to stage input files --
 * S1         fileReceiveFinished(F1)
 * S1         fileConsumed(F1)
 * S1         computationStarted(C1)
 * S1         -- perform action/call external application to process input files --
 * S1         computationFinished(C1)
 * S1         fileProduced(F2)
 * S1         fileSendStarted(F2)
 * S1         -- do gridftp put to save output files --
 * S1         fileSendFinished(F2)
 * S1         publishURL(F2)
 * ...
 * S1         sendingResult|Fault(myID, invokerID, output|fault, ...)
 * WF1    receivedResult|Fault(myID, invokeeID, output|fault, ...)
 * S1         sendingResponseSucceeded|Failed(myID, invokerID, [error], ...)
 * S1         flush()
 * ...
 *        -- finished all work --
 * WF1    flush()
 * WF1    workflowTerminated(baseId, ...)
 * S1     serviceTerminated(baseId, ...)
 * </pre>
 */
public interface CalderNotifier extends GenericNotifier {

    public void queryStarted(WorkflowTrackingContext context, String radarName, String... descriptionAndAnnotation);

    public void queryFailedToStart(WorkflowTrackingContext context, String radarName,
            String... descriptionAndAnnotation);

    public void queryActive(WorkflowTrackingContext context, String radarName, String... descriptionAndAnnotation);

    public void queryExpired(WorkflowTrackingContext context, String radarName, String... descriptionAndAnnotation);

    public void triggerFound(WorkflowTrackingContext context, String radarName, String... descriptionAndAnnotation);

    public void queryPublishResult(WorkflowTrackingContext context,
            LeadCrosscutParametersUtil leadCrosscutParametersUtil);

    public void queryNoDetection(WorkflowTrackingContext context, String... descriptionAndAnnotation);

}
