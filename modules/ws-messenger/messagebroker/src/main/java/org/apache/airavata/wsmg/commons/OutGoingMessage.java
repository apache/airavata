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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.wsmg.broker.AdditionalMessageContent;
import org.apache.airavata.wsmg.broker.ConsumerInfo;

public class OutGoingMessage implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = -6999667921413261492L;

    String textMessage;

    AdditionalMessageContent additionalMessageContent;

    List<ConsumerInfo> consumerInfoList = null;

    // ConsumerInfo consumerInfo=null;

    /**
	 * 
	 */
    public OutGoingMessage() {
        // super();
        // TODO Auto-generated constructor stub
        // consumerInfo=new ConsumerInfo();
    }

    /**
     * @param textMessage
     * @param additionalMessageContent
     * @param consumerInfoList
     */
    public OutGoingMessage(String textMessage, AdditionalMessageContent additionalMessageContent,
            List<ConsumerInfo> consumerInfoList) {
        super();
        // TODO Auto-generated constructor stub
        this.textMessage = textMessage;
        this.additionalMessageContent = additionalMessageContent;
        this.consumerInfoList = consumerInfoList;
    }

    /**
     * @param consumerInfo
     *            The consumerInfo to set.
     */
    public void addConsumerInfo(ConsumerInfo consumerInfo) {
        // this.consumerInfo = consumerInfo;
        if (consumerInfoList == null) {
            consumerInfoList = new ArrayList<ConsumerInfo>();
        }
        consumerInfoList.add(consumerInfo);
    }

    /**
     * @return Returns the textMessage.
     */
    public String getTextMessage() {
        return textMessage;
    }

    /**
     * @param textMessage
     *            The textMessage to set.
     */
    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    /**
     * @return Returns the consumerInfoList.
     */
    public List<ConsumerInfo> getConsumerInfoList() {
        return consumerInfoList;
    }

    /**
     * @param consumerInfoList
     *            The consumerInfoList to set.
     */
    public void setConsumerInfoList(List<ConsumerInfo> consumerInfoList) {
        this.consumerInfoList = consumerInfoList;
    }

    /**
     * @return Returns the soapHeader.
     */
    public AdditionalMessageContent getAdditionalMessageContent() {
        return additionalMessageContent;
    }

    /**
     * @param soapHeader
     *            The soapHeader to set.
     */
    public void setAdditionalMessageContent(AdditionalMessageContent soapHeader) {
        this.additionalMessageContent = soapHeader;
    }

}
