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
package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GatewayProfileRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(GatewayProfileRepositoryTest.class);

    private GwyResourceProfileRepository gwyResourceProfileRepository;

    public GatewayProfileRepositoryTest() {
        super(Database.APP_CATALOG);
        gwyResourceProfileRepository = new GwyResourceProfileRepository();
    }

    @Test
    public void gatewayProfileRepositorytest() throws AppCatalogException, ApplicationSettingsException {

        // Verify that the default Gateway Resource Profile exists already
        List<GatewayResourceProfile> defaultGatewayResourceProfileList = this.gwyResourceProfileRepository
                .getAllGatewayProfiles();
        assertEquals(1, defaultGatewayResourceProfileList.size());
        assertEquals(ServerSettings.getDefaultUserGateway(), defaultGatewayResourceProfileList.get(0).getGatewayID());

        GatewayResourceProfile gf = new GatewayResourceProfile();
        ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
        ComputeResourceDescription cm1 = new ComputeResourceDescription();
        cm1.setHostName("localhost");
        cm1.setResourceDescription("test compute host");
        String hostId1 = computeResourceRepository.addComputeResource(cm1);

        ComputeResourceDescription cm2 = new ComputeResourceDescription();
        cm2.setHostName("localhost");
        cm2.setResourceDescription("test compute host");
        String hostId2 = computeResourceRepository.addComputeResource(cm2);

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

        String gwId = gwyResourceProfileRepository.addGatewayResourceProfile(gf);
        GatewayResourceProfile retrievedProfile = null;
        if (gwyResourceProfileRepository.isExists(gwId)) {
            retrievedProfile = gwyResourceProfileRepository.getGatewayProfile(gwId);
            System.out.println("************ gateway id ************** :" + retrievedProfile.getGatewayID());
            assertTrue("Retrieved gateway id matched", retrievedProfile.getGatewayID().equals("testGateway"));
            assertTrue(retrievedProfile.getCredentialStoreToken().equals("testCredential"));
            assertTrue(retrievedProfile.getIdentityServerPwdCredToken().equals("pwdCredential"));
            assertTrue(retrievedProfile.getIdentityServerTenant().equals("testTenant"));
        }

        gwyResourceProfileRepository.addGatewayResourceProfile(gf1);
        List<GatewayResourceProfile> getGatewayResourceList = gwyResourceProfileRepository.getAllGatewayProfiles();
        assertEquals("should be 3 gateway profiles (1 default and 2 just added)", 3, getGatewayResourceList.size());

        List<ComputeResourcePreference> preferences = gwyResourceProfileRepository
                .getAllComputeResourcePreferences(gwId);
        System.out.println("compute preferences size : " + preferences.size());
        assertTrue(preferences.size() == 2);
        if (preferences != null && !preferences.isEmpty()) {
            ComputeResourcePreference pref1 = preferences.stream().filter(p -> p.getComputeResourceId().equals(hostId1)).findFirst().get();
            assertTrue(pref1.isOverridebyAiravata());
            ComputeResourcePreference pref2 = preferences.stream().filter(p -> p.getComputeResourceId().equals(hostId2)).findFirst().get();
            assertFalse(pref2.isOverridebyAiravata());
            for (ComputeResourcePreference cm : preferences) {
                System.out.println("******** host id ********* : " + cm.getComputeResourceId());
                System.out.println(cm.getPreferredBatchQueue());
                System.out.println(cm.getPreferredDataMovementProtocol());
                System.out.println(cm.getPreferredJobSubmissionProtocol());
            }
        }
        computeResourceRepository.removeComputeResource(hostId1);
        computeResourceRepository.removeComputeResource(hostId2);
        gwyResourceProfileRepository.delete("testGateway");
        gwyResourceProfileRepository.delete("testGateway1");
    }

}
