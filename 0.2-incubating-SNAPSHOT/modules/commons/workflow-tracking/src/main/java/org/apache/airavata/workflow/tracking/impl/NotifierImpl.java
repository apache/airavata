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

package org.apache.airavata.workflow.tracking.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.airavata.commons.LeadCrosscutParametersUtil;
import org.apache.airavata.workflow.tracking.Notifier;
import org.apache.airavata.workflow.tracking.common.DataDurationObj;
import org.apache.airavata.workflow.tracking.common.DataObj;
import org.apache.airavata.workflow.tracking.common.DurationObj;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
import org.apache.airavata.workflow.tracking.impl.state.DataDurationImpl;
import org.apache.airavata.workflow.tracking.impl.state.DataObjImpl;
import org.apache.airavata.workflow.tracking.impl.state.DurationImpl;
import org.apache.airavata.workflow.tracking.types.*;

/**
 * DOES NOT SUPPORT MULTI_THREADING -- PUBLISHER QUEUE, DATA CONSUMED/PRODUCED BATCHING * Utility to create and send
 * Lead notification messages for an application (script/web service). Since it extends WorkflowNotifierImpl, it can
 * also send workflow related notifications.
 * 
 * The constructor of this class uses the following properties from CONSTS: BROKER_URL, TOPIC, WORKFLOW_ID, NODE_ID,
 * TIMESTEP, SERVICE_ID, SERVICE_WSDL, IN_XML_MESSAGE, NAME_RESOLVER_URL, FILE_ACCESS_PROTOCOL, DISABLE_NAME_RESOLVER,
 * BATCH_PROVENANCE_MSGS, ASYNC_PUB_MODE
 * 
 */
public class NotifierImpl extends ProvenanceNotifierImpl implements Notifier {

    private static final String WFT_NS = "http://lead.extreme.indiana.edu/namespaces/2006/06/workflow_tracking";

    private static final HashMap<String, String> NS_MAP = new HashMap<String, String>();
    static {
        NS_MAP.put("", WFT_NS);
    }

    // public NotifierImpl(ConstructorProps props) throws XMLStreamException, IOException {
    // super( props);
    // }

