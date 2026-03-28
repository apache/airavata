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
package org.apache.airavata.server.thrift.config;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.server.thrift.handler.AiravataServerHandler;
import org.apache.airavata.credential.handler.CredentialStoreServerHandler;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.execution.handler.RegistryServerHandler;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.server.thrift.handler.OrchestratorServerHandler;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.service.profile.groupmanager.cpi.GroupManagerService;
import org.apache.airavata.service.profile.groupmanager.cpi.group_manager_cpiConstants;
import org.apache.airavata.server.thrift.handler.GroupManagerServiceHandler;
import org.apache.airavata.server.thrift.handler.IamAdminServicesHandler;
import org.apache.airavata.server.thrift.handler.TenantProfileServiceHandler;
import org.apache.airavata.server.thrift.handler.UserProfileServiceHandler;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.iam.admin.services.cpi.iam_admin_services_cpiConstants;
import org.apache.airavata.service.profile.tenant.cpi.TenantProfileService;
import org.apache.airavata.service.profile.tenant.cpi.profile_tenant_cpiConstants;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.service.profile.user.cpi.profile_user_cpiConstants;
import org.apache.airavata.sharing.handler.SharingRegistryServerHandler;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.TMultiplexedProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThriftProcessorConfig {

    private static final Logger logger = LoggerFactory.getLogger(ThriftProcessorConfig.class);

    @Bean
    public OrchestratorServerHandler orchestratorServerHandler() {
        try {
            OrchestratorServerHandler handler = new OrchestratorServerHandler();
            logger.info("OrchestratorServerHandler initialized successfully");
            return handler;
        } catch (Exception e) {
            logger.warn("Orchestrator service failed to initialize (ZooKeeper/Helix may not be available): {}",
                    e.getMessage());
            return null;
        }
    }

    @Bean
    public TMultiplexedProcessor thriftMultiplexedProcessor(
            AiravataServerHandler airavataServerHandler,
            RegistryServerHandler registryServerHandler,
            SharingRegistryServerHandler sharingRegistryServerHandler,
            CredentialStoreServerHandler credentialStoreServerHandler,
            UserProfileServiceHandler userProfileServiceHandler,
            TenantProfileServiceHandler tenantProfileServiceHandler,
            IamAdminServicesHandler iamAdminServicesHandler,
            GroupManagerServiceHandler groupManagerServiceHandler,
            @Autowired(required = false) OrchestratorServerHandler orchestratorServerHandler) {

        TMultiplexedProcessor processor = new TMultiplexedProcessor();

        processor.registerProcessor("Airavata",
                new Airavata.Processor<>(airavataServerHandler));

        processor.registerProcessor("RegistryService",
                new RegistryService.Processor<>(registryServerHandler));

        processor.registerProcessor("SharingRegistry",
                new SharingRegistryService.Processor<>(sharingRegistryServerHandler));

        processor.registerProcessor("CredentialStore",
                new CredentialStoreService.Processor<>(credentialStoreServerHandler));

        processor.registerProcessor(profile_user_cpiConstants.USER_PROFILE_CPI_NAME,
                new UserProfileService.Processor<>(userProfileServiceHandler));

        processor.registerProcessor(profile_tenant_cpiConstants.TENANT_PROFILE_CPI_NAME,
                new TenantProfileService.Processor<>(tenantProfileServiceHandler));

        processor.registerProcessor(iam_admin_services_cpiConstants.IAM_ADMIN_SERVICES_CPI_NAME,
                new IamAdminServices.Processor<>(iamAdminServicesHandler));

        processor.registerProcessor(group_manager_cpiConstants.GROUP_MANAGER_CPI_NAME,
                new GroupManagerService.Processor<>(groupManagerServiceHandler));

        if (orchestratorServerHandler != null) {
            processor.registerProcessor("Orchestrator",
                    new OrchestratorService.Processor<>(orchestratorServerHandler));
            logger.info("Orchestrator service registered on thrift processor");
        } else {
            logger.warn("Orchestrator service not available — skipping registration");
        }

        logger.info("Thrift TMultiplexedProcessor configured with services: "
                + "Airavata, RegistryService, SharingRegistry, CredentialStore, "
                + "UserProfile, TenantProfile, IamAdminServices, GroupManager");

        return processor;
    }
}
