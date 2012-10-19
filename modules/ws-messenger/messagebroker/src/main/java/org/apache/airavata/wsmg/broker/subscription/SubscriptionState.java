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

package org.apache.airavata.wsmg.broker.subscription;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.broker.ConsumerInfo;
import org.apache.airavata.wsmg.commons.util.OMElementComparator;
import org.apache.airavata.wsmg.messenger.OutGoingQueue;
import org.apache.airavata.wsmg.util.BrokerUtil;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SubscriptionState {

    private static final Log logger = LogFactory.getLog(SubscriptionState.class);

    private long creationTime = 0;
    private long lastAvailableTime = 0;

    private int unAvailableCounter = 0;
    private boolean isNeverExpire = false;
    private boolean isWsrmPolicy;

    public String subId;
    public String curNotif;
    private String localTopicString;
    private String xpathString;
    private String subscribeXml;

    ConsumerInfo consumerInfo = null;

    EndpointReference consumerReference;

    URI consumerURI = null;

    private OutGoingQueue outGoingQueue;

    /**
     * @return Returns the creationTime.
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * @param creationTime
     *            The creationTime to set.
     */
    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setId(String id) {
        subId = id;
    }

    public String getId() {
        return subId;
    }

    public boolean isWsrmPolicy() {
        return isWsrmPolicy;
    }

    public void setWsrmPolicy(boolean wsrmPolicy) {
        this.isWsrmPolicy = wsrmPolicy;
    }

    // TODO: outGoingQueue is not belong to this class. Move it to elsewhere
    // related to notification handler in wsntAdapter.
    public SubscriptionState(EndpointReference consumerRef, boolean useNotify, boolean wsrmEnabled, String topic,
            String xpath, String type, OutGoingQueue outGoingQueue) {
        this.consumerReference = consumerRef;
        try {
            this.consumerURI = new URI(consumerRef.getAddress());
        } catch (URISyntaxException e) {
            // this should not happen
            logger.error("invalid consumer URI returned by axis om", e);

        }
        this.outGoingQueue = outGoingQueue;
        // if (topic == null) {
        // throw new IllegalArgumentException();
        // }
        this.localTopicString = topic;
        this.xpathString = xpath;
        this.isWsrmPolicy = wsrmEnabled;
        consumerInfo = new ConsumerInfo(consumerRef.getAddress(), type, useNotify, false);

    }

    public void resume() {
        consumerInfo.setPaused(false);
    }

    public void pause() {
        consumerInfo.setPaused(true);
    }

    public String getConsumerIPAddressStr() {
        return consumerURI.toString();
    }

    public URI getConsumerAddressURI() {
        return consumerURI;
    }

    public String getLocalTopic() {
        // QName topicExpressionQName =
        // xsul.util.XsulUtil.toQName(localTopicString, localTopicString
        // .requiredTextContent());
        // String topicLocalString = topicExpressionQName.getLocalPart();
        return localTopicString;
    }

    /**
     * @return Returns the consumeReference.
     */
    public EndpointReference getConsumerReference() {
        return consumerReference;
    }

    /**
     * @return Returns the curNotif.
     */
    public String getCurNotif() {
        return curNotif;
    }

    /**
     * @param curNotif
     *            The curNotif to set.
     */
    public void setCurNotif(String curNotif) {
        this.curNotif = curNotif;
    }

    /**
     * @return Returns the outGoingQueue.
     */
    public OutGoingQueue getOutGoingQueue() {
        return outGoingQueue;
    }

    /**
     * @param outGoingQueue
     *            The outGoingQueue to set.
     */
    public void setOutGoingQueue(OutGoingQueue outGoingQueue) {
        this.outGoingQueue = outGoingQueue;
    }

    /**
     * @return Returns the consumerInfo.
     */
    public ConsumerInfo getConsumerInfo() {
        return consumerInfo;
    }

    public void resetUnAvailableCounter() {
        unAvailableCounter = 0;
    }

    public int addUnAvailableCounter() {
        unAvailableCounter++;
        return unAvailableCounter;
    }

    /**
     * @return Returns the unAvailableCounter.
     */
    public int getUnAvailableCounter() {
        return unAvailableCounter;
    }

    public String getXpathString() {
        return xpathString;
    }

    public void setXpathString(String xpathString) {
        this.xpathString = xpathString;
    }

    /**
     * @return Returns the isNeverExpire.
     */
    public boolean isNeverExpire() {
        return isNeverExpire;
    }

    /**
     * @param isNeverExpire
     *            The isNeverExpire to set.
     */
    public void setNeverExpire(boolean neverExpire) {
        this.isNeverExpire = neverExpire;
    }

    /**
     * @return Returns the lastAvailableTime.
     */
    public long getLastAvailableTime() {
        return lastAvailableTime;
    }

    /**
     * @param lastAvailableTime
     *            The lastAvailableTime to set.
     */
    public void setLastAvailableTime(long lastAvailableTime) {
        this.lastAvailableTime = lastAvailableTime;

    }

    public void setSubscribeXml(String xml) {
        subscribeXml = xml;
    }

    public String getSubscribeXml() {
        return subscribeXml;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SubscriptionState) {
            SubscriptionState subscription = (SubscriptionState) o;
            return BrokerUtil.sameStringValue(subscription.getLocalTopic(), this.getLocalTopic())
                    && BrokerUtil.sameStringValue(subscription.getXpathString(), this.getXpathString())
                    && BrokerUtil.sameStringValue(subscription.getConsumerIPAddressStr(),
                            this.getConsumerIPAddressStr()) && equalReferenceParameters(subscription);
        }
        return false;
    }

    private boolean equalReferenceParameters(SubscriptionState anotherSubscription) {

        Map<QName, OMElement> otherRefProperties = anotherSubscription.getConsumerReference()
                .getAllReferenceParameters();
        Map<QName, OMElement> myRefProperties = getConsumerReference().getAllReferenceParameters();

        /*
         * Basic comparison
         */
        if (otherRefProperties == null && myRefProperties == null) {
            return true;
        } else if (otherRefProperties == null || myRefProperties == null) {
            return false;
        } else if (otherRefProperties.size() != myRefProperties.size()) {
            return false;
        }

        /*
         * This OMElementComparator supports ignore list, but we don't use it here.
         */
        Iterator<Entry<QName, OMElement>> iterator = otherRefProperties.entrySet().iterator();
        while (iterator.hasNext()) {

            Entry<QName, OMElement> entry = iterator.next();
            if (!myRefProperties.containsKey(entry.getKey())) {
                return false;
            }

            OMElement myElement = myRefProperties.get(entry.getKey());
            OMElement otherElement = entry.getValue();

            if (!OMElementComparator.compare(myElement, otherElement)) {
                return false;
            }
        }
        return true;
    }

}
