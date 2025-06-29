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
package org.apache.airavata.messaging.core;

@Deprecated
public abstract class MessagingConstants {
    public static final String RABBITMQ_BROKER_URL = "rabbitmq.broker.url";
    public static final String RABBITMQ_STATUS_EXCHANGE_NAME = "rabbitmq.status.exchange.name";
    public static final String RABBITMQ_TASK_EXCHANGE_NAME = "rabbitmq.task.exchange.name";

    public static final String RABBIT_ROUTING_KEY = "routingKey";
    public static final String RABBIT_QUEUE = "queue";
    public static final String RABBIT_CONSUMER_TAG = "consumerTag";
    public static final String DURABLE_QUEUE = "durable.queue";
    public static final String PREFETCH_COUNT = "prefetch.count";
}
