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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.airavata.cloud.aurora.client.AuroraThriftClient;
import org.apache.airavata.cloud.aurora.client.bean.IdentityBean;
import org.apache.airavata.cloud.aurora.client.bean.JobConfigBean;
import org.apache.airavata.cloud.aurora.client.bean.JobKeyBean;
import org.apache.airavata.cloud.aurora.client.bean.ProcessBean;
import org.apache.airavata.cloud.aurora.client.bean.ResourceBean;
import org.apache.airavata.cloud.aurora.client.bean.ResponseBean;
import org.apache.airavata.cloud.aurora.client.bean.TaskConfigBean;
import org.apache.airavata.cloud.aurora.util.AuroraThriftClientUtil;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.GroovyMap;
import org.apache.airavata.gfac.core.Script;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.JobSubmissionTask;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.impl.AuroraUtils;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuroraJobSubmissionTask implements JobSubmissionTask{

    private static final Logger log = LoggerFactory.getLogger(AuroraJobSubmissionTask.class);

    @Override
    public JobStatus cancel(TaskContext taskcontext) throws TaskException {
        JobStatus jobStatus = new JobStatus();
        jobStatus.setJobState(JobState.ACTIVE);
        return jobStatus;
    }

    @Override
    public void init(Map<String, String> propertyMap) throws TaskException {

    }

    @Override
    public TaskStatus execute(TaskContext taskContext) {
        TaskStatus taskStatus = new TaskStatus(TaskState.COMPLETED); // set to completed.
        ProcessContext processContext = taskContext.getParentProcessContext();
        JobModel jobModel = processContext.getJobModel();
        jobModel.setTaskId(taskContext.getTaskId());
        String jobIdAndName = "A" + GFacUtils.generateJobName();
        jobModel.setJobName(jobIdAndName);
        JobStatus jobStatus = new JobStatus();
        jobStatus.setJobState(JobState.SUBMITTED);

        try {
            JobKeyBean jobKey = new JobKeyBean(AuroraUtils.ENVIRONMENT, AuroraUtils.ROLE, jobIdAndName);
            IdentityBean owner = new IdentityBean(AuroraUtils.ROLE);
            GroovyMap groovyMap = GFacUtils.createGroovyMap(processContext, taskContext);
            groovyMap.add(Script.JOB_SUBMITTER_COMMAND, "sh");
            String templateFileName = GFacUtils.getTemplateFileName(ResourceJobManagerType.CLOUD);
            String script = GFacUtils.generateScript(groovyMap, templateFileName);
            Set<ProcessBean> processes = new LinkedHashSet<>();
            ProcessBean process_1 = new ProcessBean("main_process", script, false);
            processes.add(process_1);

            groovyMap.getStringValue(Script.STANDARD_OUT_FILE)
                    .ifPresent(stdout -> {
                        ProcessBean stdOutProcess = new ProcessBean("stdout_copy_process", "cp .logs/main_process/0/stdout " + stdout, false);
                        processes.add(stdOutProcess);
                    });

            groovyMap.getStringValue(Script.STANDARD_ERROR_FILE)
                    .ifPresent(stderr -> {
                        ProcessBean stdErrProcess = new ProcessBean("stderr_copy_process", "cp .logs/main_process/0/stderr " + stderr, false);
                        processes.add(stdErrProcess);
                    });

            ResourceBean resources = new ResourceBean(1.5, 512, 512);

            TaskConfigBean taskConfig = new TaskConfigBean("Airavata-Aurora-" + jobIdAndName, processes, resources);
            JobConfigBean jobConfig = new JobConfigBean(jobKey, owner, taskConfig, AuroraUtils.CLUSTER);

            String executorConfigJson = AuroraThriftClientUtil.getExecutorConfigJson(jobConfig);
            log.info("Executor Config for Job {} , {}", jobIdAndName, executorConfigJson);

            AuroraThriftClient client = AuroraThriftClient.getAuroraThriftClient();
            ResponseBean response = client.createJob(jobConfig);
            log.info("Response for job {}, {}", jobIdAndName, response);
            jobModel.setJobDescription(resources.toString());

            jobModel.setJobId(jobIdAndName);
            jobStatus.setReason("Successfully Submitted");
            jobModel.setJobStatuses(Arrays.asList(jobStatus ));
            jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            taskContext.getParentProcessContext().setJobModel(jobModel);

            GFacUtils.saveJobModel(processContext, jobModel);
            GFacUtils.saveJobStatus(processContext, jobModel);
            taskStatus.setReason("Successfully submitted job to Aurora");
        } catch (Throwable e) {
            String msg = "Error occurred while submitting Aurora job";
            log.error(msg, e);
            taskStatus.setState(TaskState.FAILED);
            taskStatus.setReason(msg);
            taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
        }

        taskContext.setTaskStatus(taskStatus);
        try {
            GFacUtils.saveAndPublishTaskStatus(taskContext);
        } catch (GFacException e) {
            log.error("Error while saving task status", e);
        }
        return taskStatus;
    }

    @Override
    public TaskStatus recover(TaskContext taskContext) {
        return execute(taskContext);
    }

    @Override
    public TaskTypes getType() {
        return TaskTypes.JOB_SUBMISSION;
    }
}
