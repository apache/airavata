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
package org.apache.airavata.agents.api;

import org.apache.airavata.service.CredentialStoreService;
import org.apache.airavata.service.RegistryService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class AgentUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    private final RegistryService registryService;
    private final CredentialStoreService credentialStoreService;

    public AgentUtils(
            ApplicationContext applicationContext,
            RegistryService registryService,
            CredentialStoreService credentialStoreService) {
        AgentUtils.applicationContext = applicationContext;
        this.registryService = registryService;
        this.credentialStoreService = credentialStoreService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        AgentUtils.applicationContext = applicationContext;
    }

    // Instance methods for Spring DI
    public RegistryService getRegistryServiceInstance() {
        return registryService;
    }

    public CredentialStoreService getCredentialServiceInstance() {
        return credentialStoreService;
    }

    // Static methods for backward compatibility - delegate to instance
    public static RegistryService getRegistryService() throws AgentException {
        if (applicationContext != null) {
            return applicationContext.getBean(AgentUtils.class).getRegistryServiceInstance();
        }
        throw new AgentException("ApplicationContext not available. RegistryService cannot be retrieved.");
    }

    public static CredentialStoreService getCredentialService() throws AgentException {
        if (applicationContext != null) {
            return applicationContext.getBean(AgentUtils.class).getCredentialServiceInstance();
        }
        throw new AgentException("ApplicationContext not available. CredentialStoreService cannot be retrieved.");
    }
}
