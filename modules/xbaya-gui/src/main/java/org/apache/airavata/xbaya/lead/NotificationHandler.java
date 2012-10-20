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

package org.apache.airavata.xbaya.lead;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.workflow.tracking.NotifierFactory;
import org.apache.airavata.workflow.tracking.WorkflowNotifier;
import org.apache.airavata.workflow.tracking.common.InvocationContext;
import org.apache.airavata.workflow.tracking.common.InvocationEntity;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.builder.XmlElement;

import xsul.XmlConstants;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.lead.LeadContextHeader;
import xsul.message_router.MessageContext;
import xsul.xbeans_util.XBeansUtil;
import xsul.xhandler.BaseHandler;

public class NotificationHandler extends BaseHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationHandler.class);

    private static final String INVOKING_MESSAGE = "Invoking a workflow";

    private static final String RECEIVE_RESULT_MESSAGE = "A workflow finished successfully.";

    private static final String RECEIVE_FAULT_MESSAGE = "A workflow failed.";

    private LeadContextHeader leadContext;

    private WorkflowNotifier notifier;

    private WorkflowTrackingContext context;

    private InvocationContext invocationContext;

    private InvocationEntity invocationEntity;

    private WorkflowContextHeaderBuilder builder;

    /**
     * Constructs a NotificationHandler.
     * 
     * @param leadContext
     */
    public NotificationHandler(LeadContextHeader leadContext) {
        super(NotificationHandler.class.getName());
        this.leadContext = leadContext;
        this.notifier = NotifierFactory.createNotifier();
        URI myWorkflowID = null;
        URI myServiceID = URI.create(XBayaConstants.APPLICATION_SHORT_NAME);
        String userDN = this.leadContext.getUserDn();
        if (userDN != null ){
            if(userDN.trim().length() == 0) {
                String serviceIDAsString = XBayaConstants.APPLICATION_SHORT_NAME + ":" + userDN.trim();
                try {
                    myServiceID = new URI(null, null, serviceIDAsString, null);
                } catch (URISyntaxException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        String myNodeID = null;
        Integer myTimestep = null;
        EndpointReference epr = new EndpointReference(leadContext.getEventSink().getAddress().toString());
        this.invocationEntity = this.notifier.createEntity(myWorkflowID, myServiceID, myNodeID, myTimestep);
        this.context = this.notifier.createTrackingContext(new Properties(), epr.getAddress().toString(), myWorkflowID,
                myServiceID, myNodeID, myTimestep);
    }

    public NotificationHandler(WorkflowContextHeaderBuilder builder) {
        super(NotificationHandler.class.getName());
        this.builder = builder;
        this.notifier = NotifierFactory.createNotifier();
        URI myWorkflowID = null;
        URI myServiceID = URI.create(XBayaConstants.APPLICATION_SHORT_NAME);
        String userDN = this.builder.getUserIdentifier();
        if (userDN != null) {
            if (userDN.trim().length() == 0) {
                String serviceIDAsString = XBayaConstants.APPLICATION_SHORT_NAME + ":" + userDN.trim();
                try {
                    myServiceID = new URI(null, null, serviceIDAsString, null);
                } catch (URISyntaxException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        String myNodeID = null;
        Integer myTimestep = null;
        EndpointReference epr = new EndpointReference(builder.getWorkflowMonitoringContext().getEventPublishEpr());
        this.invocationEntity = this.notifier.createEntity(myWorkflowID, myServiceID, myNodeID, myTimestep);
        this.context = this.notifier.createTrackingContext(new Properties(), epr.getAddress().toString(), myWorkflowID,
                myServiceID, myNodeID, myTimestep);
    }

    /**
     * @see xsul.xhandler.BaseHandler#processOutgoingXml(org.xmlpull.v1.builder.XmlElement,
     *      xsul.message_router.MessageContext)
     */
    @Override
    public boolean processOutgoingXml(XmlElement soapEnvelope, MessageContext context)
            throws DynamicInfosetInvokerException {
        logger.info("soapEnvelope: " + XMLUtil.xmlElementToString(soapEnvelope));

        URI serviceWorkflowID = null;
        URI serviceServiceID = URI.create(this.builder.getWorkflowMonitoringContext().getServiceInstanceId());
        if (serviceServiceID == null) {
            serviceServiceID = URI.create("NoWorkflowIDSet");
        }
//        Integer serviceTimestep = null;
/*        String timeStep = Integer.toString(this.builder.getWorkflowMonitoringContext().getWorkflowTimeStep());
        if (timeStep != null) {
            try {
                serviceTimestep = new Integer(this.builder.getWorkflowMonitoringContext().getWorkflowTimeStep());
            } catch (NumberFormatException e) {
                logger.error(e.getMessage(), e);
            }
        }*/
        XmlElement soapHeader = soapEnvelope.element(null, XmlConstants.S_HEADER);
        XmlElement soapBody = soapEnvelope.element(null, XmlConstants.S_BODY);
        XmlObject headerObject = null;
        if (soapHeader != null) {
            headerObject = XBeansUtil.xmlElementToXmlObject(soapHeader);
        }
        XmlObject bodyObject = XBeansUtil.xmlElementToXmlObject(soapBody);

        this.invocationContext = this.notifier.invokingService(this.context, this.invocationEntity, headerObject,
                bodyObject, INVOKING_MESSAGE);
        return super.processOutgoingXml(soapEnvelope, context);
    }

    /**
     * @see xsul.xhandler.BaseHandler#processIncomingXml(org.xmlpull.v1.builder.XmlElement,
     *      xsul.message_router.MessageContext)
     */
    @Override
    public boolean processIncomingXml(XmlElement soapEnvelope, MessageContext context)
            throws DynamicInfosetInvokerException {
        logger.info("soapEnvelope: " + XMLUtil.xmlElementToString(soapEnvelope));

        XmlElement soapHeader = soapEnvelope.element(null, XmlConstants.S_HEADER);
        XmlObject headerObject = null;
        if (soapHeader != null) {
            headerObject = XBeansUtil.xmlElementToXmlObject(soapHeader);
        }

        XmlElement soapBody = soapEnvelope.element(null, XmlConstants.S_BODY);
        XmlObject bodyObject = XBeansUtil.xmlElementToXmlObject(soapBody);
        XmlElement faultElement = soapBody.element(null, "Fault");
        if (faultElement == null) {
            this.notifier.receivedResult(this.context, this.invocationContext, headerObject, bodyObject,
                    RECEIVE_RESULT_MESSAGE);
        } else {
            this.notifier.receivedFault(this.context, this.invocationContext, headerObject, bodyObject,
                    RECEIVE_FAULT_MESSAGE);
        }

        return super.processIncomingXml(soapEnvelope, context);
    }
}