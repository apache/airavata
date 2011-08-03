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

import org.apache.airavata.workflow.tracking.common.DataDurationObj;
import org.apache.airavata.workflow.tracking.common.DataObj;
import org.apache.airavata.workflow.tracking.common.DurationObj;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;

/**
 * Utility to create and send Lead performance related notification messages. this calculates time taken to send/receive
 * files, and for executing the application's computation section
 * 
 * <pre>
 *          fileReceiveStarted(F1)
 *          -- do gridftp get to stage input files --
 *          fileReceiveFinished(F1)
 *          computationStarted(C1)
 *          -- call fortran code to process input files --
 *          computationFinished(C1)
 *          fileSendStarted(F2)
 *          -- do gridftp put to save output files --
 *          fileSendFinished(F2)
 *          flush()
 * </pre>
 */
public interface PerformanceNotifier {

    /**
     * called at the begining of a computational section (e.g. before invoking fortran code in jython script). This
     * method returns a DurationObj which should be passed to the {@link #computationFinished} method. This object
     * records the start timestamp of this computational section. A notification message is not sent until the
     * {@link #computationFinished} method is called.
     * 
     * @return a DurationObj recording the start time of this computation
     * 
     */
    public DurationObj computationStarted();

    /**
     * called at the end of a computational section (e.g. return from invoking fortran code in jython script). The
     * DurationObj from {@link #computationStarted} should be passed to this method. A notification message is sent with
     * the name and duration of this computation. A human readable name for this computation can be passed as part of
     * the descriptionAndAnnotation.
     * 
     * @param entity
     *            identity of the workflow/service's invocation that did this computation
     * @param a
     *            DurationObj recording the start time returned by the {@link #computationStarted} method
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return the passed {@link DurationObj} updated the end timestamp
     */
    public DurationObj computationFinished(WorkflowTrackingContext context, DurationObj compObj,
            String... descriptionAndAnnotation);

    /**
     * called after a computational section (e.g. invoking fortran code in jython script) where the duration of
     * computation was recorded by the application itself. This can be used instead of the two calls to
     * {@link #computationStarted and {@link #computationFinished}. A notification message is sent with the duration of
     * this computation. A human readable name for this computation can be passed as part of the
     * descriptionAndAnnotation.
     * 
     * @param entity
     *            identity of the workflow/service's invocation that did this computation
     * @param durationMillis
     *            the time taken for this computation in milliseconds
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return the passed {@link DurationObj} updated the end timestamp
     * 
     */
    public DurationObj computationDuration(WorkflowTrackingContext context, long durationMillis,
            String... descriptionAndAnnotation);

    /**
     * called at the begining of sending a local file or directory to remote host. A {@link DataObj} created earlier
     * using the {@link ProvenanceNotifier#dataProduced}, {@link ProvenanceNotifier#dataConsumed}, or
     * {@link #dataReceiveStarted} should be passed, along with the remote URL. A {@link DataDurationObject} with the
     * start timestamp of sending is returned. This should be passed to the {@link #dataSendFinish} method upon
     * completion of the sending. A notification message is not sent until the {@link #dataSendFinished} method is
     * called.
     * 
     * @param dataObj
     *            the local {@link DataObj} that is being sent. This would have been created by calling
     *            {@link ProvenanceNotifier#dataProduced}, {@link ProvenanceNotifier#dataConsumed}, or
     *            {@link #dataReceiveStarted}
     * @param remoteLocation
     *            the remote URl to which this file is being copied
     * 
     * @return a {@link DataDurationObj} with the timestamp of send start and encapsulating the DataObj
     * 
     */
    public DataDurationObj dataSendStarted(DataObj dataObj, URI remoteLocation);

    /**
     * Sends a notification about the locl file/directory that was sent to a remote URL along with its file/dir size and
     * send duration.
     * 
     * @param entity
     *            identity of the workflow/service's invocation that sent this file
     * @param dataDurationObj
     *            a {@link DataDurationObj} returned by the call to {@link #dataSendStarted}
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return the passed {@link DataDurationObj} updated with the end timestamp
     * 
     */
    public DataDurationObj dataSendFinished(WorkflowTrackingContext context, DataDurationObj dataObj,
            String... descriptionAndAnnotation);

