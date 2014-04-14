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

import java.util.HashMap;
import java.util.Map;

import org.apache.airavata.wsmg.broker.subscription.SubscriptionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerListManager {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerListManager.class);

    protected Map<String, ConsumerList> token2ConsumerListMap = new HashMap<String, ConsumerList>();

    protected Map<String, String> subId2Token = new HashMap<String, String>();

    // token can be a topic or an XPath String
    public void addToConsumerList(String token, SubscriptionState subscribeRequest, String subscriptionId) {
        ConsumerList consumerList = token2ConsumerListMap.get(token);
        if (consumerList == null) { // new topic
            consumerList = new ConsumerList();
            token2ConsumerListMap.put(token, consumerList);
        }
        consumerList.addConsumer(subscriptionId, subscribeRequest.getConsumerInfo());
        subId2Token.put(subscriptionId, token);

    }

    public String getTokenBySubscriptionId(String subscriptionId) {
        String token = subId2Token.get(subscriptionId);
        return token;
    }

    public int removeFromConsumerList(String subscriptionId, String token) {
        String tokenString = null;
        if (token == null) {
            tokenString = subId2Token.get(subscriptionId);
        } else {
            tokenString = token;
        }

        ConsumerList consumerList = token2ConsumerListMap.get(tokenString);
        if (consumerList == null) {
            logger.error("*****ERROR:Cannot find the token to delete: " + tokenString);
            return 0;
        }
        int result = consumerList.removeConsumer(subscriptionId);
        subId2Token.remove(subscriptionId);
        return result;
    }

    public ConsumerList getConsumerListByToken(String token) {
        ConsumerList consumerList = token2ConsumerListMap.get(token);
        return consumerList;
    }

}
