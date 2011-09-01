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

package org.apache.airavata.xbaya.test.service;

import java.net.URI;
import java.util.Properties;

import org.apache.airavata.workflow.tracking.Notifier;
import org.apache.airavata.workflow.tracking.NotifierFactory;
import org.apache.airavata.workflow.tracking.common.InvocationContext;
import org.apache.airavata.workflow.tracking.common.InvocationEntity;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.xmlbeans.XmlObject;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;

import xsul.XmlConstants;
import xsul.lead.LeadContextHeader;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.xbeans_util.XBeansUtil;
import xsul5.MLogger;



public class ServiceNotificationSender {

    private final static MLogger logger = MLogger.getLogger();

    private final static String INVOKED_MESSAGE = "Service is invoked";

    private final static String SENDING_RESULT_MESSAGE = "Sending successful result of invocation";

    private Notifier notifier;

    private InvocationEntity initiator;

    private InvocationEntity receiver;

    private InvocationContext invocationContext;

    private WorkflowTrackingContext context;

    private SoapUtil soapFragrance;

    /**
     * @param inputElement
     * @return The ServiceNotificationSender
     */
    public static ServiceNotificationSender invoked(XmlElement inputElement) {
        try {

            XmlElement soapBody = (XmlElement) inputElement.getParent();
            XmlElement soapEnvelope = (XmlElement) soapBody.getParent();
            SoapUtil soapFragrance = SoapUtil.selectSoapFragrance(soapEnvelope,
                    new SoapUtil[] { Soap11Util.getInstance(), Soap12Util.getInstance() });
            XmlElement soapHeader = soapEnvelope.element(null, XmlConstants.S_HEADER);
            XmlElement leadHeader = soapHeader.element(LeadContextHeader.NS, LeadContextHeader.TYPE.getLocalPart());
            logger.finest("leadHeader: " + XMLUtil.xmlElementToString(leadHeader));
            if (leadHeader == null) {
                return null;
            }
            LeadContextHeader leadContext = new LeadContextHeader(leadHeader);
            ServiceNotificationSender sender = new ServiceNotificationSender(soapFragrance, leadContext);
            sender.serviceInvoked(inputElement);

            return sender;
        } catch (RuntimeException e) {
            logger.caught(e);
            return null;
        }
    }

    /**
     * Constructs a ServiceNotificationSender.
     * 
     * @param soapFragrance
     * @param leadContext
     */
    private ServiceNotificationSender(SoapUtil soapFragrance, LeadContextHeader leadContext) {
        this.soapFragrance = soapFragrance;
        this.notifier = NotifierFactory.createNotifier();

        URI workflowID = leadContext.getWorkflowId();
        String serviceIDString = leadContext.getServiceId();
        if (serviceIDString == null) {
            serviceIDString = "serviceIDWasNull";
        }
        URI serviceID = URI.create(serviceIDString);
        String nodeID = leadContext.getNodeId();
        String timeStepString = leadContext.getTimeStep();
        Integer timeStep = null;
        if (timeStepString != null) {
            try {
                timeStep = new Integer(timeStepString);
            } catch (NumberFormatException e) {
                logger.caught(e);
            }
        }
        this.initiator = this.notifier.createEntity(workflowID, serviceID, nodeID, timeStep);
        this.receiver = this.notifier.createEntity(workflowID, serviceID, nodeID, timeStep);
        EndpointReference epr = new EndpointReference(leadContext.getEventSink().getAddress().toString());
        this.context = this.notifier.createTrackingContext(new Properties(),epr.toString(),workflowID,serviceID,nodeID,timeStep);
    }

    /**
     * @param inputElement
     */
    private void serviceInvoked(XmlElement inputElement) {
        XmlElement soapBody = (XmlElement) inputElement.getParent();
        XmlElement soapEnvelope = (XmlElement) soapBody.getParent();
        XmlElement soapHeader = soapEnvelope.element(null, XmlConstants.S_HEADER);
        XmlObject headerObject = XBeansUtil.xmlElementToXmlObject(soapHeader);
        XmlObject bodyObject = XBeansUtil.xmlElementToXmlObject(soapBody);
        this.invocationContext = this.notifier.serviceInvoked(this.context,this.initiator, headerObject, bodyObject,
                INVOKED_MESSAGE);
    }

    /**
     * @param outputElement
     */
    public void sendingResult(XmlElement outputElement) {
        try {
            XmlDocument document = this.soapFragrance.wrapBodyContent(outputElement);
            XmlElement soapEnvelope = document.getDocumentElement();
            XmlElement soapHeader = soapEnvelope.element(null, XmlConstants.S_HEADER);
            XmlElement soapBody = soapEnvelope.element(null, XmlConstants.S_BODY);
            XmlObject headerObject = null;
            if (soapHeader != null) {
                headerObject = XBeansUtil.xmlElementToXmlObject(soapHeader);
            }
            XmlObject bodyObject = XBeansUtil.xmlElementToXmlObject(soapBody);
            this.notifier.sendingResult(this.context,this.invocationContext, headerObject, bodyObject, SENDING_RESULT_MESSAGE);
        } catch (RuntimeException e) {
            logger.caught(e);
        }
    }
}