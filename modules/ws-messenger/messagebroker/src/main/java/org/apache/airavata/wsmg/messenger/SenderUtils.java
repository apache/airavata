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

package org.apache.airavata.wsmg.messenger;

import java.io.StringReader;
import java.lang.reflect.Constructor;

import javax.xml.stream.XMLStreamException;

import org.apache.airavata.wsmg.broker.AdditionalMessageContent;
import org.apache.airavata.wsmg.broker.ConsumerInfo;
import org.apache.airavata.wsmg.commons.CommonRoutines;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.NameSpaceConstants;
import org.apache.airavata.wsmg.commons.config.ConfigurationManager;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.apache.airavata.wsmg.messenger.protocol.Axis2Protocol;
import org.apache.airavata.wsmg.messenger.protocol.DeliveryProtocol;
import org.apache.airavata.wsmg.messenger.protocol.SendingException;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.addressing.EndpointReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * this class is not thread safe
 * */

public class SenderUtils {

    private static final Logger logger = LoggerFactory.getLogger(SenderUtils.class);

    OMFactory factory = OMAbstractFactory.getOMFactory();

    ConsumerUrlManager urlManager;

    DeliveryProtocol protocol;

    public SenderUtils(ConsumerUrlManager urlMan, ConfigurationManager config) {
        urlManager = urlMan;

        /*
         * Invoke factory and config
         */
        String protocolClass = config.getConfig(WsmgCommonConstants.DELIVERY_PROTOCOL,
                "org.apache.airavata.wsmg.messenger.protocol.Axis2Protocol");
        try {
            Class cl = Class.forName(protocolClass);
            Constructor<DeliveryProtocol> co = cl.getConstructor(null);
            protocol = co.newInstance((Object[]) null);

        } catch (Exception e) {
            // fallback to normal class
            logger.error("Cannot initial protocol sender", e);
            protocol = new Axis2Protocol();
        }

        protocol.setTimeout(Long.parseLong(config.getConfig(WsmgCommonConstants.CONFIG_SOCKET_TIME_OUT, "20000")));

    }

    public void send(ConsumerInfo consumerInfo, OMElement notificationMessageBodyEl,
            AdditionalMessageContent additionalMessageContent) {

        if (consumerInfo.isPaused()) {
            return;
        }

        if (notificationMessageBodyEl == null) {
            logger.info("notification message is null, IGNORED");
            return;
        }

        if (urlManager.isUnavailable(consumerInfo.getConsumerEprStr())) {
            logger.info("consumer url is unavailable: " + consumerInfo.getConsumerEprStr());
            return;
        }

        EndpointReference consumerReference = new EndpointReference(consumerInfo.getConsumerEprStr());

        /*
         * Extract message
         */
        OMElement message = null;
        if (consumerInfo.getType().compareTo("wsnt") == 0) {
            if (consumerInfo.isUseNotify()) {
                message = wrapRawMessageToWsntWrappedFormat(notificationMessageBodyEl, additionalMessageContent);
            } else {
                message = notificationMessageBodyEl;
            }
        } else { // wse
            message = notificationMessageBodyEl;
        }

        long timeElapsed = -1;
        long startTime = -1;

        startTime = System.currentTimeMillis();

        try {

            /*
             * sending message out
             */
            protocol.deliver(consumerInfo, message, additionalMessageContent);

            long finishTime = System.currentTimeMillis();
            timeElapsed = finishTime - startTime;
            if (WSMGParameter.showTrackId)
                logger.info(String.format("track id = %s : delivered to: %s in %d ms",
                        additionalMessageContent.getTrackId(), consumerReference.getAddress(), timeElapsed));

            urlManager.onSucessfullDelivery(consumerReference, timeElapsed);
        } catch (SendingException ex) {

            long finishTime = System.currentTimeMillis();
            timeElapsed = finishTime - startTime;

            urlManager.onFailedDelivery(consumerReference, finishTime, timeElapsed, ex, additionalMessageContent);

        }
    }

    public OMElement wrapRawMessageToWsntWrappedFormat(OMElement rawNotif,
            AdditionalMessageContent additionalMessageContent) {

        OMElement fullNotif = factory.createOMElement("Notify", NameSpaceConstants.WSNT_NS);

        OMElement notificationMessageEl = factory.createOMElement("NotificationMessage",
                NameSpaceConstants.WSNT_NS, fullNotif);

        String topicElString = additionalMessageContent.getTopicElement();
        if (topicElString != null) {
            OMElement topicEl = null;
            try {
                topicEl = CommonRoutines.reader2OMElement(new StringReader(topicElString));
            } catch (XMLStreamException e) {
                logger.error("XMLStreamreader exception when setting topicEl", e);
            }
            notificationMessageEl.addChild(topicEl);
        }
        String producerReferenceElString = additionalMessageContent.getProducerReference();
        if (producerReferenceElString != null) {
            OMElement producerReferenceEl = null;
            try {
                producerReferenceEl = CommonRoutines.reader2OMElement(new StringReader(producerReferenceElString));
            } catch (XMLStreamException e) {
                logger.error("XMLStreamException at creating producerReferenceEl", e);
            }
            notificationMessageEl.addChild(producerReferenceEl);
        }

        OMElement messageEl = factory.createOMElement("Message", NameSpaceConstants.WSNT_NS, notificationMessageEl);
        messageEl.addChild(rawNotif);

        return fullNotif;

    }

}
