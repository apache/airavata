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
import org.apache.airavata.service.ServiceFactory;

public class AgentUtils {

    public static RegistryService getRegistryService() throws AgentException {
        try {
            return ServiceFactory.getInstance().getRegistryService();
        } catch (Exception e) {
            throw new AgentException("Unable to create registry service...", e);
        }
    }

    public static CredentialStoreService getCredentialService() throws AgentException {
        try {
            return ServiceFactory.getInstance().getCredentialStoreService();
        } catch (Exception e) {
            throw new AgentException("Unable to create credential service...", e);
        }
    }
}
