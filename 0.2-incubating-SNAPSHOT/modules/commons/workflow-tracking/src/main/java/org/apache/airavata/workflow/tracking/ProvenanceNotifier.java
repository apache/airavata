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
import java.util.List;

import org.apache.airavata.workflow.tracking.common.DataObj;
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
public interface ProvenanceNotifier extends WorkflowNotifier, GenericNotifier {

    /**
     * Sends a notification indicating that a file/directory was used during this invocation. The dataId for this
     * file/directory recorded with the naming service may be passed, along with the absolute path to the local
     * file/directory. This will append a mapping from the dataId to the local file with the resolver (or create a new
     * dataId if dataId not passed) A message with the (created or passed) dataId, local path, and size of the local
     * file/directory is sent.
     * 
     * @param context
     *            current workflow tracking context, this includes in parameter entity the identity of the
     *            workflow/service's invocation that consumed this file
     * @param dataId
     *            the dataId for this file/dir (registered with the name resolver if available).
     * @param locations
     *            the list of URLs to the replicas of this file/dir. the first URL should be the absolute path to this
     *            file/dir on the local host, in the form of file://localhostName/path/to/file.txt
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return a DataObj recording the dataId, local path, timestamp and size of file/dir
     * 
     */
    public DataObj dataConsumed(WorkflowTrackingContext context, URI dataId, List<URI> locations,
            String... descriptionAndAnnotation);

    public DataObj dataConsumed(WorkflowTrackingContext context, URI dataId, List<URI> locations, int sizeInBytes,
            String... descriptionAndAnnotation);

    /**
     * Sends a notification indicating that a file/directory was used by this invocation. A fileobj representing this
     * file and returned by another file method should be passed. This will append a mapping from the dataId to the
     * local file with the resolver (or create a new dataId if leadId not already set) if not already done. A message
     * with the leadId, local path, and size of the local file/directory is sent.
     * 
     * @param context
     *            current workflow tracking context, this includes in parameter entity the identity of the
     *            workflow/service's invocation that consumed this file
     * @param dataObj
     *            data object recording the dataId, local/remote URLs, timestamp of the file/dir, that was returned by
     *            another data notification method
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return the file object passed to this method with file/dir size filled in if not already when passed.
     * 
     */
    public DataObj dataConsumed(WorkflowTrackingContext context, DataObj fileObj, String... descriptionAndAnnotation);

    /**
     * Sends a notification indicating that a file/directory was created by this invocation. This file/directory is
     * optionally registered with a naming service and a new leadId created for it. The absolute path to the local
     * file/directory should be passed. A message with the file leadId, local path, and size of the local file/directory
     * is sent.
     * 
     * @param context
     *            current workflow tracking context, this includes in parameter entity the identity of the
     *            workflow/service's invocation that consumed this file
     * @param dataId
     *            the dataId for this file/dir (registered with the name resolver if available).
     * @param locations
     *            the list of URLs to the replicas of this file/dir. the first URL should be the absolute path to this
     *            file/dir on the local host, in the form of file://localhostName/path/to/file.txt
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return a FileObj recording the leadId, local path, timestamp and size of file/dir
     * 
     */
    public DataObj dataProduced(WorkflowTrackingContext context, URI dataId, List<URI> locations,
            String... descriptionAndAnnotation);

    public DataObj dataProduced(WorkflowTrackingContext context, URI dataId, List<URI> locations, int sizeInBytes,
            String... descriptionAndAnnotation);

    /**
     * Sends a notification indicating that a file/directory was created by this invocation. A fileobj representing this
     * file and returned by another file method should be passed. if file was not registered with the resolver, this
     * method optionally adds a mapping with the resolver. A message with the file leadId, local path, and size of the
     * local file/directory is sent.
     * 
     * @param context
     *            current workflow tracking context, this includes in parameter entity the identity of the
     *            workflow/service's invocation that consumed this file
     * @param dataObj
     *            data object recording the dataId, local/remote URLs, timestamp of the file/dir, that was returned by
     *            another data notification method
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return the file object passed to this method with file/dir size filled in if not already when passed.
     * 
     */
    public DataObj dataProduced(WorkflowTrackingContext context, DataObj fileObj, String... descriptionAndAnnotation);

}
