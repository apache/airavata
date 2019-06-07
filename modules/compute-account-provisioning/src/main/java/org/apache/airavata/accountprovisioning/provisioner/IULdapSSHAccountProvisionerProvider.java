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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class IULdapSSHAccountProvisionerProvider implements SSHAccountProvisionerProvider {

    public static final ConfigParam LDAP_HOST = new ConfigParam("ldap-host")
            .setDescription("Hostname of LDAP server")
            .setOptional(false)
            .setType(ConfigParam.ConfigParamType.STRING);
    public static final ConfigParam LDAP_PORT = new ConfigParam("ldap-port")
            .setDescription("Port of LDAP server")
            .setOptional(false)
            .setType(ConfigParam.ConfigParamType.STRING);
    public static final ConfigParam LDAP_USERNAME = new ConfigParam("ldap-username")
            .setDescription("Username for LDAP server")
            .setOptional(false)
            .setType(ConfigParam.ConfigParamType.STRING);
    public static final ConfigParam LDAP_PASSWORD = new ConfigParam("ldap-password")
            .setDescription("Password for LDAP server")
            .setOptional(false)
            .setType(ConfigParam.ConfigParamType.CRED_STORE_PASSWORD_TOKEN);
    public static final ConfigParam LDAP_BASE_DN = new ConfigParam("ldap-base-dn")
            .setDescription("Base DN for searching, modifying cluster LDAP")
            .setOptional(false)
            .setType(ConfigParam.ConfigParamType.STRING);
    public static final ConfigParam CANONICAL_SCRATCH_LOCATION = new ConfigParam("canonical-scratch-location")
            .setDescription("Pattern for scratch location. Use ${username} as replacement for username. For example, '/N/dc2/scratch/${username}/iu-gateway'.")
            .setOptional(false)
            .setType(ConfigParam.ConfigParamType.STRING);
    public static final ConfigParam CYBERGATEWAY_GROUP_DN = new ConfigParam("cybergateway-group-dn")
            .setDescription("Cybergateway group DN")
            .setOptional(false)
            .setType(ConfigParam.ConfigParamType.STRING);
    public static final List<ConfigParam> CONFIG_PARAMS = Arrays.asList(LDAP_HOST, LDAP_PORT, LDAP_USERNAME, LDAP_PASSWORD, LDAP_BASE_DN, CANONICAL_SCRATCH_LOCATION, CYBERGATEWAY_GROUP_DN);

    @Override
    public List<ConfigParam> getConfigParams() {
        return CONFIG_PARAMS;
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
