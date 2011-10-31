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

import org.apache.airavata.wsmg.client.MsgBrokerClientException;
import org.apache.airavata.wsmg.client.NotificationHandler;
import org.apache.airavata.wsmg.commons.MsgBoxQNameConstants;
import org.apache.airavata.wsmg.msgbox.client.MsgBoxClient;
import org.apache.axis2.addressing.EndpointReference;

public class MsgboxHandler {

    protected MsgBoxClient msgBoxUser = null;

    public EndpointReference createPullMsgBox(String msgBoxLocation, long timeout) throws MsgBrokerClientException {
        msgBoxUser = new MsgBoxClient();
        EndpointReference msgBoxAddr = null;

        try {
            msgBoxAddr = msgBoxUser.createMessageBox(msgBoxLocation, timeout);
        } catch (RemoteException e) {
            throw new MsgBrokerClientException("unable to create msgbox", e);
        }

        return msgBoxAddr;
    }

    public EndpointReference createPullMsgBox(String msgBoxServerLoc) throws MsgBrokerClientException {
        return createPullMsgBox(msgBoxServerLoc, 500L);
    }

    public MessagePuller startPullingEventsFromMsgBox(EndpointReference msgBoxEpr, NotificationHandler handler,
            long interval, long timeout) throws MsgBrokerClientException {
        if (msgBoxUser == null) {
            throw new MsgBrokerClientException("Unable start pulling, the messagebox client was not initialized");
        }

        MessagePuller messagePuller = new MessagePuller(msgBoxUser, msgBoxEpr, handler, interval, timeout);
        messagePuller.startPulling();
        return messagePuller;
    }

    public MessagePuller startPullingFromExistingMsgBox(EndpointReference msgBoxAddr, NotificationHandler handler,
            long interval, long timeout) throws MsgBrokerClientException {

        String toAddress = msgBoxAddr.getAddress();
        int biginIndex = toAddress.indexOf("clientid");
        String clientId = toAddress.substring(biginIndex + "clientid".length() + 1);

        if ((msgBoxAddr.getAllReferenceParameters() == null || msgBoxAddr.getAllReferenceParameters()
                .get(MsgBoxQNameConstants.MSG_BOXID_QNAME).getText() == null)
                && biginIndex == -1)
            throw new MsgBrokerClientException("Invalid Message Box Address");
        this.msgBoxUser = new MsgBoxClient();
        MessagePuller messagePuller = new MessagePuller(msgBoxUser, msgBoxAddr, handler, interval, timeout);
        messagePuller.startPulling();
        return messagePuller;
    }

    public String deleteMsgBox(EndpointReference msgBoxEpr, long timeout) throws MsgBrokerClientException {

        String msgBoxEventSink = msgBoxEpr.getAddress();

        String formattedEventSink = null;

        if (msgBoxEpr.getAddress().contains("clientid")) {
            formattedEventSink = msgBoxEventSink;
        } else {
            if (msgBoxEpr.getAllReferenceParameters() == null)
                throw new MsgBrokerClientException("Invalid Message Box EPR, no reference parameters found");
            String msgBoxId = msgBoxEpr.getAllReferenceParameters().get(MsgBoxQNameConstants.MSG_BOXID_QNAME).getText();
            if (msgBoxId == null)
                throw new MsgBrokerClientException(
                        "Invalid Message Box EPR, reference parameter MsgBoxAddress is missing");
            String format = msgBoxEventSink.endsWith("/") ? "%sclientid/%s" : "%s/clientid/%s";

            formattedEventSink = String.format(format, msgBoxEventSink, msgBoxId);

        }

        if (this.msgBoxUser == null) {
            this.msgBoxUser = new MsgBoxClient();
        }

        String resp = null;
        try {
            resp = msgBoxUser.deleteMsgBox(msgBoxEpr, timeout);
        } catch (RemoteException e) {
            throw new MsgBrokerClientException("unable to delete the msg box", e);
        }

        return resp;
    }

    public void stopPullingEventsFromMsgBox(MessagePuller msgPuller) {
        msgPuller.stopPulling();
    }

}
