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
package org.apache.airavata.orchestrator.validator;

import java.util.Arrays;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.ClassMetadata;

/**
 * Enables a {@link JobMetadataValidator} bean iff its simple class name (without package) appears in the comma-separated list
 * configured by {@code services.monitor.compute.validators}.
 * 
 * Example: If the property is "BatchQueueValidator,ExperimentStatusValidator", then only those validator beans will be enabled.
 */
public class ComputeValidatorEnabledCondition implements Condition {

    public static final String VALIDATORS_PROPERTY = "services.monitor.compute.validators";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String configured = context.getEnvironment().getProperty(VALIDATORS_PROPERTY, "");
        if (configured == null || configured.isBlank()) {
            return false;
        }

        String className = null;
        if (metadata instanceof ClassMetadata) {
            className = ((ClassMetadata) metadata).getClassName();
        }
        if (className == null || className.isBlank()) {
            return false;
        }

        // Extract simple class name from full class name
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);

        // Match against simple class names in the configuration
        return Arrays.stream(configured.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .anyMatch(s -> s.equals(simpleClassName));
    }
}
