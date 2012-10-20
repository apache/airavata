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

package org.apache.airavata.wsmg.messenger.strategy.impl;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.airavata.wsmg.broker.AdditionalMessageContent;
import org.apache.airavata.wsmg.broker.ConsumerInfo;
import org.apache.airavata.wsmg.commons.CommonRoutines;
import org.apache.airavata.wsmg.commons.OutGoingMessage;
import org.apache.airavata.wsmg.messenger.Deliverable;
import org.apache.airavata.wsmg.messenger.strategy.SendingStrategy;
import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialSender implements SendingStrategy {

    private static final Logger log = LoggerFactory.getLogger(SerialSender.class);

    public void init() {
    }

    public void shutdown() {
    }

    public void addMessageToSend(OutGoingMessage outMessage, Deliverable deliverable) {
        sendNotification(outMessage, deliverable);
    }

    public void sendNotification(OutGoingMessage outGoingMessage, Deliverable deliverable) {

        if (outGoingMessage == null) {
            log.error("Got a null outgoing message");
            return;
        }
        String messageString = outGoingMessage.getTextMessage();

        List<ConsumerInfo> consumerInfoList = outGoingMessage.getConsumerInfoList();
        AdditionalMessageContent soapHeader = outGoingMessage.getAdditionalMessageContent();

        try {
            OMElement messgae2Send = CommonRoutines.reader2OMElement(new StringReader(messageString));

            for (ConsumerInfo obj : consumerInfoList) {
                deliverable.send(obj, messgae2Send, soapHeader);
            }

        } catch (XMLStreamException e) {
            log.error(e.getMessage(), e);
        }
    }
}
