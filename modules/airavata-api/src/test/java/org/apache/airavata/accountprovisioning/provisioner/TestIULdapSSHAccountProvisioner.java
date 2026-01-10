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
package org.apache.airavata.accountprovisioning.provisioner;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.accountprovisioning.ConfigParam;
import org.apache.airavata.accountprovisioning.InvalidUsernameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for IU LDAP SSH Account Provisioner.
 * 
 * This test requires external infrastructure:
 * - SSH tunnel to server with firewall access to bazooka.hps.iu.edu
 * - LDAP_PASSWORD environment variable set
 * 
 * To run:
 * 1. Set up SSH tunnel: ssh airavata@apidev.scigap.org -L 9000:bazooka.hps.iu.edu:636 -N &
 * 2. Add to /etc/hosts: 127.0.0.1 bazooka.hps.iu.edu
 * 3. Set LDAP_PASSWORD environment variable
 * 4. Run: mvn test -Dtest=TestIULdapSSHAccountProvisioner
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "LDAP_PASSWORD", matches = ".+", disabledReason = "Requires LDAP_PASSWORD env var and SSH tunnel setup")
public class TestIULdapSSHAccountProvisioner {
    private static final Logger logger = LoggerFactory.getLogger(TestIULdapSSHAccountProvisioner.class);
    private IULdapSSHAccountProvisioner sshAccountProvisioner;
    private Map<ConfigParam, String> config;

    @BeforeEach
    public void setUp() throws InvalidUsernameException {
        sshAccountProvisioner = new IULdapSSHAccountProvisioner();
        config = new HashMap<>();
        // Create SSH tunnel to server that has firewall access to bazooka:
        //   ssh airavata@apidev.scigap.org -L 9000:bazooka.hps.iu.edu:636 -N &
        // Put entry in /etc/hosts with the following
        //   127.0.0.1	bazooka.hps.iu.edu
        config.put(IULdapSSHAccountProvisionerProvider.LDAP_HOST, "bazooka.hps.iu.edu");
        config.put(IULdapSSHAccountProvisionerProvider.LDAP_PORT, "9000"); // ssh tunnel port
        config.put(IULdapSSHAccountProvisionerProvider.LDAP_USERNAME, "cn=sgrcusr,dc=rt,dc=iu,dc=edu");
        // Note: LDAP password should be provided via environment variable or test properties
        config.put(IULdapSSHAccountProvisionerProvider.LDAP_PASSWORD, System.getenv("LDAP_PASSWORD"));
        config.put(IULdapSSHAccountProvisionerProvider.LDAP_BASE_DN, "ou=bigred2-sgrc,dc=rt,dc=iu,dc=edu");
        config.put(
                IULdapSSHAccountProvisionerProvider.CANONICAL_SCRATCH_LOCATION,
                "/N/dc2/scratch/${username}/iu-gateway");
        config.put(
                IULdapSSHAccountProvisionerProvider.CYBERGATEWAY_GROUP_DN,
                "cn=cybergateway,ou=Group,dc=rt,dc=iu,dc=edu");
        sshAccountProvisioner.init(config);
    }

    @Test
    public void testAccountProvisioningOperations() throws InvalidUsernameException {
        String userId = "machrist@iu.edu";

        // Test hasAccount
        boolean hasAccount = sshAccountProvisioner.hasAccount(userId);
        logger.info("hasAccount={}", hasAccount);

        // Test getScratchLocation
        String scratchLocation = sshAccountProvisioner.getScratchLocation(userId);
        logger.info("scratchLocation={}", scratchLocation);
        assertNotNull(scratchLocation, "Scratch location should not be null");

        // Test SSH key provisioning
        String sshPublicKey = "foobar12345";
        boolean sshAccountProvisioningComplete =
                sshAccountProvisioner.isSSHAccountProvisioningComplete(userId, sshPublicKey);
        logger.info("isSSHAccountProvisioningComplete={}", sshAccountProvisioningComplete);

        if (!sshAccountProvisioningComplete) {
            sshAccountProvisioner.installSSHKey(userId, sshPublicKey);
            sshAccountProvisioningComplete =
                    sshAccountProvisioner.isSSHAccountProvisioningComplete(userId, sshPublicKey);
            logger.info("isSSHAccountProvisioningComplete after install={}", sshAccountProvisioningComplete);
            assertTrue(sshAccountProvisioningComplete, "SSH key provisioning should be complete after installation");
        }
    }
}
