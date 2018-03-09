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

import org.apache.airavata.model.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.groupresourceprofile.*;
import org.apache.airavata.registry.core.repositories.util.Initialize;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class GroupResourceProfileRepositoryTest {

    private static Initialize initialize;
    private ComputeResourceRepository computeResourceRepository;
    private GroupResourceProfileRepository groupResourceProfileRepository;
    private String gatewayId = "TEST_GATEWAY";
    private String groupResourceProfileId = "TEST_GROUP_PROFILE_ID";
    private static final Logger logger = LoggerFactory.getLogger(GroupResourceProfileRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("appcatalog-derby.sql");
            initialize.initializeDB();
            computeResourceRepository = new ComputeResourceRepository();
            groupResourceProfileRepository = new GroupResourceProfileRepository();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        groupResourceProfileRepository.removeGroupResourceProfile(groupResourceProfileId);
        initialize.stopDerbyServer();
    }

    @Test
    public void GroupResourceProfileRepositoryTest() throws AppCatalogException {
        ComputeResourceDescription description = new ComputeResourceDescription();

        description.setHostName("localhost");
        description.setResourceDescription("test compute resource");
        List<String> ipdaresses = new ArrayList<String>();
        ipdaresses.add("222.33.43.444");
        ipdaresses.add("23.344.44.454");
        description.setIpAddresses(ipdaresses);

        BatchQueue batchQueue1 = new BatchQueue();
        batchQueue1.setQueueName("queue1");
        batchQueue1.setQueueDescription("que1Desc1");
        batchQueue1.setMaxRunTime(10);
        batchQueue1.setMaxNodes(4);
        batchQueue1.setMaxJobsInQueue(1);

        BatchQueue batchQueue2 = new BatchQueue();
        batchQueue2.setQueueName("queue2");
        batchQueue2.setQueueDescription("que1Desc2");
        batchQueue2.setMaxRunTime(10);
        batchQueue2.setMaxNodes(4);
        batchQueue2.setMaxJobsInQueue(1);

        List<BatchQueue> batchQueueList = new ArrayList<BatchQueue>();
        batchQueueList.add(batchQueue1);
        batchQueueList.add(batchQueue2);
        description.setBatchQueues(batchQueueList);

        String resourceId1 = computeResourceRepository.addComputeResource(description);

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

        String resourceId2 = computeResourceRepository.addComputeResource(cm2);

        ComputeResourceDescription computeResourceDescription = computeResourceRepository.getComputeResource(resourceId2);

        GroupResourceProfile groupResourceProfile = new GroupResourceProfile();
        groupResourceProfile.setGatewayId(gatewayId);
        groupResourceProfile.setGroupResourceProfileId(groupResourceProfileId);
        groupResourceProfile.setGroupResourceProfileName("TEST_GROUP_PROFILE_NAME");

        GroupAccountSSHProvisionerConfig groupAccountSSHProvisionerConfig = new GroupAccountSSHProvisionerConfig();
        groupAccountSSHProvisionerConfig.setGroupResourceProfileId(groupResourceProfileId);
        groupAccountSSHProvisionerConfig.setResourceId(resourceId1);
        groupAccountSSHProvisionerConfig.setConfigName("configName");
        groupAccountSSHProvisionerConfig.setConfigValue("configvalue");

        GroupComputeResourcePreference groupComputeResourcePreference1 = new GroupComputeResourcePreference();
        groupComputeResourcePreference1.setComputeResourceId(resourceId1);
        groupComputeResourcePreference1.setGroupResourceProfileId(groupResourceProfileId);
        groupComputeResourcePreference1.addToGroupSSHAccountProvisionerConfigs(groupAccountSSHProvisionerConfig);

        GroupComputeResourcePreference groupComputeResourcePreference2 = new GroupComputeResourcePreference();
        groupComputeResourcePreference2.setComputeResourceId(resourceId2);
        groupComputeResourcePreference2.setGroupResourceProfileId(groupResourceProfileId);

        List<GroupComputeResourcePreference> groupComputeResourcePreferenceList = new ArrayList<>();
        groupComputeResourcePreferenceList.add(groupComputeResourcePreference1);
        groupComputeResourcePreferenceList.add(groupComputeResourcePreference2);

        groupResourceProfile.setComputePreferences(groupComputeResourcePreferenceList);

        ComputeResourcePolicy computeResourcePolicy = new ComputeResourcePolicy();
        computeResourcePolicy.setComputeResourceId(resourceId1);
        computeResourcePolicy.setResourcePolicyId("TEST_COM_RESOURCE_POLICY_ID1");
        computeResourcePolicy.setGroupResourceProfileId(groupResourceProfileId);
        computeResourcePolicy.addToAllowedBatchQueues("queue1");

        ComputeResourcePolicy computeResourcePolicy2 = new ComputeResourcePolicy();
        computeResourcePolicy2.setComputeResourceId(resourceId2);
        computeResourcePolicy2.setResourcePolicyId("TEST_COM_RESOURCE_POLICY_ID2");
        computeResourcePolicy2.setGroupResourceProfileId(groupResourceProfileId);
        computeResourcePolicy2.addToAllowedBatchQueues("cmqueue1");

        List<ComputeResourcePolicy> computeResourcePolicyList =  new ArrayList<>();
        computeResourcePolicyList.add(computeResourcePolicy);
        computeResourcePolicyList.add(computeResourcePolicy2);

        groupResourceProfile.setComputeResourcePolicies(computeResourcePolicyList);

        BatchQueueResourcePolicy batchQueueResourcePolicy = new BatchQueueResourcePolicy();
        batchQueueResourcePolicy.setComputeResourceId(resourceId1);
        batchQueueResourcePolicy.setGroupResourceProfileId(groupResourceProfileId);
        batchQueueResourcePolicy.setResourcePolicyId("TEST_BQ_RESOURCE_POLICY_ID1");
        batchQueueResourcePolicy.setQueuename("queue1");
        batchQueueResourcePolicy.setMaxAllowedCores(2);
        batchQueueResourcePolicy.setMaxAllowedWalltime(10);

        BatchQueueResourcePolicy batchQueueResourcePolicy2 = new BatchQueueResourcePolicy();
        batchQueueResourcePolicy2.setComputeResourceId(resourceId2);
        batchQueueResourcePolicy2.setGroupResourceProfileId(groupResourceProfileId);
        batchQueueResourcePolicy2.setResourcePolicyId("TEST_BQ_RESOURCE_POLICY_ID2");
        batchQueueResourcePolicy2.setQueuename("cmqueue1");
        batchQueueResourcePolicy2.setMaxAllowedCores(3);
        batchQueueResourcePolicy2.setMaxAllowedWalltime(12);

        List<BatchQueueResourcePolicy> batchQueueResourcePolicyList = new ArrayList<>();
        batchQueueResourcePolicyList.add(batchQueueResourcePolicy);
        batchQueueResourcePolicyList.add(batchQueueResourcePolicy2);

        groupResourceProfile.setBatchQueueResourcePolicies(batchQueueResourcePolicyList);

        groupResourceProfileRepository.addGroupResourceProfile(groupResourceProfile);

        if (groupResourceProfileRepository.isGroupResourceProfileExists(groupResourceProfileId)) {
            GroupResourceProfile getGroupResourceProfile = groupResourceProfileRepository.getGroupResourceProfile(groupResourceProfileId);

            assertTrue(getGroupResourceProfile.getGatewayId().equals(gatewayId));
            assertTrue(getGroupResourceProfile.getGroupResourceProfileId().equals(groupResourceProfileId));

            assertTrue(getGroupResourceProfile.getComputePreferences().size() == 2);
            assertTrue(getGroupResourceProfile.getComputeResourcePolicies().size() == 2);
            assertTrue(getGroupResourceProfile.getBatchQueueResourcePolicies().size() == 2);
        }

        assertTrue(groupResourceProfileRepository.getGroupComputeResourcePreference(resourceId1,groupResourceProfileId) != null);
        assertTrue(groupResourceProfileRepository.getGroupComputeResourcePreference(resourceId1,groupResourceProfileId).getGroupSSHAccountProvisionerConfigs().size() == 1);

        ComputeResourcePolicy getComputeResourcePolicy = groupResourceProfileRepository.getComputeResourcePolicy( "TEST_COM_RESOURCE_POLICY_ID1");
        assertTrue(getComputeResourcePolicy.getAllowedBatchQueues().get(0).equals("queue1"));

        BatchQueueResourcePolicy getBatchQueuePolicy = groupResourceProfileRepository.getBatchQueueResourcePolicy("TEST_BQ_RESOURCE_POLICY_ID2");
        assertTrue(getBatchQueuePolicy != null);
        assertTrue(getBatchQueuePolicy.getMaxAllowedCores() == 3);
        assertTrue(getBatchQueuePolicy.getMaxAllowedWalltime() == 12);

        assertTrue(groupResourceProfileRepository.getAllGroupResourceProfiles(gatewayId).size() == 1);
        assertTrue(groupResourceProfileRepository.getAllGroupComputeResourcePreferences(groupResourceProfileId).size() == 2);
        assertTrue(groupResourceProfileRepository.getAllGroupComputeResourcePolicies(groupResourceProfileId).size() == 2);
        assertTrue(groupResourceProfileRepository.getAllGroupBatchQueueResourcePolicies(groupResourceProfileId).size() == 2);

        groupResourceProfileRepository.removeGroupResourceProfile(groupResourceProfileId);
        computeResourceRepository.removeComputeResource(resourceId1);
        computeResourceRepository.removeComputeResource(resourceId2);

    }
}
