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
package org.apache.airavata.registry.repositories;

import org.apache.airavata.registry.repositories.common.TestBase;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, WorkspaceRepositoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@Transactional
public class WorkspaceRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {"org.apache.airavata.service", "org.apache.airavata.registry", "org.apache.airavata.config"},
            excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.config.ThriftServerLauncher.class,
                            org.apache.airavata.monitor.realtime.RealtimeMonitor.class,
                            org.apache.airavata.monitor.email.EmailBasedMonitor.class,
                            org.apache.airavata.monitor.cluster.ClusterStatusMonitorJob.class,
                            org.apache.airavata.monitor.AbstractMonitor.class,
                            org.apache.airavata.helix.impl.controller.HelixController.class,
                            org.apache.airavata.helix.impl.participant.GlobalParticipant.class,
                            org.apache.airavata.helix.impl.workflow.PreWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.PostWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.ParserWorkflowManager.class
                        }),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.apache\\.airavata\\.monitor\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.apache\\.airavata\\.helix\\..*")
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {}

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
    //        Assertions.assertTrue(!gateway.getGatewayId().isEmpty());
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
    //        Assertions.assertTrue(!userProfile.getAiravataInternalUserId().isEmpty());
    //
    //        /*
    //         * Workspace UserProfile Repository Update Operation Test
    //		 */
    //        userProfile.setComments(USER_COMMENT);
    //        userProfileRepository.update(userProfile);
    //        userProfile = userProfileRepository.get(userId);
    //        logger.info("Comments: {}", userProfile.getComments());
    //        Assertions.assertEquals(userProfile.getComments(), USER_COMMENT);
    //
    //		/*
    //         * Workspace UserProfile Repository Select Operation Test
    //		 */
    //        userProfile = userProfileRepository.get(userId);
    //        Assertions.assertNotNull(userProfile);
    //
    //		/*
    //         * Workspace UserProfile Repository Delete Operation
    //		 */
    //        boolean deleteResult = userProfileRepository.delete(userId);
    //        Assertions.assertTrue(deleteResult);
    //        deleteResult = gatewayRepository.delete(gatewayId);
    //        Assertions.assertTrue(deleteResult);
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
    //        Assertions.assertTrue(!gateway.getGatewayId().isEmpty());
    //
    //		/*
    //         * UserProfile Instance creation required for Project Creation
    //		 */
    //        UserProfile userProfile = new UserProfile();
    //        userProfile.setAiravataInternalUserId(userId);
    //        userProfile.setGatewayId(gateway.getGatewayId());
    //        userProfile = userProfileRepository.create(userProfile);
    //        Assertions.assertTrue(!userProfile.getAiravataInternalUserId().isEmpty());
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
    //        Assertions.assertTrue(!project.getProjectID().isEmpty());
    //
    //        /*
    //         * Workspace Project Repository Update Operation Test
    //		 */
    //        project.setDescription(PROJECT_DESCRIPTION);
    //        projectRepository.update(project);
    //        project = projectRepository.get(projectId);
    //        Assertions.assertEquals(project.getDescription(), PROJECT_DESCRIPTION);
    //
    //		/*
    //         * Workspace Project Repository Select Operation Test
    //		 */
    //        project = projectRepository.get(projectId);
    //        Assertions.assertNotNull(project);
    //
    //		/*
    //         * Workspace Project Repository Delete Operation
    //		 */
    //        boolean deleteResult = projectRepository.delete(projectId);
    //        Assertions.assertTrue(deleteResult);
    //
    //        deleteResult = userProfileRepository.delete(userId);
    //        Assertions.assertTrue(deleteResult);
    //
    //        deleteResult = gatewayRepository.delete(gatewayId);
    //        Assertions.assertTrue(deleteResult);
    //
    //
    //    }
    public WorkspaceRepositoryTest() {
        super(TestBase.Database.EXP_CATALOG, TestBase.Database.APP_CATALOG);
    }

    // Test methods can be added here when needed
    // Previous test code was commented out and can be restored if needed
}
