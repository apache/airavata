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
package org.apache.airavata.messaging.config;

import org.apache.airavata.messaging.service.EventPublisher;
import org.apache.airavata.messaging.service.MessagingFactory;
import org.apache.airavata.messaging.service.Publisher;
import org.apache.airavata.messaging.service.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    private static final Logger logger = LoggerFactory.getLogger(MessagingConfig.class);

    @Bean
    public EventPublisher eventPublisher() {
        Publisher statusPublisher = null;
        Publisher experimentPublisher = null;
        try {
            statusPublisher = MessagingFactory.getPublisher(Type.STATUS);
        } catch (Exception e) {
            logger.warn("Failed to create status publisher — status events will be dropped: {}", e.getMessage());
        }
        try {
            experimentPublisher = MessagingFactory.getPublisher(Type.EXPERIMENT_LAUNCH);
        } catch (Exception e) {
            logger.warn(
                    "Failed to create experiment publisher — experiment events will be dropped: {}", e.getMessage());
        }
        return new EventPublisher(statusPublisher, experimentPublisher);
    }
}
