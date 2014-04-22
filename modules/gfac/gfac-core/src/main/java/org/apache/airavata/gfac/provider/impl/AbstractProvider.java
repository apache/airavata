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

package org.apache.airavata.gfac.provider.impl;

import java.util.List;

import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.job.monitor.MonitorID;
import org.apache.airavata.job.monitor.command.TaskCancelRequest;
import org.apache.airavata.job.monitor.event.MonitorPublisher;
import org.apache.airavata.job.monitor.state.JobStatusChangeRequest;
import org.apache.airavata.model.workspace.experiment.JobDetails;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.model.workspace.experiment.JobStatus;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.DataType;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.utils.Constants.FieldConstants.JobDetaisConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProvider implements GFacProvider{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected Registry registry = null;
	protected JobDetails details;     //todo we need to remove this and add methods to fill Job details, this is not a property of a provider
	protected JobStatus status;   //todo we need to remove this and add methods to fill Job details, this is not a property of a provider
	protected JobExecutionContext jobExecutionContext;

	private MonitorPublisher monitorPublisher;

	public void initialize(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException {
		registry = RegistryFactory.getDefaultRegistry();
		details = new JobDetails();
		status = new JobStatus();
		this.jobExecutionContext=jobExecutionContext;
	}
	
	@Override
	public void taskCancelRequested(TaskCancelRequest request) {
		try {
			List<Object> jobDetails = registry.get(DataType.JOB_DETAIL, JobDetaisConstants.TASK_ID, request.getTaskId());
			for (Object o : jobDetails) {
				JobDetails jd=(JobDetails)o;
				JobState jobState = jd.getJobStatus().getJobState();
				if (jobState!=JobState.CANCELED || jobState!=JobState.CANCELING || jobState!=JobState.COMPLETE || jobState!=JobState.FAILED){
					MonitorID monitorId = new MonitorID(null, jd.getJobID(), request.getTaskId(), request.getExperimentId(), null, null);
					monitorPublisher.publish(new JobStatusChangeRequest(monitorId, JobState.CANCELING));
					log.debug("Canceling job "+jd.getJobID());
					cancelJob(jd.getJobID(), jobExecutionContext);
				}
			}
		} catch (RegistryException e) {
			log.error("Error retrieving job details for Task "+request.getTaskId(),e);
		} catch (Exception e) {
			log.error("Error canceling jobs!!!",e);
		}
	}
	
	@Override
	public void setup(Object... configurations) {
		for (Object configuration : configurations) {
			if (configuration instanceof MonitorPublisher){
				this.monitorPublisher=(MonitorPublisher) configuration;
			} 
		}
	}
}
