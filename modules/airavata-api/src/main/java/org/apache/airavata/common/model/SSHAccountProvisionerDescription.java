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
package org.apache.airavata.common.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain model: SSHAccountProvisionerDescription
 */
public class SSHAccountProvisionerDescription {
    private String name;
    private boolean canCreateAccount;
    private boolean canInstallSSHKey;
    private List<SSHAccountProvisionerConfigParam> configParams;

    public SSHAccountProvisionerDescription() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getCanCreateAccount() {
        return canCreateAccount;
    }

    public void setCanCreateAccount(boolean canCreateAccount) {
        this.canCreateAccount = canCreateAccount;
    }

    public boolean getCanInstallSSHKey() {
        return canInstallSSHKey;
    }

    public void setCanInstallSSHKey(boolean canInstallSSHKey) {
        this.canInstallSSHKey = canInstallSSHKey;
    }

    public List<SSHAccountProvisionerConfigParam> getConfigParams() {
        return configParams;
    }

    public void setConfigParams(List<SSHAccountProvisionerConfigParam> configParams) {
        this.configParams = configParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SSHAccountProvisionerDescription that = (SSHAccountProvisionerDescription) o;
        return Objects.equals(name, that.name)
                && Objects.equals(canCreateAccount, that.canCreateAccount)
                && Objects.equals(canInstallSSHKey, that.canInstallSSHKey)
                && Objects.equals(configParams, that.configParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, canCreateAccount, canInstallSSHKey, configParams);
    }

    @Override
    public String toString() {
        return "SSHAccountProvisionerDescription{" + "name=" + name + ", canCreateAccount=" + canCreateAccount
                + ", canInstallSSHKey=" + canInstallSSHKey + ", configParams=" + configParams + "}";
    }
}