    /**
     * called after sending a local file or directory to remote host. Details of the local file and the remote URL
     * should be passed. This method can be used to send a file transfer duration message when the duration is directly
     * provided by the user. This is used in place of a call to {@link #dataSendStarted} and {@link #dataSendFinished}.
     * 
     * @param entity
     *            identity of the workflow/service's invocation that sent this file
     * @param dataId
     *            the dataId for the file/dir being sent (optionally registered with nameresolver).
     * @param localLocation
     *            the local URL for the sent file, as an absolute path to the file/dir on the local host, in the form of
     *            file://localhostName/path/to/file.txt
     * @param remoteLocation
     *            the remote URl to which this file is being copied
     * @param sizeInBytes
     *            the size of the transfered file/dir in bytes
     * @param durationMillis
     *            the time taken for this transfer to take place in milliseconds
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return the a {@link DataDurationObj encapsulating a newly created {@link DataObj} and having the start/end
     *         timestamps.
     * 
     */
    public DataDurationObj dataSendDuration(WorkflowTrackingContext context, URI dataID, URI localLocation,
            URI remoteLocation, int sizeInBytes, long durationMillis, String... descriptionAndAnnotation);

    /**
     * called at the begining of receiving a local file or directory from remote host. The UUID for this file/directory
     * recorded with the naming service may be passed, along with the absolute path to the local file/directory, and URL
     * to the remote file location. A {@link DataObj} containing the start timestamp and other file info passed is
     * returned. This returned {@link DataObj} needs to be passed to the {@link #dataReceiveFinish} method upon which a
     * notification message is sent. This method does not send a notification nor does it map the leadId with the
     * resolver.
     * 
     * @param dataId
     *            the dataId for the file/dir being received (optionally registered with nameresolver).
     * @param remoteLocation
     *            the remote URL from which this file is being copied
     * @param localLocation
     *            the local URL for the file being received, as an absolute path to the file/dir on the local host, in
     *            the form of file://localhostName/path/to/file.txt
     * 
     * @return a DataDurationObj with the receive start timestamp, and also containing {@link DataObj} created using the
     *         passed dataId, local path, and remote URL
     * 
     * 
     */
    public DataDurationObj dataReceiveStarted(URI dataID, URI remoteLocation, URI localLocation);

    /**
     * Sends a notification about the local file/directory that was received from a remote URL along with file/dir size
     * and receive duration. This also appends the dataId to the local file AND the remote file mapping with the name
     * resolver, or creates a mappings if the dataId was not passed.
     * 
     * @param entity
     *            identity of the workflow/service's invocation that received this file
     * @param dataDurationObj
     *            the {@link DataDurationObj} returned by the call to {@link #dataReceiveStarted}
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return the passed {@link DataDurationObj} updated with the end timestamp
     * 
     */
    public DataDurationObj dataReceiveFinished(WorkflowTrackingContext context, DataDurationObj dataObj,
            String... descriptionAndAnnotation);

    /**
     * called after receiving a local file or directory to remote host. Details of the local file and the remote URL
     * should be passed. This method can be used to send a file transfer duration message when the duration is directly
     * provided by the user. This is used in place of a call to {@link #dataReceiveStarted} and
     * {@link #dataReceiveFinished}.
     * 
     * @param entity
     *            identity of the workflow/service's invocation that sent this file
     * @param dataId
     *            the dataId for the file/dir being sent (optionally registered with nameresolver).
     * @param remoteLocation
     *            the remote URL from which this file is being copied
     * @param localLocation
     *            the local URL where the file/dir is received, as an absolute path to it on the local host, in the form
     *            of file://localhostName/path/to/file.txt
     * @param sizeInBytes
     *            the size of the transfered file/dir in bytes
     * @param durationMillis
     *            the time taken for this transfer to take place in milliseconds
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return the a {@link DataDurationObj encapsulating a newly created {@link DataObj} and having the start/end
     *         timestamps.
     * 
     */
    public DataDurationObj dataReceiveDuration(WorkflowTrackingContext context, URI dataID, URI remoteLocation,
            URI localLocation, int sizeInBytes, long durationMillis, String... descriptionAndAnnotation);
}
