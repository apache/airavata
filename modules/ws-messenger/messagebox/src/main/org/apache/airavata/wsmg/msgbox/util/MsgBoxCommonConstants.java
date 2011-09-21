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

package org.apache.airavata.wsmg.msgbox.util;

import javax.xml.namespace.QName;

public class MsgBoxCommonConstants {
    public static final String MSGBOX_STORAGE = "msgbox.storage";
    public static final String AXIS_MODULE_NAME_ADDRESSING = "addressing";
    public static final QName MSG_BOXID_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/", "msgboxid",
            "msg");

    public static final QName STOREMSG_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/",
            "storeMessages", "msg");
    public static final QName DESTROYMSG_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/",
            "destroyMsgBox", "msg");
    public static final QName TAKEMSGS_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/",
            "takeMessages", "msg");
    public static final QName CREATEMSG_BOX = new QName("http://org.apache.airavata/xgws/msgbox/2004/", "createMsgBox",
            "msg");       
    
    public static final QName STOREMSG_RESP_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/",
            "storeMessagesResponse", "msg");    
    public static final QName DESTROY_MSGBOX_RESP_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/",
            "destroyMsgBoxResponse", "msg");
    public static final QName CREATE_MSGBOX_RESP_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/",
            "createMsgBoxResponse", "msg");
    public static final QName TAKE_MSGBOX_RESP_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/",
            "takeMessagesResponse", "msg");
    
    
    public static final QName MSGBOX_STATUS_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/", "status", "msg");
    public static final QName MSGBOX_MESSAGE_QNAME = new QName("http://org.apache.airavata/xgws/msgbox/2004/", "messages", "msg");

}
