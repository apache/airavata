/**
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
package org.apache.airavata.gfac.monitor.cloud;

import org.apache.airavata.cloud.aurora.client.AuroraThriftClient;
import org.apache.airavata.cloud.aurora.client.bean.JobDetailsResponseBean;
import org.apache.airavata.cloud.aurora.client.bean.JobKeyBean;
import org.apache.airavata.cloud.aurora.client.sdk.ScheduledTask;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacThreadPoolExecutor;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.monitor.JobMonitor;
import org.apache.airavata.gfac.impl.AuroraUtils;
import org.apache.airavata.gfac.impl.Factory;
import org.apache.airavata.gfac.impl.GFacWorker;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class AuroraJobMonitor implements JobMonitor, Runnable {
    private static final Logger log = LoggerFactory.getLogger(AuroraJobMonitor.class);



    private static AuroraJobMonitor auroraJobMonitor;
    private Timer timer;
    private Map<String,TaskContext> jobMonitoringMap;
    private AuroraJobMonitor(){
        jobMonitoringMap = new ConcurrentHashMap<>();
        timer = new Timer("Aurora status poll timer", true);

    }

    public static AuroraJobMonitor getInstance(){
        if (auroraJobMonitor == null) {
            synchronized (AuroraJobMonitor.class){
                if (auroraJobMonitor == null) {
                    auroraJobMonitor = new AuroraJobMonitor();
                }
            }
        }
        return auroraJobMonitor;
    }
    @Override
    public void run() {
        AuroraTimer task = null;
        try {
            task = new AuroraTimer();
            timer.schedule(task, 5000, 5000);
        } catch (Exception e) {
            log.error("Error couldn't run Aurora status poll timer task");
        }
    }

    @Override
    public void monitor(String jobId, TaskContext taskContext) {
        jobMonitoringMap.put(jobId, taskContext);
        log.info("Added JobId : {} to Aurora Job Monitoring map", jobId);
        taskContext.getParentProcessContext().setPauseTaskExecution(true);
    }

    @Override
    public void stopMonitor(String jobId, boolean runOutFlow) {
        jobMonitoringMap.remove(jobId);
    }

    @Override
    public boolean isMonitoring(String jobId) {
        return jobMonitoringMap.get(jobId) != null;
    }

    @Override
    public void canceledJob(String jobId) {
        throw new IllegalStateException("Method not yet implemented");
    }

    class AuroraTimer extends TimerTask {

        AuroraThriftClient client;
        public AuroraTimer() throws Exception {
            client = AuroraThriftClient.getAuroraThriftClient();

        }


        @Override

        public void run() {
            while(true){
                RegistryService.Client registryClient = Factory.getRegistryServiceClient();
                try {
                    JobKeyBean jobKeyBean = new JobKeyBean(AuroraUtils.ENVIRONMENT, AuroraUtils.ROLE, "dummy");
                    Iterator<Map.Entry<String, TaskContext>> iterator = jobMonitoringMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, TaskContext> currentEntry = iterator.next();
                        try {
                            jobKeyBean.setName(currentEntry.getKey());
                            JobDetailsResponseBean jobDetailsResponseBean = client.getJobDetails(jobKeyBean);
                            List<ScheduledTask> tasks = jobDetailsResponseBean.getTasks();
                            switch (tasks.get(0).getStatus()) {
                                case FINISHED:
                                    iterator.remove();
                                    processJob(registryClient, currentEntry.getKey(), currentEntry.getValue(), JobState.COMPLETE);
                                    break;
                                case FAILED:
                                    iterator.remove();
                                    processJob(registryClient, currentEntry.getKey(), currentEntry.getValue(), JobState.FAILED);
                                    break;
                                case RUNNING:
                                    updateStatus(registryClient, currentEntry.getKey(), currentEntry.getValue(), JobState.ACTIVE);
                                    break;
                                default:
                                    log.info("Job {} is in {} state", currentEntry.getKey(), tasks.get(0).getStatus().name());
                                    break;
                            }
                        } catch (Exception e) {
                            log.error("Error while getting response for job : {}", currentEntry.getKey());

                        }
                    }
                } finally {
                    if (registryClient != null) {
                        ThriftUtils.close(registryClient);
                    }
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    log.warn("Aurora Monitoring task interrupted");
                }
            }
        }

        private void updateStatus(RegistryService.Client registryClient, String jobKey, TaskContext taskContext, JobState jobState) {
            ProcessContext pc = taskContext.getParentProcessContext();
            JobModel jobModel = pc.getJobModel();
            if (jobModel.getJobStatuses().get(0).getJobState() != jobState) {
                JobStatus jobStatus = new JobStatus(jobState);
                jobStatus.setReason("Aurora return " + jobState.name());
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                jobModel.setJobStatuses(Arrays.asList(jobStatus));
                try {
                    GFacUtils.saveJobStatus(pc, registryClient, jobModel);
                } catch (GFacException e) {
                    log.error("Error while saving job status {}, job : {}, task :{}, process:{} exp:{}",
                            jobState.name(), jobKey, taskContext.getTaskId(), pc.getProcessId(), pc.getExperimentId());
                }
            }
        }

        private void processJob(RegistryService.Client registryClient, String jobKey, TaskContext taskContext, JobState jobState) {
            JobStatus jobStatus = new JobStatus();
            jobStatus.setJobState(jobState);
            if (jobState == JobState.COMPLETE) {
                jobStatus.setReason("Aurora Job completed");
            } else if (jobState == JobState.FAILED) {
                jobStatus.setReason("Aurora Job Failed");
            }
            ProcessContext pc = taskContext.getParentProcessContext();
            JobModel jobModel = pc.getJobModel();
            jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            jobModel.setJobStatuses(Arrays.asList(jobStatus));
            try {
                GFacUtils.saveJobStatus(pc, registryClient, jobModel);
            } catch (GFacException e) {
                log.error("Error while saving job status for job : {} ", jobKey);
            }

            TaskStatus taskStatus = new TaskStatus(TaskState.COMPLETED);
            taskStatus.setReason("Job monitoring completed with final state: " + TaskState.COMPLETED.name());
            taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            taskContext.setTaskStatus(taskStatus);
            try {
                GFacUtils.saveAndPublishTaskStatus(taskContext, registryClient);
            } catch (GFacException e) {
                log.error("Error while saving task status for exp : {} , process : {} , task : {} , job : {}",
                        taskContext.getExperimentId(), taskContext.getProcessId(), taskContext.getTaskId(), jobKey);
            }

            if (pc.isCancel()) {
                ProcessStatus processStatus = new ProcessStatus(ProcessState.CANCELLING);
                processStatus.setReason("Process has been cancelled");
                pc.setProcessStatus(processStatus);
                try {
                    GFacUtils.saveAndPublishProcessStatus(pc, registryClient);
                } catch (GFacException e) {
                    log.error("Error while cancelling process, exp : {}, process : {}", pc.getExperimentId(), pc.getProcessId());
                }
            }

            try {
                GFacThreadPoolExecutor.getCachedThreadPool().execute(new GFacWorker(pc));
            } catch (GFacException e) {
                log.error("Error while running output tasks for exp : {} , process : {}", taskContext.getExperimentId(), pc.getProcessId());

                ProcessStatus processStatus = new ProcessStatus(ProcessState.FAILED);
                processStatus.setReason("Failed to run output tasks");
                processStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                pc.setProcessStatus(processStatus);
                try {
                    GFacUtils.saveAndPublishProcessStatus(pc, registryClient);
                } catch (GFacException ex) {
                    log.error("Error while updating process status to FAILED, exp : {}, process : {}", pc.getExperimentId(), pc.getProcessId());
                }
            }
        }

    }

}
