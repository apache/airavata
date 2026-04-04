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
package org.apache.airavata.compute.util;

import jakarta.annotation.PostConstruct;
import org.apache.airavata.interfaces.CredentialProvider;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.task.SchedulerUtils;
import org.springframework.stereotype.Component;

@Component
public class AgentUtils {

    private static CredentialProvider credentialProviderInstance;

    private final CredentialProvider credentialProvider;

    public AgentUtils(CredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    @PostConstruct
    void init() {
        credentialProviderInstance = credentialProvider;
    }

    public static RegistryHandler getRegistryServiceClient() {
        return SchedulerUtils.getRegistryHandler();
    }

    public static CredentialProvider getCredentialClient() throws AgentException {
        if (credentialProviderInstance == null) {
            throw new AgentException("CredentialProvider has not been initialized — ensure Spring context is active");
        }
        return credentialProviderInstance;
    }
}
