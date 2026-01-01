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
package org.apache.airavata.helix.impl.task;

import java.util.EnumMap;
import java.util.Map;
import org.apache.airavata.common.model.ComputeResourceType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "services.helix.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "services.registryService.enabled", havingValue = "true", matchIfMissing = true)
public class TaskFactory {

    private final Map<ComputeResourceType, HelixTaskFactory> factories;

    public TaskFactory(SlurmTaskFactory slurmTaskFactory, AWSTaskFactory awsTaskFactory) {
        this.factories = new EnumMap<>(ComputeResourceType.class);
        this.factories.put(ComputeResourceType.SLURM, slurmTaskFactory);
        this.factories.put(ComputeResourceType.AWS, awsTaskFactory);
    }

    public HelixTaskFactory getFactory(ComputeResourceType type) {
        HelixTaskFactory factory = factories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("No TaskFactory for " + type);
        }
        return factory;
    }
}
