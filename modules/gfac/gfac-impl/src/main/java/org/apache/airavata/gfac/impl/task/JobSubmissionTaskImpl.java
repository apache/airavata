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
import org.apache.airavata.common.utils.LocalEventPublisher;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.JobDescriptor;
import org.apache.airavata.gfac.core.cluster.RemoteCluster;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.JobSubmissionTask;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            AppCatalog appCatalog = processContext.getAppCatalog();
            String resourceHostId = processContext.getProcessModel().getResourceSchedule().getResourceHostId();
            ComputeResourceDescription computeResource = appCatalog.getComputeResource().getComputeResource(resourceHostId);
            LocalEventPublisher publisher = processContext.getLocalEventPublisher();
            RemoteCluster remoteCluster = processContext.getRemoteCluster();
            JobDescriptor jobDescriptor = GFacUtils.createJobDescriptor(processContext);
        } catch (AppCatalogException e) {
            log.error("Error while instatiating app catalog",e);
            throw new TaskException("Error while instatiating app catalog", e);
        } catch (ApplicationSettingsException e) {
            log.error("Error occurred while creating job descriptor", e);
            throw new TaskException("Error occurred while creating job descriptor", e);
        } catch (GFacException e) {
            log.error("Error occurred while creating job descriptor", e);
            throw new TaskException("Error occurred while creating job descriptor", e);
        }


        return null;
    }

    @Override
    public TaskState recover(TaskContext taskContext) throws TaskException {
        return null;
    }
}
