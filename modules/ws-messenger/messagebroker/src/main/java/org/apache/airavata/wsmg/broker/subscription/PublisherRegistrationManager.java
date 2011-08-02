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

package org.apache.airavata.wsmg.broker.subscription;

import java.net.URI;

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.broker.context.ProcessingContext;
import org.apache.airavata.wsmg.commons.WsmgNameSpaceConstants;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.log4j.Logger;

/**
 * TODO: this is in the specification. not implemented.
 * 
 */

public class PublisherRegistrationManager {
    int counter = 1;
    OMFactory factory = OMAbstractFactory.getOMFactory();
    URI serviceLocation = null;
    org.apache.log4j.Logger logger = Logger.getLogger(PublisherRegistrationManager.class);

    public PublisherRegistrationManager(URI serviceLocation) {
        this.serviceLocation = serviceLocation;
    }

    // FIXME : response message constructed is wrong. and will throw an error if invoked
    // since response message in msgContext is null.!
    public void registerPublisher(ProcessingContext ctx) {

        OMElement eprEl = ctx.getSoapBody().getFirstChildWithName(
                new QName(WsmgNameSpaceConstants.WSBR_NS.getNamespaceURI(), "PublisherReference"));
        new EndpointReference(eprEl.getText());
        // SubscriptionState state = new SubscriptionState(publisherRef);
        // String key = "sub"+(counter++)+"@"+PREFIX;
        // subscriptions.put(key, state);
        EndpointReference publisherRegistrationRef = new EndpointReference(serviceLocation.toASCIIString());
        publisherRegistrationRef.addReferenceParameter(new QName(WsmgNameSpaceConstants.WIDGET_NS.getNamespaceURI(),
                WsmgNameSpaceConstants.RESOURCE_ID), "pub" + (counter++));

        OMElement publisherRegistrationReferenceOMElement;
        try {
            publisherRegistrationReferenceOMElement = EndpointReferenceHelper.toOM(factory, publisherRegistrationRef,
                    new QName(WsmgNameSpaceConstants.WSBR_NS.getNamespaceURI(), "SubscriptionManager"),
                    WsmgNameSpaceConstants.WSA_NS.getNamespaceURI());

            ctx.getRespMessage().addChild(publisherRegistrationReferenceOMElement);
        } catch (AxisFault e) {
            // TODO add with throws clause
            logger.fatal("axis fault found at publisher registrationgmanager", e);
            e.printStackTrace();
        }
    }
}
