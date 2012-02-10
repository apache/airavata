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

package org.apache.airavata.wsmg.performance_evaluator.rtt.util;

public interface ConfigKeys {
    String CONFIG_FILE_NAME = "configurations.properties";
    String BROKER_URL = "broker.eventing.service.epr";
    String CONSUMER_EPR = "consumer.location";
    String CONSUMER_PORT = "consumer.port";
    String TOPIC_SIMPLE = "topic.simple";
    String TOPIC_XPATH = "topic.xpath";
    String PUBLISH_TIME_INTERVAL = "publish.time.interval";
    String IS_XPATH_ENABLED = "is.xpath.enabled";
    String XPATH = "topic.xpath";
    String PAYLOAD_MULTIPLYER = "payload.multiplyer";
    String PROTOCOL = "protocol.used";
    String NUMBER_OF_SUBS_PERTOPIC = "num.subscribers.per.topic";
    String NOTIFICATIONS_PUBLISHED_PER_TOPIC = "notifications.per.topic";
    String NUMBER_OF_TOPICS_PUBLISHED = "number.of.topics";
    String SCHEDULER_REPEAT_PERIOD = "stat.timeout.monitor.scheduler.period";
    String PERFORMANCE_TEST_TIMEOUT = "performance.test.timeout.period.millis";
    String NUMBER_OF_SUBSCRIBERS = "number.of.subscriber.servers";
    String MULTI_THREAD_PER_SUB = "num.muti.thread.per.sub";
}
