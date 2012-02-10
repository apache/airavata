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

package org.apache.airavata.workflow.tracking;

/**
 * Convenience interface that groups all methods used to send Lead notification messages. This extends the application
 * and workflow notifier interfaces.
 * 
 * A typical sequence of usage of this interface would be as follows:
 * 
 * <pre>
 *  WORKFLOW (Using Workflow Notifier Interface)::
 *      workflowStarted(W1)
 *      invokeServiceStarted(S1)
 *      -- invoke service --
 *      -- service invokes application --
 *      APPLICATION (Using Application Notifier Interface):
 *          appStarted(A1)
 *          info(...)
 *          fileReceiveStarted(F1)
 *          -- do gridftp get to stage input files --
 *          fileReceiveFinished(F1)
 *          fileConsumed(F1)
 *          computationStarted(C1)
 *          -- call fortran code to process input files --
 *          computationFinished(C1)
 *          fileProduced(F2)
 *          fileSendStarted(F2)
 *          -- do gridftp put to save output files --
 *          fileSendFinished(F2)
 *          publishURL(F2)
 *          appFinishedSuccess(A1, F2) | appFinishedFailed(A1, ERR)
 *          flush()
 *  WORKFLOW (Using Workflow Notifier Interface):
 *      -- return from service invocation --
 *      invokeServiceFinishedSuccess(S1) | invokeServiceFinishedFailed(S1, ERR)
 *      invokeServiceStarted(S2)
 *      -- invoke service --
 *      ...
 *      workflowFinishedSuccess(W1) | workflowFinishedFailer(W1, ERR)
 *      flush()
 * </pre>
 * 
 * @version $Revision: 1.6 $
 * @author
 */
public interface Notifier extends ProvenanceNotifier, PerformanceNotifier, AuditNotifier, ResourceNotifier {

    // public static final String WORKFLOW_ID = "workflow_tracking.workflow_id";
    // public static final String NODE_ID = "workflow_tracking.node_id";
    // public static final String TIMESTEP = "workflow_tracking.timestep";
    // public static final String SERVICE_ID = "workflow_tracking.service_id";
}
