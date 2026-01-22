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
package org.apache.airavata.orchestrator;

import org.springframework.context.ApplicationContext;

/**
 * Holds the Spring ApplicationContext for workflow activities.
 *
 * <p>Workflow activities are instantiated by Dapr without Spring injection,
 * so this class provides static access to Spring beans.
 */
public final class WorkflowRuntimeHolder {

    private static ApplicationContext applicationContext;

    private WorkflowRuntimeHolder() {}

    public static void initialize(ApplicationContext context) {
        applicationContext = context;
    }

    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("WorkflowRuntimeHolder not initialized");
        }
        return applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return getApplicationContext().getBean(beanClass);
    }
}
