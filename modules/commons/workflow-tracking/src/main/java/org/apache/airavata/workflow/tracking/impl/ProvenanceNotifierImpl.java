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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.airavata.workflow.tracking.ProvenanceNotifier;
import org.apache.airavata.workflow.tracking.common.DataObj;
import org.apache.airavata.workflow.tracking.common.InvocationContext;
import org.apache.airavata.workflow.tracking.common.InvocationEntity;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
import org.apache.airavata.workflow.tracking.impl.state.DataObjImpl;
import org.apache.airavata.workflow.tracking.impl.state.InvocationContextImpl;
import org.apache.airavata.workflow.tracking.impl.state.InvocationEntityImpl;
import org.apache.airavata.workflow.tracking.types.AcknowledgeFailureType;
import org.apache.airavata.workflow.tracking.types.AcknowledgeSuccessType;
import org.apache.airavata.workflow.tracking.types.BaseNotificationType;
import org.apache.airavata.workflow.tracking.types.DataConsumedDocument;
import org.apache.airavata.workflow.tracking.types.DataProducedDocument;
import org.apache.airavata.workflow.tracking.types.DataProductNotificationType;
import org.apache.airavata.workflow.tracking.types.DataProductType;
import org.apache.airavata.workflow.tracking.types.FaultMessageType;
import org.apache.airavata.workflow.tracking.types.FaultReceiverType;
import org.apache.airavata.workflow.tracking.types.FaultResponderType;
import org.apache.airavata.workflow.tracking.types.InvocationMessageType;
import org.apache.airavata.workflow.tracking.types.InvokingServiceDocument;
import org.apache.airavata.workflow.tracking.types.InvokingServiceFailedDocument;
import org.apache.airavata.workflow.tracking.types.InvokingServiceSucceededDocument;
import org.apache.airavata.workflow.tracking.types.ReceivedFaultDocument;
import org.apache.airavata.workflow.tracking.types.ReceivedResultDocument;
import org.apache.airavata.workflow.tracking.types.RequestInitiatorType;
import org.apache.airavata.workflow.tracking.types.RequestReceiverType;
import org.apache.airavata.workflow.tracking.types.ResultReceiverType;
import org.apache.airavata.workflow.tracking.types.ResultResponderType;
import org.apache.airavata.workflow.tracking.types.SendingFaultDocument;
import org.apache.airavata.workflow.tracking.types.SendingResponseFailedDocument;
import org.apache.airavata.workflow.tracking.types.SendingResponseSucceededDocument;
import org.apache.airavata.workflow.tracking.types.SendingResultDocument;
import org.apache.airavata.workflow.tracking.types.ServiceInitializedDocument;
import org.apache.airavata.workflow.tracking.types.ServiceInvokedDocument;
import org.apache.airavata.workflow.tracking.types.ServiceTerminatedDocument;
import org.apache.airavata.workflow.tracking.types.WorkflowInitializedDocument;
import org.apache.airavata.workflow.tracking.types.WorkflowInvokedDocument;
import org.apache.airavata.workflow.tracking.types.WorkflowTerminatedDocument;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;

/**
 * DOES NOT SUPPORT MULTI_THREADING -- PUBLISHER QUEUE, DATA CONSUMED/PRODUCED BATCHING
 * 
 * Utility to create and send Lead notification messages using new notification schema from a Workflow Engine
 * 
 * The constructor of this class uses the following properties from CONSTS: BROKER_URL, TOPIC, WORKFLOW_ID, NODE_ID,
 * TIMESTEP, SERVICE_ID, ASYNC_PUB_MODE
 */
public class ProvenanceNotifierImpl extends GenericNotifierImpl implements ProvenanceNotifier {

    private DataConsumedDocument dataConsumedBatchActivity;
    private DataProducedDocument dataProducedBatchActivity;

    // public ProvenanceNotifierImpl(ConstructorProps props) throws XMLStreamException, IOException {
    // super(props);
    // DATA_BATCHED = Boolean.parseBoolean((String)props.get(ENABLE_BATCH_PROVENANCE));
    // }

