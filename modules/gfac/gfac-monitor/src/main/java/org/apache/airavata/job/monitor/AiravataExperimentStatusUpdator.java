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
package org.apache.airavata.job.monitor;

import java.util.Calendar;

import org.apache.airavata.job.monitor.state.ExperimentStatus;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.ExperimentState;
import org.apache.airavata.registry.cpi.DataType;
import org.apache.airavata.registry.cpi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class AiravataExperimentStatusUpdator implements AbstractActivityListener{
    private final static Logger logger = LoggerFactory.getLogger(AiravataExperimentStatusUpdator.class);

    private Registry airavataRegistry;

    public Registry getAiravataRegistry() {
        return airavataRegistry;
    }

    public void setAiravataRegistry(Registry airavataRegistry) {
        this.airavataRegistry = airavataRegistry;
    }

    @Subscribe
    public void updateRegistry(ExperimentStatus taskStatus) {
        ExperimentState state = taskStatus.getState();
        if (state != null) {
            try {
                String taskID = taskStatus.getMonitorID().getTaskID();
                updateExperimentStatus(taskID, state);
            } catch (Exception e) {
                logger.error("Error persisting data" + e.getLocalizedMessage(), e);
            }
        }
    }
    
    public  void updateExperimentStatus(String experimentId, ExperimentState state) throws Exception {
    	Experiment details = (Experiment)airavataRegistry.get(DataType.EXPERIMENT, experimentId);
        if(details == null) {
            details = new Experiment();
            details.setExperimentID(experimentId);
        }
        org.apache.airavata.model.workspace.experiment.ExperimentStatus status = new org.apache.airavata.model.workspace.experiment.ExperimentStatus();
        status.setExperimentState(state);
        status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
        airavataRegistry.update(org.apache.airavata.registry.cpi.DataType.EXPERIMENT, details, experimentId);
    }

	@Override
	public void setup(Object... configurations) {
		for (Object configuration : configurations) {
			if (configuration instanceof Registry){
				this.airavataRegistry=(Registry)configuration;
			} 
		}
	}
}
