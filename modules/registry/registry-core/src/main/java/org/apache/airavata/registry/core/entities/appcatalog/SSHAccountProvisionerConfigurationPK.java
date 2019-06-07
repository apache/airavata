/**
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
 */

package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;

/**
 * The primary key class for the ssh_account_provisioner_config database table.
 */
public class SSHAccountProvisionerConfigurationPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gatewayId;
    private String resourceId;
    private String configName;

    public SSHAccountProvisionerConfigurationPK(String gatewayId, String resourceId, String configName) {
        this.gatewayId = gatewayId;
        this.resourceId = resourceId;
        this.configName = configName;
    }

    public SSHAccountProvisionerConfigurationPK() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SSHAccountProvisionerConfigurationPK)) return false;

        SSHAccountProvisionerConfigurationPK that = (SSHAccountProvisionerConfigurationPK) o;

        if (!gatewayId.equals(that.gatewayId)) return false;
        if (!resourceId.equals(that.resourceId)) return false;
        return configName.equals(that.configName);
    }

    @Override
    public int hashCode() {
        int result = gatewayId.hashCode();
        result = 31 * result + resourceId.hashCode();
        result = 31 * result + configName.hashCode();
        return result;
    }
}
