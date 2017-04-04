/**
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
 */
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.xbaya.jython.lib;
//
//import java.net.URI;
//import java.util.Iterator;
//
//import org.apache.airavata.common.utils.StringUtil;
//import org.apache.airavata.common.utils.XMLUtil;
//import org.apache.airavata.workflow.tracking.WorkflowNotifier;
//import org.apache.airavata.workflow.tracking.common.InvocationContext;
//import org.apache.airavata.workflow.tracking.common.InvocationEntity;
//import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
//import org.apache.airavata.workflow.tracking.impl.state.InvocationContextImpl;
//import org.apache.axis2.addressing.EndpointReference;
//import org.apache.xmlbeans.XmlException;
//import org.apache.xmlbeans.XmlObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.xmlpull.infoset.XmlElement;
//
//import xsul.wsif.WSIFMessage;
//
//public class ServiceNotificationSender implements ServiceNotifiable {
//
//    private static final Logger logger = LoggerFactory.getLogger(ServiceNotificationSender.class);
//
//    private WorkflowNotifier notifier;
//
//    private URI workflowID;
//
//    private String nodeID;
//
//    private String serviceID;
//
//    private InvocationEntity receiver;
//
//    private InvocationEntity initiator;
//
//    private InvocationContext invocationContext;
//
//    private WorkflowTrackingContext context;
//
//    private EndpointReference eventSink;
//
//    /**
//     * Constructs a ServiceNotificationSender.
//     *
//     * @param notifier
//     * @param eventSink
//     * @param initiator
//     * @param workflowID
//     * @param nodeID
//     */
//    protected ServiceNotificationSender(WorkflowNotifier notifier, EndpointReference eventSink,
//            InvocationEntity initiator, URI workflowID, String nodeID, WorkflowTrackingContext context, InvocationContext invocationContext) {
//        this.notifier = notifier;
//        this.eventSink = eventSink;
//        this.initiator = initiator;
//        this.workflowID = workflowID;
//        this.nodeID = nodeID;
//        this.context = context;
//        // In case of creating a service on the fly, there is no serviceID at
//        // the beginning.
//        this.serviceID = "";
//        this.invocationContext = invocationContext;
//        URI receiverWorkflowID = this.workflowID;
//        URI receiverServiceID = URI.create(this.serviceID);
//        String receiverWorkflowNodeID = this.nodeID;
//        Integer workflowTimeStep = new Integer(0);
//        this.receiver = this.notifier.createEntity(receiverWorkflowID, receiverServiceID, receiverWorkflowNodeID,
//                workflowTimeStep);
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.ServiceNotifiable#setServiceID(java.lang.String)
//     */
//    @Override
//    public void setServiceID(String serviceID) {
//        logger.debug("SerivceID:" + serviceID);
//        this.serviceID = serviceID;
//
//        URI receiverWorkflowID = this.workflowID;
//        URI receiverServiceID = URI.create(this.serviceID);
//        String receiverWorkflowNodeID = this.nodeID;
//        Integer workflowTimeStep = new Integer(0);
//        this.receiver = this.notifier.createEntity(receiverWorkflowID, receiverServiceID, receiverWorkflowNodeID,
//                workflowTimeStep);
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.ServiceNotifiable#getEventSink()
//     */
//    @Override
//    public EndpointReference getEventSink() {
//        return this.eventSink;
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.ServiceNotifiable#getWorkflowID()
//     */
//    @Override
//    public URI getWorkflowID() {
//        return this.workflowID;
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.ServiceNotifiable#invokingService(xsul.wsif.WSIFMessage)
//     */
//    @Override
//    public void invokingService(WSIFMessage inputs) {
//        String message = "";
//        Iterator partIt = inputs.partNames().iterator();
//        boolean first = true;
//        while (partIt.hasNext()) {
//            if (first) {
//                first = false;
//            } else {
//                message += ", ";
//            }
//
//            String name = (String) partIt.next();
//            Object value = inputs.getObjectPart(name);
//            if(value instanceof org.xmlpull.v1.builder.XmlElement){
//                message += name + "=";
//                Iterator children = ((org.xmlpull.v1.builder.XmlElement) value).children();
//                while (children.hasNext()){
//                    message += children.next();
//                }
//            }else{
//                message += name + "=" + value;
//            }
//        }
//        XmlObject header = null;
//        XmlObject body;
//        try {
//            body = XmlObject.Factory.parse(inputs.toString());
//        } catch (XmlException e) {
//            logger.warn("Failed to parse " + inputs.toString(), e);
//            body = null; // Send notification anyway.
//        }
//        this.invocationContext = this.notifier.invokingService(this.context, this.receiver, header, body, message);
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.ServiceNotifiable#serviceFinished(xsul.wsif.WSIFMessage)
//     */
//    @Override
//    public void serviceFinished(WSIFMessage outputs) {
//        String message = "";
//        Iterator partIt = outputs.partNames().iterator();
//        boolean first = true;
//        while (partIt.hasNext()) {
//            if (first) {
//                first = false;
//            } else {
//                message += ", ";
//            }
//            String name = (String) partIt.next();
//            Object value = outputs.getObjectPart(name);
//            if(value instanceof org.xmlpull.v1.builder.XmlElement){
//                message += name + "=";
//                Iterator children = ((org.xmlpull.v1.builder.XmlElement) value).children();
//                while (children.hasNext()){
//                    message += children.next();
//                }
//            }else{
//             message += name + "=" + value;
//            }
//        }
//        XmlObject header = null;
//        XmlObject body;
//        try {
//            body = XmlObject.Factory.parse(outputs.toString());
//        } catch (XmlException e) {
//            logger.warn("Failed to parse " + outputs.toString(), e);
//            body = null; // Send notification anyway.
//        }
//        this.notifier.receivedResult(this.context, this.invocationContext, header, body, message);
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.ServiceNotifiable#invocationFailed(java.lang.String,
//     * java.lang.Throwable)
//     */
//    @Override
//    public void invocationFailed(String message, Throwable e) {
//
//        // TODO there are two types of error messages.
//        // The first one is while creating a service. (No API)
//        // The second is while invoking a service.
//        // e.g. notifier.invokingServiceFailed().
//
//        // XXX If error occurs before invoking a service, create a fake
//        // invocation context.
//        if (this.invocationContext == null) {
//            this.invocationContext = new InvocationContextImpl(this.initiator, this.receiver);
//        }
//
//        logger.error(e.getMessage(), e);
//        if (message == null || "".equals(message)) {
//            message = "Error";
//        }
//        if (e != null) {
//            message += ": " + e.toString();
//        }
//        if (e != null) {
//            String stackTrace = StringUtil.getStackTraceInString(e);
//            XmlElement stackTraceElement = XMLUtil.BUILDER.newFragment("stackTrace");
//            stackTraceElement.addChild(stackTrace);
//            String annotation = XMLUtil.xmlElementToString(stackTraceElement);
//            this.notifier.invokingServiceFailed(this.context, this.invocationContext, e, message, annotation);
//        } else {
//            this.notifier.invokingServiceFailed(this.context, this.invocationContext, message);
//        }
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.ServiceNotifiable#receivedFault(java.lang.String)
//     */
//    @Override
//    @Deprecated
//    public void receivedFault(String message) {
//        // XXX If error occurs before invoking a service, create a fake
//        // invocation context.
//        if (this.invocationContext == null) {
//            this.invocationContext = new InvocationContextImpl(this.initiator, this.receiver);
//        }
//
//        if (message == null || "".equals(message)) {
//            message = "Error";
//        }
//        this.notifier.receivedFault(this.context, this.invocationContext, message);
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.ServiceNotifiable#receivedFault(xsul.wsif.WSIFMessage)
//     */
//    @Override
//    public void receivedFault(WSIFMessage fault) {
//        // XXX If error occurs before invoking a service, create a fake
//        // invocation context.
//        if (this.invocationContext == null) {
//            this.invocationContext = new InvocationContextImpl(this.initiator, this.receiver);
//        }
//
//        String message = "Received a fault message from the service";
//        XmlObject header = null;
//        XmlObject body;
//        try {
//            body = XmlObject.Factory.parse(fault.toString());
//        } catch (XmlException e) {
//            logger.warn("Failed to parse " + fault.toString(), e);
//            body = null; // Send notification anyway.
//        }
//        this.notifier.receivedFault(this.context, this.invocationContext, header, body, message);
//    }
//}