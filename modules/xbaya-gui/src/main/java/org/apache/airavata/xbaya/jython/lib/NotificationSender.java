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
//import java.util.Properties;
//
//import org.apache.airavata.common.utils.StringUtil;
//import org.apache.airavata.common.utils.XMLUtil;
//import org.apache.airavata.workflow.tracking.NotifierFactory;
//import org.apache.airavata.workflow.tracking.WorkflowNotifier;
//import org.apache.airavata.workflow.tracking.common.InvocationContext;
//import org.apache.airavata.workflow.tracking.common.InvocationEntity;
//import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
//import org.apache.axis2.addressing.EndpointReference;
//import org.python.core.PyObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.xmlpull.infoset.XmlElement;
//
//public class NotificationSender implements WorkflowNotifiable {
//
//    protected static final Logger logger = LoggerFactory.getLogger(NotificationSender.class);
//
//    protected WorkflowNotifier notifier;
//
//    protected String brokerURL;
//
//    protected String topic;
//
//    protected URI workflowID;
//
//    protected InvocationEntity initiator;
//
//    protected InvocationEntity receiver;
//
//    protected InvocationContext invocationContext;
//
//    protected EndpointReference eventSink;
//
//    protected WorkflowTrackingContext context;
//
//    /**
//     * Constructs a NotificationSender.
//     *
//     * @param brokerURL
//     * @param topic
//     */
//    public NotificationSender(URI brokerURL, String topic) {
//        this(brokerURL.toString(), topic);
//    }
//
//    /**
//     * Creates a NotificationSender.
//     *
//     * @param brokerURL
//     *            The location of notification brokerUrl.
//     * @param topic
//     *            The notification topic.
//     */
//    public NotificationSender(String brokerURL, String topic) {
//        logger.debug("brokerURL:" + brokerURL + "topic:" + topic);
//        this.topic = topic;
//        this.brokerURL = brokerURL;
//        this.workflowID = URI.create(StringUtil.convertToJavaIdentifier(this.topic));
//        this.eventSink = new EndpointReference(this.brokerURL);
//        Properties props = new Properties();
//
//        this.notifier = NotifierFactory.createNotifier();
//        URI initiatorWorkflowID = null;
//        URI initiatorServiceID = URI.create(StringUtil.convertToJavaIdentifier(topic));
//        String initiatorWorkflowNodeID = null;
//        Integer initiatorWorkflowTimeStep = null;
//        this.context = this.notifier.createTrackingContext(props, brokerURL, initiatorWorkflowID, initiatorServiceID,
//                initiatorWorkflowNodeID, initiatorWorkflowTimeStep);
//        this.context.setTopic(topic);
//        this.initiator = this.notifier.createEntity(initiatorWorkflowID, initiatorServiceID, initiatorWorkflowNodeID,
//                initiatorWorkflowTimeStep);
//
//        URI receiverWorkflowID = this.workflowID;
//        URI receiverServiceID = this.workflowID;
//        String receiverWorkflowNodeID = null;
//        Integer receiverWorkflowTimeStep = null;
//        this.receiver = this.notifier.createEntity(receiverWorkflowID, receiverServiceID, receiverWorkflowNodeID,
//                receiverWorkflowTimeStep);
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable#getEventSink()
//     */
//    @Override
//    public EndpointReference getEventSink() {
//        return this.eventSink;
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable#workflowStarted(org.python.core.PyObject[],
//     * java.lang.String[])
//     */
//    @Override
//    public void workflowStarted(PyObject[] args, String[] keywords) {
//        String message = "";
//        for (int i = 0; i < args.length; i++) {
//            if (i != 0) {
//                message += ", ";
//            }
//            message += keywords[i] + "=" + args[i];
//        }
//        this.invocationContext = this.notifier.workflowInvoked(this.context, this.initiator, message);
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable#workflowStarted(java.lang.Object[],
//     * java.lang.String[])
//     */
//    @Override
//    public void workflowStarted(Object[] args, String[] keywords) {
//        String message = "";
//        for (int i = 0; i < args.length; i++) {
//            if (i != 0) {
//                message += ", ";
//            }
//            message += keywords[i] + "=" + args[i];
//        }
//        this.invocationContext = this.notifier.workflowInvoked(this.context, this.initiator, message);
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable#workflowFinished(java.lang.Object[],
//     * java.lang.String[])
//     */
//    @Override
//    public void workflowFinished(Object[] args, String[] keywords) {
//        String message = "";
//        for (int i = 0; i < args.length; i++) {
//            if (i != 0) {
//                message += ", ";
//            }
//            message += keywords[i] + "=" + args[i];
//        }
//        this.notifier.sendingResult(context, this.invocationContext, message);
//        this.notifier.workflowTerminated(context, this.workflowID, "Workflow finished successfully.");
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable#sendingPartialResults(java.lang.Object[],
//     * java.lang.String[])
//     */
//    @Override
//    public void sendingPartialResults(Object[] args, String[] keywords) {
//        String message = "";
//        for (int i = 0; i < args.length; i++) {
//            if (i != 0) {
//                message += ", ";
//            }
//            message += keywords[i] + "=" + args[i];
//        }
//        this.notifier.sendingResult(context, this.invocationContext, message);
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable#workflowFinished(org.python.core.PyObject[],
//     * java.lang.String[])
//     */
//    @Override
//    public void workflowFinished(PyObject[] args, String[] keywords) {
//        String message = "";
//        for (int i = 0; i < args.length; i++) {
//            if (i != 0) {
//                message += ", ";
//            }
//            message += keywords[i] + "=" + args[i];
//        }
//        this.notifier.sendingResult(context, this.invocationContext, message);
//        this.notifier.workflowTerminated(context, this.workflowID, "Workflow Execution Finished.");
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable#workflowTerminated()
//     */
//    @Override
//    public void workflowTerminated() {
//        this.notifier.workflowTerminated(context, this.workflowID, "Workflow Execution Finished.");
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable#workflowFailed(java.lang.String)
//     */
//    @Override
//    public void workflowFailed(String message) {
//        workflowFailed(message, null);
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable#workflowFailed(java.lang.Throwable)
//     */
//    @Override
//    public void workflowFailed(Throwable e) {
//        workflowFailed(null, e);
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable#workflowFailed(java.lang.String,
//     * java.lang.Throwable)
//     */
//    @Override
//    public void workflowFailed(String message, Throwable e) {
//        if(e != null)
//            logger.error(e.getMessage(), e);
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
//            this.notifier.sendingFault(context, this.invocationContext, message,
//                    XMLUtil.xmlElementToString(stackTraceElement));
//        } else {
//            this.notifier.sendingFault(context, this.invocationContext, message);
//        }
//    }
//
//    public void info(String message) {
//        this.notifier.info(context, message);
//    }
//
//    /*
//     * (non-Javadoc)
//     *
//     * @see org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable#createServiceNotificationSender(java.lang.String)
//     */
//    @Override
//    public ServiceNotifiable createServiceNotificationSender(String nodeID) {
//        return new ServiceNotificationSender(this.notifier, this.eventSink, this.initiator, this.workflowID, nodeID,
//                this.context,this.invocationContext);
//    }
//
//    @Override
//    public void cleanup(){
//        this.notifier.delete();
//    }
//
//    public String getTopic() {
//        return topic;
//    }
//}