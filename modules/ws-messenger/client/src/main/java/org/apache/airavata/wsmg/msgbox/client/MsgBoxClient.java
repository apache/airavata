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

package org.apache.airavata.wsmg.msgbox.client;

import java.rmi.RemoteException;
import java.util.Iterator;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the client class to invoke MsgBoxService this is using four separate classes to invoke four methods
 * createMsgBox,destroyMsgBox,takeMessages and storeMessages operations of the service.
 */
public class MsgBoxClient {

    int msgsAtOnce = 10;
    private static final Log logger = LogFactory.getLog(MsgBoxClient.class);

    public MsgBoxClient() {
    }

    public EndpointReference createMessageBox(String msgBoxLocation, long timeout) throws RemoteException {
        CreateMsgBox msgBox = new CreateMsgBox(msgBoxLocation, timeout);
        return msgBox.execute();
    }

    public String storeMessage(EndpointReference msgBoxEpr, long timeout, OMElement messageIn) throws RemoteException {
        StoreMessage strMsg = new StoreMessage(msgBoxEpr, timeout);
        return strMsg.execute(messageIn);
    }

    public Iterator<OMElement> takeMessagesFromMsgBox(EndpointReference msgBoxEpr, long timeout) throws RemoteException {
        TakeMessages takeMsgs = new TakeMessages(msgBoxEpr, timeout);
        return takeMsgs.execute();
    }

    public String deleteMsgBox(EndpointReference msgBoxEpr, long timeout) throws RemoteException {
        DestroyMsgBox destroyMsgBox = new DestroyMsgBox(msgBoxEpr, timeout);
        return destroyMsgBox.execute();
    }
}
