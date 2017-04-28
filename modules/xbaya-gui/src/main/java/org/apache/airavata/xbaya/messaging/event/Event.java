/**
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
 */
package org.apache.airavata.xbaya.messaging.event;

public class Event {

    /**
     */
    public enum Type {
        /**
         * GPEL_CONFIGURATION_CHANGED
         */
        GPEL_CONFIGURATION_CHANGED,
        /**
         * GPEL_CONNECTED
         */
        GPEL_ENGINE_CONNECTED,
        /**
         * GPEL_ENGINE_DISCONNECTED
         */
        GPEL_ENGINE_DISCONNECTED,

        /**
         * MONITOR_CONFIGURATION_CHANGED
         */
        MONITOR_CONFIGURATION_CHANGED,
        /**
         * MONITOR_STARTED
         */
        MONITOR_STARTED,
        /**
         * MONITOR_STOPED
         */
        MONITOR_STOPED,

        /**
         * KARMA_STARTED
         */
        KARMA_STARTED,

        /**
         * MYLEAD_CONFIGURATION_CHANGED
         */
        MYLEAD_CONFIGURATION_CHANGED,
    }

    private Type type;

    /**
     * Constructs a Event.
     * 
     * @param type
     */
    public Event(Type type) {
        this.type = type;
    }

    /**
     * @return The type of the event
     */
    public Type getType() {
        return this.type;
    }

}