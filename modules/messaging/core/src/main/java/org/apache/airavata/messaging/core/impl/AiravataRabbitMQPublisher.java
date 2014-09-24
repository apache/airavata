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

package org.apache.airavata.messaging.core.impl;

import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.TaskStatusChangeEvent;
import org.apache.airavata.model.messaging.event.WorkflowNodeStatusChangeEvent;

public class AiravataRabbitMQPublisher implements Publisher {
    private String brokerUrl;
    private String routingKey;
    private String exchangeName;
    private int prefetchCount;
    private boolean isRequeueOnFail;

    public AiravataRabbitMQPublisher() {

        RabbitMQProducer rabbitMQProducer = new RabbitMQProducer(brokerUrl, routingKey, exchangeName, prefetchCount, isRequeueOnFail);
    }

    public void publish(ExperimentStatusChangeEvent event) {

    }

    public void publish(WorkflowNodeStatusChangeEvent event) {

    }

    public void publish(TaskStatusChangeEvent event) {

    }

    public void publish(JobStatusChangeEvent event) {

    }
}
