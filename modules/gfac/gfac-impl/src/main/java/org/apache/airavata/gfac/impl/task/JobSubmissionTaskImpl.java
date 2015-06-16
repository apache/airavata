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
 *
*/

package org.apache.airavata.gfac.impl.task;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.LocalEventPublisher;
import org.apache.airavata.gfac.core.*;
import org.apache.airavata.gfac.core.cluster.RemoteCluster;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.JobSubmissionTask;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.impl.Factory;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class JobSubmissionTaskImpl implements JobSubmissionTask {
    private static final Logger log = LoggerFactory.getLogger(JobSubmissionTaskImpl.class);
    @Override
    public void init(Map<String, String> propertyMap) throws TaskException {

    }

    @Override
    public TaskState execute(TaskContext taskContext) throws TaskException {
        try {
            ProcessContext processContext = taskContext.getParentProcessContext();
            RemoteCluster remoteCluster = processContext.getRemoteCluster();
            JobDescriptor jobDescriptor = GFacUtils.createJobDescriptor(processContext);
            ResourceJobManager resourceJobManager = GFacUtils.getResourceJobManager(processContext);
            JobManagerConfiguration jConfig = null;
            if (resourceJobManager != null){
                String installedParentPath = resourceJobManager.getJobManagerBinPath();
                if (installedParentPath == null) {
                    installedParentPath = "/";
                }
                ResourceJobManagerType resourceJobManagerType = resourceJobManager.getResourceJobManagerType();
                if (resourceJobManagerType == null) {
                    log.error("No Job Manager is configured, so we are picking pbs as the default job manager");
                    jConfig = Factory.getPBSJobManager(installedParentPath);
                } else {
                    if (Constants.PBS_JOB_MANAGER.equalsIgnoreCase(resourceJobManagerType.toString())) {
                        jConfig = Factory.getPBSJobManager(installedParentPath);
                    } else if (Constants.SLURM_JOB_MANAGER.equalsIgnoreCase(resourceJobManagerType.toString())) {
                        jConfig = Factory.getSLURMJobManager(installedParentPath);
                    } else if (Constants.SUN_GRID_ENGINE_JOB_MANAGER.equalsIgnoreCase(resourceJobManagerType.toString())) {
                        jConfig = Factory.getUGEJobManager(installedParentPath);
                    } else if (Constants.LSF_JOB_MANAGER.equals(resourceJobManagerType.toString())) {
                        jConfig = Factory.getLSFJobManager(installedParentPath);
                    }
                }
            }
            File jobFile = GFacUtils.createJobFile(jobDescriptor, jConfig);
            if (jobFile != null && jobFile.exists()){
                String jobId = remoteCluster.submitBatchJob(jobFile.getPath(), processContext.getWorkingDir());
            }

        } catch (AppCatalogException e) {
            log.error("Error while instatiating app catalog",e);
            throw new TaskException("Error while instatiating app catalog", e);
        } catch (ApplicationSettingsException e) {
            log.error("Error occurred while creating job descriptor", e);
            throw new TaskException("Error occurred while creating job descriptor", e);
        } catch (GFacException e) {
            log.error("Error occurred while creating job descriptor", e);
            throw new TaskException("Error occurred while creating job descriptor", e);
        } catch (SSHApiException e) {
            log.error("Error occurred while submitting the job", e);
            throw new TaskException("Error occurred while submitting the job", e);
        }
        return null;
    }

    @Override
    public TaskState recover(TaskContext taskContext) throws TaskException {
        return null;
    }
}
