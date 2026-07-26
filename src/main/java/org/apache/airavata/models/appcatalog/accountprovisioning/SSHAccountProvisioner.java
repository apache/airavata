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
package org.apache.airavata.models.appcatalog.accountprovisioning;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.accountprovisioning.proto.SSHAccountProvisioner}.
 */
public record SSHAccountProvisioner(
        String name,
        boolean canCreateAccount,
        boolean canInstallSshKey,
        List<SSHAccountProvisionerConfigParam> configParams) {

    public SSHAccountProvisioner {
        configParams = configParams == null ? List.of() : List.copyOf(configParams);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String name = "";
        private boolean canCreateAccount;
        private boolean canInstallSshKey;
        private List<SSHAccountProvisionerConfigParam> configParams = new ArrayList<>();

        private Builder() {}

        private Builder(SSHAccountProvisioner source) {
            this.name = source.name;
            this.canCreateAccount = source.canCreateAccount;
            this.canInstallSshKey = source.canInstallSshKey;
            this.configParams = new ArrayList<>(source.configParams);
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setCanCreateAccount(boolean canCreateAccount) {
            this.canCreateAccount = canCreateAccount;
            return this;
        }

        public Builder setCanInstallSshKey(boolean canInstallSshKey) {
            this.canInstallSshKey = canInstallSshKey;
            return this;
        }

        public Builder addConfigParams(SSHAccountProvisionerConfigParam value) {
            this.configParams.add(value);
            return this;
        }

        public Builder addAllConfigParams(Iterable<SSHAccountProvisionerConfigParam> values) {
            values.forEach(this.configParams::add);
            return this;
        }

        public Builder clearConfigParams() {
            this.configParams.clear();
            return this;
        }

        public SSHAccountProvisioner build() {
            return new SSHAccountProvisioner(name, canCreateAccount, canInstallSshKey, configParams);
        }
    }
}
