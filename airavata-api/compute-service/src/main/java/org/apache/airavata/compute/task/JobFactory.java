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
package org.apache.airavata.compute.task;

import org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManagerType;

public class JobFactory {

    public static String getTemplateFileName(ResourceJobManagerType resourceJobManagerType) {
        return switch (resourceJobManagerType) {
            case FORK -> "FORK_Groovy.template";
            case PBS -> "PBS_Groovy.template";
            case SLURM -> "SLURM_Groovy.template";
            case UGE -> "UGE_Groovy.template";
            case LSF -> "LSF_Groovy.template";
            case CLOUD -> "CLOUD_Groovy.template";
            case HTCONDOR -> "HTCONDOR_Groovy.template";
            default -> null;
        };
    }

    public static JobManagerConfiguration getJobManagerConfiguration(ResourceJobManager resourceJobManager)
            throws Exception {
        if (resourceJobManager == null) {
            throw new Exception("Resource job manager can not be null");
        }

        String templateFileName = "templates/" + getTemplateFileName(resourceJobManager.getResourceJobManagerType());
        switch (resourceJobManager.getResourceJobManagerType()) {
            case PBS:
                return new PBSJobConfiguration(
                        templateFileName,
                        ".pbs",
                        resourceJobManager.getJobManagerBinPath(),
                        resourceJobManager.getJobManagerCommandsMap(),
                        new PBSOutputParser());
            case SLURM:
                return new SlurmJobConfiguration(
                        templateFileName,
                        ".slurm",
                        resourceJobManager.getJobManagerBinPath(),
                        resourceJobManager.getJobManagerCommandsMap(),
                        new SlurmOutputParser());
            case LSF:
                return new LSFJobConfiguration(
                        templateFileName,
                        ".lsf",
                        resourceJobManager.getJobManagerBinPath(),
                        resourceJobManager.getJobManagerCommandsMap(),
                        new LSFOutputParser());
            case UGE:
                return new UGEJobConfiguration(
                        templateFileName,
                        ".pbs",
                        resourceJobManager.getJobManagerBinPath(),
                        resourceJobManager.getJobManagerCommandsMap(),
                        new UGEOutputParser());
            case FORK:
                return new ForkJobConfiguration(
                        templateFileName,
                        ".sh",
                        resourceJobManager.getJobManagerBinPath(),
                        resourceJobManager.getJobManagerCommandsMap(),
                        new ForkOutputParser());
            case HTCONDOR:
                return new HTCondorJobConfiguration(
                        templateFileName,
                        ".submit",
                        resourceJobManager.getJobManagerBinPath(),
                        resourceJobManager.getJobManagerCommandsMap(),
                        new HTCondorOutputParser());
            case CLOUD:
                return new CloudJobManagerConfiguration(templateFileName);
            // We don't have a job configuration manager for CLOUD type
            default:
                throw new Exception("Could not find a job manager configuration for job manager type "
                        + resourceJobManager.getResourceJobManagerType());
        }
    }
}
