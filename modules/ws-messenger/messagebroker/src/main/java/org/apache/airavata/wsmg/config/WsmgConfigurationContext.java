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

package org.apache.airavata.wsmg.config;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.airavata.wsmg.broker.NotificationProcessor;
import org.apache.airavata.wsmg.broker.subscription.SubscriptionManager;
import org.apache.airavata.wsmg.commons.config.ConfigurationManager;
import org.apache.airavata.wsmg.commons.storage.WsmgQueue;
import org.apache.airavata.wsmg.commons.storage.WsmgStorage;
import org.apache.airavata.wsmg.matching.AbstractMessageMatcher;
import org.apache.airavata.wsmg.matching.XPath.YFilterMessageMatcher;
import org.apache.airavata.wsmg.messenger.OutGoingQueue;

public class WsmgConfigurationContext {

    private OutGoingQueue outgoingQueue = null;

    private List<AbstractMessageMatcher> messageMatchers = new LinkedList<AbstractMessageMatcher>();

    private ReentrantReadWriteLock messegeMatchersLock = new ReentrantReadWriteLock();

    private ConfigurationManager configurationManager;

    private SubscriptionManager subscriptionMan;

    private NotificationProcessor notificationProcessor;

    private WsmgStorage storage;
    
    private WsmgQueue queue;   

    public WsmgConfigurationContext() {
        outgoingQueue = new OutGoingQueue();
        setDirectFilter();
    }

    private void setDirectFilter() {
        messageMatchers.add(new YFilterMessageMatcher());
        // messageMatchers.add(new DirectWsntMessageMatcher(subscriptions,
        // publisherRegistrationDB));
    }

    public List<AbstractMessageMatcher> getMessageMatchers() {
        return messageMatchers;
    }
    
    public OutGoingQueue getOutgoingQueue() {
        return outgoingQueue;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public SubscriptionManager getSubscriptionManager() {
        return subscriptionMan;
    }

    public NotificationProcessor getNotificationProcessor() {
        return notificationProcessor;
    }

    public void setConfigurationManager(ConfigurationManager configMan) {
        this.configurationManager = configMan;
    }

    public void setSubscriptionManager(SubscriptionManager subMan) {
        this.subscriptionMan = subMan;
    }

    public void setNotificationProcessor(NotificationProcessor processor) {
        this.notificationProcessor = processor;
    }

    public WsmgStorage getStorage() {
        return storage;
    }

    public void setStorage(WsmgStorage s) {
        storage = s;
    }
    
    public WsmgQueue getQueue() {
        return queue;
    }

    public void setQueue(WsmgQueue s) {
        queue = s;
    }    

    public ReentrantReadWriteLock getMessegeMatcherLock() {
        return messegeMatchersLock;
    }

}
