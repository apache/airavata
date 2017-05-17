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
//*/
//package org.apache.airavata.xbaya.interpreter;
//
//
//import org.apache.airavata.workflow.tracking.NotifierFactory;
//import org.apache.airavata.workflow.tracking.WorkflowNotifier;
//import org.apache.airavata.workflow.tracking.common.InvocationContext;
//import org.apache.airavata.workflow.tracking.common.InvocationEntity;
//import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
//import org.apache.axis2.addressing.EndpointReference;
//
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
//import java.net.URI;
//import java.util.Properties;
//
//public class NotificationSender {
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
//     * @param brokerURL The location of notification brokerUrl.
//     * @param topic     The notification topic.
//     */
//    public NotificationSender(String brokerURL, String topic) {
//        this.topic = topic;
//        this.brokerURL = brokerURL;
//        this.workflowID = URI.create(this.convertToJavaIdentifier(this.topic));
//        this.eventSink = new EndpointReference(this.brokerURL);
//        Properties props = new Properties();
//
//        this.notifier = NotifierFactory.createNotifier();
//        URI initiatorWorkflowID = null;
//        URI initiatorServiceID = URI.create(this.convertToJavaIdentifier(topic));
//        String initiatorWorkflowNodeID = null;
//        Integer initiatorWorkflowTimeStep = null;
//        this.context = this.notifier.createTrackingContext(props, brokerURL, initiatorWorkflowID,
//                initiatorServiceID, initiatorWorkflowNodeID, initiatorWorkflowTimeStep);
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
//    /**
//     * @return The event sink EPR.
//     */
//    public EndpointReference getEventSink() {
//        return this.eventSink;
//    }
//
//
//    public void workflowStarted(String message) {
//        this.invocationContext = this.notifier.workflowInvoked(this.context, this.initiator, message);
//    }
//
//
//    public void workflowFinished(String message) {
//        this.notifier.sendingResult(context, this.invocationContext, message);
//        this.notifier.workflowTerminated(context, this.workflowID, "Workflow finished successfully.");
//    }
//
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
//
//    public void workflowTerminated() {
//        this.notifier.workflowTerminated(context, this.workflowID, "Workflow finished successfully.");
//    }
//
//    /**
//     * Sends a START_INCOMPLETED notification message.
//     *
//     * @param message The message to send
//     */
//    public void workflowFailed(String message) {
//        workflowFailed(message, null);
//    }
//
//    /**
//     * Sends a START_INCOMPLETED notification message.
//     *
//     * @param e
//     */
//    public void workflowFailed(Throwable e) {
//        workflowFailed(null, e);
//    }
//
//    /**
//     * Sends a START_INCOMPLETED notification message.
//     *
//     * @param message The message to send
//     * @param e
//     */
//    public void workflowFailed(String message, Throwable e) {
//        this.notifier.sendingFault(context, this.invocationContext, message);
//    }
//
//    public void info(String message) {
//        this.notifier.info(context, message);
//    }
//
//    /**
//     * @param throwable
//     * @return The stackTrace in String
//     */
//    public static String getStackTraceInString(Throwable throwable) {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        PrintStream printStream = new PrintStream(byteArrayOutputStream);
//        throwable.printStackTrace(printStream);
//        printStream.flush();
//        return byteArrayOutputStream.toString();
//    }
//
//    public static String convertToJavaIdentifier(String name) {
//
//        final char REPLACE_CHAR = '_';
//
//        if (name == null || name.length() == 0) {
//            return "" + REPLACE_CHAR;
//        }
//
//        StringBuilder buf = new StringBuilder();
//
//        char c = name.charAt(0);
//        if (!Character.isJavaIdentifierStart(c)) {
//            // Add _ at the beggining instead of replacing it to _. This is
//            // more readable if the name is like 3D_Model.
//            buf.append(REPLACE_CHAR);
//        }
//
//        for (int i = 0; i < name.length(); i++) {
//            c = name.charAt(i);
//            if (Character.isJavaIdentifierPart(c)) {
//                buf.append(c);
//            } else {
//                buf.append(REPLACE_CHAR);
//            }
//        }
//
//        return buf.toString();
//    }
//}