/*
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
 *
*/
package org.apache.airavata.registry.core.repositories;

import org.apache.airavata.model.WorkflowModel;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.registry.core.entities.workflowcatalog.WorkflowEntity;
import org.apache.airavata.registry.core.entities.workspacecatalog.GatewayEntity;
import org.apache.airavata.registry.core.entities.workspacecatalog.UserProfileEntity;
import org.apache.airavata.registry.core.repositories.workflowcatalog.WorkflowRepository;
import org.apache.airavata.registry.core.repositories.workspacecatalog.GatewayRepository;
import org.apache.airavata.registry.core.repositories.workspacecatalog.UserProfileRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowRepositoryTest {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private UserProfileRepository userProfileRepository;
    private WorkflowRepository workflowRepository;
    private String gatewayId;
    private String userId;
    private String templateId;

    private final String GATEWAY_DOMAIN = "test1.com";
    private final String WORKFLOW_NAME = "test Workflow";

    @Before
    public void setupRepository() {
        gatewayRepository = new GatewayRepository(Gateway.class, GatewayEntity.class);
        userProfileRepository = new UserProfileRepository(UserProfile.class, UserProfileEntity.class);
        gatewayId = "test.com" + System.currentTimeMillis();
        userId = "testuser" + System.currentTimeMillis();
        workflowRepository = new WorkflowRepository(WorkflowModel.class, WorkflowEntity.class);
        templateId = "templateId" + System.currentTimeMillis();
    }


    @Test
    public void workflowRepositoryTest() {

		/*
         * Creating Gateway required for UserProfile & Workflow creation
		 */
        Gateway gateway = new Gateway();
        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.ACTIVE);
        gateway.setGatewayId(gatewayId);
        gateway.setDomain(GATEWAY_DOMAIN);
        gateway = gatewayRepository.create(gateway);
        Assert.assertTrue(!gateway.getGatewayId().isEmpty());

		/*
         * UserProfile Instance creation required for Workflow Creation
		 */
        UserProfile userProfile = new UserProfile();
        userProfile.setAiravataInternalUserId(userId);
        userProfile.setGatewayId(gateway.getGatewayId());
        userProfile = userProfileRepository.create(userProfile);
        Assert.assertTrue(!userProfile.getAiravataInternalUserId().isEmpty());

        /*
         * Workflow Instance Creation
         */

        WorkflowModel workflowModel = new WorkflowModel();
        workflowModel.setTemplateId(templateId);
        workflowModel.setCreatedUser(userId);
        workflowModel.setGatewayId(gatewayId);
        workflowModel.setName(WORKFLOW_NAME);


        /*
         * Workflow Repository Insert Operation Test
		 */
        workflowModel = workflowRepository.create(workflowModel);
        Assert.assertTrue(!workflowModel.getTemplateId().isEmpty());


        /*
         * Workflow Repository Update Operation Test
		 */
        workflowModel.setGraph("test");
        workflowRepository.update(workflowModel);
        workflowModel = workflowRepository.get(templateId);
        Assert.assertEquals(workflowModel.getGraph(), "test");

		/*
         * Workflow Repository Select Operation Test
		 */
        workflowModel = workflowRepository.get(templateId);
        Assert.assertNotNull(workflowModel);

		/*
         * Workflow Repository Delete Operation
		 */

        boolean deleteResult = workflowRepository.delete(templateId);
        Assert.assertTrue(deleteResult);

        deleteResult = userProfileRepository.delete(userId);
        Assert.assertTrue(deleteResult);

        deleteResult = gatewayRepository.delete(gatewayId);
        Assert.assertTrue(deleteResult);


    }
}