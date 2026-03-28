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
package org.apache.airavata.thrift.server.config;

import java.util.Arrays;
import java.util.List;
import org.apache.airavata.common.db.DBInitConfig;
import org.apache.airavata.common.db.DBInitializer;
import org.apache.airavata.credential.repository.util.CredentialStoreDBInitConfig;
import org.apache.airavata.execution.util.AppCatalogDBInitConfig;
import org.apache.airavata.execution.util.ExpCatalogDBInitConfig;
import org.apache.airavata.execution.util.ReplicaCatalogDBInitConfig;
import org.apache.airavata.execution.util.WorkflowCatalogDBInitConfig;
import org.apache.airavata.security.profile.user.core.utils.UserProfileCatalogDBInitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class BackgroundServicesConfig {

    private static final Logger logger = LoggerFactory.getLogger(BackgroundServicesConfig.class);

    private final List<DBInitConfig> dbInitConfigs = Arrays.asList(
            new ExpCatalogDBInitConfig(),
            new AppCatalogDBInitConfig(),
            new ReplicaCatalogDBInitConfig(),
            new WorkflowCatalogDBInitConfig(),
            new CredentialStoreDBInitConfig(),
            new UserProfileCatalogDBInitConfig());

    @PostConstruct
    public void initializeDatabases() {
        logger.info("Initializing databases...");
        for (DBInitConfig dbInitConfig : dbInitConfigs) {
            DBInitializer.initializeDB(dbInitConfig);
        }
        logger.info("Databases initialized successfully");

        // TODO: Migrate remaining background services to Spring-managed beans:
        // - DBEventManagerRunner
        // - MonitoringServer (Prometheus metrics)
        // - ComputationalResourceMonitoringService
        // - DataInterpreterService
        // - ProcessReschedulingService
        // - HelixController + GlobalParticipant
        // - PreWorkflowManager, PostWorkflowManager, ParserWorkflowManager
        // - EmailBasedMonitor, RealtimeMonitor
    }
}
