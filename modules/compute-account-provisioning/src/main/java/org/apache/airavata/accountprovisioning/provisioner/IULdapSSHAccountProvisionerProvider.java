/*
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
 *
 */

package org.apache.airavata.accountprovisioning.provisioner;

import org.apache.airavata.accountprovisioning.ConfigParam;
import org.apache.airavata.accountprovisioning.SSHAccountProvisioner;
import org.apache.airavata.accountprovisioning.SSHAccountProvisionerProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IULdapSSHAccountProvisionerProvider implements SSHAccountProvisionerProvider {

    @Override
    public List<ConfigParam> getConfigParams() {
        List<ConfigParam> configParams = new ArrayList<>();
        configParams.add(new ConfigParam("ldap-host")
                .setDescription("Hostname of LDAP server")
                .setOptional(false)
                .setType(ConfigParam.ConfigParamType.STRING));
        configParams.add(new ConfigParam("ldap-port")
                .setDescription("Port of LDAP server")
                .setOptional(false)
                .setType(ConfigParam.ConfigParamType.STRING));
        configParams.add(new ConfigParam("ldap-username")
                .setDescription("Username for LDAP server")
                .setOptional(false)
                .setType(ConfigParam.ConfigParamType.STRING));
        configParams.add(new ConfigParam("ldap-password")
                .setDescription("Password for LDAP server")
                .setOptional(false)
                .setType(ConfigParam.ConfigParamType.CRED_STORE_PASSWORD_TOKEN));
        return configParams;
    }

    @Override
    public SSHAccountProvisioner createSSHAccountProvisioner(Map<ConfigParam,String> config) {
        SSHAccountProvisioner sshAccountProvisioner = new IULdapSSHAccountProvisioner();
        sshAccountProvisioner.init(config);
        return sshAccountProvisioner;
    }

    @Override
    public boolean canCreateAccount() {
        return false;
    }

    @Override
    public boolean canInstallSSHKey() {
        return true;
    }
}
