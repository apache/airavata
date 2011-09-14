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

package org.apache.airavata.wsmg.client;

import org.apache.airavata.wsmg.client.util.ClientUtil;

public class SubscriptionInfo {
    String eventSourceLocIn = null;
    String eventSinkLocIn = null;
    String topicExpression = null;
    String xpathExpression = null;
    String eventSinkEndpointReferenceNS = null;
    String eventSinkEndpointReference = null;
    long expireTime = ClientUtil.EXPIRE_TIME;

    /**
     * @param eventSourceLocIn
     * @param eventSinkLocIn
     * @param topicExpression
     * @param xpathExpression
     * @param eventSinkEndpointReferenceNS
     * @param eventSinkEndpointReference
     * @param expireTime
     */
    public SubscriptionInfo(String eventSourceLocIn, String eventSinkLocIn, String topicExpression,
            String xpathExpression, String eventSinkEndpointReferenceNS, String eventSinkEndpointReference,
            long expireTime) {
        this.eventSourceLocIn = eventSourceLocIn;
        this.eventSinkLocIn = eventSinkLocIn;
        this.topicExpression = topicExpression;
        this.xpathExpression = xpathExpression;
        this.eventSinkEndpointReferenceNS = eventSinkEndpointReferenceNS;
        this.eventSinkEndpointReference = eventSinkEndpointReference;
        this.expireTime = expireTime;
    }

    /**
     * @return Returns the eventSinkEndpointReference.
     */
    public String getEventSinkEndpointReference() {
        return eventSinkEndpointReference;
    }

    /**
     * @param eventSinkEndpointReference
     *            The eventSinkEndpointReference to set.
     */
    public void setEventSinkEndpointReference(String eventSinkEndpointReference) {
        this.eventSinkEndpointReference = eventSinkEndpointReference;
    }

    /**
     * @return Returns the eventSinkEndpointReferenceNS.
     */
    public String getEventSinkEndpointReferenceNS() {
        return eventSinkEndpointReferenceNS;
    }

    /**
     * @param eventSinkEndpointReferenceNS
     *            The eventSinkEndpointReferenceNS to set.
     */
    public void setEventSinkEndpointReferenceNS(String eventSinkEndpointReferenceNS) {
        this.eventSinkEndpointReferenceNS = eventSinkEndpointReferenceNS;
    }

    /**
     * @return Returns the eventSinkLocIn.
     */
    public String getEventSinkLocIn() {
        return eventSinkLocIn;
    }

    /**
     * @param eventSinkLocIn
     *            The eventSinkLocIn to set.
     */
    public void setEventSinkLocIn(String eventSinkLocIn) {
        this.eventSinkLocIn = eventSinkLocIn;
    }

    /**
     * @return Returns the eventSourceLocIn.
     */
    public String getEventSourceLocIn() {
        return eventSourceLocIn;
    }

    /**
     * @param eventSourceLocIn
     *            The eventSourceLocIn to set.
     */
    public void setEventSourceLocIn(String eventSourceLocIn) {
        this.eventSourceLocIn = eventSourceLocIn;
    }

    /**
     * @return Returns the expireTime.
     */
    public long getExpireTime() {
        return expireTime;
    }

    /**
     * @param expireTime
     *            The expireTime to set.
     */
    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    /**
     * @return Returns the topicExpression.
     */
    public String getTopicExpression() {
        return topicExpression;
    }

    /**
     * @param topicExpression
     *            The topicExpression to set.
     */
    public void setTopicExpression(String topicExpression) {
        this.topicExpression = topicExpression;
    }

    /**
     * @return Returns the xpathExpression.
     */
    public String getXpathExpression() {
        return xpathExpression;
    }

    /**
     * @param xpathExpression
     *            The xpathExpression to set.
     */
    public void setXpathExpression(String xpathExpression) {
        this.xpathExpression = xpathExpression;
    }

}
