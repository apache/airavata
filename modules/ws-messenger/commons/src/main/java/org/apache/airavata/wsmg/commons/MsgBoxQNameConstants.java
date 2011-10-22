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

package org.apache.airavata.wsmg.commons;

import javax.xml.namespace.QName;

public class MsgBoxQNameConstants {

    public static final QName MSG_BOXID_QNAME = new QName("http://org.apache.airavata/ws-messenger/msgbox/2011/",
            "MsgBoxId", "msgbox");
    public static final QName MSGBOX_STATUS_QNAME = new QName("http://org.apache.airavata/ws-messenger/msgbox/2011/",
            "status", "msgbox");
    public static final QName MSGBOX_MESSAGE_QNAME = new QName("http://org.apache.airavata/ws-messenger/msgbox/2011/",
            "messages", "msgbox");

    /*
     * Request
     */
    public static final QName STOREMSG_QNAME = new QName("http://org.apache.airavata/ws-messenger/msgbox/2011/",
            "storeMessages", "msgbox");
    public static final QName DESTROYMSG_QNAME = new QName("http://org.apache.airavata/ws-messenger/msgbox/2011/",
            "destroyMsgBox", "msgbox");
    public static final QName TAKEMSGS_QNAME = new QName("http://org.apache.airavata/ws-messenger/msgbox/2011/",
            "takeMessages", "msgbox");
    public static final QName CREATEMSG_BOX = new QName("http://org.apache.airavata/ws-messenger/msgbox/2011/",
            "createMsgBox", "msgbox");

    /*
     * Response
     */
    public static final QName STOREMSG_RESP_QNAME = new QName("http://org.apache.airavata/ws-messenger/msgbox/2011/",
            "storeMessagesResponse", "msgbox");
    public static final QName DESTROY_MSGBOX_RESP_QNAME = new QName(
            "http://org.apache.airavata/ws-messenger/msgbox/2011/", "destroyMsgBoxResponse", "msgbox");
    public static final QName CREATE_MSGBOX_RESP_QNAME = new QName(
            "http://org.apache.airavata/ws-messenger/msgbox/2011/", "createMsgBoxResponse", "msgbox");
    public static final QName TAKE_MSGBOX_RESP_QNAME = new QName(
            "http://org.apache.airavata/ws-messenger/msgbox/2011/", "takeMessagesResponse", "msgbox");
}
