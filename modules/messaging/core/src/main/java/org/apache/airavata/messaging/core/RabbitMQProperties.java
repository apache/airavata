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
package org.apache.airavata.messaging.core;

public class RabbitMQProperties {
    private String brokerUrl;
    private EXCHANGE_TYPE exchangeType;
    private String exchangeName;
    private int prefetchCount;
    private boolean durable;
    private String queueName;
    private String consumerTag = "default";
    private boolean autoRecoveryEnable;
    private boolean autoAck;

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public RabbitMQProperties setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
        return this;
    }

    public boolean isDurable() {
        return durable;
    }

    public RabbitMQProperties setDurable(boolean durable) {
        this.durable = durable;
        return this;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public RabbitMQProperties setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
        return this;
    }

    public int getPrefetchCount() {
        return prefetchCount;
    }

    public RabbitMQProperties setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
        return this;
    }

    public String getQueueName() {
        return queueName;
    }

    public RabbitMQProperties setQueueName(String queueName) {
        this.queueName = queueName;
        return this;
    }

    public String getConsumerTag() {
        return consumerTag;
    }

    public RabbitMQProperties setConsumerTag(String consumerTag) {
        this.consumerTag = consumerTag;
        return this;
    }

    public boolean isAutoRecoveryEnable() {
        return autoRecoveryEnable;
    }

    public RabbitMQProperties setAutoRecoveryEnable(boolean autoRecoveryEnable) {
        this.autoRecoveryEnable = autoRecoveryEnable;
        return this;
    }

    public String getExchangeType() {
        return exchangeType.type;
    }

    public RabbitMQProperties setExchangeType(EXCHANGE_TYPE exchangeType) {
        this.exchangeType = exchangeType;
        return this;
    }

    public boolean isAutoAck() {
        return autoAck;
    }

    public RabbitMQProperties setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
        return this;
    }

    public enum EXCHANGE_TYPE{
        TOPIC("topic"),
        FANOUT("fanout");

        private String type;

        EXCHANGE_TYPE(String type) {
            this.type = type;
        }
    }
}
