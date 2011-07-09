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

package org.apache.airavata.wsmg.matching.XPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.airavata.wsmg.broker.AdditionalMessageContent;
import org.apache.airavata.wsmg.broker.ConsumerInfo;
import org.apache.airavata.wsmg.broker.ConsumerList;
import org.apache.airavata.wsmg.broker.ConsumerListManager;
import org.apache.airavata.wsmg.broker.subscription.SubscriptionState;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.apache.airavata.wsmg.matching.AbstractMessageMatcher;
import org.apache.airavata.wsmg.messenger.OutGoingQueue;
import org.apache.airavata.wsmg.transports.jms.MessageMatcherConnection;
import org.apache.log4j.Logger;

public class YFilterMessageMatcher extends AbstractMessageMatcher {

    private OutGoingQueue outGoingQueue = null;

    // private HashMap subIdToQuery=new HashMap();
    // private HashMap yFilterIdToXPath=new HashMap();
    private HashMap<String, YFilterInfo> topicToYFilterInfo = new HashMap<String, YFilterInfo>();
    private HashMap<String, String> subIdToTopic = new HashMap<String, String>();
    // private Map xpath2ConsumerListMap = new HashMap();

    // used for topic only subscription, so that we don't have to create a
    // YFilter object
    private ConsumerListManager consumerListmanager = new ConsumerListManager();

    // private String[] queries = new String[1000000];

    private Logger logger = Logger.getLogger(YFilterMessageMatcher.class);

    public YFilterMessageMatcher(Map<Object, Object> publisherRegistrationDB) {
        super(publisherRegistrationDB);
        // TODO Auto-generated constructor stub
    }

    public void start(String carrierLocation) {

        currentMessageCache = new Hashtable<String, String>();
    }

    @Override
    public void populateMatches(String wsntMessageConverterClassName,
            AdditionalMessageContent additionalMessageContent, String message, String topic,
            List<ConsumerInfo> matchedConsumers) {

        assert (matchedConsumers != null);

        if (WSMGParameter.debugYFilter)
            logger.info("Message In YFilterAdapter=" + message);

        // Important Get a Read Lock....
        readLockUnlockConsumers(true);
        try {

            // 1, Topic only
            ConsumerList topicConsumerList = consumerListmanager.getConsumerListByToken(topic);
            if (topicConsumerList != null) {// has subscription to this topic

                ArrayList<ConsumerInfo> list = topicConsumerList.getConsumerList();
                matchedConsumers.addAll(list);
            }
            // 2, wild card topic only
            ConsumerList wildcardConsumerList = consumerListmanager
                    .getConsumerListByToken(WsmgCommonConstants.WILDCARD_TOPIC);
            if (wildcardConsumerList != null) {// has wildcard subscriptions
                List<ConsumerInfo> wildCardConsumerInfoList = wildcardConsumerList.getConsumerList();
                if (wildCardConsumerInfoList != null) {
                    // System.out.println("ConsumerListSize2="+wildCardConsumerInfoList.size());
                    matchedConsumers.addAll(wildCardConsumerInfoList);
                }
            }
            // 3, topic with Xpath
            YFilterInfo yfilterInfo = topicToYFilterInfo.get(topic);
            if (yfilterInfo != null) {
                List<ConsumerInfo> topicAndXPathConsumerInfoList = yfilterInfo.getMatchingConsumerList(message);
                if (topicAndXPathConsumerInfoList != null) {
                    // System.out.println("ConsumerListSize3="+topicAndXPathConsumerInfoList.size());
                    matchedConsumers.addAll(topicAndXPathConsumerInfoList);
                }
            }
            // 4, wild card topic with Xpath (XPath only)
            yfilterInfo = topicToYFilterInfo.get(WsmgCommonConstants.WILDCARD_TOPIC);
            if (yfilterInfo != null) {
                List<ConsumerInfo> wildcardTopicAndXPathConsumerInfoList = yfilterInfo.getMatchingConsumerList(message);
                if (wildcardTopicAndXPathConsumerInfoList != null) {
                    // System.out.println("ConsumerListSize4="+wildcardTopicAndXPathConsumerInfoList.size());
                    matchedConsumers.addAll(wildcardTopicAndXPathConsumerInfoList);
                }
            }

        } finally {

            // Release the Read Lock...
            readLockUnlockConsumers(false);
        }

    }

    public int handleUnsubscribe(String subscriptionId) {

        int ret = 1;

        writeLockUnlockConsumers(true);
        try {
            String topicExpression = subIdToTopic.get(subscriptionId);
            if (subscriptionId.startsWith("T")) { // Topic only
                consumerListmanager.removeFromConsumerList(subscriptionId, topicExpression);
            } else {
                YFilterInfo yfilterInfo = topicToYFilterInfo.get(topicExpression);
                if (yfilterInfo != null) {
                    yfilterInfo.removeSubscription(subscriptionId);
                    if (yfilterInfo.getCounter() == 0) {
                        yfilterInfo = null;
                        topicToYFilterInfo.remove(topicExpression);
                    }
                } else {
                    System.out.println("ERROR: Cannot find subscription with the subId=" + subscriptionId);
                    ret = 0;
                }
            }
        } finally {
            writeLockUnlockConsumers(false);
        }

        return ret;
    }

    public MessageMatcherConnection handleSubscribe(SubscriptionState subscribeRequest, String subscriptionId) {

        // Get the write lock
        writeLockUnlockConsumers(true);
        try {

            String topicExpression = subscribeRequest.getLocalTopic();
            subIdToTopic.put(subscriptionId, topicExpression);

            String xpathExpression = subscribeRequest.getXpathString();
            if (xpathExpression == null || xpathExpression.length() == 0) { // Topic
                // only
                consumerListmanager.addToConsumerList(topicExpression, subscribeRequest, subscriptionId);
            } else {
                YFilterInfo yfilterInfo = topicToYFilterInfo.get(topicExpression);
                if (yfilterInfo == null) {
                    yfilterInfo = new YFilterInfo();
                    topicToYFilterInfo.put(topicExpression, yfilterInfo);
                }
                yfilterInfo.addXPathQuery(xpathExpression, subscriptionId, subscribeRequest);
            }

            if (outGoingQueue == null) {
                outGoingQueue = subscribeRequest.getOutGoingQueue();
            }

        } finally {
            // release the write lock
            writeLockUnlockConsumers(false);
        }

        return null;
    }

}
