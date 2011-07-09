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

package org.apache.airavata.workflow.tracking.client;

import org.apache.xmlbeans.XmlObject;

/**
 * Interface to be implemented to receive notifications after starting subscription
 * 
 */
public interface Callback {

    /**
     * Method deliverMessage is called when a Lead Message is received on the subscribed topic.
     * 
     * @param topic
     *            the topic to which this message was sent. This can also be retrieved from the messageObj XMlObject
     *            directly after typecasting.
     * @param messageObj
     *            the XmlObject representing one of the LeadMessages, This needs to be typecast to the correct message
     *            type before being used.
     * 
     */
    public void deliverMessage(String topic, NotificationType notificationType, XmlObject messageObj);

}
