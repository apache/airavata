/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.helix.impl.task.submission.config;

import org.apache.airavata.helix.impl.task.submission.config.app.*;
import org.apache.airavata.helix.impl.task.submission.config.app.parser.*;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobFactory {

    private final static Logger logger = LoggerFactory.getLogger(JobFactory.class);

    public static String getTemplateFileName(ResourceJobManagerType resourceJobManagerType) {
        switch (resourceJobManagerType) {
            case FORK:
                return "FORK_Groovy.template";
            case PBS:
                return "PBS_Groovy.template";
            case SLURM:
                return "SLURM_Groovy.template";
            case UGE:
                return "UGE_Groovy.template";
            case LSF:
                return "LSF_Groovy.template";
            case CLOUD:
                return "CLOUD_Groovy.template";
            case HTCONDOR:
                return "HTCONDOR_Groovy.template";
            default:
                return null;
        }
    }

    public static ResourceJobManager getResourceJobManager(RegistryService.Client registryClient,
                                                           JobSubmissionProtocol submissionProtocol,
                                                           JobSubmissionInterface jobSubmissionInterface) throws Exception {
        try {
            if (submissionProtocol == JobSubmissionProtocol.SSH ) {
                SSHJobSubmission sshJobSubmission = getSSHJobSubmission(registryClient, jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (sshJobSubmission != null) {
                    return sshJobSubmission.getResourceJobManager();
                }
            } else if (submissionProtocol == JobSubmissionProtocol.LOCAL) {
                LOCALSubmission localJobSubmission = getLocalJobSubmission(registryClient, jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (localJobSubmission != null) {
                    return localJobSubmission.getResourceJobManager();
                }
            } else if (submissionProtocol == JobSubmissionProtocol.SSH_FORK){
                SSHJobSubmission sshJobSubmission = getSSHJobSubmission(registryClient, jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (sshJobSubmission != null) {
                    return sshJobSubmission.getResourceJobManager();
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch a resource job manager for protocol " + submissionProtocol + " and interface " + jobSubmissionInterface.getJobSubmissionInterfaceId(), e);
            throw new Exception("Failed to fetch a resource job manager for protocol " + submissionProtocol + " and interface " + jobSubmissionInterface.getJobSubmissionInterfaceId(), e);
        }

        // If not resource job manager is found, throw an exception to fail fast
        throw new Exception("No resource job manager for protocol " + submissionProtocol + " and interface " + jobSubmissionInterface.getJobSubmissionInterfaceId());
    }

    public static LOCALSubmission getLocalJobSubmission(RegistryService.Client registryClient, String submissionId) throws AppCatalogException {
        try {
            return registryClient.getLocalJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving local job submission with submission id : " + submissionId;
            throw new AppCatalogException(errorMsg, e);
        }
    }

    public static SSHJobSubmission getSSHJobSubmission(RegistryService.Client registryClient, String submissionId) throws AppCatalogException {
        try {
            return registryClient.getSSHJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SSH job submission with submission id : " + submissionId;
            throw new AppCatalogException(errorMsg, e);
        }
    }

    public static JobManagerConfiguration getJobManagerConfiguration(ResourceJobManager resourceJobManager) throws Exception {
        if(resourceJobManager == null) {
            throw new Exception("Resource job manager can not be null");
        }

        String templateFileName = getTemplateFileName(resourceJobManager.getResourceJobManagerType());
        switch (resourceJobManager.getResourceJobManagerType()) {
            case PBS:
                return new PBSJobConfiguration(templateFileName, ".pbs", resourceJobManager.getJobManagerBinPath(),
                        resourceJobManager.getJobManagerCommands(), new PBSOutputParser());
            case SLURM:
                return new SlurmJobConfiguration(templateFileName, ".slurm", resourceJobManager
                        .getJobManagerBinPath(), resourceJobManager.getJobManagerCommands(), new SlurmOutputParser());
            case LSF:
                return new LSFJobConfiguration(templateFileName, ".lsf", resourceJobManager.getJobManagerBinPath(),
                        resourceJobManager.getJobManagerCommands(), new LSFOutputParser());
            case UGE:
                return new UGEJobConfiguration(templateFileName, ".pbs", resourceJobManager.getJobManagerBinPath(),
                        resourceJobManager.getJobManagerCommands(), new UGEOutputParser());
            case FORK:
                return new ForkJobConfiguration(templateFileName, ".sh", resourceJobManager.getJobManagerBinPath(),
                        resourceJobManager.getJobManagerCommands(), new ForkOutputParser());
            case HTCONDOR:
                return new HTCondorJobConfiguration(templateFileName, ".submit", resourceJobManager
                        .getJobManagerBinPath(), resourceJobManager.getJobManagerCommands(), new HTCondorOutputParser());
            // We don't have a job configuration manager for CLOUD type
            default:
                throw new Exception("Could not find a job manager configuration for job manager type "
                        + resourceJobManager.getResourceJobManagerType());
        }

    }
}
