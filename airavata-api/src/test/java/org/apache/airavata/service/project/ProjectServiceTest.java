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
package org.apache.airavata.execution.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.execution.handler.RegistryServerHandler;
import org.apache.airavata.execution.service.RequestContext;
import org.apache.airavata.execution.service.ServiceAuthorizationException;
import org.apache.airavata.execution.service.ServiceException;
import org.apache.airavata.sharing.handler.SharingRegistryServerHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    RegistryServerHandler registryHandler;

    @Mock
    SharingRegistryServerHandler sharingHandler;

    ProjectService projectService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(registryHandler, sharingHandler);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void createProject_returnsProjectId() throws Exception {
        Project project = new Project();
        project.setName("test-proj");
        project.setGatewayId("testGateway");
        project.setOwner("testUser");

        when(registryHandler.createProject("testGateway", project)).thenReturn("proj-123");

        String result = projectService.createProject(ctx, "testGateway", project);

        assertEquals("proj-123", result);
        verify(registryHandler).createProject("testGateway", project);
    }

    @Test
    void getProject_ownerGetsAccess() throws Exception {
        Project project = new Project();
        project.setOwner("testUser");
        project.setGatewayId("testGateway");

        when(registryHandler.getProject("proj-123")).thenReturn(project);

        Project result = projectService.getProject(ctx, "proj-123");

        assertNotNull(result);
        assertEquals("testUser", result.getOwner());
    }

    @Test
    void getProject_nonOwnerRejectedWhenSharingDisabled() throws Exception {
        Project project = new Project();
        project.setOwner("otherUser");
        project.setGatewayId("testGateway");

        when(registryHandler.getProject("proj-123")).thenReturn(project);

        // sharing disabled (ServerSettings.isEnableSharing() returns false by default in tests)
        Project result = projectService.getProject(ctx, "proj-123");

        assertNull(result);
        verifyNoInteractions(sharingHandler);
    }

    @Test
    void deleteProject_rejectsNonOwnerWithoutWriteAccess() throws Exception {
        Project project = new Project();
        project.setOwner("otherUser");
        project.setGatewayId("testGateway");

        when(registryHandler.getProject("proj-123")).thenReturn(project);

        // sharing disabled — non-owner should be rejected
        assertThrows(ServiceAuthorizationException.class, () -> projectService.deleteProject(ctx, "proj-123"));
    }

    @Test
    void deleteProject_ownerCanDelete() throws Exception {
        Project project = new Project();
        project.setOwner("testUser");
        project.setGatewayId("testGateway");

        when(registryHandler.getProject("proj-123")).thenReturn(project);
        when(registryHandler.deleteProject("proj-123")).thenReturn(true);

        boolean result = projectService.deleteProject(ctx, "proj-123");

        assertTrue(result);
        verify(registryHandler).deleteProject("proj-123");
    }

    @Test
    void getUserProjects_delegatesToRegistry() throws Exception {
        List<Project> projects = List.of(new Project(), new Project());
        when(registryHandler.getUserProjects("testGateway", "testUser", 10, 0)).thenReturn(projects);

        List<Project> result = projectService.getUserProjects(ctx, "testGateway", "testUser", 10, 0);

        assertEquals(2, result.size());
        verify(registryHandler).getUserProjects("testGateway", "testUser", 10, 0);
    }

    @Test
    void updateProject_rejectsOwnerChange() throws Exception {
        Project existing = new Project();
        existing.setOwner("testUser");
        existing.setGatewayId("testGateway");

        Project updated = new Project();
        updated.setOwner("newOwner");
        updated.setGatewayId("testGateway");

        when(registryHandler.getProject("proj-123")).thenReturn(existing);

        assertThrows(ServiceException.class, () -> projectService.updateProject(ctx, "proj-123", updated));
    }
}
