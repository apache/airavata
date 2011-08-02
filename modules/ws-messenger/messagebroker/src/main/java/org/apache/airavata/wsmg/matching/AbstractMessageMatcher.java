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

package org.apache.airavata.wsmg.matching;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.airavata.wsmg.broker.AdditionalMessageContent;
import org.apache.airavata.wsmg.broker.ConsumerInfo;
import org.apache.airavata.wsmg.broker.subscription.SubscriptionState;
import org.apache.airavata.wsmg.transports.jms.MessageMatcherConnection;

public abstract class AbstractMessageMatcher {

    protected Map<String, String> currentMessageCache;

    protected Map<Object, Object> publisherRegistrationDB;

    private ReentrantReadWriteLock consumerListLock = new ReentrantReadWriteLock();

    // infer types of
    // key and value

    public AbstractMessageMatcher(Map<Object, Object> publisherRegistrationDB) {

        this.publisherRegistrationDB = publisherRegistrationDB;
        this.currentMessageCache = new HashMap<String, String>();
    }

    public abstract void start(String carrierLocation);

    // Message can be either String or XmlElement. Added XMLElement for
    // performance consideration so that if not using queue,
    // we don't need to serialize to String
    // If we already serialized to String because of the uing queue, we don't
    // have to change back to XMLElement until the delivery to consumers

    public abstract void populateMatches(String wsntMessageConverterClassName,
            AdditionalMessageContent additionalMessageContent, String message, String topic,
            List<ConsumerInfo> matchedConsumers);

    public abstract int handleUnsubscribe(String subscriptionId);

    public abstract MessageMatcherConnection handleSubscribe(SubscriptionState subscribeRequest, String subscriptionId);

    public String handleGetCurrentMessage(String topic) {
        String currentMessage = currentMessageCache.get(topic);
        return currentMessage;
    }

    public void readLockUnlockConsumers(boolean lock) {
        ReadLock readlock = consumerListLock.readLock();
        lockUnlock(readlock, lock);
    }

    public void writeLockUnlockConsumers(boolean lock) {
        WriteLock writeLock = consumerListLock.writeLock();
        lockUnlock(writeLock, lock);
    }

    private void lockUnlock(Lock l, boolean lock) {

        if (lock) {
            l.lock();
        } else {
            l.unlock();
        }

    }

}
