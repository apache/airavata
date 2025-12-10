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
package org.apache.airavata.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

/**
 * BeanPostProcessor that injects Environment into AiravataServerProperties
 * before its @PostConstruct methods run. This ensures Environment is available
 * when bindProperties() is called.
 */
public class AiravataServerPropertiesPostProcessor implements BeanPostProcessor, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(AiravataServerPropertiesPostProcessor.class);
    private final Environment environment;

    public AiravataServerPropertiesPostProcessor(Environment environment) {
        this.environment = environment;
        logger.info("[BEAN-INIT] AiravataServerPropertiesPostProcessor created with Environment");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AiravataServerProperties) {
            logger.info("[BEAN-INIT] Setting Environment on AiravataServerProperties (bean: {}) BEFORE initialization", beanName);
            AiravataServerProperties properties = (AiravataServerProperties) bean;
            properties.setEnvironment(environment);
            logger.info("[BEAN-INIT] Environment set on AiravataServerProperties successfully");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AiravataServerProperties) {
            logger.info("[BEAN-INIT] AiravataServerProperties (bean: {}) initialized AFTER @PostConstruct", beanName);
        }
        return bean;
    }

    @Override
    public int getOrder() {
        // Run early, before @PostConstruct methods
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

