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
import org.apache.airavata.api.project.ProjectWithAccess;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceAuthorizationException;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.exception.ServiceNotFoundException;
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
    void getMostRecentWritableProject_picksNewestWritable() throws Exception {
        // Caller-bound candidate set (sharing enabled path: searchEntityIds + searchProjects).
        when(sharingHandler.searchEntityIds(eq("testGateway"), eq("testUser@testGateway"), anyList(), eq(0), eq(-1)))
                .thenReturn(List.of("proj-old", "proj-new"));

        Project older = Project.newBuilder()
                .setProjectId("proj-old")
                .setOwner("testUser")
                .setGatewayId("testGateway")
                .setCreationTime(1000L)
                .build();
        Project newer = Project.newBuilder()
                .setProjectId("proj-new")
                .setOwner("testUser")
                .setGatewayId("testGateway")
                .setCreationTime(2000L)
                .build();
        when(projectRegistry.searchProjects(eq("testGateway"), eq("testUser"), anyList(), anyMap(), anyInt(), anyInt()))
                .thenReturn(List.of(older, newer));

        ProjectWithAccess result = projectService.getMostRecentWritableProject(ctx, "testGateway", "testUser");

        assertEquals("proj-new", result.getProject().getProjectId());
        assertTrue(result.getAccess().getIsOwner());
        assertTrue(result.getAccess().getUserHasWriteAccess());
    }

    @Test
    void getMostRecentWritableProject_usesWriteGrantForNonOwner() throws Exception {
        when(sharingHandler.searchEntityIds(eq("testGateway"), eq("testUser@testGateway"), anyList(), eq(0), eq(-1)))
                .thenReturn(List.of("proj-shared"));

        Project shared = Project.newBuilder()
                .setProjectId("proj-shared")
                .setOwner("otherUser")
                .setGatewayId("testGateway")
                .setCreationTime(5000L)
                .build();
        when(projectRegistry.searchProjects(eq("testGateway"), eq("testUser"), anyList(), anyMap(), anyInt(), anyInt()))
                .thenReturn(List.of(shared));
        // Sharing enabled: non-owner holds a WRITE grant.
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "proj-shared", "testGateway:WRITE"))
                .thenReturn(true);

        ProjectWithAccess result = projectService.getMostRecentWritableProject(ctx, "testGateway", "testUser");

        assertEquals("proj-shared", result.getProject().getProjectId());
        assertFalse(result.getAccess().getIsOwner());
        assertTrue(result.getAccess().getUserHasWriteAccess());
    }

    @Test
    void getMostRecentWritableProject_throwsWhenNoneWritable() throws Exception {
        // Empty caller-accessible set -> getUserProjects returns empty -> NOT_FOUND.
        when(sharingHandler.searchEntityIds(eq("testGateway"), eq("testUser@testGateway"), anyList(), eq(0), eq(-1)))
                .thenReturn(List.of());

        assertThrows(
                ServiceNotFoundException.class,
                () -> projectService.getMostRecentWritableProject(ctx, "testGateway", "testUser"));
    }

    @Test
    void createProjectWithAccess_ownerGetsFullAccess() throws Exception {
        Project project = Project.newBuilder()
                .setName("test-proj")
                .setGatewayId("testGateway")
                .setOwner("testUser")
                .build();

        when(projectRegistry.createProject("testGateway", project)).thenReturn("proj-123");
        // getProjectWithAccess re-reads the new project to derive flags from a single source of truth.
        Project created = project.toBuilder().setProjectId("proj-123").build();
        when(projectRegistry.getProject("proj-123")).thenReturn(created);

        ProjectWithAccess result = projectService.createProjectWithAccess(ctx, "testGateway", project);

        assertEquals("proj-123", result.getProject().getProjectId());
        assertTrue(result.getAccess().getIsOwner());
        assertTrue(result.getAccess().getUserHasWriteAccess());
        verify(projectRegistry).createProject("testGateway", project);
    }

    @Test
    void updateProjectWithAccess_ownerGetsFullAccess() throws Exception {
        Project existing = Project.newBuilder()
                .setProjectId("proj-123")
                .setOwner("testUser")
                .setGatewayId("testGateway")
                .build();
        Project updated = Project.newBuilder()
                .setProjectId("proj-123")
                .setOwner("testUser")
                .setGatewayId("testGateway")
                .setName("renamed")
                .build();

        when(projectRegistry.getProject("proj-123")).thenReturn(existing);

        ProjectWithAccess result = projectService.updateProjectWithAccess(ctx, "proj-123", updated);

        assertEquals("proj-123", result.getProject().getProjectId());
        assertTrue(result.getAccess().getIsOwner());
        assertTrue(result.getAccess().getUserHasWriteAccess());
        verify(projectRegistry).updateProject("proj-123", updated);
    }

    @Test
    void updateProjectWithAccess_nonOwnerWithWriteGrant() throws Exception {
        Project existing = Project.newBuilder()
                .setProjectId("proj-123")
                .setOwner("otherUser")
                .setGatewayId("testGateway")
                .build();
        Project updated = Project.newBuilder()
                .setProjectId("proj-123")
                .setOwner("otherUser")
                .setGatewayId("testGateway")
                .setName("renamed")
                .build();

        when(projectRegistry.getProject("proj-123")).thenReturn(existing);
        // Sharing enabled: non-owner holds a WRITE grant on this project.
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "proj-123", "testGateway:WRITE"))
                .thenReturn(true);

        ProjectWithAccess result = projectService.updateProjectWithAccess(ctx, "proj-123", updated);

        assertEquals("proj-123", result.getProject().getProjectId());
        assertFalse(result.getAccess().getIsOwner());
        assertTrue(result.getAccess().getUserHasWriteAccess());
        verify(projectRegistry).updateProject("proj-123", updated);
    }

    @Test
    void updateProjectWithAccess_rejectsOwnerChange() throws Exception {
        Project existing = Project.newBuilder()
                .setProjectId("proj-123")
                .setOwner("testUser")
                .setGatewayId("testGateway")
                .build();
        Project updated = Project.newBuilder()
                .setProjectId("proj-123")
                .setOwner("newOwner")
                .setGatewayId("testGateway")
                .build();

        when(projectRegistry.getProject("proj-123")).thenReturn(existing);

        assertThrows(
                ServiceException.class, () -> projectService.updateProjectWithAccess(ctx, "proj-123", updated));
        verify(projectRegistry, never()).updateProject(anyString(), any());
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
