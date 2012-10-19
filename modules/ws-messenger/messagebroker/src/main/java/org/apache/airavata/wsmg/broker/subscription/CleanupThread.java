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

import java.util.Iterator;
import java.util.Set;

import org.apache.airavata.wsmg.commons.CommonRoutines;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class CleanUpThread implements Runnable {

    private static final Log logger = LogFactory.getLog(CleanUpThread.class);

    private SubscriptionManager subMan;

    public CleanUpThread(SubscriptionManager manager) {
        this.subMan = manager;
    }

    public void run() {
        logger.debug("CleanUpThread started");
        String key = null;
        SubscriptionState subscription = null;
        Set<String> keySet = null;
        // long expirationTime=300000*12*24; //5 min*12*24=1 day
        final long expirationTime = WSMGParameter.expirationTime;
        final long skipCheckInterval = 1000 * 60 * 10; // 10 minutes
        final long checkupInterval = 1000 * 60 * 5; // 5 minutes
        int MAX_TRY = 3;
        logger.info("Starting Subscription Cleaning up Thread.");
        while (true) {
            long currentTime = System.currentTimeMillis();
            long expiredStartTime = 0;
            if (WSMGParameter.requireSubscriptionRenew) {
                expiredStartTime = currentTime - expirationTime; // expired
            }
            long availabilityCheckTime = 0;
            availabilityCheckTime = currentTime - skipCheckInterval; // It's
            // time
            // to
            // check
            // again

            // logger.finest("CleanUpThread loop");
            keySet = subMan.getShallowSubscriptionsCopy().keySet();
            // Go through all the subscriptions and delete expired ones
            for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
                key = iterator.next();
                subscription = subMan.getShallowSubscriptionsCopy().get(key);
                if (subscription.isNeverExpire()) {
                    continue;
                }
                long subscriptionCreationTime = subscription.getCreationTime();
                long lastAvailableTime = subscription.getLastAvailableTime();
                if (WSMGParameter.requireSubscriptionRenew) { // expired
                    if (subscriptionCreationTime < expiredStartTime) { // expired
                        // or
                        // need
                        // to
                        // check
                        // again
                        try {
                            subMan.removeSubscription(key);
                        } catch (AxisFault e) {
                            logger.error(e.getMessage(), e);
                        }
                        // Not need to remove the key from the keyset since
                        // the keyset
                        // "is backed by the map, so changes to the map are reflected in the set, and vice-versa."
                        // i.remove(); //add this will cause
                        // ConcurrentModificationException
                        logger.info("*****Deleted (expiration)" + key + "-->" + subscription.getConsumerIPAddressStr()
                                + "##" + subscription.getLocalTopic());
                        logger.info("*****Deleted (expiration)" + key + "-->" + subscription.getConsumerIPAddressStr()
                                + "##" + subscription.getLocalTopic());
                        continue;
                    }
                }
                if (lastAvailableTime < availabilityCheckTime) {
                    // It's time to check again
                    if (CommonRoutines.isAvailable(subscription.getConsumerAddressURI())) {
                        // It's time to check but still available and do not
                        // require subscriptio renew
                        // set a mark saying it has been check at this time
                        subscription.setLastAvailableTime(currentTime);
                        if (subscription.getUnAvailableCounter() > 0) { // failed
                            // in
                            // previous
                            // try
                            subscription.resetUnAvailableCounter();
                        }
                    } else {
                        int counter = subscription.addUnAvailableCounter();
                        // System.out.println("UnavailableCounter="+counter);
                        // logger.finest("UnavailableCounter="+counter);
                        if (counter > MAX_TRY) {
                            try {
                                subMan.removeSubscription(key);
                            } catch (AxisFault e) {
                                // TODO Auto-generated catch block
                                logger.error(e.getMessage(), e);
                            }

                            // Remove from hashtable seperately to avoid
                            // conccurent access problem to the hashtable
                            // with
                            // i.next()
                            iterator.remove();
                            logger.info("*****Deleted (unavailable)" + key + "-->"
                                    + subscription.getConsumerIPAddressStr() + "##" + subscription.getLocalTopic());
                            logger.info("*****Deleted (unavailable)" + key + "-->"
                                    + subscription.getConsumerIPAddressStr() + "##" + subscription.getLocalTopic());
                        }
                    }
                }
            }
            try {
                Thread.sleep(checkupInterval);
            } catch (InterruptedException e) {
                logger.error("thread was interrupped", e);
            }
        }

    }
}
