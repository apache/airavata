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
package org.apache.airavata.research.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceAuthorizationException;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.interfaces.ProjectRegistry;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.model.workspace.proto.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectServiceTest {

    @Mock
    ProjectRegistry projectRegistry;

    @Mock
    SharingFacade sharingHandler;

    ProjectService projectService;
    RequestContext ctx;

    @BeforeEach
    void setUp() throws Exception {
        // Sharing is enabled via airavata-server.properties on the classpath.
        // Configure the sharing mock to allow all access checks.
        when(sharingHandler.userHasAccess(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);
        when(sharingHandler.searchEntityIds(anyString(), anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(List.of());

        projectService = new ProjectService(projectRegistry, sharingHandler);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void createProject_returnsProjectId() throws Exception {
        Project project = Project.newBuilder().setName("test-proj").build();
        project = project.toBuilder().setGatewayId("testGateway").build();
        project = project.toBuilder().setOwner("testUser").build();

        when(projectRegistry.createProject("testGateway", project)).thenReturn("proj-123");

        String result = projectService.createProject(ctx, "testGateway", project);

        assertEquals("proj-123", result);
        verify(projectRegistry).createProject("testGateway", project);
    }

    @Test
    void getProject_ownerGetsAccess() throws Exception {
        Project project = Project.newBuilder().setOwner("testUser").build();
        project = project.toBuilder().setGatewayId("testGateway").build();

        when(projectRegistry.getProject("proj-123")).thenReturn(project);

        Project result = projectService.getProject(ctx, "proj-123");

        assertNotNull(result);
        assertEquals("testUser", result.getOwner());
    }

    @Test
    void getProject_nonOwnerRejectedWhenSharingEnabled() throws Exception {
        Project project = Project.newBuilder().setOwner("otherUser").build();
        project = project.toBuilder().setGatewayId("testGateway").build();

        when(projectRegistry.getProject("proj-123")).thenReturn(project);

        // Sharing enabled: non-owner without READ permission is rejected
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "proj-123", "testGateway:READ"))
                .thenReturn(false);

        assertThrows(ServiceAuthorizationException.class, () -> projectService.getProject(ctx, "proj-123"));
    }

    @Test
    void deleteProject_rejectsNonOwnerWithoutWriteAccess() throws Exception {
        Project project = Project.newBuilder().setOwner("otherUser").build();
        project = project.toBuilder().setGatewayId("testGateway").build();

        when(projectRegistry.getProject("proj-123")).thenReturn(project);

        // Sharing enabled: non-owner without WRITE permission is rejected
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "proj-123", "testGateway:WRITE"))
                .thenReturn(false);

        assertThrows(ServiceAuthorizationException.class, () -> projectService.deleteProject(ctx, "proj-123"));
    }

    @Test
    void deleteProject_ownerCanDelete() throws Exception {
        Project project = Project.newBuilder().setOwner("testUser").build();
        project = project.toBuilder().setGatewayId("testGateway").build();

        when(projectRegistry.getProject("proj-123")).thenReturn(project);
        when(projectRegistry.deleteProject("proj-123")).thenReturn(true);

        boolean result = projectService.deleteProject(ctx, "proj-123");

        assertTrue(result);
        verify(projectRegistry).deleteProject("proj-123");
    }

    @Test
    void getUserProjects_delegatesToRegistry() throws Exception {
        // With sharing enabled, getUserProjects uses searchEntityIds + searchProjects path
        when(sharingHandler.searchEntityIds(eq("testGateway"), eq("testUser@testGateway"), anyList(), eq(0), eq(-1)))
                .thenReturn(List.of("proj-1", "proj-2"));

        List<Project> projects = List.of(Project.getDefaultInstance(), Project.getDefaultInstance());
        when(projectRegistry.searchProjects(eq("testGateway"), eq("testUser"), anyList(), anyMap(), eq(10), eq(0)))
                .thenReturn(projects);

        List<Project> result = projectService.getUserProjects(ctx, "testGateway", "testUser", 10, 0);

        assertEquals(2, result.size());
    }

    @Test
    void updateProject_rejectsOwnerChange() throws Exception {
        Project existing = Project.newBuilder().setOwner("testUser").build();
        existing = existing.toBuilder().setGatewayId("testGateway").build();

        Project updated = Project.newBuilder()
                .setOwner("newOwner")
                .setGatewayId("testGateway")
                .build();

        when(projectRegistry.getProject("proj-123")).thenReturn(existing);

        assertThrows(ServiceException.class, () -> projectService.updateProject(ctx, "proj-123", updated));
    }
}
