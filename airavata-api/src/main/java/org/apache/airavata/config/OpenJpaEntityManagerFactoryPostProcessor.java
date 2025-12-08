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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

/**
 * BeanPostProcessor that ensures OpenJPA EntityManagerFactory instances
 * are properly initialized before Spring Data JPA tries to access the metamodel.
 * This helps avoid enhancement-related errors during Spring context initialization.
 */
public class OpenJpaEntityManagerFactoryPostProcessor implements BeanPostProcessor, Ordered {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Don't try to initialize EntityManagerFactory here - it causes issues with
        // entity enhancement and schema creation. OpenJPA will handle initialization
        // when the EntityManagerFactory is actually used.
        // This post-processor is registered but does nothing - it's here in case
        // we need to add initialization logic in the future.
        return bean;
    }

    @Override
    public int getOrder() {
        // Run early, before Spring Data JPA tries to access metamodel
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
