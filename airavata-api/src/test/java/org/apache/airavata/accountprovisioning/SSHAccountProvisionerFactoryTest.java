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
package org.apache.airavata.accountprovisioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.accountprovisioning.provisioner.TestSSHAccountProvisioner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, SSHAccountProvisionerFactoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
public class SSHAccountProvisionerFactoryTest {

    public SSHAccountProvisionerFactoryTest() {
        // Spring Boot test - no dependencies to inject for this utility test
    }

    @Test
    public void testGetSSHAccountProvisionerImplementationNames() {

        List<SSHAccountProvisionerProvider> sshAccountProvisionerProviders =
                SSHAccountProvisionerFactory.getSSHAccountProvisionerProviders();
        List<String> sshAccountProvisionerNames = sshAccountProvisionerProviders.stream()
                .map(SSHAccountProvisionerProvider::getName)
                .collect(Collectors.toList());
        Assertions.assertTrue(
                sshAccountProvisionerNames.contains("TestSSHAccountProvisioner"),
                "names should contain TestSSHAccountProvisioner");
    }

    @Test
    public void testGetSSHAccountProvisionerConfigParams() {

        List<ConfigParam> configParams =
                SSHAccountProvisionerFactory.getSSHAccountProvisionerConfigParams("TestSSHAccountProvisioner");
        Assertions.assertEquals(5, configParams.size());
        ConfigParam ldaphost = configParams.get(0);
        Assertions.assertEquals("ldaphost", ldaphost.getName());
        Assertions.assertEquals(ConfigParam.ConfigParamType.STRING, ldaphost.getType());
        ConfigParam ldapport = configParams.get(1);
        Assertions.assertEquals("ldapport", ldapport.getName());
        Assertions.assertEquals(ConfigParam.ConfigParamType.STRING, ldapport.getType());
        ConfigParam ldapUsername = configParams.get(2);
        Assertions.assertEquals("ldap_username", ldapUsername.getName());
        Assertions.assertEquals(ConfigParam.ConfigParamType.STRING, ldapUsername.getType());
        ConfigParam ldapPassword = configParams.get(3);
        Assertions.assertEquals("ldap_password", ldapPassword.getName());
        Assertions.assertEquals(ConfigParam.ConfigParamType.CRED_STORE_PASSWORD_TOKEN, ldapPassword.getType());
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
        TestSSHAccountProvisioner sshAccountProvisioner = (TestSSHAccountProvisioner)
                SSHAccountProvisionerFactory.createSSHAccountProvisioner("TestSSHAccountProvisioner", config);
        // Make sure all of the config params and values were passed to SSHAccountProvisioner
        Assertions.assertTrue(sshAccountProvisioner.getConfig().containsKey(test1));
        Assertions.assertTrue(sshAccountProvisioner.getConfig().containsKey(test2));
        Assertions.assertTrue(sshAccountProvisioner.getConfig().containsKey(test3));
    }

    @org.springframework.context.annotation.Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.accountprovisioning",
                "org.apache.airavata.config"
            },
            excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.config.ThriftServerLauncher.class
                        })
            })
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {}
}
