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
package org.apache.airavata.gfac.core.monitor;

import com.google.common.eventbus.Subscribe;
import org.apache.airavata.common.logger.AiravataLogger;
import org.apache.airavata.common.logger.AiravataLoggerFactory;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.listener.AbstractActivityListener;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.JobStatusChangeRequestEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.workspace.experiment.JobDetails;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

public class AiravataJobStatusUpdator implements AbstractActivityListener {
    private final static Logger logger = LoggerFactory.getLogger(AiravataJobStatusUpdator.class);
    private Registry airavataRegistry;

    private MonitorPublisher monitorPublisher;
    private Publisher publisher;


    public Registry getAiravataRegistry() {
        return airavataRegistry;
    }

    public void setAiravataRegistry(Registry airavataRegistry) {
        this.airavataRegistry = airavataRegistry;
    }


    @Subscribe
    public void updateRegistry(JobStatusChangeRequestEvent jobStatus) throws Exception{
        /* Here we need to parse the jobStatus message and update
                the registry accordingly, for now we are just printing to standard Out
                 */
        JobState state = jobStatus.getState();
        if (state != null) {
            try {
                String taskID = jobStatus.getJobIdentity().getTaskId();
                String jobID = jobStatus.getJobIdentity().getJobId();
                String expId = jobStatus.getJobIdentity().getExperimentId();
                updateJobStatus(expId,taskID, jobID, state);
    			logger.debug("expId - {}: Publishing job status for " + jobStatus.getJobIdentity().getJobId() + ":"
                        + state.toString(),jobStatus.getJobIdentity().getExperimentId());
                JobStatusChangeEvent event = new JobStatusChangeEvent(jobStatus.getState(), jobStatus.getJobIdentity());
                monitorPublisher.publish(event);
                String messageId = AiravataUtils.getId("JOB");
                MessageContext msgCntxt = new MessageContext(event, MessageType.JOB, messageId, jobStatus.getJobIdentity().getGatewayId());
                msgCntxt.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
                publisher.publish(msgCntxt);
            } catch (Exception e) {
                logger.error("expId - " + jobStatus.getJobIdentity().getExperimentId() + ": Error persisting data"
                        + e.getLocalizedMessage(), e);
                throw new Exception("Error persisting job status..", e);
            }
        }
    }

    public  void updateJobStatus(String expId, String taskId, String jobID, JobState state) throws Exception {
        logger.info("expId - {}: Updating job status for " + jobID + ":" + state.toString(), expId);
        CompositeIdentifier ids = new CompositeIdentifier(taskId, jobID);
        JobDetails details = (JobDetails) airavataRegistry.get(RegistryModelType.JOB_DETAIL, ids);
        if (details == null) {
            details = new JobDetails();
        }
        org.apache.airavata.model.workspace.experiment.JobStatus status = new org.apache.airavata.model.workspace.experiment.JobStatus();
        if (JobState.CANCELED.equals(details.getJobStatus().getJobState()) ||
                JobState.CANCELING.equals(details.getJobStatus().getJobState())) {
            status.setJobState(details.getJobStatus().getJobState());
        } else {
            status.setJobState(state);
        }
        status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
        details.setJobStatus(status);
        details.setJobId(jobID);
        logger.debug("Updating job status for "+jobID+":"+details.getJobStatus().toString());
        airavataRegistry.update(RegistryModelType.JOB_STATUS, status, ids);
    }

	@SuppressWarnings("unchecked")
	public void setup(Object... configurations) {
		for (Object configuration : configurations) {
			if (configuration instanceof Registry){
				this.airavataRegistry=(Registry)configuration;
			} else if (configuration instanceof MonitorPublisher){
				this.monitorPublisher=(MonitorPublisher) configuration;
			} else if (configuration instanceof Publisher){
                this.publisher=(Publisher) configuration;
            }
		}
	}
}
