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

import com.google.common.eventbus.EventBus;
import org.apache.airavata.job.monitor.core.PullMonitor;
import org.apache.airavata.job.monitor.core.PushMonitor;
import org.apache.airavata.job.monitor.event.MonitorPublisher;
import org.apache.airavata.job.monitor.exception.AiravataMonitorException;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/*
this is the manager class for monitoring system of airavata,
This simply handle the monitoring flow of the system.
Keeps available jobs to monitor in a queue and once they are done
remove them from the queue, this will be done by AiravataJobUpdator.
 */
public class MonitorManager {
    private final static Logger logger = LoggerFactory.getLogger(MonitorManager.class);

    List<PullMonitor> pullMonitors;

    List<PushMonitor> pushMonitors;

    BlockingQueue<MonitorID> jobsToMonitor;

    MonitorPublisher monitorPublisher;

    /**
     * This will initialize the major monitoring system.
     */
    public MonitorManager() {
        pullMonitors = new ArrayList<PullMonitor>();
        pushMonitors = new ArrayList<PushMonitor>();
        jobsToMonitor = new LinkedBlockingDeque<MonitorID>();
        monitorPublisher = new MonitorPublisher(new EventBus());
        monitorPublisher.registerListener(new AiravataJobStatusUpdator(new RegistryImpl(),jobsToMonitor));
    }

    public void addPushMonitor(PushMonitor monitor) {
        pushMonitors.add(monitor);
    }

    public void addPullMonitor(PullMonitor monitor) {
        pullMonitors.add(monitor);
    }

    /**
     * In this method we assume that we give higher preference to Push
     * Monitorig mechanism if there's any configured, otherwise Pull
     * monitoring will be launched.
     * Ex: If there's a reasource which doesn't support Push, we have
     * to live with Pull MOnitoring.
     * @throws AiravataMonitorException
     */
    public void launchMonitor() throws AiravataMonitorException{

    }
}
