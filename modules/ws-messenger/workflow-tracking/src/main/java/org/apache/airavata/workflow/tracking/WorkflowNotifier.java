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

import java.net.URI;

import org.apache.airavata.workflow.tracking.common.InvocationContext;
import org.apache.airavata.workflow.tracking.common.InvocationEntity;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
import org.apache.xmlbeans.XmlObject;

/**
 * Utility to create and send Lead notification messages from a Workflow Engine
 * 
 * A typical sequence of usage of this interface would be as follows:
 * 
 * <pre>
 *  WORKFLOW (Using this:Workflow Notifier)::
 *      workflowStarted(W1)
 *      invokeServiceStarted(S1)
 *      -- invoke service --
 *      -- service invokes application --
 *      APPLICATION (Using Notifier Interface):
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
 *  WORKFLOW (Using this:Workflow Notifier):
 *      -- return from service invocation --
 *      invokeServiceFinishedSuccess(S1) | invokeServiceFinishedFailed(S1, ERR)
 *      invokeServiceStarted(S2)
 *      -- invoke service --
 *      ...
 *      workflowFinishedSuccess(W1) | workflowFinishedFailer(W1, ERR)
 *      flush()
 * </pre>
 * 
 * @version $Revision: 1.4 $
 * @author
 */
public interface WorkflowNotifier extends ServiceNotifier {

    /**
     * send a message indicating the workflow has been instantiated and will accept messages henceforth.
     * 
     * @param context
     *            current workflow tracking context.
     * @param serviceID
     *            an URI that identifies the workflow instance
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void workflowInitialized(WorkflowTrackingContext context, URI serviceID, String... descriptionAndAnnotation);

    /**
     * send a message indicating the workflow instance has terminated and will not accept or generate any future
     * messages.
     * 
     * @param context
     *            current workflow tracking context.
     * @param serviceID
     *            an URI that identifies the workflow instance
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void workflowTerminated(WorkflowTrackingContext context, URI serviceID, String... descriptionAndAnnotation);

    /**
     * Method workflowInvoked. This workflow has received a message from the initiator to start executing.
     * 
     * @param context
     *            current workflow tracking context.
     * @param receiver
     *            identity of this workflow invocation
     * @param initiator
     *            identity of entity that invoked this workflow
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return an InvocationContext that encapsulates this invocation. This object should be passed to any notification
     *         that is generated as part of this invocation. It can also be used as the InvocationEntity for this
     *         workflow in future notifications.
     * 
     */
    public InvocationContext workflowInvoked(WorkflowTrackingContext context, InvocationEntity initiator,
            String... descriptionAndAnnotation);

    /**
     * Method workflowInvoked. This workflow has received a message from the initiator to start executing.
     * 
     * @param context
     *            current workflow tracking context.
     * @param receiver
     *            identity of this workflow invocation
     * @param initiator
     *            identity of entity that invoked this workflow
     * @param header
     *            the context within which this invocation takes place (soap:header)
     * @param body
     *            the message that causes this invocation (soap:body)
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return an InvocationContext that encapsulates this invocation. This object should be passed to any notification
     *         that is generated as part of this invocation.
     * 
     */
    public InvocationContext workflowInvoked(WorkflowTrackingContext context, InvocationEntity initiator,
            XmlObject header, XmlObject body, String... descriptionAndAnnotation);

    /**
     * Method invokingService. This workflow is initiating an invocation upon another service.
     * 
     * @param context
     *            current workflow tracking context.
     * @param initiator
     *            identity of this workflow invocation
     * @param receiver
     *            identity of entity that this workflow is invoking
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return an InvocationContext that encapsulates this invocation. This object should be passed to any notification
     *         that is generated as part of this invocation.
     * 
     */
    public InvocationContext invokingService(WorkflowTrackingContext context, InvocationEntity receiver,
            String... descriptionAndAnnotation);

    /**
     * Method invokingService. This workflow is initiating an invocation upon another service.
     * 
     * @param context
     *            current workflow tracking context.
     * @param initiator
     *            identity of this workflow invocation
     * @param receiver
     *            identity of entity that this workflow is invoking
     * @param header
     *            the context for this invocation (soap:header)
     * @param body
     *            the message that is sent with the action and input to start the invocation (soap:body)
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return an InvocationContext that encapsulates this invocation. This object should be passed to any notification
     *         that is generated as part of this invocation.
     * 
     */
    public InvocationContext invokingService(WorkflowTrackingContext context, InvocationEntity receiver,
            XmlObject header, XmlObject body, String... descriptionAndAnnotation);

    /**
     * Method invokingServiceSucceeded. Acnowledge that the invocation request was received by the remote service
     * successfully.
     * 
     * @param context
     *            current workflow tracking context.
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @param context
     *            the context for this invocation as returned by invokeService
     * 
     */
    public void invokingServiceSucceeded(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation);

    /**
     * Method invokingServiceFailed. Report that the invocation request could not be sent to the remote service. Can be
     * a local failure or a remote failure.
     * 
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @param wtcontext
     *            current workflow tracking context.
     * @param context
     *            the context for this invocation as returned by invokeService
     * @param error
     *            a String describing the error
     * 
     */
    public void invokingServiceFailed(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation);

    /**
     * Method invokingServiceFailed. Report that the invocation request could not be sent to the remote service. Can be
     * a local failure or a remote failure.
     * 
     * @param wtcontext
     *            current workflow tracking context.
     * @param context
     *            the context for this invocation as returned by invokeService
     * @param trace
     *            a throwable that has the trace for the error
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void invokingServiceFailed(WorkflowTrackingContext wtcontext, InvocationContext context, Throwable trace,
            String... descriptionAndAnnotation);

    /**
     * Method receivedResult. Indicates that the invocation that this workflow initiated has received a response. This
     * happens when a request-response pattern is followed.
     * 
     * @param wtcontext
     *            current workflow tracking context.
     * @param context
     *            the context for this invocation as returned by invokeService
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void receivedResult(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation);

    /**
     * Method receivedResult. Indicates that the invocation that this workflow initiated has received a response. This
     * happens when a request-response pattern is followed.
     * 
     * @param wtcontext
     *            current workflow tracking context.
     * @param context
     *            the context for this invocation as returned by invokeService
     * @param header
     *            the context for the response to the invocation (soap:header)
     * @param body
     *            the message that is received as output of the invocation (soap:body)
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void receivedResult(WorkflowTrackingContext wtcontext, InvocationContext context, XmlObject header,
            XmlObject body, String... descriptionAndAnnotation);

    /**
     * Method receivedResult. Indicates that the invocation that this workflow initiated has received a fault as
     * response. This happens when a request-response pattern is followed.
     * 
     * @param wtcontext
     *            current workflow tracking context.
     * @param context
     *            the context for this invocation as returned by invokeService
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void receivedFault(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation);

    /**
     * Method receivedResult. Indicates that the invocation that this workflow initiated has received a fault as
     * response. This happens when a request-response pattern is followed.
     * 
     * @param wtcontext
     *            current workflow tracking context.
     * @param context
     *            the context for this invocation as returned by invokeService
     * @param header
     *            the context for the response to the invocation (soap:header)
     * @param fault
     *            the fault that is sent as output of the invocation (soap:fault)
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void receivedFault(WorkflowTrackingContext wtcontext, InvocationContext context, XmlObject header,
            XmlObject faultBody, String... descriptionAndAnnotation);

}
