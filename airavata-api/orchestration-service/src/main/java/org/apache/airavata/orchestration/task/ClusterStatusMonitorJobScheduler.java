/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.orchestration.task;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.exception.ApplicationSettingsException;
import org.apache.airavata.interfaces.CredentialProvider;
import org.apache.airavata.interfaces.SSHConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class ClusterStatusMonitorJobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ClusterStatusMonitorJobScheduler.class);

    private final ThreadPoolTaskScheduler taskScheduler;
    private final CredentialProvider credentialProvider;
    private final SSHConnectionService sshConnectionService;
    private ScheduledFuture<?> scheduledFuture;

    public ClusterStatusMonitorJobScheduler(
            CredentialProvider credentialProvider, SSHConnectionService sshConnectionService) {
        this.credentialProvider = credentialProvider;
        this.sshConnectionService = sshConnectionService;
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("cluster-status-monitor-");
        taskScheduler.initialize();
    }

    public void scheduleClusterStatusMonitoring() throws ApplicationSettingsException {
        int intervalSeconds = Integer.parseInt(ServerSettings.getClusterStatusMonitoringRepeatTime());
        scheduledFuture = taskScheduler.scheduleAtFixedRate(
                new ClusterStatusMonitorJob(credentialProvider, sshConnectionService),
                Duration.ofSeconds(intervalSeconds));
    }
}
