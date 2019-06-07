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
package org.apache.airavata.accountprovisioning;

import org.apache.airavata.accountprovisioning.provisioner.TestSSHAccountProvisioner;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SSHAccountProvisionerFactoryTest {

    @Test
    public void testGetSSHAccountProvisionerImplementationNames() {

        List<SSHAccountProvisionerProvider> sshAccountProvisionerProviders = SSHAccountProvisionerFactory.getSSHAccountProvisionerProviders();
        List<String> sshAccountProvisionerNames = sshAccountProvisionerProviders.stream().map(SSHAccountProvisionerProvider::getName).collect(Collectors.toList());
        Assert.assertTrue("names should contain TestSSHAccountProvisioner", sshAccountProvisionerNames.contains("TestSSHAccountProvisioner"));
    }

    @Test
    public void testGetSSHAccountProvisionerConfigParams() {

        List<ConfigParam> configParams = SSHAccountProvisionerFactory.getSSHAccountProvisionerConfigParams("TestSSHAccountProvisioner");
        Assert.assertEquals(5, configParams.size());
        ConfigParam ldaphost = configParams.get(0);
        Assert.assertEquals("ldaphost", ldaphost.getName());
        Assert.assertEquals(ConfigParam.ConfigParamType.STRING, ldaphost.getType());
        ConfigParam ldapport = configParams.get(1);
        Assert.assertEquals("ldapport", ldapport.getName());
        Assert.assertEquals(ConfigParam.ConfigParamType.STRING, ldapport.getType());
        ConfigParam ldapUsername = configParams.get(2);
        Assert.assertEquals("ldap_username", ldapUsername.getName());
        Assert.assertEquals(ConfigParam.ConfigParamType.STRING, ldapUsername.getType());
        ConfigParam ldapPassword = configParams.get(3);
        Assert.assertEquals("ldap_password", ldapPassword.getName());
        Assert.assertEquals(ConfigParam.ConfigParamType.CRED_STORE_PASSWORD_TOKEN, ldapPassword.getType());
    }

    @Test
    public void testCreateSSHAccountProvisioner() {

        Map<ConfigParam, String> config = new HashMap<>();
        ConfigParam test1 = new ConfigParam("test1");
        config.put(test1, "value1");
        ConfigParam test2 = new ConfigParam("test2");
        config.put(test2, "value2");
        ConfigParam test3 = new ConfigParam("test3");
        config.put(test3, "value3");
        TestSSHAccountProvisioner sshAccountProvisioner = (TestSSHAccountProvisioner) SSHAccountProvisionerFactory.createSSHAccountProvisioner("TestSSHAccountProvisioner", config);
        // Make sure all of the config params and values were passed to SSHAccountProvisioner
        Assert.assertTrue(sshAccountProvisioner.getConfig().containsKey(test1));
        Assert.assertTrue(sshAccountProvisioner.getConfig().containsKey(test2));
        Assert.assertTrue(sshAccountProvisioner.getConfig().containsKey(test3));
    }
}
