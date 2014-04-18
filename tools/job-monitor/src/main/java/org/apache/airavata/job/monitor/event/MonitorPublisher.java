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
package org.apache.airavata.job.monitor.event;

import org.apache.airavata.job.monitor.MonitorID;
import org.apache.airavata.job.monitor.state.ExperimentStatus;
import org.apache.airavata.job.monitor.state.JobStatus;
import org.apache.airavata.job.monitor.state.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class MonitorPublisher {
    private final static Logger logger = LoggerFactory.getLogger(MonitorPublisher.class);
    private EventBus eventBus;

    public MonitorPublisher(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void registerListener(Object listener) {
        eventBus.register(listener);
    }

    public void publish(JobStatus jobStatus) {
        eventBus.post(jobStatus);
    }

    public void publish(TaskStatus taskStatus) {
        eventBus.post(taskStatus);
    }
    
    public void publish(ExperimentStatus experimentStatus) {
        eventBus.post(experimentStatus);
    }
    
    public void publish(MonitorID monitorID){
        eventBus.post(monitorID);
    }
}
