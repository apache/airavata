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

package org.apache.airavata.wsmg.processors;

import java.io.Serializable;

import org.apache.airavata.wsmg.broker.AdditionalMessageContent;

public class ReceivedMessage implements Serializable {

    private static final long serialVersionUID = 7908767667077753895L;

    String topicLocalString = null;

    // String soapHeader = null;
    AdditionalMessageContent soapHeader;

    String notificationMessage = null;

    String wsntMessageConverterClassName;

    /**
     * @param topicLocalString
     * @param producerReference
     * @param notificationMessageEl
     * @param wsntMessageConverterClassName
     */
    public ReceivedMessage(String topicLocalString, AdditionalMessageContent soapHeader, String notificationMessageEl,
            String wsntMessageConverterClassName) {
        super();
        // TODO Auto-generated constructor stub
        this.topicLocalString = topicLocalString;
        this.soapHeader = soapHeader;
        this.notificationMessage = notificationMessageEl;
        this.wsntMessageConverterClassName = wsntMessageConverterClassName;
    }

    /**
     * @return Returns the soapHeader.
     */
    public AdditionalMessageContent getSoapHeader() {
        return soapHeader;
    }

    /**
     * @param soapHeader
     *            The soapHeader to set.
     */
    public void setSoapHeader(AdditionalMessageContent soapHeader) {
        this.soapHeader = soapHeader;
    }

    /**
     * @return Returns the notificationMessage.
     */
    public String getNotificationMessage() {
        return notificationMessage;
    }

    /**
     * @param notificationMessage
     *            The notificationMessage to set.
     */
    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    /**
     * @return Returns the topicLocalString.
     */
    public String getTopicLocalString() {
        return topicLocalString;
    }

    /**
     * @param topicLocalString
     *            The topicLocalString to set.
     */
    public void setTopicLocalString(String topicLocalString) {
        this.topicLocalString = topicLocalString;
    }

    /**
     * @return Returns the wsntMessageConverterClassName.
     */
    public String getWsntMessageConverterClassName() {
        return wsntMessageConverterClassName;
    }

    /**
     * @param wsntMessageConverterClassName
     *            The wsntMessageConverterClassName to set.
     */
    public void setWsntMessageConverterClassName(String wsntMessageConverterClassName) {
        this.wsntMessageConverterClassName = wsntMessageConverterClassName;
    }

}
