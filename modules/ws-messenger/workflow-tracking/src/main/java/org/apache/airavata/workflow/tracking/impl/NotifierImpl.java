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

import edu.indiana.extreme.lead.calder.types.*;
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

    public void queryStarted(WorkflowTrackingContext context, String radarName, String... descriptionAndAnnotation) {

        QueryStartedDocument queryStartedDoc = QueryStartedDocument.Factory.newInstance();
        CalderNotificationType calderNotificationType = queryStartedDoc.addNewQueryStarted();
        calderNotificationType.setRadarName(radarName);

        sendNotification(context, queryStartedDoc, descriptionAndAnnotation, "[Query started for " + radarName + "]");
    }

    public void queryFailedToStart(WorkflowTrackingContext context, String radarName,
            String... descriptionAndAnnotation) {
        QueryFailedToStartDocument queryFailedToStartDoc = QueryFailedToStartDocument.Factory.newInstance();
        CalderNotificationType calderNotificationType = queryFailedToStartDoc.addNewQueryFailedToStart();
        calderNotificationType.setRadarName(radarName);

        sendNotification(context, queryFailedToStartDoc, descriptionAndAnnotation, "[Query failed to start for "
                + radarName + "]");

    }

    public void queryActive(WorkflowTrackingContext context, String radarName, String... descriptionAndAnnotation) {
        QueryActiveDocument queryActiveDoc = QueryActiveDocument.Factory.newInstance();
        CalderNotificationType calderNotificationType = queryActiveDoc.addNewQueryActive();
        calderNotificationType.setRadarName(radarName);

        sendNotification(context, queryActiveDoc, descriptionAndAnnotation, "[Query active for " + radarName + "]");

    }

    public void queryExpired(WorkflowTrackingContext context, String radarName, String... descriptionAndAnnotation) {
        QueryExpiredDocument queryExpiredDoc = QueryExpiredDocument.Factory.newInstance();
        CalderNotificationType calderNotificationType = queryExpiredDoc.addNewQueryExpired();
        calderNotificationType.setRadarName(radarName);

        sendNotification(context, queryExpiredDoc, descriptionAndAnnotation, "[Query Expired for " + radarName + "]");

    }

    public void triggerFound(WorkflowTrackingContext context, String radarName, String... descriptionAndAnnotation) {
        TriggerFoundDocument triggerFoundDoc = TriggerFoundDocument.Factory.newInstance();
        CalderNotificationType calderNotificationType = triggerFoundDoc.addNewTriggerFound();
        calderNotificationType.setRadarName(radarName);

        sendNotification(context, triggerFoundDoc, descriptionAndAnnotation, "[Trigger found for " + radarName + "]");

    }

    public void queryPublishResult(WorkflowTrackingContext context,
            LeadCrosscutParametersUtil leadCrosscutParametersUtil) {
        QueryPublishResultDocument queryPublishResultDoc = QueryPublishResultDocument.Factory.newInstance();
        CalderClusterNotificationType calderClusterNotificationType = queryPublishResultDoc.addNewQueryPublishResult();
        calderClusterNotificationType.setCtrlat(leadCrosscutParametersUtil.getCenterLatitude());
        calderClusterNotificationType.setCtrlon(leadCrosscutParametersUtil.getCenterLongitude());
        calderClusterNotificationType.setNorthbc(leadCrosscutParametersUtil.getNorthBc());
        calderClusterNotificationType.setSouthbc(leadCrosscutParametersUtil.getSouthBc());
        calderClusterNotificationType.setEastbc(leadCrosscutParametersUtil.getEastBc());
        calderClusterNotificationType.setWestbc(leadCrosscutParametersUtil.getWestBc());

        sendNotification(context, queryPublishResultDoc, null,
                "[Clustering result for query found at " + leadCrosscutParametersUtil.getCenterLatitude() + " "
                        + leadCrosscutParametersUtil.getCenterLongitude() + "]");

    }

    public void queryNoDetection(WorkflowTrackingContext context, String... descriptionAndAnnotation) {
        QueryNoDetectionDocument queryNoDetectionDoc = QueryNoDetectionDocument.Factory.newInstance();
        CalderNotificationType calderNotificationType = queryNoDetectionDoc.addNewQueryNoDetection();
        calderNotificationType.setRadarName("");
        sendNotification(context, queryNoDetectionDoc, descriptionAndAnnotation,
                "[No mining results detected for query]");
    }

    // /**
    // * Create a notifier object with the default values set. Creating this is
    // convinient
    // * if the same service/application needs to generate multiple messages,
    // since the various
    // * IDs passed in the constructor will be filled into the message
    // automatically. If you're
    // * sending just a single message from a service/app, you can use the
    // static methods that are
    // * available.
    // *
    // * @param brokerLoc_ the location of the broker service in the form
    // "host:port"
    // * @param topic_ the topic to which messages should be sent
    // * @param workflowID_ the workflow instance ID to which this
    // service/application belongs
    // * @param nodeID_ the node ID within the workflow which this
    // service/application represents
    // * @param wfTimeStep_ the unique timestep within the workflow instance at
    // which this
    // * service/application invocation is taking place
    // * @param serviceID_
    // * @param serviceWsdl_ the concrete WSDL of the service (or service
    // wrapper of an
    // * application) which is sending the notification. This is used
    // * to retrieve the unique service QName that can identify this
    // * service instance, and to get the schema for the response message
    // * @param incomingMessageXml_ the icoming message that started this
    // service that should contain
    // * end point reference (ReplyTo EPR is minimum) where the results of
    // * this service/application should be sent to.
    // * @param nameResolverWsdl_ WSDL location of name resolver
    // * @param fileProtocol_ the protocol used to access local files created by
    // this application. e.g. gridftp://, http://, ftp://, etc. Defaults to
    // file://.
    // * @param enableNameResolver whether name resolution is enabled
    // * @param batchProvMessages whether provenance messages should be batched
    // and sent as one message
    // */
    // private void init(String brokerLoc_, String topic_,
    // String workflowID_, String nodeID_, String wfTimeStep_,
    // String serviceID_, String serviceWsdl_,
    // String incomingMessageXml_,
    // String nameResolverWsdl_,
    // String fileProtocol_,
    // boolean enableNameResolver,
    // boolean enableBatchProvenance) {
    //

    /**
     * Method main. Simple self test by generating some notifications
     * 
     * @param args
     *            a String[]
     * 
     */
    // public static void main(String args[]) throws Exception {
    //
    // System.err.println("Started " + NotifierImpl.class);
    //
    // for (int i=0; i < 10; i++) {
    // ProvenanceNotifier n =
    // new NotifierImpl(Props.newProps(BROKER_URL,
    // "rainier.extreme.indiana.edu:12346"). // brokerLoc
    // set(TOPIC, "lead_wft_test_topic"). // topic
    // set(WORKFLOW_ID, "wfId93643").
    // set(NODE_ID, "nodeId26").
    // set(TIMESTEP, "12"). // id'ss
    // set(SERVICE_ID, "http://service.qname/").
    // set(NAME_RESOLVER_URL,
    // "http://rainier.extreme.indiana.edu:33330/name_resolver?wsdl").
    // set(ENABLE_NAME_RESOLVER, "true")
    // );
    //
    // n.serviceInvoked("SampleApplication");
    // //n.debug("debug message from the applcation..");
    // //n.publishURL("LEAD home", "http://lead.ou.edu");
    // n.dataConsumed("urn:lead-ou-edu:iub:f81d4fae-7dec-11d0-a765-00a0c91e6bf6",
    // "/foo/bar/input.data");
    // n.fileProduced("/foo/bar/output.data");
    // n.dataConsumed("urn:lead-ou-edu:iub:f81d4fae-7dec-11d0-a765-00a0c91e6bf7",
    // "/acme/input.2");
    // n.fileProduced("/acme/output.2");
    // n.sendingResult(null, null);
    // }
    // //n.workflowFinishedSuccess("rainier.extreme.indiana.edu:12346",
    // "lead_wft_test_topic", "wfId93643");
    // System.err.println("finsished " + NotifierImpl.class);
    // }
}
