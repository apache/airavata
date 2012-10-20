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

import java.io.StringReader;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.airavata.wsmg.broker.context.ContextParameters;
import org.apache.airavata.wsmg.broker.context.ProcessingContext;
import org.apache.airavata.wsmg.broker.context.ProcessingContextBuilder;
import org.apache.airavata.wsmg.broker.wseventing.WSEProcessingContextBuilder;
import org.apache.airavata.wsmg.broker.wseventing.WSEProtocolSupport;
import org.apache.airavata.wsmg.broker.wsnotification.WSNTProtocolSupport;
import org.apache.airavata.wsmg.broker.wsnotification.WSNotificationProcessingContextBuilder;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.NameSpaceConstants;
import org.apache.airavata.wsmg.commons.storage.WsmgStorage;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.apache.airavata.wsmg.config.WsmgConfigurationContext;
import org.apache.airavata.wsmg.matching.AbstractMessageMatcher;
import org.apache.airavata.wsmg.messenger.OutGoingQueue;
import org.apache.airavata.wsmg.util.RunTimeStatistics;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages subscribers.
 * 
 */
public class SubscriptionManager {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionManager.class);

    private HashMap<String, SubscriptionState> subscriptions = new HashMap<String, SubscriptionState>();

    private ReentrantReadWriteLock subscriptionLock = new ReentrantReadWriteLock();

    private WSEProtocolSupport wseProtocalSupport = new WSEProtocolSupport();

    private WSNTProtocolSupport wsntProtocolSupport = new WSNTProtocolSupport();

    private WsmgStorage subscriptionDB;

    private WsmgConfigurationContext wsmgConfig;

    private OutGoingQueue outGoingQueue;

    private int counter = 1;

    public SubscriptionManager(WsmgConfigurationContext paramters, WsmgStorage storage) {
        init(paramters, storage);
    }

    private void init(WsmgConfigurationContext parameters, WsmgStorage storage) {

        this.wsmgConfig = parameters;

        subscriptionDB = storage;
        outGoingQueue = parameters.getOutgoingQueue();
        if (WSMGParameter.enableAutoCleanSubscriptions) {
            CleanUpThread cleanUpThread = new CleanUpThread(this);
            Thread t = new Thread(cleanUpThread);
            t.start();
        }

        try {
            checkSubscriptionDB(storage);
        } catch (AxisFault e) {
            log.error("Subscription database has malformed" + " subscriptions. Ignoring them.", e);

        }

    }

    /**
     * @return Returns the subscriptions.
     */
    public AbstractMap<String, SubscriptionState> getShallowSubscriptionsCopy() {

        AbstractMap<String, SubscriptionState> ret = null;
        readLockUnlockSubscriptions(true);
        try {
            ret = new HashMap<String, SubscriptionState>(subscriptions);
        } finally {
            readLockUnlockSubscriptions(false);
        }

        return ret;

    }

    public void subscribe(ProcessingContext ctx) throws AxisFault {

        String subId = createSubscription(null, ctx);
        if (subId == null) {
            log.error("ERROR: No subscription created");
            return;
        }

        if (NameSpaceConstants.WSE_NS.equals(ctx.getContextParameter(ContextParameters.SUBSCRIBE_ELEMENT)
                .getNamespace())) {
            wseProtocalSupport.createSubscribeResponse(ctx, subId);

        } else { // WSNT

            wsntProtocolSupport.createSubscribeResponse(ctx, subId);
        }
    }

    /**
     * @param subscriptionId
     *            this is the ID that is in the SOAP header
     * @param ctx
     *            contexts constructed with the body elements
     * @return subscription id
     * @throws AxisFault
     */
    private String createSubscription(String subscriptionId, ProcessingContext ctx) throws AxisFault {

        SubscriptionState state = null;
        String key = null;

        // get the first element element inside the soap body element and check
        // whether namespace is WSE
        if (NameSpaceConstants.WSE_NS.equals(ctx.getContextParameter(ContextParameters.SUBSCRIBE_ELEMENT)
                .getNamespace())) {
            state = wseProtocalSupport.createSubscriptionState(ctx, outGoingQueue);
        } else { // Handle WSNT

            state = wsntProtocolSupport.createSubscriptionState(ctx, outGoingQueue);
        }

        if (subscriptionId == null) { // New subscription entry
            key = checkSubscriptionExist(state);
            if (key != null) { // just renew previous subscriptions
                return key;
            }
            // new subscriptions

            state.setCreationTime(System.currentTimeMillis());

            key = generateSubscriptionId(state.getXpathString() != null && state.getXpathString().length() > 0);

        } else { // Startup from previous subscription database
            key = subscriptionId;
        }

        for (AbstractMessageMatcher m : wsmgConfig.getMessageMatchers()) {
            m.handleSubscribe(state, key);
        }

        if (subscriptionId == null) { // New subscription entry,

            RunTimeStatistics.totalSubscriptions++;
            try {
                String subscribeXml = ctx.getContextParameter(ContextParameters.SUBSCRIBE_ELEMENT)
                        .toStringWithConsume();

                state.setId(key);
                state.setSubscribeXml(subscribeXml);
                subscriptionDB.insert(state);

            } catch (Exception ex) {
                log.error("unable to insert subscription to database", ex);
                throw new AxisFault("unable to insert subscription to database ", ex);
            }
        }

        addToSubscriptionMap(key, state);
        return key;
    }

    private void addToSubscriptionMap(String key, SubscriptionState state) {

        writeLockUnlockSubscription(true);
        try {
            subscriptions.put(key, state);
        } finally {
            writeLockUnlockSubscription(false);
        }

    }

    /**
     * @param xpathString
     * @return
     */
    private String generateSubscriptionId(boolean xPath) {
        String key;
        String subIdPrefix = null; // Used to indicate weather a subscription
        // has an XPath subscription Or Topic
        // only.
        if (!xPath) {
            subIdPrefix = "T";
        } else {
            subIdPrefix = "X";
        }
        key = subIdPrefix + "sub" + (counter++) + "@" + WsmgCommonConstants.PREFIX;
        return key;
    }

    /**
     * if find the subscription already exists, return the current subscriptionId else return null;
     */

    public String checkSubscriptionExist(SubscriptionState state) {

        String key = null;

        readLockUnlockSubscriptions(true);
        try {

            for (Iterator<String> keyIterator = subscriptions.keySet().iterator(); keyIterator.hasNext();) {

                String currentKey = keyIterator.next();
                SubscriptionState value = subscriptions.get(currentKey);

                if (value.equals(state)) {
                    value.setCreationTime(System.currentTimeMillis());
                    log.info("Subscription Already exists." + " Using the current subscriptionId");
                    key = currentKey;
                    break;
                }

            }

        } finally {
            readLockUnlockSubscriptions(false);
        }

        return key;
    }

    public void checkSubscriptionDB(WsmgStorage storage) throws AxisFault {
        OMElement subscribeXmlElement;
        String subscriptionId;
        // Read subscription Info from Subscription DB
        List<SubscriptionEntry> subscriptionEntry = storage.getAllSubscription();
        if (subscriptionEntry == null) {
            return;
        }

        WSNotificationProcessingContextBuilder wsntBuilder = new WSNotificationProcessingContextBuilder();
        WSEProcessingContextBuilder wseBuilder = new WSEProcessingContextBuilder();

        // Create subscription for these entries from DB
        for (int i = 0; i < subscriptionEntry.size(); i++) {

            ProcessingContextBuilder processingCtxBuilder = null;

            log.info("Subscription No. " + i + " is " + subscriptionEntry.get(i).getSubscriptionId());

            StringReader sr = new StringReader(subscriptionEntry.get(i).getSubscribeXml());
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader inflow;
            try {
                inflow = inputFactory.createXMLStreamReader(sr);

                StAXOMBuilder builder = new StAXOMBuilder(inflow); // get the
                // root
                // element (in
                // this case the
                // envelope)
                subscribeXmlElement = builder.getDocumentElement();

                if (subscribeXmlElement.getNamespace().getNamespaceURI()
                        .equals(NameSpaceConstants.WSNT_NS.getNamespaceURI())) {
                    processingCtxBuilder = wsntBuilder;

                } else {
                    processingCtxBuilder = wseBuilder;
                }

                subscriptionId = subscriptionEntry.get(i).getSubscriptionId();

                ProcessingContext context = processingCtxBuilder.build(subscribeXmlElement);
                createSubscription(subscriptionId, context);

            } catch (XMLStreamException e) {
                log.error("error occured while checking subscription db", e);
            }
        }
        RunTimeStatistics.totalSubscriptionsAtStartUp += subscriptionEntry.size();
    }

    // This is used for debug
    public void showAllSubscription() {
        String key = null;
        SubscriptionState value = null;
        Set<String> keySet = subscriptions.keySet();
        log.info("List of all subscriptions:");
        for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
            key = iterator.next();
            value = subscriptions.get(key);
            log.info("******" + key + "-->" + value.getConsumerIPAddressStr() + "##" + value.getLocalTopic());
        }
    }

    public int unsubscribe(ProcessingContext ctx) throws AxisFault {

        String subscriptionId = ctx.getContextParameter(ContextParameters.SUB_ID);
        if (subscriptionId == null || subscriptionId.trim().length() == 0) {
            throw new AxisFault("subscription identifier is not provided");
        }

        removeSubscription(subscriptionId);
        RunTimeStatistics.totalUnSubscriptions++;
        return 0;
    }

    int removeSubscription(String subId) throws AxisFault {

        SubscriptionState subscription = null;

        writeLockUnlockSubscription(true);
        try {
            subscription = subscriptions.remove(subId);
        } finally {
            writeLockUnlockSubscription(false);
        }

        if (subscription == null) {
            throw AxisFault.makeFault(new RuntimeException("unknown subscription: " + subId));

        }

        subscriptionDB.delete(subId);

        for (AbstractMessageMatcher mm : wsmgConfig.getMessageMatchers()) {
            mm.handleUnsubscribe(subId);
        }

        return 0;
    }

    public void resumeSubscription(ProcessingContext ctx) throws AxisFault {

        String subscriptionId = ctx.getContextParameter(ContextParameters.SUB_ID);

        if (subscriptionId == null) {
            throw AxisFault.makeFault(new RuntimeException("missing subscription id"));
        }

        writeLockUnlockSubscription(true);// lock
        try {
            SubscriptionState subscription = subscriptions.get(subscriptionId);

            if (subscription == null) {

                throw AxisFault.makeFault(new RuntimeException("no subscription found for id: " + subscriptionId));
            }

            subscription.resume();
        } finally {
            // this will execute even exception is thrown.
            writeLockUnlockSubscription(false);
        }
    }

    public void pauseSubscription(ProcessingContext ctx) throws AxisFault {

        String subscriptionId = ctx.getContextParameter(ContextParameters.SUB_ID);

        if (subscriptionId == null) {
            throw AxisFault.makeFault(new RuntimeException("missing subscription id"));
        }

        writeLockUnlockSubscription(true);// read lock should be sufficient
        // (since we are not modifying the
        // map)
        try {
            SubscriptionState subscription = subscriptions.get(subscriptionId);

            if (subscription == null) {

                throw AxisFault.makeFault(new RuntimeException("no subscription found for id: " + subscriptionId));

            }

            subscription.pause();
        } finally {
            // this will execute even exception is thrown.
            writeLockUnlockSubscription(false);
        }
    }

    public void readLockUnlockSubscriptions(boolean lock) {
        ReadLock readlock = subscriptionLock.readLock();
        lockUnlock(readlock, lock);
    }

    public void writeLockUnlockSubscription(boolean lock) {
        WriteLock writeLock = subscriptionLock.writeLock();
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
