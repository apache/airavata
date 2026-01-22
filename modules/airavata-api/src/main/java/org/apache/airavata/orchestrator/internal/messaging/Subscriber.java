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
package org.apache.airavata.orchestrator.internal.messaging;

import java.util.List;
import java.util.function.BiFunction;
import org.apache.airavata.common.exception.AiravataException;

/**
 * Subscriber interface for message consumption.
 * For Dapr: the supplier is not used; the handler is provided at construction. For legacy
 * compatibility the supplier is accepted but may be ignored.
 */
public interface Subscriber {
    /**
     * Start listening for messages.
     *
     * @param supplier legacy supplier parameter; may be ignored by Dapr impl (e.g. (a,b) -&gt; null)
     * @param queueName queue or Dapr topic name
     * @param routingKeys routing keys or topic filters
     * @return unique id for this subscription
     */
    String listen(BiFunction<Object, Object, Object> supplier, String queueName, List<String> routingKeys)
            throws AiravataException;

    void stopListen(String id) throws AiravataException;

    void sendAck(long deliveryTag);
}
