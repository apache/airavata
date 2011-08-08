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

package org.apache.airavata.xbaya.jython.lib;

import java.net.URI;

import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.airavata.xbaya.util.StringUtil;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.python.core.PyObject;
import org.xmlpull.infoset.XmlElement;

import xsul.XmlConstants;
import xsul.ws_addressing.WsaEndpointReference;
import xsul5.MLogger;
import edu.indiana.extreme.lead.workflow_tracking.Notifier;
import edu.indiana.extreme.lead.workflow_tracking.NotifierFactory;
import edu.indiana.extreme.lead.workflow_tracking.common.AnnotationProps;
import edu.indiana.extreme.lead.workflow_tracking.common.ConstructorConsts;
import edu.indiana.extreme.lead.workflow_tracking.common.ConstructorProps;
import edu.indiana.extreme.lead.workflow_tracking.common.InvocationContext;
import edu.indiana.extreme.lead.workflow_tracking.common.InvocationEntity;

public class NotificationSender {

    protected static final MLogger logger = MLogger.getLogger();

    protected Notifier notifier;

    protected String brokerURL;

    protected String topic;

    protected URI workflowID;

    protected InvocationEntity initiator;

    protected InvocationEntity receiver;

    protected InvocationContext invocationContext;

    protected WsaEndpointReference eventSink;

    /**
     * Constructs a NotificationSender.
     * 
     * @param brokerURL
     * @param topic
     */
    public NotificationSender(URI brokerURL, String topic) {
        this(brokerURL.toString(), topic);
    }

    /**
     * Creates a NotificationSender.
     * 
     * @param brokerURL
     *            The location of notification brokerUrl.
     * @param topic
     *            The notification topic.
     */
    public NotificationSender(String brokerURL, String topic) {
        logger.entering(new Object[] { brokerURL, topic });
        this.topic = topic;
        this.brokerURL = brokerURL;
        this.workflowID = URI.create(StringUtil.convertToJavaIdentifier(this.topic));
        //todo have to remove the xsul dependency completely
        URI temporaryURI = URI.create(WseMsgBrokerClient.createEndpointReference(this.brokerURL, this.topic).getAddress());
        this.eventSink = new WsaEndpointReference(temporaryURI);
        ConstructorProps props = ConstructorProps.newProps();
        props.set(ConstructorConsts.BROKER_EPR, XmlConstants.BUILDER.serializeToString(this.eventSink));
        AnnotationProps annotationProps = AnnotationProps.newProps();
        props.set(ConstructorConsts.ANNOTATIONS, annotationProps);

        this.notifier = NotifierFactory.createNotifier(props);

        URI initiatorWorkflowID = null;
        URI initiatorServiceID = URI.create("XBaya");
        String initiatorWorkflowNodeID = null;
        Integer initiatorWorkflowTimeStep = null;
        this.initiator = this.notifier.createEntity(initiatorWorkflowID, initiatorServiceID, initiatorWorkflowNodeID,
                initiatorWorkflowTimeStep);

        URI receiverWorkflowID = this.workflowID;
        URI receiverServiceID = this.workflowID;
        String receiverWorkflowNodeID = null;
        Integer receiverWorkflowTimeStep = null;
        this.receiver = this.notifier.createEntity(receiverWorkflowID, receiverServiceID, receiverWorkflowNodeID,
                receiverWorkflowTimeStep);
    }

    /**
     * @return The event sink EPR.
     */
    public WsaEndpointReference getEventSink() {
        return this.eventSink;
    }

    /**
     * @param args
     * @param keywords
     */
    public void workflowStarted(PyObject[] args, String[] keywords) {
        logger.entering(new Object[] { args, keywords });
        String message = "";
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                message += ", ";
            }
            message += keywords[i] + "=" + args[i];
        }
        this.invocationContext = this.notifier.workflowInvoked(this.receiver, this.initiator, message);
    }

    public void workflowStarted(Object[] args, String[] keywords) {
        logger.entering(new Object[] { args, keywords });
        String message = "";
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                message += ", ";
            }
            message += keywords[i] + "=" + args[i];
        }
        this.invocationContext = this.notifier.workflowInvoked(this.receiver, this.initiator, message);
    }

    /**
     * @param args
     * @param keywords
     */
    public void workflowFinished(Object[] args, String[] keywords) {
        logger.entering(new Object[] { args, keywords });
        String message = "";
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                message += ", ";
            }
            message += keywords[i] + "=" + args[i];
        }
        this.notifier.sendingResult(this.invocationContext, message);
        this.notifier.workflowTerminated(this.workflowID, "Workflow finished successfully.");
    }

    public void sendingPartialResults(Object[] args, String[] keywords) {
        logger.entering(new Object[] { args, keywords });
        String message = "";
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                message += ", ";
            }
            message += keywords[i] + "=" + args[i];
        }
        this.notifier.sendingResult(this.invocationContext, message);
    }

    /**
     * @param args
     * @param keywords
     */
    public void workflowFinished(PyObject[] args, String[] keywords) {
        logger.entering(new Object[] { args, keywords });
        String message = "";
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                message += ", ";
            }
            message += keywords[i] + "=" + args[i];
        }
        this.notifier.sendingResult(this.invocationContext, message);
        this.notifier.workflowTerminated(this.workflowID, "Workflow finished successfully.");
    }

    public void workflowTerminated() {
        this.notifier.workflowTerminated(this.workflowID, "Workflow finished successfully.");
    }

    /**
     * Sends a START_INCOMPLETED notification message.
     * 
     * @param message
     *            The message to send
     */
    public void workflowFailed(String message) {
        workflowFailed(message, null);
    }

    /**
     * Sends a START_INCOMPLETED notification message.
     * 
     * @param e
     */
    public void workflowFailed(Throwable e) {
        workflowFailed(null, e);
    }

    /**
     * Sends a START_INCOMPLETED notification message.
     * 
     * @param message
     *            The message to send
     * @param e
     */
    public void workflowFailed(String message, Throwable e) {
        logger.entering(new Object[] { message, e });
        logger.caught(e);
        if (message == null || "".equals(message)) {
            message = "Error";
        }
        if (e != null) {
            message += ": " + e.toString();
        }
        if (e != null) {
            String stackTrace = StringUtil.getStackTraceInString(e);
            XmlElement stackTraceElement = XMLUtil.BUILDER.newFragment("stackTrace");
            stackTraceElement.addChild(stackTrace);
            this.notifier.sendingFault(this.invocationContext, message, XMLUtil.xmlElementToString(stackTraceElement));
        } else {
            this.notifier.sendingFault(this.invocationContext, message);
        }
    }

    public void info(String message) {
        this.notifier.info(this.invocationContext, message);
    }

    public void resourceMapping(String host, String... message) {
        this.notifier.resourceMapping(this.invocationContext, host, 1, message);
    }

    /**
     * @param nodeID
     * @return The ServiceNoficationSender created.
     */
    public ServiceNotificationSender createServiceNotificationSender(String nodeID) {
        return new ServiceNotificationSender(this.notifier, this.eventSink, this.initiator, this.workflowID, nodeID);
    }
}