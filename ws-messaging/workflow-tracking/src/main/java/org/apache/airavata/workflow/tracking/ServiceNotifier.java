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
 * Utility to create and send Lead provenance related notification messages. this tracks files used, produced, and
 * transfered by this application, application begin and end.
 * 
 * <pre>
 *          appStarted(A1)
 *          fileConsumed(F1)
 *          fileProduced(F2)
 *          appFinishedSuccess(A1, F2) | appFinishedFailed(A1, ERR)
 *          flush()
 * </pre>
 */

public interface ServiceNotifier extends GenericNotifier {

    /**
     * send a message indicating the service has been instantiated and will accept messages henceforth.
     * 
     * @param serviceID
     *            an URI that identifies the service instance
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void serviceInitialized(WorkflowTrackingContext context, URI serviceID, String... descriptionAndAnnotation);

    /**
     * send a message indicating the service instance has terminated and will not accept or generate any future
     * messages.
     * 
     * @param serviceID
     *            an URI that identifies the service instance
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void serviceTerminated(WorkflowTrackingContext context, URI serviceID, String... descriptionAndAnnotation);

    /**
     * Method serviceInvoked. This service instance has received a message from the initiator to start executing.
     * 
     * @param receiver
     *            identity of this service invocation
     * @param initiator
     *            identity of entity that invoked this service
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return an InvocationContext that encapsulates this invocation. This object should be passed to any notification
     *         that is generated as part of this invocation.
     * 
     */
    public InvocationContext serviceInvoked(WorkflowTrackingContext context, InvocationEntity receiver,
            String... descriptionAndAnnotation);

    /**
     * Method serviceInvoked. This service instance has received a message from the initiator to start executing.
     * 
     * @param receiver
     *            identity of this service invocation
     * @param initiator
     *            identity of entity that invoked this service
     * @param header
     *            the context for this invocation (soap:header)
     * @param body
     *            the message received that has the actions and inputs (soap:body)
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return an InvocationContext that encapsulates this invocation. This object should be passed to any notification
     *         that is generated as part of this invocation.
     * 
     */
    public InvocationContext serviceInvoked(WorkflowTrackingContext wtcontext, InvocationEntity initiator,
            XmlObject header, XmlObject body, String... descriptionAndAnnotation);

    /**
     * Method sendingResult. Return the result of the invocation back to the initiator of the invocation. This happens
     * when a request-response pattern is followed.
     * 
     * @param context
     *            the context for this invocation as returned by serviceInvoked
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void sendingResult(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation);

    /**
     * Method sendingResult. Return the result of the invocation back to the initiator of the invocation. This happens
     * when a request-response pattern is followed.
     * 
     * @param context
     *            the context for this invocation as returned by serviceInvoked
     * @param header
     *            the context for the response to the invocation (soap:header)
     * @param body
     *            the message that is sent as output of the invocation (soap:body)
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void sendingResult(WorkflowTrackingContext wtcontext, InvocationContext context, XmlObject header,
            XmlObject body, String... descriptionAndAnnotation);

    /**
     * Method sendingFault. Return a fault as the response to the invocation, sent back to the initiator of the
     * invocation. This happens when a request-response pattern is followed.
     * 
     * @param context
     *            the context for this invocation as returned by serviceInvoked
     * @param error
     *            human readable description of the failure to be sent with the message
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * 
     */
    public void sendingFault(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation);

    /**
     * Method sendingFault. Return a fault as the response to the invocation, sent back to the initiator of the
     * invocation. This happens when a request-response pattern is followed.
     * 
     * @param context
     *            the context for this invocation as returned by serviceInvoked
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
    public void sendingFault(WorkflowTrackingContext wtcontext, InvocationContext context, XmlObject header,
            XmlObject faultBody, String... descriptionAndAnnotation);

    /**
     * send sendingResponseSucceeded message. Acknowledges that the response to this invocation was successfully sent to
     * the initator of the invocation.
     * 
     * @param context
     *            the context for this invocation as returned by serviceInvoked
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void sendingResponseSucceeded(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation);

    /**
     * send sendingResponseSucceeded message. Acknowledges that the response to this invocation could not be sent to the
     * initator of the invocation. Can be a local failure or a remote failure.
     * 
     * @param context
     *            the context for this invocation as returned by serviceInvoked
     * @param error
     *            human readable description of the failure to be sent with the message
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void sendingResponseFailed(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation);

    /**
     * send sendingResponseSucceeded message. Acknowledges that the response to this invocation could not be sent to the
     * initator of the invocation. Can be a local failure or a remote failure.
     * 
     * @param context
     *            the context for this invocation as returned by serviceInvoked
     * @param trace
     *            a throwable that has the trace for the error
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void sendingResponseFailed(WorkflowTrackingContext wtcontext, InvocationContext context, Throwable trace,
            String... descriptionAndAnnotation);

}
