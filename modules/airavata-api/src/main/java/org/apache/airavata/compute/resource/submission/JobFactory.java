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
package org.apache.airavata.compute.resource.submission;

import java.util.Map;
import org.apache.airavata.compute.resource.model.ComputeCapability;
import org.apache.airavata.compute.resource.model.JobManagerCommand;
import org.apache.airavata.compute.resource.model.Resource;
import org.apache.airavata.compute.resource.model.ResourceJobManagerType;
import org.apache.airavata.compute.provider.aws.AwsJobSpec;
import org.apache.airavata.compute.provider.local.LocalJobSpec;
import org.apache.airavata.compute.provider.local.LocalOutputParser;
import org.apache.airavata.compute.provider.slurm.SlurmJobSpec;
import org.apache.airavata.compute.provider.slurm.SlurmOutputParser;
import org.springframework.stereotype.Component;

@Component
public class JobFactory {

    public String getTemplateFileName(ResourceJobManagerType resourceJobManagerType) {
        return switch (resourceJobManagerType) {
            case FORK -> "FORK_Groovy.template";
            case SLURM -> "SLURM_Groovy.template";
            case CLOUD -> "CLOUD_Groovy.template";
            default -> null;
        };
    }

    // -------------------------------------------------------------------------
    // New Resource-based methods
    // -------------------------------------------------------------------------

    /**
     * Derive the {@link ResourceJobManagerType} from a {@link Resource}'s compute capability.
     * Returns {@link ResourceJobManagerType#FORK} when the resource or its compute capability is absent.
     */
    public ResourceJobManagerType getResourceJobManagerType(Resource resource) {
        if (resource == null || resource.getCapabilities() == null || resource.getCapabilities().getCompute() == null) {
            return ResourceJobManagerType.FORK;
        }
        return resource.getCapabilities().getCompute().getJobManagerTypeEnum();
    }

    /**
     * Build a {@link JobManagerSpec} from a {@link Resource}'s compute capability.
     * Reads the job manager type, bin path, and command overrides from the capability.
     */
    public JobManagerSpec getJobManagerConfiguration(Resource resource) throws Exception {
        ResourceJobManagerType type = getResourceJobManagerType(resource);
        String templateFileName = "templates/" + getTemplateFileName(type);
        String binPath = null;
        Map<JobManagerCommand, String> commands = null;
        if (resource != null && resource.getCapabilities() != null && resource.getCapabilities().getCompute() != null) {
            ComputeCapability compute = resource.getCapabilities().getCompute();
            binPath = compute.getJobManagerBinPath();
            commands = compute.getJobManagerCommands();
        }
        return switch (type) {
            case SLURM -> new SlurmJobSpec(templateFileName, ".slurm", binPath, commands, new SlurmOutputParser());
            case FORK -> new LocalJobSpec(templateFileName, ".sh", binPath, commands, new LocalOutputParser());
            case CLOUD -> new AwsJobSpec(templateFileName);
            default -> throw new Exception("Could not find a job manager spec for job manager type " + type);
        };
    }

}
