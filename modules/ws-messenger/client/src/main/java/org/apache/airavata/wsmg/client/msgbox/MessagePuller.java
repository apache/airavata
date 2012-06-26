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

package org.apache.airavata.wsmg.client.msgbox;

import java.rmi.RemoteException;
import java.util.Iterator;

import org.apache.airavata.wsmg.client.NotificationHandler;
import org.apache.airavata.wsmg.msgbox.client.MsgBoxClient;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagePuller {

    private final static Logger logger = LoggerFactory.getLogger(MessagePuller.class);

    MsgBoxClient msgBoxUser = null;

    EndpointReference msgBoxId = null;

    NotificationHandler handler = null;

    long backoff = 1000;

    long unavailableInterval = 300000;

    long timeout = 2000L;

    boolean stopPulling = false;

    public MessagePuller() {
    }

    public MessagePuller(MsgBoxClient msgBoxUser, EndpointReference msgBoxAddr, NotificationHandler handler,
            long backoff, long timeout) {
        this.msgBoxUser = msgBoxUser;
        this.msgBoxId = msgBoxAddr;
        this.handler = handler;
        this.backoff = backoff;
        this.timeout = timeout;
    }

    public MessagePuller(MsgBoxClient msgBoxUser, EndpointReference msgBoxId, NotificationHandler handler) {
        this(msgBoxUser, msgBoxId, handler, 1000, 2000);
    }

    public void startPulling() {
        Puller puller = new Puller();
        new Thread(puller).start();
    }

    public void stopPulling() {
        stopPulling = true;
    }


    protected class Puller implements Runnable {

        public void run() {

            long backofftime = backoff;
            while (!stopPulling) {
                Iterator<OMElement> messages = null;
                try {

                    messages = msgBoxUser.takeMessagesFromMsgBox(msgBoxId, timeout);

                    try {
                        if (messages == null || (!messages.hasNext())) {
                            // sleep only when nothing was found
                            Thread.sleep(backoff);
                        }
                    } catch (InterruptedException ex) {
                        logger.error("the message puller thread was interruped", ex);
                    }

                    if (messages != null && messages.hasNext()) {
                        backofftime = backoff;
                        while (messages.hasNext()) {
                            String notification = messages.next().toStringWithConsume();
                            try {
                                handler.handleNotification(notification);
                            } catch (Throwable e) {
                                logger.info("Error occured in the user callback for message" + notification
                                        + e.toString());
                            }

                        }
                    }
                } catch (Exception e) {
                    logger.error("exception on MessagePuller", e);
                    try {
                        backofftime = backofftime * 2;
                        Thread.sleep(Math.min(backofftime, unavailableInterval));
                        backofftime = Math.min(backofftime, unavailableInterval);
                    } catch (InterruptedException e1) {
                        logger.error("message puller was interruped while sleeping", e1);
                    }
                }
            }
            return;
        }
    }

}
