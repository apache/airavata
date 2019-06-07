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
package org.apache.airavata.accountprovisioning.provisioner;

import org.apache.airavata.accountprovisioning.ConfigParam;
import org.apache.airavata.accountprovisioning.SSHAccountProvisioner;
import org.apache.airavata.accountprovisioning.SSHAccountProvisionerProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestSSHAccountProvisionerProvider implements SSHAccountProvisionerProvider {

    @Override
    public String getName() {
        return "TestSSHAccountProvisioner";
    }

    @Override
    public List<ConfigParam> getConfigParams() {
        List<ConfigParam> configParams = new ArrayList<>();
        configParams.add(new ConfigParam("ldaphost")
                .setDescription("Hostname of LDAP server")
                .setOptional(false)
                .setType(ConfigParam.ConfigParamType.STRING));
        configParams.add(new ConfigParam("ldapport")
                .setDescription("Port of LDAP server")
                .setOptional(false)
                .setType(ConfigParam.ConfigParamType.STRING));
        configParams.add(new ConfigParam("ldap_username")
                .setDescription("Username for LDAP server")
                .setOptional(false)
                .setType(ConfigParam.ConfigParamType.STRING));
        configParams.add(new ConfigParam("ldap_password")
                .setDescription("Password for LDAP server")
                .setOptional(false)
                .setType(ConfigParam.ConfigParamType.CRED_STORE_PASSWORD_TOKEN));
        configParams.add(new ConfigParam("ldapBaseDN")
                .setDescription( "Base DN for the ldap entry" )
                .setOptional( false )
                .setType( ConfigParam.ConfigParamType.STRING ));
        return configParams;
    }

    @Override
    public SSHAccountProvisioner createSSHAccountProvisioner(Map<ConfigParam, String> config) {
        SSHAccountProvisioner sshAccountProvisioner = new TestSSHAccountProvisioner();
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
