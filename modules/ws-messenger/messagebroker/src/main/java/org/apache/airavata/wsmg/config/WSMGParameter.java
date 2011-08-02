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

import org.apache.airavata.wsmg.commons.storage.WsmgStorage;

public class WSMGParameter {

    // public static String TOPIC_BROKER="vm://localhost";
    public static final String OUTGOING_QUEUE_BROKER = "vm://localhost";

    public static final String INCOMING_QUEUE_BROKER = "vm://localhost";
    // not used if you don't use activeMQ as message filter
    public static final String TOPIC_BROKER = "failover:(tcp://hunk.extreme.indiana.edu:61616)?maximumRetries=10";

    // public static String
    // OUTGOING_QUEUE_BROKER="failover:(tcp://bleu.extreme.indiana.edu:61616)?maximumRetries=10";
    // public static String
    // INCOMING_QUEUE_BROKER="failover:(tcp://exodus.extreme.indiana.edu:61616)?maximumRetries=10";

    // public static String
    // OUTGOING_QUEUE_BROKER="tcp://hunk.extreme.indiana.edu:61616";
    // public static String
    // INCOMING_QUEUE_BROKER="tcp://hunk.extreme.indiana.edu:61616";
    // public static String OUTGOING_QUEUE_BROKER="tcp://localhost:61616";
    // public static String INCOMING_QUEUE_BROKER="tcp://localhost:61616";
    //
    public static final boolean testOutGoingQueueMaxiumLength = false; // default=false
    // Test deque rate without really delivering message over the network in
    // deliveryThread
    public static final boolean performRealDelivery = true; // default=true
    // when performRealDelivery=false to test dequeue, also need to set
    // testOutGoingQueueMaxiumLength = false;
    // otherwise, it will blocked for enqueue to prevent messages being
    // delivered out.

    // set to true if use embeded activeMQ
    public static final boolean useEmbeddedActiveMQ = false; // default=false

    public static final boolean useIncomingQueue = false; // default=false

    public static final boolean useOutGoingQueue = true; // default=true

    // enable or disable the TimerThread that displays the message rate
    public static final boolean measureMessageRate = false; // default=false

    // used for gt4 interop test
    public static final boolean gt4 = false; // default=false

    // stress test publisher use WSE, false=>use WSNT
    public static final boolean stressTestPublisherUseWSE = true; // default=true

    // stress test subscriber use WSE,false=>use WSNT
    public static final boolean stressTestSubscriberUseWSE = true; // default=true

    public static final boolean stressTestPublisherCatchUpPublishingRate = false;

    public static WsmgStorage OUT_GOING_QUEUE = null; // default=null

    // set to false will make the broker unable to delivery messages (only put
    // them in a queue) and depend on another thread
    public static final boolean enableDeliveryThreadInStandAloneBroker = true; // default=true
    public static final boolean enableDeliveryThreadInTomcatBroker = false; // default=false

    public static final boolean enableAutoCleanSubscriptions = false; // default=true

    public static final boolean useYFilter = false;
    public static final boolean debugYFilter = false;

    public static final boolean cleanQueueonStartUp = false; // default=true
    public static final boolean requireSubscriptionRenew = true;
    public static final long expirationTime = 1000 * 60 * 60 * 72; // 72 hours
    public static final String DB_CONFIG_NAME = "db.config";

    public static final String MESSAGEBOX_DB_CONFIG_NAME = "db.config";

    public static final boolean showTrackId = false;
    public static final String versionSetUpNote = "Added_Sub_Timeout";
    public static final boolean enableMessengerStatucCheckPort = true; // default=false
    public static final boolean allowSaveToLocalMsgBoxDB = false; // default=false
    // Following configures are for testing/merging purpose. users of WSMG can
    // ignore.
    public static final boolean useYidistributedDB = false; // default true
    public static final boolean measureInternalProcessingTime = false;
}
