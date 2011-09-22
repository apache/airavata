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

package org.apache.airavata.workflow.tracking.client;

import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.airavata.commons.WorkFlowUtils;
import org.apache.airavata.workflow.tracking.impl.subscription.LeadNotificationHandler;
import org.apache.airavata.workflow.tracking.impl.subscription.MessageBoxNotificationHandler;
import org.apache.airavata.wsmg.client.protocol.WSEProtocolClient;
import org.apache.airavata.wsmg.commons.NameSpaceConstants;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.log4j.Logger;

public class LeadNotificationManager {

    private final static org.apache.log4j.Logger logger = Logger.getLogger(LeadNotificationManager.class);

    /**
     * THis API call could be used if the user created a Subscription in an earlier run and now the service is being
     * restarted and the user wants to use the earlier subscription. All the following information is in the
     * Subscription object returned in the createSubscription API call.
     * 
     * @param messageBoxUrl
     * @param brokerLocation
     * @param eprOfMessageBox
     * @param SubscriptionID
     * @param topic
     * @param xpath
     *            Xpath expression to subscribe to. Can be null.
     * @param callback
     * @param subscribePermanatly
     *            Makes the renew a permanant subscription
     * @return
     * @throws Exception
     */
    public static Subscription startListeningToSavedSubscription(String brokerLocation, EndpointReference msgBoxAddrs,
            String SubscriptionID, String topic, String xpath, Callback callback, boolean subscribePermanatly)
            throws Exception {

        return new MessageBoxNotificationHandler(msgBoxAddrs.getAddress(), brokerLocation)
                .startListeningToPreviousMessageBox(msgBoxAddrs, SubscriptionID, topic, xpath, callback,
                        subscribePermanatly);
    }

    /**
     * This API call could be used to keep-alive a subscription. It would not start a new listener.
     * 
     * @param messageBoxUrl
     * @param brokerLocation
     * @param eprOfMessageBox
     * @param SubscriptionID
     * @param topic
     * @param xpath
     *            Xpath expression to subscribe to. Can be null.
     * @param subscribePermanatly
     *            Makes the renew a permanant subscription
     * @return
     * @throws Exception
     */
    public static Subscription renewMessageboxSubscription(String messageBoxUrl, String brokerLocation,
            String eprOfMessageBox, String SubscriptionID, String topic, String xpath, boolean subscribePermanatly)
            throws Exception {

        return new MessageBoxNotificationHandler(messageBoxUrl, brokerLocation).renewMessageboxSubscription(
                eprOfMessageBox, SubscriptionID, topic, xpath, subscribePermanatly);
    }

    public static Subscription renewMessageboxSubscription(String brokerLocation, EndpointReference eprOfMessageBox,
            String SubscriptionID, String topic, String xpath, boolean subscribePermanatly) throws Exception {

        return new MessageBoxNotificationHandler(eprOfMessageBox.getAddress(), brokerLocation)
                .renewMessageboxSubscription(eprOfMessageBox, SubscriptionID, topic, xpath, subscribePermanatly);
    }

    /**
     * Create a messagebox subscription and does all the broker subscriptions required.
     * 
     * @param messageBoxUrl
     * @param brokerLocation
     * @param topic
     * @param xpath
     *            Xpath expression to subscribe to. Can be null.
     * @param callback
     * @param subscribePermanatly
     *            Cretes the subscriptions permamntly
     * @param userAgent
     *            This will be displayed in the messagebox subscription can be null
     * @return
     * @throws RemoteException
     */
    public static Subscription createMessageBoxSubscription(String messageBoxUrl, String brokerLocation, String topic,
            String xpath, Callback callback, boolean subscribePermanatly) throws Exception {

        return new MessageBoxNotificationHandler(messageBoxUrl, brokerLocation).createMsgBoxSubscription(topic, xpath,
                callback, subscribePermanatly);
    }

    /**
     * Create a messagebox subscription and does all the broker subscriptions required.
     * 
     * @param messageBoxUrl
     * @param brokerLocation
     * @param topic
     * @param xpath
     * @param callback
     * @param userAgent
     *            This will be displayed in the messagebox subscription can be null
     * @return
     * @throws Exception
     */
    public static Subscription createMessageBoxSubscription(String messageBoxUrl, String brokerLocation, String topic,
            String xpath, Callback callback) throws Exception {

        return new MessageBoxNotificationHandler(messageBoxUrl, brokerLocation).createSubscription(topic, xpath,
                callback, false);
    }

    /**
     * @param brokerLocation
     * @param topic
     * @param callback
     * @return
     * @throws Exception
     */
    public static Subscription createSubscription(String brokerLocation, String topic, Callback callback,
            int consumerServerPort) throws Exception {
        LeadNotificationHandler handler = new LeadNotificationHandler(brokerLocation, topic, callback,
                consumerServerPort);

        return handler.createSubscription();
    }

    public static String getBrokerPublishEPR(String brokerURL, String topic) {

        brokerURL = WorkFlowUtils.formatURLString(brokerURL);

        EndpointReference encodedEpr = WSEProtocolClient.createEndpointReference(brokerURL, topic);

        String ret = null;

        try {
            OMElement eprCrEl = EndpointReferenceHelper.toOM(OMAbstractFactory.getOMFactory(), encodedEpr, new QName(
                    "EndpointReference"), NameSpaceConstants.WSA_NS.getNamespaceURI());

            ret = eprCrEl.toStringWithConsume();

        } catch (Exception e) {
            logger.error("unable to convert broker url", e);
        }

        return ret;

        /*
         * String epr = null; brokerURL = WorkFlowUtils.formatURLString(brokerURL); if (brokerURL.endsWith("/")) { epr =
         * brokerURL + STRING_TOPIC + topic; } else { epr = brokerURL + "/" + STRING_TOPIC + topic; }
         * 
         * epr = "<wsa:EndpointReference " + "xmlns:wsa='http://www.w3.org/2005/08/addressing'>" + "<wsa:Address>" + epr
         * + "</wsa:Address>" + "</wsa:EndpointReference>"; return epr;
         */

    }
}
