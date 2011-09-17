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

package org.apache.airavata.wsmg.matching.simpleTopic;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.airavata.wsmg.broker.AdditionalMessageContent;
import org.apache.airavata.wsmg.broker.ConsumerInfo;
import org.apache.airavata.wsmg.broker.ConsumerList;
import org.apache.airavata.wsmg.broker.ConsumerListManager;
import org.apache.airavata.wsmg.broker.subscription.SubscriptionState;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.matching.AbstractMessageMatcher;
import org.apache.airavata.wsmg.messenger.OutGoingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectWsntMessageMatcher extends AbstractMessageMatcher {

    private static final Logger logger = LoggerFactory.getLogger(DirectWsntMessageMatcher.class);
    
    private ConsumerListManager consumerListmanager = new ConsumerListManager();

    private OutGoingQueue outGoingQueue = null;   

    public DirectWsntMessageMatcher(

    Map<Object, Object> publisherRegistrationDB) {
        super(publisherRegistrationDB);
    }

    public DirectWsntMessageMatcher(Map<String, SubscriptionState> subscriptionDB,
            Map<Object, Object> publisherRegistrationDB, String carrier) {
        super(publisherRegistrationDB);
    }

    public void start(String carrierLocation) {

        currentMessageCache = new Hashtable<String, String>();
    }

    public void handleSubscribe(SubscriptionState subscribeRequest, String subscriptionId) {

        String topicExpression = subscribeRequest.getLocalTopic();
        if (topicExpression == null || topicExpression.length() == 0) {
            logger.error("ERROR:WsntAdapterConnection creation failed.");
            return;
        }

        writeLockUnlockConsumers(true);

        try {
            consumerListmanager.addToConsumerList(topicExpression, subscribeRequest, subscriptionId);
            if (outGoingQueue == null) {
                outGoingQueue = subscribeRequest.getOutGoingQueue();
            }
        } finally {
            writeLockUnlockConsumers(false);
        }

        return;

    }

    public int handleUnsubscribe(String subscriptionId) {

        int ret = 0;

        writeLockUnlockConsumers(true);
        try {
            ret = consumerListmanager.removeFromConsumerList(subscriptionId, null);
        } finally {
            writeLockUnlockConsumers(false);
        }

        return ret;
    }

    @Override
    public void populateMatches(String wsntMessageConverterClassName,
            AdditionalMessageContent additionalMessageContent, String message, String topic,
            List<ConsumerInfo> matchedConsumers) {

        assert (matchedConsumers != null);

        readLockUnlockConsumers(true);

        try {

            ConsumerList topicConsumerList = consumerListmanager.getConsumerListByToken(topic);
            ConsumerList wildcardConsumerList = consumerListmanager
                    .getConsumerListByToken(WsmgCommonConstants.WILDCARD_TOPIC);
            if (topicConsumerList != null) {// has subscription to this topic

                ArrayList<ConsumerInfo> list = topicConsumerList.getConsumerList();

                matchedConsumers.addAll(list);
            }
            if (wildcardConsumerList != null) {// has wildcard subscriptions
                List<ConsumerInfo> wildCardConsumerInfoList = wildcardConsumerList.getConsumerList();
                if (wildCardConsumerInfoList != null) {
                    matchedConsumers.addAll(wildCardConsumerInfoList);
                }
            }

        } finally {
            readLockUnlockConsumers(false);
        }

    }

}
