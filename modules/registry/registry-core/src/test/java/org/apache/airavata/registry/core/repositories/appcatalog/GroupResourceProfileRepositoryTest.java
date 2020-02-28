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

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.groupresourceprofile.*;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GroupResourceProfileRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ComputeResourceRepository.class);

    private ComputeResourceRepository computeResourceRepository;
    private GroupResourceProfileRepository groupResourceProfileRepository;
    private String gatewayId = "TEST_GATEWAY";
    private String groupResourceProfileId = null;
    private String resourceId1 = null;
    private String resourceId2 = null;

    private final String QUEUE1_NAME = "queue1";
    private final String QUEUE2_NAME = "queue2";

    public GroupResourceProfileRepositoryTest() {
        super(Database.APP_CATALOG);
        computeResourceRepository = new ComputeResourceRepository();
        groupResourceProfileRepository = new GroupResourceProfileRepository();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ComputeResourceDescription description = new ComputeResourceDescription();

        description.setHostName("localhost");
        description.setResourceDescription("test compute resource");
        List<String> ipdaresses = new ArrayList<String>();
        ipdaresses.add("222.33.43.444");
        ipdaresses.add("23.344.44.454");
        description.setIpAddresses(ipdaresses);

        BatchQueue batchQueue1 = new BatchQueue();
        batchQueue1.setQueueName(QUEUE1_NAME);
        batchQueue1.setQueueDescription("que1Desc1");
        batchQueue1.setMaxRunTime(10);
        batchQueue1.setMaxNodes(4);
        batchQueue1.setMaxJobsInQueue(1);

        BatchQueue batchQueue2 = new BatchQueue();
        batchQueue2.setQueueName(QUEUE2_NAME);
        batchQueue2.setQueueDescription("que1Desc2");
        batchQueue2.setMaxRunTime(10);
        batchQueue2.setMaxNodes(4);
        batchQueue2.setMaxJobsInQueue(1);

        List<BatchQueue> batchQueueList = new ArrayList<BatchQueue>();
        batchQueueList.add(batchQueue1);
        batchQueueList.add(batchQueue2);
        description.setBatchQueues(batchQueueList);

        this.resourceId1 = computeResourceRepository.addComputeResource(description);

        ComputeResourceDescription cm2 = new ComputeResourceDescription();
        cm2.setHostName("localhost2");
        cm2.setResourceDescription("test compute host");

        BatchQueue cm_batchQueue1 = new BatchQueue();
        cm_batchQueue1.setQueueName("cmqueue1");
        cm_batchQueue1.setQueueDescription("cmque1Desc1");
        cm_batchQueue1.setMaxRunTime(10);
        cm_batchQueue1.setMaxNodes(4);
        cm_batchQueue1.setMaxJobsInQueue(1);

        BatchQueue cm_batchQueue2 = new BatchQueue();
        cm_batchQueue2.setQueueName("cmqueue2");
        cm_batchQueue2.setQueueDescription("cmque1Desc2");
        cm_batchQueue2.setMaxRunTime(10);
        cm_batchQueue2.setMaxNodes(4);
        cm_batchQueue2.setMaxJobsInQueue(1);

        List<BatchQueue> cmbatchQueueList = new ArrayList<BatchQueue>();
        cmbatchQueueList.add(cm_batchQueue1);
        cmbatchQueueList.add(cm_batchQueue2);
        cm2.setBatchQueues(cmbatchQueueList);

        this.resourceId2 = computeResourceRepository.addComputeResource(cm2);
    }

    @Test
    public void GroupResourceProfileRepositoryTest() throws AppCatalogException {

        GroupResourceProfile groupResourceProfile = new GroupResourceProfile();
        groupResourceProfile.setGatewayId(gatewayId);
        groupResourceProfile.setGroupResourceProfileName("TEST_GROUP_PROFILE_NAME");
        groupResourceProfile.setDefaultCredentialStoreToken("test-cred-store-token");

        GroupAccountSSHProvisionerConfig groupAccountSSHProvisionerConfig = new GroupAccountSSHProvisionerConfig();
        groupAccountSSHProvisionerConfig.setResourceId(resourceId1);
        groupAccountSSHProvisionerConfig.setConfigName("configName");
        groupAccountSSHProvisionerConfig.setConfigValue("configvalue");

        ComputeResourceReservation reservation1 = new ComputeResourceReservation();
        reservation1.setReservationName("test-reservation1");
        reservation1.setStartTime(AiravataUtils.getCurrentTimestamp().getTime());
        reservation1.setEndTime(AiravataUtils.getCurrentTimestamp().getTime() + 100000);
        reservation1.addToQueueNames(QUEUE1_NAME);
        reservation1.addToQueueNames(QUEUE2_NAME);

        ComputeResourceReservation reservation2 = new ComputeResourceReservation();
        reservation2.setReservationName("test-reservation2");
        reservation2.setStartTime(AiravataUtils.getCurrentTimestamp().getTime() + 200000);
        reservation2.setEndTime(AiravataUtils.getCurrentTimestamp().getTime() + 300000);
        reservation2.addToQueueNames(QUEUE1_NAME);

        GroupComputeResourcePreference groupComputeResourcePreference1 = new GroupComputeResourcePreference();
        groupComputeResourcePreference1.setComputeResourceId(resourceId1);
        groupComputeResourcePreference1.addToGroupSSHAccountProvisionerConfigs(groupAccountSSHProvisionerConfig);
        groupComputeResourcePreference1.addToReservations(reservation1);
        groupComputeResourcePreference1.addToReservations(reservation2);

        GroupComputeResourcePreference groupComputeResourcePreference2 = new GroupComputeResourcePreference();
        groupComputeResourcePreference2.setComputeResourceId(resourceId2);

        List<GroupComputeResourcePreference> groupComputeResourcePreferenceList = new ArrayList<>();
        groupComputeResourcePreferenceList.add(groupComputeResourcePreference1);
        groupComputeResourcePreferenceList.add(groupComputeResourcePreference2);

        groupResourceProfile.setComputePreferences(groupComputeResourcePreferenceList);

        ComputeResourcePolicy computeResourcePolicy = new ComputeResourcePolicy();
        computeResourcePolicy.setComputeResourceId(resourceId1);
        computeResourcePolicy.addToAllowedBatchQueues("queue1");

        ComputeResourcePolicy computeResourcePolicy2 = new ComputeResourcePolicy();
        computeResourcePolicy2.setComputeResourceId(resourceId2);
        computeResourcePolicy2.addToAllowedBatchQueues("cmqueue1");

        List<ComputeResourcePolicy> computeResourcePolicyList =  new ArrayList<>();
        computeResourcePolicyList.add(computeResourcePolicy);
        computeResourcePolicyList.add(computeResourcePolicy2);

        groupResourceProfile.setComputeResourcePolicies(computeResourcePolicyList);

        BatchQueueResourcePolicy batchQueueResourcePolicy = new BatchQueueResourcePolicy();
        batchQueueResourcePolicy.setComputeResourceId(resourceId1);
        batchQueueResourcePolicy.setQueuename("queue1");
        batchQueueResourcePolicy.setMaxAllowedCores(2);
        batchQueueResourcePolicy.setMaxAllowedWalltime(10);

        BatchQueueResourcePolicy batchQueueResourcePolicy2 = new BatchQueueResourcePolicy();
        batchQueueResourcePolicy2.setComputeResourceId(resourceId2);
        batchQueueResourcePolicy2.setQueuename("cmqueue1");
        batchQueueResourcePolicy2.setMaxAllowedCores(3);
        batchQueueResourcePolicy2.setMaxAllowedWalltime(12);

        List<BatchQueueResourcePolicy> batchQueueResourcePolicyList = new ArrayList<>();
        batchQueueResourcePolicyList.add(batchQueueResourcePolicy);
        batchQueueResourcePolicyList.add(batchQueueResourcePolicy2);

        groupResourceProfile.setBatchQueueResourcePolicies(batchQueueResourcePolicyList);

        groupResourceProfileId = groupResourceProfileRepository.addGroupResourceProfile(groupResourceProfile);

        String computeResourcePolicyId1 = null;
        String batchQueueResourcePolicyId2 = null;
        if (groupResourceProfileRepository.isGroupResourceProfileExists(groupResourceProfileId)) {
            GroupResourceProfile getGroupResourceProfile = groupResourceProfileRepository.getGroupResourceProfile(groupResourceProfileId);

            assertTrue(getGroupResourceProfile.getGatewayId().equals(gatewayId));
            assertTrue(getGroupResourceProfile.getGroupResourceProfileId().equals(groupResourceProfileId));
            assertEquals("test-cred-store-token", getGroupResourceProfile.getDefaultCredentialStoreToken());

            assertTrue(getGroupResourceProfile.getComputePreferences().size() == 2);
            assertTrue(getGroupResourceProfile.getComputeResourcePolicies().size() == 2);
            assertTrue(getGroupResourceProfile.getBatchQueueResourcePolicies().size() == 2);
            computeResourcePolicyId1 = getGroupResourceProfile.getComputeResourcePolicies()
                    .stream()
                    .filter(crp -> crp.getComputeResourceId().equals(resourceId1))
                    .map(crp -> crp.getResourcePolicyId())
                    .findFirst()
                    .get();
            batchQueueResourcePolicyId2 = getGroupResourceProfile.getBatchQueueResourcePolicies()
                    .stream()
                    .filter(bqrp -> bqrp.getComputeResourceId().equals(resourceId2))
                    .map(bqrp -> bqrp.getResourcePolicyId())
                    .findFirst()
                    .get();
        }

        assertTrue(groupResourceProfileRepository.getGroupComputeResourcePreference(resourceId1,groupResourceProfileId) != null);
        assertTrue(groupResourceProfileRepository.getGroupComputeResourcePreference(resourceId1,groupResourceProfileId).getGroupSSHAccountProvisionerConfigs().size() == 1);
        // verify reservation1
        assertEquals(2, groupResourceProfileRepository.getGroupComputeResourcePreference(resourceId1,groupResourceProfileId).getReservations().size());
        ComputeResourceReservation retrievedReservation1 = groupResourceProfileRepository.getGroupComputeResourcePreference(resourceId1, groupResourceProfileId).getReservations().get(0);
        assertEquals(reservation1.getReservationName(), retrievedReservation1.getReservationName());
        assertEquals(reservation1.getStartTime(), retrievedReservation1.getStartTime());
        assertEquals(reservation1.getEndTime(), retrievedReservation1.getEndTime());

        ComputeResourcePolicy getComputeResourcePolicy = groupResourceProfileRepository.getComputeResourcePolicy(computeResourcePolicyId1);
        assertTrue(getComputeResourcePolicy.getAllowedBatchQueues().get(0).equals("queue1"));

        BatchQueueResourcePolicy getBatchQueuePolicy = groupResourceProfileRepository.getBatchQueueResourcePolicy(batchQueueResourcePolicyId2);
        assertTrue(getBatchQueuePolicy != null);
        assertTrue(getBatchQueuePolicy.getMaxAllowedCores() == 3);
        assertTrue(getBatchQueuePolicy.getMaxAllowedWalltime() == 12);

        assertTrue(groupResourceProfileRepository.getAllGroupResourceProfiles(gatewayId, null).size() == 0);
        assertTrue(groupResourceProfileRepository.getAllGroupResourceProfiles(gatewayId, Collections.emptyList()).size() == 0);
        assertTrue(groupResourceProfileRepository.getAllGroupComputeResourcePreferences(groupResourceProfileId).size() == 2);
        assertTrue(groupResourceProfileRepository.getAllGroupComputeResourcePolicies(groupResourceProfileId).size() == 2);
        assertTrue(groupResourceProfileRepository.getAllGroupBatchQueueResourcePolicies(groupResourceProfileId).size() == 2);

        // AIRAVATA-2872 Test setting resourceSpecificCredentialStoreToken to a value and then changing it to null
        GroupResourceProfile retrievedGroupResourceProfile = groupResourceProfileRepository.getGroupResourceProfile(groupResourceProfileId);
        GroupComputeResourcePreference retrievedGroupComputeResourcePreference = retrievedGroupResourceProfile.getComputePreferences().stream()
                .filter(pref -> pref.getComputeResourceId().equals(resourceId1))
                .findFirst()
                .get();
        assertNull(retrievedGroupComputeResourcePreference.getResourceSpecificCredentialStoreToken());
        retrievedGroupComputeResourcePreference.setResourceSpecificCredentialStoreToken("abc123");
        groupResourceProfileRepository.updateGroupResourceProfile(retrievedGroupResourceProfile);

        GroupResourceProfile retrievedGroupResourceProfile2 = groupResourceProfileRepository.getGroupResourceProfile(groupResourceProfileId);
        GroupComputeResourcePreference retrievedGroupComputeResourcePreference2 = retrievedGroupResourceProfile2.getComputePreferences().stream()
                .filter(pref -> pref.getComputeResourceId().equals(resourceId1))
                .findFirst()
                .get();
        assertEquals("abc123", retrievedGroupComputeResourcePreference2.getResourceSpecificCredentialStoreToken());
        retrievedGroupComputeResourcePreference2.setResourceSpecificCredentialStoreToken(null);
        assertNull(retrievedGroupComputeResourcePreference2.getResourceSpecificCredentialStoreToken());
        groupResourceProfileRepository.updateGroupResourceProfile(retrievedGroupResourceProfile2);

        GroupResourceProfile retrievedGroupResourceProfile3 = groupResourceProfileRepository.getGroupResourceProfile(groupResourceProfileId);
        GroupComputeResourcePreference retrievedGroupComputeResourcePreference3 = retrievedGroupResourceProfile3.getComputePreferences().stream()
                .filter(pref -> pref.getComputeResourceId().equals(resourceId1))
                .findFirst()
                .get();
        assertNull(retrievedGroupComputeResourcePreference3.getResourceSpecificCredentialStoreToken());

        // Orphan removal test
        assertEquals(2, retrievedGroupResourceProfile3.getComputePreferencesSize());
        retrievedGroupResourceProfile3.setComputePreferences(retrievedGroupResourceProfile3.getComputePreferences().subList(0, 1));
        groupResourceProfileRepository.updateGroupResourceProfile(retrievedGroupResourceProfile3);
        GroupResourceProfile retrievedGroupResourceProfile4 = groupResourceProfileRepository.getGroupResourceProfile(groupResourceProfileId);
        assertEquals(1, retrievedGroupResourceProfile4.getComputePreferencesSize());

        groupResourceProfileRepository.removeGroupResourceProfile(groupResourceProfileId);

    }

    @Test
    public void testUpdatingGroupResourceProfileWithoutCreationTime() throws AppCatalogException {
        GroupResourceProfile groupResourceProfile = new GroupResourceProfile();
        groupResourceProfile.setGatewayId(gatewayId);
        groupResourceProfile.setGroupResourceProfileName("TEST_GROUP_PROFILE_NAME");
        groupResourceProfile.setDefaultCredentialStoreToken("test-cred-store-token");

        // Simulate what is like for a client that only gets back the id from
        // the create operation but not any fields, like creation time, that are
        // populated by the create operation
        GroupResourceProfile cloneGroupResourceProfile = groupResourceProfile.deepCopy();
        String groupResourceProfileId = groupResourceProfileRepository.addGroupResourceProfile(groupResourceProfile);
        long creationTime = groupResourceProfileRepository.getGroupResourceProfile(groupResourceProfileId).getCreationTime();
        cloneGroupResourceProfile.setGroupResourceProfileId(groupResourceProfileId);
        groupResourceProfileRepository.updateGroupResourceProfile(cloneGroupResourceProfile);
        long creationTimeAfterUpdate = groupResourceProfileRepository.getGroupResourceProfile(groupResourceProfileId).getCreationTime();
        Assert.assertEquals("creationTime should be the same after update", creationTime, creationTimeAfterUpdate);
    }

    @Test
    public void testRemovingReservation() throws AppCatalogException {

        GroupResourceProfile groupResourceProfile = new GroupResourceProfile();
        groupResourceProfile.setGatewayId(gatewayId);
        groupResourceProfile.setGroupResourceProfileName("TEST_GROUP_PROFILE_NAME");

        ComputeResourceReservation reservation1 = new ComputeResourceReservation();
        reservation1.setReservationName("test-reservation1");
        reservation1.setStartTime(AiravataUtils.getCurrentTimestamp().getTime());
        reservation1.setEndTime(AiravataUtils.getCurrentTimestamp().getTime() + 100000);
        reservation1.addToQueueNames(QUEUE1_NAME);
        reservation1.addToQueueNames(QUEUE2_NAME);

        ComputeResourceReservation reservation2 = new ComputeResourceReservation();
        reservation2.setReservationName("test-reservation2");
        reservation2.setStartTime(AiravataUtils.getCurrentTimestamp().getTime() + 200000);
        reservation2.setEndTime(AiravataUtils.getCurrentTimestamp().getTime() + 300000);
        reservation2.addToQueueNames(QUEUE1_NAME);

        GroupComputeResourcePreference groupComputeResourcePreference1 = new GroupComputeResourcePreference();
        groupComputeResourcePreference1.setComputeResourceId(resourceId1);
        groupComputeResourcePreference1.addToReservations(reservation1);
        groupComputeResourcePreference1.addToReservations(reservation2);

        groupResourceProfile.addToComputePreferences(groupComputeResourcePreference1);

        String groupResourceProfileId = groupResourceProfileRepository.addGroupResourceProfile(groupResourceProfile);

        // Remove one of the reservations
        {
            GroupResourceProfile retrievedGroupResourceProfile = groupResourceProfileRepository
                    .getGroupResourceProfile(groupResourceProfileId);
            List<ComputeResourceReservation> retrievedReservations = retrievedGroupResourceProfile
                    .getComputePreferences().get(0).getReservations();
            assertEquals(2, retrievedReservations.size());
            retrievedReservations.remove(1);

            groupResourceProfileRepository.updateGroupResourceProfile(retrievedGroupResourceProfile);
        }

        {
            GroupResourceProfile retrievedGroupResourceProfile = groupResourceProfileRepository
                    .getGroupResourceProfile(groupResourceProfileId);
            List<ComputeResourceReservation> retrievedReservations = retrievedGroupResourceProfile
                    .getComputePreferences().get(0).getReservations();
            assertEquals(1, retrievedReservations.size());
            assertEquals(reservation1.getReservationName(), retrievedReservations.get(0).getReservationName());
        }

    }

    @Test
    public void testUpdatingReservation() throws AppCatalogException {

        GroupResourceProfile groupResourceProfile = new GroupResourceProfile();
        groupResourceProfile.setGatewayId(gatewayId);
        groupResourceProfile.setGroupResourceProfileName("TEST_GROUP_PROFILE_NAME");

        ComputeResourceReservation reservation1 = new ComputeResourceReservation();
        reservation1.setReservationName("test-reservation1");
        reservation1.setStartTime(AiravataUtils.getCurrentTimestamp().getTime());
        reservation1.setEndTime(AiravataUtils.getCurrentTimestamp().getTime() + 100000);
        reservation1.addToQueueNames(QUEUE1_NAME);
        reservation1.addToQueueNames(QUEUE2_NAME);

        ComputeResourceReservation reservation2 = new ComputeResourceReservation();
        reservation2.setReservationName("test-reservation2");
        reservation2.setStartTime(AiravataUtils.getCurrentTimestamp().getTime() + 200000);
        reservation2.setEndTime(AiravataUtils.getCurrentTimestamp().getTime() + 300000);
        reservation2.addToQueueNames(QUEUE1_NAME);

        GroupComputeResourcePreference groupComputeResourcePreference1 = new GroupComputeResourcePreference();
        groupComputeResourcePreference1.setComputeResourceId(resourceId1);
        groupComputeResourcePreference1.addToReservations(reservation1);
        groupComputeResourcePreference1.addToReservations(reservation2);

        groupResourceProfile.addToComputePreferences(groupComputeResourcePreference1);

        String groupResourceProfileId = groupResourceProfileRepository.addGroupResourceProfile(groupResourceProfile);

        // Update one of the reservations
        long newStartTime = AiravataUtils.getCurrentTimestamp().getTime() + 1000*1000;
        long newEndTime = AiravataUtils.getCurrentTimestamp().getTime() + 2*1000*1000;
        {
            GroupResourceProfile retrievedGroupResourceProfile = groupResourceProfileRepository
                    .getGroupResourceProfile(groupResourceProfileId);
            List<ComputeResourceReservation> retrievedReservations = retrievedGroupResourceProfile
                    .getComputePreferences().get(0).getReservations();
            assertEquals(2, retrievedReservations.size());
            // push into future, should sort second on next retrieval
            retrievedReservations.get(0).setStartTime(newStartTime);
            retrievedReservations.get(0).setEndTime(newEndTime);

            groupResourceProfileRepository.updateGroupResourceProfile(retrievedGroupResourceProfile);
        }

        {
            GroupResourceProfile retrievedGroupResourceProfile = groupResourceProfileRepository
                    .getGroupResourceProfile(groupResourceProfileId);
            List<ComputeResourceReservation> retrievedReservations = retrievedGroupResourceProfile
                    .getComputePreferences().get(0).getReservations();
            assertEquals(2, retrievedReservations.size());
            // first reservation should now sort second
            ComputeResourceReservation reservation = retrievedReservations.get(1);
            assertEquals(reservation1.getReservationName(), reservation.getReservationName());
            assertEquals(newStartTime, reservation.getStartTime());
            assertEquals(newEndTime, reservation.getEndTime());
        }

    }

    @Test
    public void testAddingQueueToReservation() throws AppCatalogException {

        GroupResourceProfile groupResourceProfile = new GroupResourceProfile();
        groupResourceProfile.setGatewayId(gatewayId);
        groupResourceProfile.setGroupResourceProfileName("TEST_GROUP_PROFILE_NAME");

        ComputeResourceReservation reservation1 = new ComputeResourceReservation();
        reservation1.setReservationName("test-reservation1");
        reservation1.setStartTime(AiravataUtils.getCurrentTimestamp().getTime());
        reservation1.setEndTime(AiravataUtils.getCurrentTimestamp().getTime() + 100000);
        reservation1.addToQueueNames(QUEUE1_NAME);

        GroupComputeResourcePreference groupComputeResourcePreference1 = new GroupComputeResourcePreference();
        groupComputeResourcePreference1.setComputeResourceId(resourceId1);
        groupComputeResourcePreference1.addToReservations(reservation1);

        groupResourceProfile.addToComputePreferences(groupComputeResourcePreference1);

        String groupResourceProfileId = groupResourceProfileRepository.addGroupResourceProfile(groupResourceProfile);

        // add queue to the reservation
        {
            GroupResourceProfile retrievedGroupResourceProfile = groupResourceProfileRepository
                    .getGroupResourceProfile(groupResourceProfileId);
            List<ComputeResourceReservation> retrievedReservations = retrievedGroupResourceProfile
                    .getComputePreferences().get(0).getReservations();
            assertEquals(1, retrievedReservations.size());
            ComputeResourceReservation reservation = retrievedReservations.get(0);
            assertEquals(1, reservation.getQueueNamesSize());
            reservation.addToQueueNames(QUEUE2_NAME);

            groupResourceProfileRepository.updateGroupResourceProfile(retrievedGroupResourceProfile);
        }

        {
            GroupResourceProfile retrievedGroupResourceProfile = groupResourceProfileRepository
                    .getGroupResourceProfile(groupResourceProfileId);
            List<ComputeResourceReservation> retrievedReservations = retrievedGroupResourceProfile
                    .getComputePreferences().get(0).getReservations();
            assertEquals(1, retrievedReservations.size());
            ComputeResourceReservation reservation = retrievedReservations.get(0);
            assertEquals(new HashSet<>(Arrays.asList(QUEUE1_NAME, QUEUE2_NAME)), new HashSet<>(reservation.getQueueNames()));
        }

    }

    @Test
    public void testRemovingQueueFromReservation() throws AppCatalogException {

        GroupResourceProfile groupResourceProfile = new GroupResourceProfile();
        groupResourceProfile.setGatewayId(gatewayId);
        groupResourceProfile.setGroupResourceProfileName("TEST_GROUP_PROFILE_NAME");

        ComputeResourceReservation reservation1 = new ComputeResourceReservation();
        reservation1.setReservationName("test-reservation1");
        reservation1.setStartTime(AiravataUtils.getCurrentTimestamp().getTime());
        reservation1.setEndTime(AiravataUtils.getCurrentTimestamp().getTime() + 100000);
        reservation1.addToQueueNames(QUEUE1_NAME);
        reservation1.addToQueueNames(QUEUE2_NAME);

        GroupComputeResourcePreference groupComputeResourcePreference1 = new GroupComputeResourcePreference();
        groupComputeResourcePreference1.setComputeResourceId(resourceId1);
        groupComputeResourcePreference1.addToReservations(reservation1);

        groupResourceProfile.addToComputePreferences(groupComputeResourcePreference1);

        String groupResourceProfileId = groupResourceProfileRepository.addGroupResourceProfile(groupResourceProfile);

        // add queue to the reservation
        {
            GroupResourceProfile retrievedGroupResourceProfile = groupResourceProfileRepository
                    .getGroupResourceProfile(groupResourceProfileId);
            List<ComputeResourceReservation> retrievedReservations = retrievedGroupResourceProfile
                    .getComputePreferences().get(0).getReservations();
            assertEquals(1, retrievedReservations.size());
            ComputeResourceReservation reservation = retrievedReservations.get(0);
            assertEquals(new HashSet<>(Arrays.asList(QUEUE1_NAME, QUEUE2_NAME)), new HashSet<>(reservation.getQueueNames()));
            reservation.unsetQueueNames();
            reservation.addToQueueNames(QUEUE1_NAME);

            groupResourceProfileRepository.updateGroupResourceProfile(retrievedGroupResourceProfile);
        }

        {
            GroupResourceProfile retrievedGroupResourceProfile = groupResourceProfileRepository
                    .getGroupResourceProfile(groupResourceProfileId);
            List<ComputeResourceReservation> retrievedReservations = retrievedGroupResourceProfile
                    .getComputePreferences().get(0).getReservations();
            assertEquals(1, retrievedReservations.size());
            ComputeResourceReservation reservation = retrievedReservations.get(0);
            assertEquals(new HashSet<>(Arrays.asList(QUEUE1_NAME)), new HashSet<>(reservation.getQueueNames()));
        }

    }
}
