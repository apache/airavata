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
package org.apache.airavata.helix.impl.participant;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.participant.HelixParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "services.helix.enabled", havingValue = "true", matchIfMissing = true)
public class GlobalParticipant extends HelixParticipant<AbstractTask> {

    private static final Logger logger = LoggerFactory.getLogger(GlobalParticipant.class);
    private final ApplicationContext applicationContext;

    @SuppressWarnings("WeakerAccess")
    public GlobalParticipant(
            List<Class<? extends AbstractTask>> taskClasses, String taskTypeName, AiravataServerProperties properties) {
        super(taskClasses, taskTypeName, properties);
        this.applicationContext = null;
        // Initialize property-dependent fields immediately for programmatic creation
        initialize();
    }

    // Constructor for Spring - uses constructor injection for properties
    // No checked exceptions - initialization happens in @PostConstruct
    @org.springframework.beans.factory.annotation.Autowired
    public GlobalParticipant(AiravataServerProperties properties, ApplicationContext applicationContext) {
        // Pass empty list for taskClasses - will be set in @PostConstruct
        // Using Collections.emptyList() to avoid ambiguity with Class<T> constructor
        super(new ArrayList<>(), null, properties);
        this.applicationContext = applicationContext;
        setApplicationContext(applicationContext);
    }

    @PostConstruct
    public void init() {
        // All initialization logic here - no exceptions in constructor
        if (applicationContext != null) {
            // Get all enabled AbstractTask beans from Spring context
            Map<String, AbstractTask> taskBeans = applicationContext.getBeansOfType(AbstractTask.class);
            List<Class<? extends AbstractTask>> taskClasses = new ArrayList<>();
            for (AbstractTask task : taskBeans.values()) {
                logger.debug(
                        "Adding task bean: {} to the global participant",
                        task.getClass().getName());
                taskClasses.add(task.getClass());
            }
            setTaskClasses(taskClasses);
        }

        // Initialize parent's property-dependent fields
        initialize();
    }

    /**
     * Standardized start method for Spring Boot integration.
     * Non-blocking: starts internal thread and returns immediately.
     */
    public void start() {
        Thread t = new Thread(this);
        t.start();
    }
}
