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

package org.apache.airavata.wsmg.commons.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.airavata.wsmg.broker.subscription.SubscriptionEntry;
import org.apache.airavata.wsmg.broker.subscription.SubscriptionState;

public class WsmgInMemoryStorage implements WsmgStorage, WsmgQueue {

    private LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();

    private Map<String, SubscriptionState> expirableSubscriptions = new ConcurrentHashMap<String, SubscriptionState>();

    private Map<String, SubscriptionState> unexpirableSubscriptions = new ConcurrentHashMap<String, SubscriptionState>();

    public void dispose() {
        queue.clear();
        expirableSubscriptions.clear();
        unexpirableSubscriptions.clear();
    }
    
    public int insert(SubscriptionState subscription) {
        if (subscription.isNeverExpire()) {
            unexpirableSubscriptions.put(subscription.getId(), subscription);
        } else {
            expirableSubscriptions.put(subscription.getId(), subscription);
        }
        return 0;
    }
    
    public int delete(String subscriptionId) {
        expirableSubscriptions.remove(subscriptionId);
        unexpirableSubscriptions.remove(subscriptionId);
        return 0;
    }

    public List<SubscriptionEntry> getAllSubscription() {

        List<SubscriptionEntry> ret = new ArrayList<SubscriptionEntry>(expirableSubscriptions.size()
                + unexpirableSubscriptions.size());

        Collection<SubscriptionState> entries = expirableSubscriptions.values();

        for (SubscriptionState s : entries) {
            SubscriptionEntry se = new SubscriptionEntry();
            se.setSubscribeXml(s.getSubscribeXml());
            se.setSubscriptionId(s.getId());
            ret.add(se);
        }
        entries = unexpirableSubscriptions.values();
        for (SubscriptionState s : entries) {
            SubscriptionEntry se = new SubscriptionEntry();
            se.setSubscribeXml(s.getSubscribeXml());
            se.setSubscriptionId(s.getId());
            ret.add(se);
        }

        return ret;
    }

    public Object blockingDequeue() {
        Object obj = null;

        try {
            obj = queue.take();
        } catch (InterruptedException ie) {
            throw new RuntimeException("interruped exception occured", ie);
        }

        return obj;
    }

    public void cleanup() {
        queue.clear();
    }

    public void enqueue(Object object, String trackId) {
        queue.offer(object);
    }
}
