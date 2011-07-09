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
import org.apache.airavata.wsmg.config.ConfigurationManager;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.apache.airavata.wsmg.messenger.ConsumerUrlManager;
import org.apache.airavata.wsmg.messenger.SenderUtils;
import org.apache.airavata.wsmg.messenger.strategy.SendingStrategy;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

public class SerialSender extends Thread implements SendingStrategy {

    Logger log = Logger.getLogger(SerialSender.class);

    private boolean stopFlag = false;

    SenderUtils sender;

    public SerialSender(ConfigurationManager config, ConsumerUrlManager urlman) {
        sender = new SenderUtils(urlman, config);
    }

    public void shutdown() {
        stopFlag = true;
        log.info("delivery thread termination notificaiton recieved");
    }

    public void run() {

        log.debug("run - delivery thread");

        while (!stopFlag) {

            try {

                OutGoingMessage outGoingMessage = (OutGoingMessage) WSMGParameter.OUT_GOING_QUEUE.blockingDequeue();

                if (WSMGParameter.showTrackId)
                    log.debug(outGoingMessage.getAdditionalMessageContent().getTrackId()
                            + ": dequeued from outgoing queue");

                sendNotification(outGoingMessage);

            } catch (Exception e) {

                log.fatal("Unexpected_exception:", e);
            }
        }
    }

    public synchronized void sendNotification(OutGoingMessage outGoingMessage) {

        if (outGoingMessage == null) {
            log.error("got a null outgoing message");
            return;
        }
        String messageString = outGoingMessage.getTextMessage();

        List<ConsumerInfo> consumerInfoList = outGoingMessage.getConsumerInfoList();
        AdditionalMessageContent soapHeader = outGoingMessage.getAdditionalMessageContent();
        deliverMessage(consumerInfoList, messageString, soapHeader);
    }

    private void deliverMessage(List<ConsumerInfo> consumerInfoList, String messageString,
            AdditionalMessageContent additionalMessageContent) {

        try {
            OMElement messgae2Send = CommonRoutines.reader2OMElement(new StringReader(messageString));

            for (ConsumerInfo obj : consumerInfoList) {

                sender.send(obj, messgae2Send, additionalMessageContent);

            }

        } catch (XMLStreamException e) {
            log.fatal(e);
        }

    }
}
