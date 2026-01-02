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
package org.apache.airavata.helix.core.util;

import org.apache.airavata.helix.task.api.TaskParamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Factory for creating TaskParamType instances.
 * Uses Spring DI with prototype scope to create new instances without reflection.
 */
@Component
public class TaskParamTypeFactory {

    private static final Logger logger = LoggerFactory.getLogger(TaskParamTypeFactory.class);

    private final BeanFactory beanFactory;

    public TaskParamTypeFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * Create a new instance of the specified TaskParamType class.
     * Uses Spring's BeanFactory to get prototype-scoped beans, which creates new instances.
     * If no bean is found, throws an exception (no fallback to reflection).
     *
     * @param clazz The TaskParamType class to instantiate
     * @return A new instance of the TaskParamType
     * @throws RuntimeException if bean is not found or instantiation fails
     */
    public TaskParamType createInstance(Class<? extends TaskParamType> clazz) {
        try {
            // Use ObjectProvider to get prototype-scoped beans (creates new instance each time)
            ObjectProvider<? extends TaskParamType> provider = beanFactory.getBeanProvider(clazz);
            TaskParamType instance = provider.getObject();
            logger.debug("Created TaskParamType instance from Spring bean: {}", clazz.getName());
            return instance;
        } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
            logger.error(
                    "TaskParamType bean not found in Spring context: {}. Make sure it's annotated with @Component and @Scope(\"prototype\")",
                    clazz.getName());
            throw new RuntimeException(
                    "TaskParamType bean not found: " + clazz.getName()
                            + ". Ensure it's a Spring bean with @Scope(\"prototype\")",
                    e);
        } catch (Exception e) {
            logger.error("Failed to create TaskParamType instance: {}", clazz.getName(), e);
            throw new RuntimeException("Failed to create TaskParamType instance: " + clazz.getName(), e);
        }
    }
}