    public ProvenanceNotifierImpl() {
        super();
    }

    /**
     * this method allows us to override the default timestamp with a user supplied one
     * 
     * @param msg
     *            a BaseNotificationType
     * @param entity
     *            an InvocationEntity
     * 
     */
    // @Override
    // protected void setIDAndTimestamp(WorkfloBaseNotificationType msg, InvocationEntity entity) {
    // if(activityTimestamp == null)
    // super.setIDAndTimestamp(msg, entity);
    // else
    // super.setIDAndTimestamp(msg, entity, activityTimestamp);
    // }

    // protected void setIDAndTimestamp(BaseNotificationType msg, URI serviceID) {
    // setIDAndTimestamp(msg, createEntity(serviceID));
    // }

    protected InvocationEntity createEntity(URI serviceID) {

        return new InvocationEntityImpl(serviceID);
    }

    // /////////////////////////////////////////////////////////////////////////////
    // //
    // WORKFLOW NOTIFIER //
    // //
    // /////////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     * 
     */
    public void workflowInitialized(WorkflowTrackingContext context, URI serviceID, String... descriptionAndAnnotation) {
        WorkflowInitializedDocument activity = WorkflowInitializedDocument.Factory.newInstance();
        activity.addNewWorkflowInitialized();
        // add timestamp and notification source; add description, and annotation if present
        sendNotification(context, activity, descriptionAndAnnotation, "[Workflow is initialized; ready to be invoked]");
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void workflowTerminated(WorkflowTrackingContext context, URI serviceID, String... descriptionAndAnnotation) {
        WorkflowTerminatedDocument activity = WorkflowTerminatedDocument.Factory.newInstance();
        BaseNotificationType activityType = activity.addNewWorkflowTerminated();
        sendNotification(context, activity, descriptionAndAnnotation,
                "[Workflow is terminated; cannot be invoked anymore]");
    }

    /**
     * {@inheritDoc}
     * 
     */
    public InvocationContext workflowInvoked(WorkflowTrackingContext context, InvocationEntity initiator,
            String... descriptionAndAnnotation) {
        return workflowInvoked(context, initiator, null, null, descriptionAndAnnotation);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public InvocationContext workflowInvoked(WorkflowTrackingContext context, InvocationEntity initiator,
            XmlObject header, XmlObject body, String... descriptionAndAnnotation) {

        WorkflowInvokedDocument activity = WorkflowInvokedDocument.Factory.newInstance();
        RequestReceiverType activityType = activity.addNewWorkflowInvoked();

        // create the invocation context; set the initiator to the remote entity
        InvocationContextImpl invocationContext = new InvocationContextImpl(context.getMyself(), initiator);
        if (initiator != null) {
            activityType.addNewInitiator().set(initiator.toBaseIDType());
        } else {
            logger.warn("Possible Error in context that was passed. "
                    + "There was no remote invoker defined for workflow invoked (initiator=NULL)");
        }

        // add header and body fields
        if (header != null || body != null) {
            InvocationMessageType request = activityType.addNewRequest();
            if (header != null)
                request.addNewHeader().set(header);
            if (body != null)
                request.addNewBody().set(body);
        }
        sendNotification(context, activity, descriptionAndAnnotation, "[Workflow is invoked]");
        return invocationContext;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public InvocationContext invokingService(WorkflowTrackingContext context, InvocationEntity receiver,
            String... descriptionAndAnnotation) {
        return invokingService(context, receiver, null, null, descriptionAndAnnotation);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public InvocationContext invokingService(WorkflowTrackingContext context, InvocationEntity receiver,
            XmlObject header, XmlObject body, String... descriptionAndAnnotation) {

        InvokingServiceDocument activity = InvokingServiceDocument.Factory.newInstance();
        RequestInitiatorType activityType = activity.addNewInvokingService();

        // create the invocation context; set the receiver to the remote entity
        InvocationContextImpl invocationContext = new InvocationContextImpl(context.getMyself(), receiver);
        activityType.addNewReceiver().set(receiver.toBaseIDType());

        // add header and body fields
        if (header != null || body != null) {
            InvocationMessageType request = activityType.addNewRequest();
            if (header != null)
                request.addNewHeader().set(header);
            if (body != null)
                request.addNewBody().set(body);
        }
        sendNotification(context, activity, descriptionAndAnnotation, "[Service is invoked]");
        return invocationContext;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void invokingServiceSucceeded(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation) {

        if (context == null)
            throw new RuntimeException("Context passed was NULL.");

        InvokingServiceSucceededDocument activity = InvokingServiceSucceededDocument.Factory.newInstance();
        AcknowledgeSuccessType activityType = activity.addNewInvokingServiceSucceeded();

        // set the remote entity as receiver
        if (context.getRemoteEntity() != null) {
            activityType.addNewReceiver().set(context.getRemoteEntity().toBaseIDType());
        } else {
            logger.warn("Error in context that was passed. "
                    + "there was no remote entity defined (requestReceiver=NULL)");
        }
        sendNotification(wtcontext, activity, descriptionAndAnnotation, "[Service finished successfully]");
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void invokingServiceFailed(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation) {

        invokingServiceFailed(wtcontext, context, null, descriptionAndAnnotation);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void invokingServiceFailed(WorkflowTrackingContext wtcontext, InvocationContext context, Throwable trace,
            String... descriptionAndAnnotation) {

        if (context == null)
            throw new RuntimeException("Context passed was NULL.");
        final InvokingServiceFailedDocument activity = InvokingServiceFailedDocument.Factory.newInstance();
        final AcknowledgeFailureType activityType = activity.addNewInvokingServiceFailed();

        // set the remote entity as receiver
        if (context.getRemoteEntity() != null) {
            activityType.addNewReceiver().set(context.getRemoteEntity().toBaseIDType());
        } else {
            logger.warn("Error in context that was passed. "
                    + "there was no remote entity defined (requestReceiver=NULL)");
        }

        // set stack trace if present
        if (trace != null) {
            final StringWriter sw = new StringWriter();
            trace.printStackTrace(new PrintWriter(sw));

            XmlString traceXmlStr = XmlString.Factory.newInstance();
            traceXmlStr.setStringValue(sw.toString());
            activityType.addNewFailure().addNewTrace().set(traceXmlStr);
        }
        sendNotification(wtcontext, activity, descriptionAndAnnotation, "[Service failed]");
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void receivedResult(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation) {

        receivedResult(wtcontext, context, null, null, descriptionAndAnnotation);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void receivedResult(WorkflowTrackingContext wtcontext, InvocationContext context, XmlObject header,
            XmlObject body, String... descriptionAndAnnotation) {

        if (context == null)
            throw new RuntimeException("Context passed was NULL.");

        ReceivedResultDocument activity = ReceivedResultDocument.Factory.newInstance();
        ResultReceiverType activityType = activity.addNewReceivedResult();

        // set the responder to the remote entity
        if (context.getRemoteEntity() != null) {
            activityType.addNewResponder().set(context.getRemoteEntity().toBaseIDType());
        } else {
            logger.warn("Error in context that was passed. " + "There was no remote entity defined (responder=NULL)");
        }

        // add header and body fields
        if (header != null || body != null) {
            InvocationMessageType result = activityType.addNewResult();
            if (header != null)
                result.addNewHeader().set(header);
            if (body != null)
                result.addNewBody().set(body);
        }
        sendNotification(wtcontext, activity, descriptionAndAnnotation, "[Service failed]");
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void receivedFault(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation) {

        receivedFault(wtcontext, context, null, null, descriptionAndAnnotation);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void receivedFault(WorkflowTrackingContext wtcontext, InvocationContext context, XmlObject header,
            XmlObject faultBody, String... descriptionAndAnnotation) {

        if (context == null)
            throw new RuntimeException("Context passed was NULL.");

        ReceivedFaultDocument activity = ReceivedFaultDocument.Factory.newInstance();
        FaultReceiverType activityType = activity.addNewReceivedFault();

        // set the responder to the remote entity
        if (context.getRemoteEntity() != null) {
            activityType.addNewResponder().set(context.getRemoteEntity().toBaseIDType());
        } else {
            logger.warn("Error in context that was passed. " + "There was no remote entity defined (responder=NULL)");
        }

        sendNotification(wtcontext, activity, descriptionAndAnnotation, "[Fault is received for invocation ]");
    }

    // /////////////////////////////////////////////////////////////////////////////
    // //
    // SERVICE NOTIFIER //
    // //
    // /////////////////////////////////////////////////////////////////////////////
    /**
     * {@inheritDoc}
     * 
     */
    public void serviceInitialized(WorkflowTrackingContext context, URI serviceID, String... descriptionAndAnnotation) {

        ServiceInitializedDocument activity = ServiceInitializedDocument.Factory.newInstance();
        activity.addNewServiceInitialized();
        sendNotification(context, activity, descriptionAndAnnotation, "[Service is initialized; ready to be invoked]");
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void serviceTerminated(WorkflowTrackingContext context, URI serviceID, String... descriptionAndAnnotation) {

        ServiceTerminatedDocument activity = ServiceTerminatedDocument.Factory.newInstance();
        activity.addNewServiceTerminated();
        sendNotification(context, activity, descriptionAndAnnotation,
                "[Service is terminated; cannot be invoked anymore]");
    }

    /**
     * {@inheritDoc}
     * 
     */
    public InvocationContext serviceInvoked(WorkflowTrackingContext context, InvocationEntity initiator,
            String... descriptionAndAnnotation) {
        return serviceInvoked(context, initiator, null, null, descriptionAndAnnotation);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public InvocationContext serviceInvoked(WorkflowTrackingContext context, InvocationEntity initiator,
            XmlObject header, XmlObject body, String... descriptionAndAnnotation) {

        ServiceInvokedDocument activity = ServiceInvokedDocument.Factory.newInstance();
        RequestReceiverType activityType = activity.addNewServiceInvoked();

        // create the invocation context; set the initiator to the remote entity
        InvocationContextImpl invocationContext = new InvocationContextImpl(context.getMyself(), initiator);
        if (initiator != null) {
            activityType.addNewInitiator().set(initiator.toBaseIDType());
        } else {
            logger.warn("Possible Error in context that was passed. "
                    + "There was no remote invoker defined (initiator=NULL)");
        }

        // add header and body fields
        if (header != null || body != null) {
            InvocationMessageType request = activityType.addNewRequest();
            if (header != null)
                request.addNewHeader().set(header);
            if (body != null)
                request.addNewBody().set(body);
        }

        sendNotification(context, activity, descriptionAndAnnotation, "[Service is invoked]");

        return invocationContext;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void sendingResult(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation) {

        sendingResult(wtcontext, context, null, null, descriptionAndAnnotation);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void sendingResult(WorkflowTrackingContext wtcontext, InvocationContext context, XmlObject header,
            XmlObject body, String... descriptionAndAnnotation) {

        if (context == null)
            throw new RuntimeException("Context passed was NULL.");

        SendingResultDocument activity = SendingResultDocument.Factory.newInstance();
        ResultResponderType activityType = activity.addNewSendingResult();

        // set the receiver to the remote entity
        if (context.getRemoteEntity() != null) {
            activityType.addNewReceiver().set(context.getRemoteEntity().toBaseIDType());
        } else {
            logger.warn("Possible Error in context that was passed. "
                    + "There was no remote entity defined (responseReceiver=NULL)");
        }

        // add header and body fields
        if (header != null || body != null) {
            InvocationMessageType result = activityType.addNewResult();
            if (header != null)
                result.addNewHeader().set(header);
            if (body != null)
                result.addNewBody().set(body);
        }

        sendNotification(wtcontext, activity, descriptionAndAnnotation,
                "[Trying to send successful result of invocation]");
    }

    /**
     * {@inheritDoc}
     * 
     * 
     */
    public void sendingFault(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation) {

        sendingFault(wtcontext, context, null, null, descriptionAndAnnotation);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void sendingFault(WorkflowTrackingContext wtcontext, InvocationContext context, XmlObject header,
            XmlObject faultBody, String... descriptionAndAnnotation) {

        if (context == null)
            throw new RuntimeException("Context passed was NULL.");

        SendingFaultDocument activity = SendingFaultDocument.Factory.newInstance();
        FaultResponderType activityType = activity.addNewSendingFault();

        // set the receiver to the remote entity
        if (context.getRemoteEntity() != null) {
            activityType.addNewReceiver().set(context.getRemoteEntity().toBaseIDType());
        } else {
            logger.warn("Error in context that was passed. "
                    + "There was no remote entity defined (responseReceiver=NULL)");
        }

        // add header and body fields
        if (header != null || faultBody != null) {
            FaultMessageType result = activityType.addNewFault();
            if (header != null)
                result.addNewHeader().set(header);
            if (faultBody != null)
                result.addNewBody().set(faultBody);
        }

        sendNotification(wtcontext, activity, descriptionAndAnnotation, "[Trying to sending fault from invocation]");
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void sendingResponseSucceeded(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation) {

        if (context == null)
            throw new RuntimeException("Context passed was NULL.");

        SendingResponseSucceededDocument activity = SendingResponseSucceededDocument.Factory.newInstance();
        AcknowledgeSuccessType activityType = activity.addNewSendingResponseSucceeded();

        // set the remote entity as receiver
        if (context.getRemoteEntity() != null) {
            activityType.addNewReceiver().set(context.getRemoteEntity().toBaseIDType());
        } else {
            logger.warn("Error in context that was passed. "
                    + "there was no remote entity defined (responseReceiver=NULL)");
        }

        sendNotification(wtcontext, activity, descriptionAndAnnotation, "[Successfully sent response of invocation]");
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void sendingResponseFailed(WorkflowTrackingContext wtcontext, InvocationContext context,
            String... descriptionAndAnnotation) {

        sendingResponseFailed(wtcontext, context, null, descriptionAndAnnotation);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void sendingResponseFailed(WorkflowTrackingContext wtcontext, InvocationContext context, Throwable trace,
            String... descriptionAndAnnotation) {

        if (context == null)
            throw new RuntimeException("Context passed was NULL.");

        SendingResponseFailedDocument activity = SendingResponseFailedDocument.Factory.newInstance();
        AcknowledgeFailureType activityType = activity.addNewSendingResponseFailed();

        // set the remote entity as receiver
        if (context.getRemoteEntity() != null) {
            activityType.addNewReceiver().set(context.getRemoteEntity().toBaseIDType());
        } else {
            logger.warn("Error in context that was passed. "
                    + "there was no remote entity defined (responseReceiver=NULL)");
        }

        // set stack trace if present
        if (trace != null) {
            final StringWriter sw = new StringWriter();
            trace.printStackTrace(new PrintWriter(sw));

            XmlString traceXmlStr = XmlString.Factory.newInstance();
            traceXmlStr.setStringValue(sw.toString());
            activityType.addNewFailure().addNewTrace().set(traceXmlStr);
        }

        sendNotification(wtcontext, activity, descriptionAndAnnotation, "[Unable to send result of invocation]");
    }

    // /////////////////////////////////////////////////////////////////////////////
    // //
    // DATA PROVENANCE //
    // //
    // /////////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     * 
     */
    public DataObj dataConsumed(WorkflowTrackingContext context, URI dataId, List<URI> locations,
            String... descriptionAndAnnotation) {

        DataObj dataObj = new DataObjImpl(dataId, locations);
        return dataConsumed(context, dataObj, descriptionAndAnnotation);
    }

    public DataObj dataConsumed(WorkflowTrackingContext context, URI dataId, List<URI> locations, int sizeInBytes,
            String... descriptionAndAnnotation) {

        DataObj dataObj = new DataObjImpl(dataId, locations, sizeInBytes);
        return dataConsumed(context, dataObj, descriptionAndAnnotation);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public DataObj dataConsumed(WorkflowTrackingContext context, DataObj dataObj, String... descriptionAndAnnotation) {
        InvocationEntity entity = context.getMyself();
        if (entity == null)
            throw new RuntimeException("Local entity passed was NULL.");
        if (dataObj == null)
            throw new RuntimeException("Data object passed was NULL.");
        if (dataObj.getId() == null)
            throw new RuntimeException("Data object's ID was NULL.");

        DataConsumedDocument activity = DataConsumedDocument.Factory.newInstance();
        DataProductNotificationType activityType = activity.addNewDataConsumed();

        // set the data product to the consumed data
        DataProductType dataProduct = activityType.addNewDataProduct();
        // set data ID and size
        dataProduct.setId(dataObj.getId().toString());
        dataProduct.setSizeInBytes(dataObj.getSizeInBytes());
        // set data URLs
        List<URI> locations = dataObj.getLocations();
        for (URI location : locations) {
            dataProduct.addLocation(location.toString());
        }
        // set data timestampp
        final Calendar cal = new GregorianCalendar();
        cal.setTime(activityTimestamp != null ? activityTimestamp : new Date());
        dataProduct.setTimestamp(cal);

        sendNotification(context, activity, descriptionAndAnnotation, "[consumed: ID=<" + dataObj.getId().toString()
                + ">; URL=<#" + locations.size() + "><" + (locations.size() > 0 ? locations.get(0) : "") + ">]");

        return dataObj;
    }

    // /**
    // * Adds the file/directory was used by this invocation to the current dataConsuemd
    // * notification batch. If the notification batch did not exist, it is created. The notification
    // * is not sent until {@link #flush()} is called.
    // *
    // * @param entity identity of the workflow/service's invocation that consumed this file
    // * @param dataObj data object recording the dataId, local/remote URLs, timestamp of
    // * the file/dir, that was returned by another data notification method
    // * @param descriptionAndAnnotation optional vararg. The first element is used as the
    // * human readable description for this notification. The subsequent strings need to be
    // * serialized XML fragments that are added as annotation to the notification.
    // *
    // * @return the data object passed to this method with file/dir size filled in if not
    // * already when passed.
    // *
    // */
    // protected DataObj dataConsumedBatched(WorkflowTrackingContext context, InvocationEntity entity, DataObj dataObj,
    // String...descriptionAndAnnotation) {
    //
    // if(entity == null) throw new RuntimeException("Local entity passed was NULL.");
    // if(dataObj == null) throw new RuntimeException("Data object passed was NULL.");
    // if(dataObj.getId() == null) throw new RuntimeException("Data object's ID was NULL.");
    //
    // if (dataConsumedBatchActivity == null) {
    //
    // // create initial consumed notification container
    // dataConsumedBatchActivity = DataConsumedDocument.Factory.newInstance();
    // DataProductNotificationType activityType = dataConsumedBatchActivity.addNewDataConsumed();
    //
    //
    // }
    //
    // // get existing consumed notification container
    // DataProductNotificationType activityType = dataConsumedBatchActivity.addNewDataConsumed();
    //
    // // add nre data product to the consumed data
    // DataProductType dataProduct = activityType.addNewDataProduct();
    // // set data ID and size
    // dataProduct.setId(dataObj.getId().toString());
    // dataProduct.setSizeInBytes(dataObj.getSizeInBytes());
    // // set data URLs
    // List<URI> locations = dataObj.getLocations();
    // for(URI location : locations){
    // dataProduct.addLocation(location.toString());
    // }
    // // set data timestampp
    // final Calendar cal = new GregorianCalendar();
    // cal.setTime(activityTimestamp != null ? activityTimestamp : new Date());
    // dataProduct.setTimestamp(cal);
    //
    // sendNotification(context, activityType, descriptionAndAnnotation,
    // "[consumed: ID=<" + dataObj.getId().toString() +
    // ">; URL=<#" + locations.size() + "><" +
    // (locations.size() > 0 ? locations.get(0) : "") +
    // ">]"
    // );
    //
    // return dataObj;
    // }

    /**
     * {@inheritDoc}
     * 
     */
    public DataObj dataProduced(WorkflowTrackingContext context, URI dataId, List<URI> locations,
            String... descriptionAndAnnotation) {

        DataObj dataObj = new DataObjImpl(dataId, locations);
        return dataProduced(context, dataObj, descriptionAndAnnotation);
    }

    public DataObj dataProduced(WorkflowTrackingContext context, URI dataId, List<URI> locations, int sizeInBytes,
            String... descriptionAndAnnotation) {

        DataObj dataObj = new DataObjImpl(dataId, locations, sizeInBytes);
        return dataProduced(context, dataObj, descriptionAndAnnotation);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public DataObj dataProduced(WorkflowTrackingContext context, DataObj dataObj, String... descriptionAndAnnotation) {
        InvocationEntity entity = context.getMyself();
        if (entity == null)
            throw new RuntimeException("Local entity passed was NULL.");
        if (dataObj == null)
            throw new RuntimeException("Data object passed was NULL.");
        if (dataObj.getId() == null)
            throw new RuntimeException("Data object's ID was NULL.");

        DataProducedDocument activity = DataProducedDocument.Factory.newInstance();
        DataProductNotificationType activityType = activity.addNewDataProduced();

        // set the data product to the produced data
        DataProductType dataProduct = activityType.addNewDataProduct();
        // set data ID and size
        dataProduct.setId(dataObj.getId().toString());
        dataProduct.setSizeInBytes(dataObj.getSizeInBytes());
        // set data URLs
        List<URI> locations = dataObj.getLocations();
        for (URI location : locations) {
            dataProduct.addLocation(location.toString());
        }
        // set data timestampp
        final Calendar cal = new GregorianCalendar();
        cal.setTime(activityTimestamp != null ? activityTimestamp : new Date());
        dataProduct.setTimestamp(cal);

        sendNotification(context, activity, descriptionAndAnnotation, "[produced: ID=<" + dataObj.getId().toString()
                + ">; URL=<#" + locations.size() + "><" + (locations.size() > 0 ? locations.get(0) : "") + ">]");

        return dataObj;
    }

    /**
     * Adds the file/directory was used by this invocation to the current dataProduced notification batch. If the
     * notification batch did not exist, it is created. The notification is not sent untill {@link #flush()} is called.
     * 
     * @param entity
     *            identity of the workflow/service's invocation that produced this file
     * @param dataObj
     *            data object recording the dataId, local/remote URLs, timestamp of the file/dir, that was returned by
     *            another data notification method
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     * @return the data object passed to this method with file/dir size filled in if not already when passed.
     * 
     */
    protected DataObj dataProducedBatched(WorkflowTrackingContext context, InvocationEntity entity, DataObj dataObj,
            String... descriptionAndAnnotation) {

        if (entity == null)
            throw new RuntimeException("Local entity passed was NULL.");
        if (dataObj == null)
            throw new RuntimeException("Data object passed was NULL.");
        if (dataObj.getId() == null)
            throw new RuntimeException("Data object's ID was NULL.");

        if (dataProducedBatchActivity == null) {

            // create initial produced notification container
            dataProducedBatchActivity = DataProducedDocument.Factory.newInstance();
        }

        // get existing produced notification container
        DataProductNotificationType activityType = dataProducedBatchActivity.addNewDataProduced();

        // add new data product to the produced data
        DataProductType dataProduct = activityType.addNewDataProduct();
        // set data ID and size
        dataProduct.setId(dataObj.getId().toString());
        dataProduct.setSizeInBytes(dataObj.getSizeInBytes());
        // set data URLs
        List<URI> locations = dataObj.getLocations();
        for (URI location : locations) {
            dataProduct.addLocation(location.toString());
        }
        // set data timestamp
        final Calendar cal = new GregorianCalendar();
        cal.setTime(activityTimestamp != null ? activityTimestamp : new Date());
        dataProduct.setTimestamp(cal);

        // add description, and annotation to DATA PRODUCT if present
        sendNotification(context, dataProducedBatchActivity, descriptionAndAnnotation, "[produced: ID=<"
                + dataObj.getId().toString() + ">; URL=<#" + locations.size() + "><"
                + (locations.size() > 0 ? locations.get(0) : "") + ">]");

        return dataObj;
    }

}
