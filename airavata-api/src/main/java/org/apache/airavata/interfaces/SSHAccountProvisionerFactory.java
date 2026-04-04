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
package org.apache.airavata.interfaces;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SSHAccountProvisionerFactory {

    private final List<SSHAccountProvisionerProvider> sshAccountProvisionerProviders;

    public SSHAccountProvisionerFactory(List<SSHAccountProvisionerProvider> sshAccountProvisionerProviders) {
        this.sshAccountProvisionerProviders = sshAccountProvisionerProviders;
    }

    public List<SSHAccountProvisionerProvider> getSSHAccountProvisionerProviders() {
        return sshAccountProvisionerProviders;
    }

    public List<ConfigParam> getSSHAccountProvisionerConfigParams(String provisionerName) {
        return getSSHAccountProvisionerProvider(provisionerName).getConfigParams();
    }

    public boolean canCreateAccount(String provisionerName) {
        return getSSHAccountProvisionerProvider(provisionerName).canCreateAccount();
    }

    public SSHAccountProvisioner createSSHAccountProvisioner(String provisionerName, Map<ConfigParam, String> config) {
        SSHAccountProvisionerProvider sshAccountProvisionerProvider = getSSHAccountProvisionerProvider(provisionerName);
        return sshAccountProvisionerProvider.createSSHAccountProvisioner(config);
    }

    private SSHAccountProvisionerProvider getSSHAccountProvisionerProvider(String provisionerName) {
        for (SSHAccountProvisionerProvider sshAccountProvisionerProvider : sshAccountProvisionerProviders) {
            if (sshAccountProvisionerProvider.getName().equals(provisionerName)) {
                return sshAccountProvisionerProvider;
            }
        }
        throw new RuntimeException("Unknown SSHAccountProvisioner named " + provisionerName);
    }
}
