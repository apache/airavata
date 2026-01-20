/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.dapr.messaging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Registry of Dapr topic subscriptions: topic -&gt; MessageHandler.
 * Used by DaprSubscriber to register and by DaprSubscriptionController to dispatch.
 */
@Component
public class DaprSubscriptionRegistry {

    private final Map<String, MessageHandler> topicToHandler = new ConcurrentHashMap<>();

    public void register(String topic, MessageHandler handler) {
        topicToHandler.put(topic, handler);
    }

    public MessageHandler get(String topic) {
        return topicToHandler.get(topic);
    }

    public MessageHandler remove(String topic) {
        return topicToHandler.remove(topic);
    }
}
