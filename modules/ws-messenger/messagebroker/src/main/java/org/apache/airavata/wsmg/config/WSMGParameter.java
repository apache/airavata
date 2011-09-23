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

import org.apache.airavata.wsmg.commons.storage.WsmgQueue;

public class WSMGParameter {

    /**
     * Global variable for the Out Going queue (contains message to send to subscribers)
     */
    public static WsmgQueue OUT_GOING_QUEUE = null; // default=null
    
    public static final boolean testOutGoingQueueMaxiumLength = false; // default=false

    // enable or disable the TimerThread that displays the message rate
    public static final boolean measureMessageRate = false; // default=false    

    public static final boolean enableAutoCleanSubscriptions = false; // default=true

    public static final boolean debugYFilter = false;

    public static final boolean cleanQueueonStartUp = false; // default=true
    public static final boolean requireSubscriptionRenew = true;
    public static final long expirationTime = 1000 * 60 * 60 * 72; // 72 hours

    public static final boolean showTrackId = false;
    public static final String versionSetUpNote = "Added_Sub_Timeout";
}
