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
package org.apache.airavata.registry.repositories.appcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.registry.exceptions.AppCatalogException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.registry.services.GwyResourceProfileService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {org.apache.airavata.config.JpaConfig.class})
@TestPropertySource(locations = "classpath:airavata.properties")
public class GatewayProfileRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(GatewayProfileRepositoryTest.class);

    private final GwyResourceProfileService gwyResourceProfileService;
    private final ComputeResourceService computeResourceService;
    private final AiravataServerProperties properties;

    public GatewayProfileRepositoryTest(
            GwyResourceProfileService gwyResourceProfileService,
            ComputeResourceService computeResourceService,
            AiravataServerProperties properties) {
        super(Database.APP_CATALOG);
        this.gwyResourceProfileService = gwyResourceProfileService;
        this.computeResourceService = computeResourceService;
        this.properties = properties;
    }

    @Test
    public void gatewayProfileRepositorytest() throws AppCatalogException, ApplicationSettingsException {

        // Verify that the default Gateway Resource Profile exists already
        List<GatewayResourceProfile> defaultGatewayResourceProfileList =
                this.gwyResourceProfileService.getAllGatewayProfiles();
        assertEquals(1, defaultGatewayResourceProfileList.size());
        assertEquals(
                properties.services.default_.gateway,
                defaultGatewayResourceProfileList.get(0).getGatewayID());

        GatewayResourceProfile gf = new GatewayResourceProfile();
        ComputeResourceDescription cm1 = new ComputeResourceDescription();
        cm1.setHostName("localhost");
        cm1.setResourceDescription("test compute host");
        String hostId1 = computeResourceService.addComputeResource(cm1);

        ComputeResourceDescription cm2 = new ComputeResourceDescription();
        cm2.setHostName("localhost");
        cm2.setResourceDescription("test compute host");
        String hostId2 = computeResourceService.addComputeResource(cm2);

        ComputeResourcePreference preference1 = new ComputeResourcePreference();
        preference1.setComputeResourceId(hostId1);
        preference1.setOverridebyAiravata(true);
        preference1.setPreferredJobSubmissionProtocol(JobSubmissionProtocol.SSH);
        preference1.setPreferredDataMovementProtocol(DataMovementProtocol.SCP);
        preference1.setPreferredBatchQueue("queue1");
        preference1.setScratchLocation("/tmp");
        preference1.setAllocationProjectNumber("project1");

        Map<String, String> sshConfig = new HashMap<>();
        sshConfig.put("ANYTEST", "check");
        preference1.setSshAccountProvisionerConfig(sshConfig);

        ComputeResourcePreference preference2 = new ComputeResourcePreference();
        preference2.setComputeResourceId(hostId2);
        preference2.setOverridebyAiravata(false);
        preference2.setPreferredJobSubmissionProtocol(JobSubmissionProtocol.LOCAL);
        preference2.setPreferredDataMovementProtocol(DataMovementProtocol.GridFTP);
        preference2.setPreferredBatchQueue("queue2");
        preference2.setScratchLocation("/tmp");
        preference2.setAllocationProjectNumber("project2");

        List<ComputeResourcePreference> list = new ArrayList<ComputeResourcePreference>();
        list.add(preference1);
        list.add(preference2);

        gf.setGatewayID("testGateway");
        gf.setCredentialStoreToken("testCredential");
        gf.setIdentityServerPwdCredToken("pwdCredential");
        gf.setIdentityServerTenant("testTenant");
        gf.setComputeResourcePreferences(list);

        GatewayResourceProfile gf1 = new GatewayResourceProfile();
        gf1.setGatewayID("testGateway1");
        gf1.setCredentialStoreToken("testCredential");
        gf1.setIdentityServerPwdCredToken("pwdCredential");
        gf1.setIdentityServerTenant("testTenant");

        String gwId = gwyResourceProfileService.addGatewayResourceProfile(gf);
        GatewayResourceProfile retrievedProfile = null;
        if (gwyResourceProfileService.isGatewayResourceProfileExists(gwId)) {
            retrievedProfile = gwyResourceProfileService.getGatewayProfile(gwId);
            logger.info("************ gateway id ************** :{}", retrievedProfile.getGatewayID());
            assertTrue(retrievedProfile.getGatewayID().equals("testGateway"), "Retrieved gateway id matched");
            assertTrue(retrievedProfile.getCredentialStoreToken().equals("testCredential"));
            assertTrue(retrievedProfile.getIdentityServerPwdCredToken().equals("pwdCredential"));
            assertTrue(retrievedProfile.getIdentityServerTenant().equals("testTenant"));
        }

        gwyResourceProfileService.addGatewayResourceProfile(gf1);
        List<GatewayResourceProfile> getGatewayResourceList = gwyResourceProfileService.getAllGatewayProfiles();
        assertEquals(3, getGatewayResourceList.size(), "should be 3 gateway profiles (1 default and 2 just added)");

        List<ComputeResourcePreference> preferences = gwyResourceProfileService.getAllComputeResourcePreferences(gwId);
        logger.info("compute preferences size : {}", preferences.size());
        assertTrue(preferences.size() == 2);
        if (preferences != null && !preferences.isEmpty()) {
            ComputeResourcePreference pref1 = preferences.stream()
                    .filter(p -> p.getComputeResourceId().equals(hostId1))
                    .findFirst()
                    .get();
            assertTrue(pref1.isOverridebyAiravata());
            ComputeResourcePreference pref2 = preferences.stream()
                    .filter(p -> p.getComputeResourceId().equals(hostId2))
                    .findFirst()
                    .get();
            assertFalse(pref2.isOverridebyAiravata());
            for (ComputeResourcePreference cm : preferences) {
                logger.info("******** host id ********* : {}", cm.getComputeResourceId());
                logger.info("Preferred Batch Queue: {}", cm.getPreferredBatchQueue());
                logger.info("Preferred Data Movement Protocol: {}", cm.getPreferredDataMovementProtocol());
                logger.info("Preferred Job Submission Protocol: {}", cm.getPreferredJobSubmissionProtocol());
            }
        }
        computeResourceService.removeComputeResource(hostId1);
        computeResourceService.removeComputeResource(hostId2);
        gwyResourceProfileService.removeGatewayResourceProfile("testGateway");
        gwyResourceProfileService.removeGatewayResourceProfile("testGateway1");
    }
}
