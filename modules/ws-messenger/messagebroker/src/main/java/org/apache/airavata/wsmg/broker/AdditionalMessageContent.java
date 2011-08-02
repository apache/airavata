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

package org.apache.airavata.wsmg.broker;

import java.io.Serializable;

public class AdditionalMessageContent implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = -5163025283681463108L;

    String action;

    String messageID;

    String topicElement;

    String producerReference;
    String trackId;

    /**
     * @param action
     * @param messageID
     */
    public AdditionalMessageContent(String action, String messageID) {
        super();
        // TODO Auto-generated constructor stub
        this.action = action;
        this.messageID = messageID;
    }

    /**
     * @return Returns the action.
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action
     *            The action to set.
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return Returns the messageID.
     */
    public String getMessageID() {
        return messageID;
    }

    /**
     * @param messageID
     *            The messageID to set.
     */
    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    /**
     * @return Returns the producerReference.
     */
    public String getProducerReference() {
        return producerReference;
    }

    /**
     * @param producerReference
     *            The producerReference to set.
     */
    public void setProducerReference(String producerReference) {
        this.producerReference = producerReference;
    }

    /**
     * @return Returns the topicElement.
     */
    public String getTopicElement() {
        return topicElement;
    }

    /**
     * @param topicElement
     *            The topicElement to set.
     */
    public void setTopicElement(String topicElement) {
        this.topicElement = topicElement;
    }

    /**
     * @return Returns the trackId.
     */
    public String getTrackId() {
        return trackId;
    }

    /**
     * @param trackId
     *            The trackId to set.
     */
    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String toString() {
        return String.format("msgId = %s, trackId = %s, topic = %s", messageID, trackId, topicElement);
    }

}
