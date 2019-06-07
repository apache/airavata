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
package org.apache.airavata.registry.core.repositories;

public class WorkspaceRepositoryTest {
//    private final static Logger logger = LoggerFactory.getLogger(WorkspaceRepositoryTest.class);
//
//    private GatewayRepository gatewayRepository;
//    private NotificationRepository notificationRepository;
//    private UserProfileRepository userProfileRepository;
//    private ProjectRepository projectRepository;
//    private String gatewayId;
//    private String notificationId;
//    private String userId;
//    private String projectId;
//
//    private final String GATEWAY_DOMAIN = "test1.com";
//    private final String NOTIFY_MESSAGE = "NotifyMe";
//    private final String USER_COMMENT = "TestComment";
//    private final String PROJECT_DESCRIPTION = "Test Description";
//
//
//    @Before
//    public void setupRepository() {
//
//        gatewayRepository = new GatewayRepository(GatewayEntity.class, GatewayEntity.class);
//        notificationRepository = new NotificationRepository(Notification.class,
//                NotificationEntity.class);
//        userProfileRepository = new UserProfileRepository(UserProfile.class, UserProfileEntity.class);
//        projectRepository = new ProjectRepository(Project.class, ProjectEntity.class);
//
//        gatewayId = "test.com" + System.currentTimeMillis();
//        notificationId = UUID.randomUUID().toString();
//        userId = "testuser" + System.currentTimeMillis();
//        projectId = "project" + System.currentTimeMillis();
//    }
//
//    @Test
//    public void userProfileRepositoryTest() {
//
//		/*
//         * Creating GatewayEntity required for UserProfile creation
//		 */
//        GatewayEntity gateway = new GatewayEntity();
//        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.ACTIVE);
//        gateway.setGatewayId(gatewayId);
//        gateway.setDomain(GATEWAY_DOMAIN);
//        gateway = gatewayRepository.create(gateway);
//        Assert.assertTrue(!gateway.getGatewayId().isEmpty());
//
//
//
//		/*
//         * UserProfile Instance creation
//		 */
//        UserProfile userProfile = new UserProfile();
//        userProfile.setAiravataInternalUserId(userId);
//        userProfile.setGatewayId(gateway.getGatewayId());
//
//        /*
//         * Workspace UserProfile Repository Insert Operation Test
//		 */
//        userProfile = userProfileRepository.create(userProfile);
//        Assert.assertTrue(!userProfile.getAiravataInternalUserId().isEmpty());
//
//        /*
//         * Workspace UserProfile Repository Update Operation Test
//		 */
//        userProfile.setComments(USER_COMMENT);
//        userProfileRepository.update(userProfile);
//        userProfile = userProfileRepository.get(userId);
//        System.out.println(userProfile.getComments());
//        Assert.assertEquals(userProfile.getComments(), USER_COMMENT);
//
//		/*
//         * Workspace UserProfile Repository Select Operation Test
//		 */
//        userProfile = userProfileRepository.get(userId);
//        Assert.assertNotNull(userProfile);
//
//		/*
//         * Workspace UserProfile Repository Delete Operation
//		 */
//        boolean deleteResult = userProfileRepository.delete(userId);
//        Assert.assertTrue(deleteResult);
//        deleteResult = gatewayRepository.delete(gatewayId);
//        Assert.assertTrue(deleteResult);
//
//
//    }
//
//    @Test
//    public void projectRepositoryTest() {
//
//		/*
//         * Creating GatewayEntity required for UserProfile & Project creation
//		 */
//        GatewayEntity gateway = new GatewayEntity();
//        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.ACTIVE);
//        gateway.setGatewayId(gatewayId);
//        gateway.setDomain(GATEWAY_DOMAIN);
//        gateway = gatewayRepository.create(gateway);
//        Assert.assertTrue(!gateway.getGatewayId().isEmpty());
//
//		/*
//         * UserProfile Instance creation required for Project Creation
//		 */
//        UserProfile userProfile = new UserProfile();
//        userProfile.setAiravataInternalUserId(userId);
//        userProfile.setGatewayId(gateway.getGatewayId());
//        userProfile = userProfileRepository.create(userProfile);
//        Assert.assertTrue(!userProfile.getAiravataInternalUserId().isEmpty());
//
//        /*
//         * Project Instance creation
//         */
//        Project project = new Project();
//        project.setGatewayId(gatewayId);
//        project.setOwner(userId);
//        project.setProjectID(projectId);
//        project.setGatewayIdIsSet(true);
//
//
//        /*
//         * Workspace Project Repository Insert Operation Test
//		 */
//        project = projectRepository.create(project);
//        Assert.assertTrue(!project.getProjectID().isEmpty());
//
//        /*
//         * Workspace Project Repository Update Operation Test
//		 */
//        project.setDescription(PROJECT_DESCRIPTION);
//        projectRepository.update(project);
//        project = projectRepository.get(projectId);
//        Assert.assertEquals(project.getDescription(), PROJECT_DESCRIPTION);
//
//		/*
//         * Workspace Project Repository Select Operation Test
//		 */
//        project = projectRepository.get(projectId);
//        Assert.assertNotNull(project);
//
//		/*
//         * Workspace Project Repository Delete Operation
//		 */
//        boolean deleteResult = projectRepository.delete(projectId);
//        Assert.assertTrue(deleteResult);
//
//        deleteResult = userProfileRepository.delete(userId);
//        Assert.assertTrue(deleteResult);
//
//        deleteResult = gatewayRepository.delete(gatewayId);
//        Assert.assertTrue(deleteResult);
//
//
//    }
}