    /**
     * @param batchProvMessages
     *            whether provenance messages should be batched and sent as one message
     * @param publisher
     *            a NotificationPublisher used to send the notifications
     * 
     */
    public NotifierImpl() {
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////
    //
    // AUDIT NOTIFIER
    //
    // //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     * 
     */
    public void resourceMapping(WorkflowTrackingContext context, String mappedResource, int retryStatusCount,
            String... descriptionAndAnnotation) {

        ResourceMappingDocument mapMsg = ResourceMappingDocument.Factory.newInstance();
        ResourceMappingType map = mapMsg.addNewResourceMapping();
        map.setMappedResource(mappedResource);
        map.setRetryStatusCount(retryStatusCount);

        sendNotification(context, mapMsg, descriptionAndAnnotation, "[Resource mapping done for" + mappedResource + "]");
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void jobStatus(WorkflowTrackingContext context, String status, int retryCount,
            String... descriptionAndAnnotation) {

        JobStatusDocument jobMsg = JobStatusDocument.Factory.newInstance();
        JobStatusType job = jobMsg.addNewJobStatus();
        job.setJobStatus(status);
        job.setRetryCount(retryCount);

        sendNotification(context, jobMsg, descriptionAndAnnotation, "[Job status is " + status + "]");
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////
    //
    // AUDIT NOTIFIER
    //
    // //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     * 
     */
    public void appAudit(WorkflowTrackingContext context, String name, URI jobHandle, String host, String queueName,
            String jobId, String dName, String projectId, String rsl, String... descriptionAndAnnotation) {
        final ApplicationAuditDocument appAuditMsg = ApplicationAuditDocument.Factory.newInstance();
        final ApplicationAuditType appAudit = appAuditMsg.addNewApplicationAudit();
        appAudit.setJobHandle(jobHandle.toString());
        appAudit.setName(name);
        appAudit.setHost(host);
        appAudit.setQueueName(queueName); // queueName is an optional element
        appAudit.setJobId(jobId); // jobId is an optional element
        appAudit.setDistinguishedName(dName);
        appAudit.setProjectId(projectId); // projectId is an optional element
        appAudit.setRsl(rsl);

        sendNotification(context, appAuditMsg, descriptionAndAnnotation, "[Audit msg for '" + name + "' at host "
                + host + " for DN " + dName + "]" // default
        );
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////
    //
    // PERFORMANCE NOTIFIER
    //
    // //////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * {@inheritDoc}
     * 
     */
    public DurationObj computationStarted() {

        return new DurationImpl();
    }

    /**
     * {@inheritDoc}
     * 
     */
    public DurationObj computationFinished(WorkflowTrackingContext context, DurationObj compObj,
            String... descriptionAndAnnotation) {

        if (context == null)
            throw new RuntimeException("Local entity passed was NULL.");
        if (compObj == null)
            throw new RuntimeException("Comp duration object passed was NULL.");

        // mark computation end
        compObj.markEndTimeMillis();

        // create activity
        ComputationDurationDocument activity = ComputationDurationDocument.Factory.newInstance();
        ComputationDurationDocument.ComputationDuration activityType = activity.addNewComputationDuration();

        activityType.setDurationInMillis(compObj.getDurationMillis());

        sendNotification(context, activity, descriptionAndAnnotation,
                "[Computation Time taken = " + compObj.getDurationMillis() + " ms]");

        return compObj;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public DurationObj computationDuration(WorkflowTrackingContext context, long durationMillis,
            String... descriptionAndAnnotation) {

        DurationObj compObj = new DurationImpl(durationMillis);
        return computationFinished(context, compObj, descriptionAndAnnotation);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public DataDurationObj dataSendStarted(DataObj dataObj, URI remoteLocation) {

        return new DataDurationImpl(dataObj, remoteLocation);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public DataDurationObj dataSendFinished(WorkflowTrackingContext context, DataDurationObj dataDurationObj,
            String... descriptionAndAnnotation) {

        if (context == null)
            throw new RuntimeException("Local entity passed was NULL.");
        if (dataDurationObj == null)
            throw new RuntimeException("Data duration object passed was NULL.");

        DataObj dataObj = null;
        if ((dataObj = dataDurationObj.getDataObj()) == null)
            throw new RuntimeException("Data duration object's DataObje was NULL.");
        if (dataObj.getId() == null)
            throw new RuntimeException("Data object's ID was NULL.");
        if (dataObj.getLocalLocation() == null)
            throw new RuntimeException("Local file URL passed in DataDurationObj.getDataObj was NULL.");
        if (dataDurationObj.getRemoteLocation() == null)
            throw new RuntimeException("Remote file URL passed in DataDurationObj was NULL.");

        // mark computation end
        dataDurationObj.markEndTimeMillis();

        // create activity
        DataSendDurationDocument activity = DataSendDurationDocument.Factory.newInstance();
        DataTransferDurationType activityType = activity.addNewDataSendDuration();

        activityType.setId(dataObj.getId().toString());
        activityType.setDurationInMillis(dataDurationObj.getDurationMillis());
        activityType.setSizeInBytes(dataObj.getSizeInBytes());

        activityType.setSource(dataObj.getLocalLocation().toString());

        activityType.setTarget(dataDurationObj.getRemoteLocation().toString());

        sendNotification(context, activity, descriptionAndAnnotation, "[Data at " + dataObj.getLocalLocation()
                + " was sent to " + dataDurationObj.getRemoteLocation() + "]");

        return dataDurationObj;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public DataDurationObj dataSendDuration(WorkflowTrackingContext context, URI dataID, URI localLocation,
            URI remoteLocation, int sizeInBytes, long durationMillis, String... descriptionAndAnnotation) {

        List<URI> locations = new ArrayList<URI>(2);
        locations.add(localLocation);
        locations.add(remoteLocation);

        DataObj dataObj = new DataObjImpl(dataID, locations, sizeInBytes);
        DataDurationObj dataDurationObj = new DataDurationImpl(dataObj, remoteLocation, durationMillis);

        return dataSendFinished(context, dataDurationObj, descriptionAndAnnotation);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public DataDurationObj dataReceiveStarted(URI dataID, URI remoteLocation, URI localLocation) {

        List<URI> locations = new ArrayList<URI>(2);
        locations.add(localLocation);
        locations.add(remoteLocation);

        DataObj dataObj = new DataObjImpl(dataID, locations);
        DataDurationObj dataDurationObj = new DataDurationImpl(dataObj, remoteLocation);

        return dataDurationObj;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public DataDurationObj dataReceiveFinished(WorkflowTrackingContext context, DataDurationObj dataDurationObj,
            String... descriptionAndAnnotation) {

        if (context == null)
            throw new RuntimeException("Local entity passed was NULL.");
        if (dataDurationObj == null)
            throw new RuntimeException("Data duration object passed was NULL.");

        DataObj dataObj = null;
        if ((dataObj = dataDurationObj.getDataObj()) == null)
            throw new RuntimeException("Data duration object's DataObj was NULL.");
        if (dataObj.getId() == null)
            throw new RuntimeException("Data object's ID was NULL.");
        if (dataObj.getLocalLocation() == null)
            throw new RuntimeException("Local file URL passed in DataDurationObj.getDataObj was NULL.");
        if (dataDurationObj.getRemoteLocation() == null)
            throw new RuntimeException("Remote file URL passed in DataDurationObj was NULL.");

        // mark computation end
        dataDurationObj.markEndTimeMillis();

        // create activity
        DataReceiveDurationDocument activity = DataReceiveDurationDocument.Factory.newInstance();
        DataTransferDurationType activityType = activity.addNewDataReceiveDuration();

        activityType.setId(dataObj.getId().toString());
        activityType.setDurationInMillis(dataDurationObj.getDurationMillis());
        activityType.setSizeInBytes(dataObj.getSizeInBytes());

        activityType.setSource(dataObj.getLocalLocation().toString());

        activityType.setTarget(dataDurationObj.getRemoteLocation().toString());

        sendNotification(context, activity, descriptionAndAnnotation,
                "[Data from " + dataDurationObj.getRemoteLocation() + " was received at " + dataObj.getLocalLocation()
                        + "]");

        return dataDurationObj;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public DataDurationObj dataReceiveDuration(WorkflowTrackingContext context, URI dataID, URI remoteLocation,
            URI localLocation, int sizeInBytes, long durationMillis, String... descriptionAndAnnotation) {

        List<URI> locations = new ArrayList<URI>(2);
        locations.add(localLocation);
        locations.add(remoteLocation);

        DataObj dataObj = new DataObjImpl(dataID, locations, sizeInBytes);
        DataDurationObj dataDurationObj = new DataDurationImpl(dataObj, remoteLocation, durationMillis);

        return dataReceiveFinished(context, dataDurationObj, descriptionAndAnnotation);
    }

}
