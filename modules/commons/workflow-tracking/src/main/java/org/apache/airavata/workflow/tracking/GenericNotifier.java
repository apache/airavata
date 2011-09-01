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
import java.util.Properties;

import org.apache.airavata.workflow.tracking.common.InvocationContext;
import org.apache.airavata.workflow.tracking.common.InvocationEntity;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
import org.apache.axis2.addressing.EndpointReference;

/**
 * Utility to create and send generic Lead notification messages. Common messages to send log, debug, information, and
 * exception messages are present.
 * 
 * <pre>
 *          info(...)
 *          publishURL(...)
 *          debug(...)
 *          warning(...)
 *          exception(...)
 *          flush()
 * </pre>
 * 
 * @version $Revision: 1.5 $
 * @author
 */
public interface GenericNotifier {

    /**
     * This is the context used across all notifiers
     * 
     * @param golbalProperties
     * @param epr
     * @return
     */
    public WorkflowTrackingContext createTrackingContext(Properties golbalProperties, String epr,
            URI workflowID, URI serviceID, String workflowNodeID, Integer workflowTimestep);

    /**
     * Method createInitialContext. This is called when this invocation was not initiated by any external entity. This
     * is usually used by the very first initiator of the workflow, which could be some external stand-alone service,
     * the workflow engine itself, a portal, etc. In all other cases, the InvocationContext is created by calling the
     * serviceInvoked or workflowInvoked methods.
     * 
     * @param context
     *            current workflow tracking context, this includes in parameter localEntity an InvocationEntity
     *            representing this entity
     * @return an InvocationContext
     * 
     */
    public InvocationContext createInitialContext(WorkflowTrackingContext context);

    /**
     * Method createInvocationContext. The invocation context is usually created when service/workflowInvoked is called.
     * But it may not be possible to retain this object during the lifecycle of an invocation. For e.g., when a workflow
     * invokes another service, the invocationcontext object created then may not be corelatable with the response
     * message for the invocation. In such cases, the InvocationContext needs to be regenerated using the information
     * available about the Local and remote Entities.
     * 
     * @param context
     *            current workflow tracking context, this includes in parameter localEntity an InvocationEntity
     *            representing this entity
     * @param remoteEntity
     *            an InvocationEntity representing the remote entity
     * 
     * @return an InvocationContext
     * 
     */
    public InvocationContext createInvocationContext(WorkflowTrackingContext context, InvocationEntity remoteEntity);

    /**
     * Method createEntity. Creates an object representing a certain entity that is the initiator, receiver, or
     * responder to an invocation. This object is created for the entity that is invoked and its remote invokee, and/or
     * for the entity and the remote entity it invokes.
     * 
     * 
     * @param workflowID
     *            an URI
     * @param serviceID
     *            an URI
     * @param workflowNodeID
     *            a String
     * @param workflowTimestep
     *            an int
     * 
     * @return an InvocationEntity
     * 
     */
    public InvocationEntity createEntity(URI workflowID, URI serviceID, String workflowNodeID, Integer workflowTimestep);

    /**
     * Send a Log message with level INFO
     * 
     * @param context
     *            current workflow tracking context, this includes in parameter localEntity an InvocationEntity
     *            representing this entity
     * @param entity
     *            identity of this workflow/service's invocation
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void info(WorkflowTrackingContext context, String... descriptionAndAnnotation);

    /**
     * Send a log message with level EXCEPTION.
     * 
     * @param context
     *            current workflow tracking context, this includes in parameter localEntity an InvocationEntity
     *            representing this entity
     * @param entity
     *            identity of this workflow/service's invocation
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void exception(WorkflowTrackingContext context, String... descriptionAndAnnotation);

    /**
     * send a log message with level WARNING.
     * 
     * @param context
     *            current workflow tracking context, this includes in parameter localEntity an InvocationEntity
     *            representing this entity
     * @param entity
     *            identity of this workflow/service's invocation
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void warning(WorkflowTrackingContext context, String... descriptionAndAnnotation);

    /**
     * Send a log message with level DEBUG
     * 
     * @param entity
     *            identity of this workflow/service's invocation
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void debug(WorkflowTrackingContext context, String... descriptionAndAnnotation);

    /**
     * Publishes a notification with the URL and title for a file/directory that is of interest to the user in the
     * context of this workflow/service execution
     * 
     * @param context
     *            current workflow tracking context, this includes in parameter localEntity an InvocationEntity
     *            representing this entity
     * @param entity
     *            identity of this workflow/service's invocation
     * @param title
     *            the caption to be displayed (e.g. as a html link) for this URL
     * @param url
     *            the URL location of the file/directory of interest
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    public void publishURL(WorkflowTrackingContext context, String title, String url,
            String... descriptionAndAnnotation);

    /**
     * Method flush. Blocks till all pending notifications have been sent. This method *has* to be called to ensure that
     * the messages have been sent to the notification broker when asynchronous and/or batch mode of publication is
     * used. This method can be called multiple times.
     * 
     * @param context
     *            current workflow tracking context.
     * 
     */
    public void flush();

    /**
     * terminates all activitis of this notifier...notifier cannot be used after this call.
     * 
     * @param context
     *            current workflow tracking context.
     */
    public void delete();
}
