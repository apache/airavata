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

import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.entities.expcatalog.GatewayEntity;
import org.apache.airavata.registry.core.entities.expcatalog.NotificationEntity;
import org.apache.airavata.registry.core.entities.expcatalog.ProjectEntity;
import org.apache.airavata.registry.core.entities.workspacecatalog.UserProfileEntity;
import org.apache.airavata.registry.core.repositories.expcatalog.GatewayRepository;
import org.apache.airavata.registry.core.repositories.expcatalog.NotificationRepository;
import org.apache.airavata.registry.core.repositories.expcatalog.ProjectRepository;
import org.apache.airavata.registry.core.repositories.workspacecatalog.UserProfileRepository;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class WorkspaceRepositoryTest {
    private final static Logger logger = LoggerFactory.getLogger(WorkspaceRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private NotificationRepository notificationRepository;
    private UserProfileRepository userProfileRepository;
    private ProjectRepository projectRepository;
    private String gatewayId;
    private String notificationId;
    private String userId;
    private String projectId;

    private final String GATEWAY_DOMAIN = "test1.com";
    private final String NOTIFY_MESSAGE = "NotifyMe";
    private final String USER_COMMENT = "TestComment";
    private final String PROJECT_DESCRIPTION = "Test Description";


    @Before
    public void setupRepository() {

        /*gatewayRepository = new GatewayRepository(Gateway.class, GatewayEntity.class);
        notificationRepository = new NotificationRepository(Notification.class,
                NotificationEntity.class);
        userProfileRepository = new UserProfileRepository(UserProfile.class, UserProfileEntity.class);
        projectRepository = new ProjectRepository(Project.class, ProjectEntity.class);

        gatewayId = "test.com" + System.currentTimeMillis();
        notificationId = UUID.randomUUID().toString();
        userId = "testuser" + System.currentTimeMillis();
        projectId = "project" + System.currentTimeMillis();*/
    }


    @Test
    public void gateWayRepositoryTest() {
//        Gateway gateway = new Gateway();
//        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.ACTIVE);
//        gateway.setGatewayId(gatewayId);

		/*
         * GateWay Repository Insert Operation Test
		 */
//        gateway = gatewayRepository.create(gateway);
//        Assert.assertTrue(!gateway.getGatewayId().isEmpty());

		/*
         * GateWay Repository Update Operation Test
		 */
//        gateway.setDomain(GATEWAY_DOMAIN);
//        gatewayRepository.update(gateway);
//        gateway = gatewayRepository.get(gateway.getGatewayId());
//        Assert.assertEquals(gateway.getDomain(), GATEWAY_DOMAIN);

		/*
         * GateWay Repository Select Operation Test
		 */
//        gateway = null;
//        gateway = gatewayRepository.get(gatewayId);
//        Assert.assertNotNull(gateway);

		/*
         * GateWay Repository Delete Operation
		 */
//        boolean deleteResult = gatewayRepository.delete(gatewayId);
//        Assert.assertTrue(deleteResult);

    }

    @Test
    public void notificationRepositoryTest() {

//        String tempNotificationId = null;
//        Gateway gateway = new Gateway();
//        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.ACTIVE);
//        gateway.setGatewayId(gatewayId);
//        gateway.setDomain(GATEWAY_DOMAIN);
//        gateway = gatewayRepository.create(gateway);
//
//        Notification notification = new Notification();
//        notification.setGatewayId(gateway.getGatewayId());
//        notification.setNotificationId(notificationId);

		/*
         * Notification INSERT Operation Test
		 */
//        notification = notificationRepository.create(notification);
//        Assert.assertTrue(!notification.getNotificationId().isEmpty());

		/*
         * Notification SELECT Operation Test
		 */
//        tempNotificationId = notification.getNotificationId();
//        notification = null;
//        notification = notificationRepository.get(tempNotificationId);
//        Assert.assertNotNull(notification);


		/*
         * Notification UPDATE Operation Test
		 */
//        notification.setNotificationMessage(NOTIFY_MESSAGE);
//        notificationRepository.update(notification);
//        notification = notificationRepository.get(notification.getNotificationId());
//        Assert.assertEquals(NOTIFY_MESSAGE, notification.getNotificationMessage());

		/*
         * Notification DELETE Operation Test
		 */
//        boolean result = notificationRepository.delete(tempNotificationId);
//        Assert.assertTrue(result);
//
//        gatewayRepository.delete(gatewayId);
    }

    @Test
    public void userProfileRepositoryTest() {

		/*
         * Creating Gateway required for UserProfile creation
		 */
//        Gateway gateway = new Gateway();
//        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.ACTIVE);
//        gateway.setGatewayId(gatewayId);
//        gateway.setDomain(GATEWAY_DOMAIN);
//        gateway = gatewayRepository.create(gateway);
//        Assert.assertTrue(!gateway.getGatewayId().isEmpty());



		/*
         * UserProfile Instance creation
		 */
//        UserProfile userProfile = new UserProfile();
//        userProfile.setAiravataInternalUserId(userId);
//        userProfile.setGatewayId(gateway.getGatewayId());

        /*
         * Workspace UserProfile Repository Insert Operation Test
		 */
//        userProfile = userProfileRepository.create(userProfile);
//        Assert.assertTrue(!userProfile.getAiravataInternalUserId().isEmpty());

        /*
         * Workspace UserProfile Repository Update Operation Test
		 */
//        userProfile.setComments(USER_COMMENT);
//        userProfileRepository.update(userProfile);
//        userProfile = userProfileRepository.get(userId);
//        System.out.println(userProfile.getComments());
//        Assert.assertEquals(userProfile.getComments(), USER_COMMENT);

		/*
         * Workspace UserProfile Repository Select Operation Test
		 */
//        userProfile = userProfileRepository.get(userId);
//        Assert.assertNotNull(userProfile);

		/*
         * Workspace UserProfile Repository Delete Operation
		 */
//        boolean deleteResult = userProfileRepository.delete(userId);
//        Assert.assertTrue(deleteResult);
//        deleteResult = gatewayRepository.delete(gatewayId);
//        Assert.assertTrue(deleteResult);


    }

    @Test
    public void projectRepositoryTest() {

		/*
         * Creating Gateway required for UserProfile & Project creation
		 */
//        Gateway gateway = new Gateway();
//        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.ACTIVE);
//        gateway.setGatewayId(gatewayId);
//        gateway.setDomain(GATEWAY_DOMAIN);
//        gateway = gatewayRepository.create(gateway);
//        Assert.assertTrue(!gateway.getGatewayId().isEmpty());

		/*
         * UserProfile Instance creation required for Project Creation
		 */
//        UserProfile userProfile = new UserProfile();
//        userProfile.setAiravataInternalUserId(userId);
//        userProfile.setGatewayId(gateway.getGatewayId());
//        userProfile = userProfileRepository.create(userProfile);
//        Assert.assertTrue(!userProfile.getAiravataInternalUserId().isEmpty());

        /*
         * Project Instance creation
         */
//        Project project = new Project();
//        project.setGatewayId(gatewayId);
//        project.setOwner(userId);
//        project.setProjectID(projectId);
//        project.setGatewayIdIsSet(true);


        /*
         * Workspace Project Repository Insert Operation Test
		 */
//        project = projectRepository.create(project);
//        Assert.assertTrue(!project.getProjectID().isEmpty());

        /*
         * Workspace Project Repository Update Operation Test
		 */
//        project.setDescription(PROJECT_DESCRIPTION);
//        projectRepository.update(project);
//        project = projectRepository.get(projectId);
//        Assert.assertEquals(project.getDescription(), PROJECT_DESCRIPTION);

		/*
         * Workspace Project Repository Select Operation Test
		 */
//        project = projectRepository.get(projectId);
//        Assert.assertNotNull(project);

		/*
         * Workspace Project Repository Delete Operation
		 */
//        boolean deleteResult = projectRepository.delete(projectId);
//        Assert.assertTrue(deleteResult);
//
//        deleteResult = userProfileRepository.delete(userId);
//        Assert.assertTrue(deleteResult);
//
//        deleteResult = gatewayRepository.delete(gatewayId);
//        Assert.assertTrue(deleteResult);


    }
}