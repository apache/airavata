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

import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.participant.HelixParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn("airavataServerProperties")
@ConditionalOnProperty(
        name = "services.helix.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class GlobalParticipant extends HelixParticipant<AbstractTask> {

    private static final Logger logger = LoggerFactory.getLogger(GlobalParticipant.class);

    public static final String[] TASK_CLASS_NAMES = {
        "org.apache.airavata.helix.impl.task.env.EnvSetupTask",
        "org.apache.airavata.helix.impl.task.staging.InputDataStagingTask",
        "org.apache.airavata.helix.impl.task.staging.OutputDataStagingTask",
        "org.apache.airavata.helix.impl.task.staging.JobVerificationTask",
        "org.apache.airavata.helix.impl.task.completing.CompletingTask",
        "org.apache.airavata.helix.impl.task.submission.ForkJobSubmissionTask",
        "org.apache.airavata.helix.impl.task.submission.DefaultJobSubmissionTask",
        "org.apache.airavata.helix.impl.task.submission.LocalJobSubmissionTask",
        "org.apache.airavata.helix.impl.task.staging.ArchiveTask",
        "org.apache.airavata.helix.impl.task.cancel.WorkflowCancellationTask",
        "org.apache.airavata.helix.impl.task.cancel.RemoteJobCancellationTask",
        "org.apache.airavata.helix.impl.task.cancel.CancelCompletingTask",
        "org.apache.airavata.helix.impl.task.parsing.DataParsingTask",
        "org.apache.airavata.helix.impl.task.parsing.ParsingTriggeringTask",
        "org.apache.airavata.helix.impl.task.mock.MockTask",
        "org.apache.airavata.helix.impl.task.aws.CreateEC2InstanceTask",
        "org.apache.airavata.helix.impl.task.aws.NoOperationTask",
        "org.apache.airavata.helix.impl.task.aws.AWSJobSubmissionTask",
        "org.apache.airavata.helix.impl.task.aws.AWSCompletingTask",
    };

    @SuppressWarnings("WeakerAccess")
    public GlobalParticipant(
            List<Class<? extends AbstractTask>> taskClasses, String taskTypeName, AiravataServerProperties properties) {
        super(taskClasses, taskTypeName, properties);
        // Initialize property-dependent fields immediately for programmatic creation
        initialize();
    }

    // Constructor for Spring - uses constructor injection for properties
    // No checked exceptions - initialization happens in @PostConstruct
    public GlobalParticipant(AiravataServerProperties properties) {
        // Pass empty list for taskClasses - will be set in @PostConstruct
        // Using Collections.emptyList() to avoid ambiguity with Class<T> constructor
        super(new ArrayList<>(), null, properties);
    }

    @PostConstruct
    public void init() {
        // All initialization logic here - no exceptions in constructor
        List<Class<? extends AbstractTask>> taskClasses = createTaskClasses();
        setTaskClasses(taskClasses);
        
        // Initialize parent's property-dependent fields
        initialize();
    }

    private static List<Class<? extends AbstractTask>> createTaskClasses() {
        ArrayList<Class<? extends AbstractTask>> taskClasses = new ArrayList<>();
        try {
            for (String taskClassName : TASK_CLASS_NAMES) {
                logger.debug("Adding task class: " + taskClassName + " to the global participant");
                taskClasses.add(Class.forName(taskClassName).asSubclass(AbstractTask.class));
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load task classes", e);
        }
        return taskClasses;
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
