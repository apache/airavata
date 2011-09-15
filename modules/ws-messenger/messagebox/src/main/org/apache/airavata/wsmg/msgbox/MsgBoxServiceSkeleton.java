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

package org.apache.airavata.wsmg.msgbox;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.commons.MsgBoxNameSpConsts;
import org.apache.airavata.wsmg.msgbox.Storage.MsgBoxStorage;
import org.apache.airavata.wsmg.msgbox.Storage.memory.InMemoryImpl;
import org.apache.airavata.wsmg.msgbox.util.MsgBoxUtils;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.service.Lifecycle;
import org.apache.log4j.Logger;

/**
 * Service class for MsgBoxService this get calle by MsgBoxServiceMessageReceiverInOut with ProcessingContext
 */
public class MsgBoxServiceSkeleton implements Lifecycle {

    public MsgBoxServiceSkeleton() {
        dbImplenented = true;
    }

    public void init(ServiceContext servicecontext) throws AxisFault {
    }

    public static void setStorage(MsgBoxStorage storageIn) {
        storage = storageIn;
    }

    public void destroy(ServiceContext serviceContext) {
        if (logger.isDebugEnabled())
            logger.debug("Stopping Service....");
        if (!dbImplenented)
            setMap2();
    }

    public OMElement createMsgBox() throws Exception {
        OMElement dd = factory.createOMElement(ProcessingContext.CREATE_MSGBOX_RESP_QNAME);
        String createdMsgBoxId = "";
        OMNamespace omNs = factory.createOMNamespace("http://org.apache.airavata/xgws/msgbox/2004/", "ns1");
        OMElement url = factory.createOMElement("msgboxid", omNs);
        try {
            createdMsgBoxId = storage.createMsgBox();
        } catch (Exception e) {
            logger.fatal((new StringBuilder()).append("Error creating the message box ").append(createdMsgBoxId)
                    .toString(), e);
            AxisFault f = new AxisFault((new StringBuilder()).append("Error creating the message box ")
                    .append(createdMsgBoxId).toString(), e);
            f.setFaultCode("6000");
            throw f;
        }
        url.setText(createdMsgBoxId);
        dd.addChild(url);
        return dd;
    }

    String getRandom(int length) {
        UUID uuid = UUID.randomUUID();
        String myRandom = uuid.toString();
        return myRandom.substring(1, length);
    }

    public OMElement storeMessages(ProcessingContext procCtxt) throws Exception {
        String clientid = "";
        OMElement message = procCtxt.getMessage();
        OMElement status = factory.createOMElement(new QName((new StringBuilder())
                .append(MsgBoxNameSpConsts.MSG_BOX.getNamespaceURI()).append("/").toString(), "status", "msg"));
        if (procCtxt.getMsgBoxAddr() != null)
            clientid = procCtxt.getMsgBoxAddr();
        try {
            storage.putMessageIntoMsgBox(clientid, procCtxt.getMessageId(), procCtxt.soapAction, message);
            status.setText("true");
        } catch (SQLException e) {
            logger.fatal((new StringBuilder()).append("Exception thrown while storing message: ").append(message)
                    .append("in msgbx: ").append(clientid).toString(), e);
            status.setText("false");
        }
        OMElement resp = factory.createOMElement(ProcessingContext.STOREMSG_RESP_QNAME);
        resp.addChild(status);
        resp.declareNamespace(MsgBoxNameSpConsts.MSG_BOX);
        return resp;
    }

    public OMElement takeMessages(ProcessingContext procCtxt) throws Exception {
        String key = "";
        if (procCtxt.getMsgBoxAddr() != null)
            key = procCtxt.getMsgBoxAddr();
        OMElement respEl = factory.createOMElement(new QName((new StringBuilder())
                .append(MsgBoxNameSpConsts.MSG_BOX.getNamespaceURI()).append("/").toString(), "takeMessagesResponse",
                "msg"));
        OMElement messageSet = factory.createOMElement(new QName((new StringBuilder())
                .append(MsgBoxNameSpConsts.MSG_BOX.getNamespaceURI()).append("/").toString(), "messages", "msg"));
        try {
            LinkedList<String> list = (LinkedList<String>) storage.takeMessagesFromMsgBox(key);
            int i = 0;
            if (list != null)
                while (list.size() > 0) {
                    messageSet.addChild(MsgBoxUtils.reader2OMElement(new StringReader(list.removeFirst())));
                    i++;
                }
            else if (logger.isDebugEnabled())
                logger.info("   no messages..");
        } catch (Exception e) {
            logger.fatal((new StringBuilder()).append("error taking mesages.. of message box.. - ").append(key)
                    .toString(), e);
        }
        respEl.addChild(messageSet);
        respEl.declareNamespace(MsgBoxNameSpConsts.MSG_BOX);
        return respEl;
    }

    public OMElement destroyMsgBox(ProcessingContext procCtxt) throws Exception {
        String addr = "";
        OMElement statusEl = factory.createOMElement(new QName(ProcessingContext.DESTROY_MSGBOX_RESP_QNAME
                .getNamespaceURI(), "status"));
        if (procCtxt.getMsgBoxAddr() != null) {
            addr = procCtxt.getMsgBoxAddr();
            storage.destroyMsgBox(addr);
            statusEl.setText("true");
        } else {
            statusEl.setText("false");
        }
        OMElement respEl = factory.createOMElement(ProcessingContext.DESTROY_MSGBOX_RESP_QNAME);
        respEl.addChild(statusEl);
        return respEl;
    }

    public void removeAncientMessages() {
        throw new UnsupportedOperationException("Do not support in memory tmeout of messages");
    }

    public void setMap2() {
        logger.debug("storing the map.. method..\n");
        if (!dbImplenented) {
            InMemoryImpl mem = (InMemoryImpl) storage;
            map = mem.getMap();
        }
    }

    private static OMFactory factory = OMAbstractFactory.getOMFactory();
    private static MsgBoxStorage storage;
    boolean dbImplenented;
    static Logger logger = Logger.getLogger(MsgBoxServiceSkeleton.class);
    ConcurrentHashMap map;

}
