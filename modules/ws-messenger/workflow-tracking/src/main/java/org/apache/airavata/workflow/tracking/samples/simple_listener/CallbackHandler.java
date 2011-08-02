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

package org.apache.airavata.workflow.tracking.samples.simple_listener;

import org.apache.airavata.workflow.tracking.client.NotificationType;
import org.apache.xmlbeans.XmlObject;

public class CallbackHandler implements org.apache.airavata.workflow.tracking.client.Callback {

    /*
     * This methods will be callbacked when the particular subcription receives a notification (non-Javadoc)
     * 
     * @see org.apache.airavata.workflow.tracking.client.Callback#deliverMessage(java.lang.String,
     * org.apache.airavata.workflow.tracking.client.NotificationType, org.apache.xmlbeans.XmlObject)
     */
    public void deliverMessage(String topic, NotificationType notificationType, XmlObject messageObj) {
        System.out.println("Received a notification of type[" + notificationType + "] for the topic[" + topic);
        System.out.println("The notification message is:");
        System.out.println(messageObj.toString());
    }

}
