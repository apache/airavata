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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;

/**
 * This class is used to invoke actual methods of MsgBoxService inside MsgBoxServiceMessageReceiverInOut.
 */
public class ProcessingContext {

    public ProcessingContext() {
    }

    public OMElement getMessage() {
        return localMessage;
    }

    public void setMessage(OMElement param) {
        localMessage = param;
    }

    public String getMsgBoxAddr() {
        return localMsgBoxAddr;
    }

    public void setMsgBoxId(String param) {
        localMsgBoxAddr = param;
    }

    public String getMessageId() {
        return messageID;
    }

    public void setMessageId(String param) {
        messageID = param;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String param) {
        soapAction = param;
    }

    protected OMElement localMessage;
    protected String soapAction;
    protected String messageID;
    protected String localMsgBoxAddr;
    public static final QName STOREMSG_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/",
            "storeMessages", "msg");
    public static final QName DESTROYMSG_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/",
            "destroyMsgBox", "msg");
    public static final QName TAKEMSGS_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/",
            "takeMessages", "ns1");
    public static final QName CREATEMSG_BOX = new QName("http://org.apache.airavata/xgws/msgbox/2004/",
            "createMsgBox", "msg");
    public static final QName STOREMSG_RESP_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/",
            "storeMessagesResponse", "msg");
    public static final QName DESTROY_MSGBOX_RESP_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/",
            "destroyMsgBoxResponse", "ns1");
    public static final QName CREATE_MSGBOX_RESP_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/",
            "createMsgBoxResponse", "msg");

}
