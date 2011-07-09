/*
 * Copyright (c) 2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: ServiceNotificationSender.java,v 1.7 2008/04/01 21:44:28 echintha Exp $
 */
package org.apache.airavata.xbaya.test.service;

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

import java.net.URI;

import org.apache.airavata.xbaya.util.XMLUtil;
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
import edu.indiana.extreme.lead.workflow_tracking.Notifier;
import edu.indiana.extreme.lead.workflow_tracking.NotifierFactory;
import edu.indiana.extreme.lead.workflow_tracking.common.ConstructorProps;
import edu.indiana.extreme.lead.workflow_tracking.common.InvocationContext;
import edu.indiana.extreme.lead.workflow_tracking.common.InvocationEntity;
import edu.indiana.extreme.lead.workflow_tracking.util.MessageUtil;

/**
 * @author Satoshi Shirasuna
 */
public class ServiceNotificationSender {

    private final static MLogger logger = MLogger.getLogger();

    private final static String INVOKED_MESSAGE = "Service is invoked";

    private final static String SENDING_RESULT_MESSAGE = "Sending successful result of invocation";

    private Notifier notifier;

    private InvocationEntity initiator;

    private InvocationEntity receiver;

    private InvocationContext invocationContext;

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

        ConstructorProps props = MessageUtil.createConstructorPropsFromLeadContext(leadContext);
        this.notifier = NotifierFactory.createNotifier(props);

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
        this.invocationContext = this.notifier.serviceInvoked(this.receiver, this.initiator, headerObject, bodyObject,
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
            this.notifier.sendingResult(this.invocationContext, headerObject, bodyObject, SENDING_RESULT_MESSAGE);
        } catch (RuntimeException e) {
            logger.caught(e);
        }
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2007 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */
