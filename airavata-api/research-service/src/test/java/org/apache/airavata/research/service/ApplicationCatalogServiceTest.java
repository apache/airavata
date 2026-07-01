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
import org.apache.airavata.api.appcatalog.ApplicationDeploymentWithAccess;
import org.apache.airavata.api.appcatalog.ApplicationInterfaceWithAccess;
import org.apache.airavata.api.appcatalog.ApplicationModuleWithAccess;
import org.apache.airavata.config.Constants;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.iam.service.GatewayGroupsInitializer;
import org.apache.airavata.interfaces.AppCatalogRegistry;
import org.apache.airavata.interfaces.ComputeRegistry;
import org.apache.airavata.interfaces.CredentialProvider;
import org.apache.airavata.interfaces.RegistryProvider;
import org.apache.airavata.interfaces.ResourceProfileRegistry;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.group.proto.ResourcePermissionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicationCatalogServiceTest {

    @Mock
    AppCatalogRegistry appCatalogRegistry;

    @Mock
    ComputeRegistry computeRegistry;

    @Mock
    ResourceProfileRegistry resourceProfileRegistry;

    @Mock
    RegistryProvider registryProvider;

    @Mock
    SharingFacade sharingHandler;

    @Mock
    CredentialProvider credentialHandler;

    @Mock
    GatewayGroupsInitializer gatewayGroupsInitializer;

    ApplicationCatalogService service;
    RequestContext ctx;

    @BeforeEach
    void setUp() throws Exception {
        // Sharing is enabled via airavata-server.properties on the classpath.
        // Configure the sharing mock to allow all access checks.
        when(sharingHandler.userHasAccess(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        service = new ApplicationCatalogService(
                appCatalogRegistry,
                computeRegistry,
                resourceProfileRegistry,
                registryProvider,
                sharingHandler,
                credentialHandler,
                gatewayGroupsInitializer);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    // -------------------------------------------------------------------------
    // Application Modules
    // -------------------------------------------------------------------------

    @Test
    void registerApplicationModule_delegatesToRegistry() throws Exception {
        ApplicationModule module =
                ApplicationModule.newBuilder().setAppModuleName("Gaussian").build();
        when(appCatalogRegistry.registerApplicationModule("testGateway", module))
                .thenReturn("mod-1");

        String result = service.registerApplicationModule(ctx, "testGateway", module);

        assertEquals("mod-1", result);
        verify(appCatalogRegistry).registerApplicationModule("testGateway", module);
    }

    @Test
    void getApplicationModule_delegatesToRegistry() throws Exception {
        ApplicationModule module =
                ApplicationModule.newBuilder().setAppModuleId("mod-1").build();
        module = module.toBuilder().setAppModuleName("Gaussian").build();
        when(appCatalogRegistry.getApplicationModule("mod-1")).thenReturn(module);

        ApplicationModule result = service.getApplicationModule(ctx, "mod-1");

        assertNotNull(result);
        assertEquals("mod-1", result.getAppModuleId());
        verify(appCatalogRegistry).getApplicationModule("mod-1");
    }

    @Test
    void getApplicationModuleWithAccess_stampsGatewayAdminWriteFalseForNonAdmin() throws Exception {
        // Modules are not sharing entities: is_owner is always false and write follows gateway-admin
        // status. The default ctx holds no roles, so the caller is not a gateway admin.
        ApplicationModule module =
                ApplicationModule.newBuilder().setAppModuleId("mod-1").build();
        when(appCatalogRegistry.getApplicationModule("mod-1")).thenReturn(module);

        ApplicationModuleWithAccess result = service.getApplicationModuleWithAccess(ctx, "mod-1");

        assertEquals("mod-1", result.getApplicationModule().getAppModuleId());
        assertFalse(result.getAccess().getIsOwner());
        assertFalse(result.getAccess().getUserHasWriteAccess());
        verify(appCatalogRegistry).getApplicationModule("mod-1");
    }

    @Test
    void getApplicationModuleWithAccess_stampsWriteTrueForGatewayAdmin() throws Exception {
        ApplicationModule module =
                ApplicationModule.newBuilder().setAppModuleId("mod-1").build();
        when(appCatalogRegistry.getApplicationModule("mod-1")).thenReturn(module);
        RequestContext adminCtx = new RequestContext(
                "adminUser",
                "testGateway",
                "token123",
                Map.of("userName", "adminUser", "gatewayId", "testGateway"),
                List.of(Constants.ROLE_GATEWAY_ADMIN));

        ApplicationModuleWithAccess result = service.getApplicationModuleWithAccess(adminCtx, "mod-1");

        assertFalse(result.getAccess().getIsOwner());
        assertTrue(result.getAccess().getUserHasWriteAccess());
    }

    @Test
    void getAllApplicationModulesWithAccess_stampsSameFlagsForEveryModule() throws Exception {
        ApplicationModule m1 =
                ApplicationModule.newBuilder().setAppModuleId("mod-1").build();
        ApplicationModule m2 =
                ApplicationModule.newBuilder().setAppModuleId("mod-2").build();
        when(appCatalogRegistry.getAllAppModules("testGateway")).thenReturn(List.of(m1, m2));
        RequestContext adminCtx = new RequestContext(
                "adminUser",
                "testGateway",
                "token123",
                Map.of("userName", "adminUser", "gatewayId", "testGateway"),
                List.of(Constants.ROLE_GATEWAY_ADMIN));

        List<ApplicationModuleWithAccess> result =
                service.getAllApplicationModulesWithAccess(adminCtx, "testGateway");

        assertEquals(2, result.size());
        for (ApplicationModuleWithAccess m : result) {
            assertFalse(m.getAccess().getIsOwner());
            assertTrue(m.getAccess().getUserHasWriteAccess());
        }
        verify(appCatalogRegistry).getAllAppModules("testGateway");
    }

    @Test
    void updateApplicationModule_delegatesToRegistry() throws Exception {
        ApplicationModule module =
                ApplicationModule.newBuilder().setAppModuleId("mod-1").build();
        when(appCatalogRegistry.updateApplicationModule("mod-1", module)).thenReturn(true);

        boolean result = service.updateApplicationModule(ctx, "mod-1", module);

        assertTrue(result);
        verify(appCatalogRegistry).updateApplicationModule("mod-1", module);
    }

    @Test
    void deleteApplicationModule_delegatesToRegistry() throws Exception {
        when(appCatalogRegistry.deleteApplicationModule("mod-1")).thenReturn(true);

        boolean result = service.deleteApplicationModule(ctx, "mod-1");

        assertTrue(result);
        verify(appCatalogRegistry).deleteApplicationModule("mod-1");
    }

    @Test
    void getAllAppModules_delegatesToRegistry() throws Exception {
        ApplicationModule m1 =
                ApplicationModule.newBuilder().setAppModuleId("mod-1").build();
        ApplicationModule m2 =
                ApplicationModule.newBuilder().setAppModuleId("mod-2").build();
        when(appCatalogRegistry.getAllAppModules("testGateway")).thenReturn(List.of(m1, m2));

        List<ApplicationModule> result = service.getAllAppModules(ctx, "testGateway");

        assertEquals(2, result.size());
        verify(appCatalogRegistry).getAllAppModules("testGateway");
    }

    // -------------------------------------------------------------------------
    // Application Deployments (with sharing logic)
    // -------------------------------------------------------------------------

    @Test
    void getApplicationDeployment_returnsDeploymentWhenSharingDisabled() throws Exception {
        // ServerSettings.isEnableSharing() returns false in test env, so no sharing check is done
        ApplicationDeploymentDescription dep = ApplicationDeploymentDescription.newBuilder()
                .setAppDeploymentId("dep-1")
                .build();
        when(appCatalogRegistry.getApplicationDeployment("dep-1")).thenReturn(dep);

        ApplicationDeploymentDescription result = service.getApplicationDeployment(ctx, "dep-1");

        assertNotNull(result);
        assertEquals("dep-1", result.getAppDeploymentId());
        verify(appCatalogRegistry).getApplicationDeployment("dep-1");
    }

    @Test
    void updateApplicationDeployment_delegatesToRegistry() throws Exception {
        ApplicationDeploymentDescription dep = ApplicationDeploymentDescription.newBuilder()
                .setAppDeploymentId("dep-1")
                .build();
        when(appCatalogRegistry.updateApplicationDeployment("dep-1", dep)).thenReturn(true);

        // sharing disabled in test env — no access check
        boolean result = service.updateApplicationDeployment(ctx, "dep-1", dep);

        assertTrue(result);
        verify(appCatalogRegistry).updateApplicationDeployment("dep-1", dep);
    }

    @Test
    void getAppModuleDeployedResources_delegatesToRegistry() throws Exception {
        when(appCatalogRegistry.getAppModuleDeployedResources("mod-1")).thenReturn(List.of("dep-1", "dep-2"));

        List<String> result = service.getAppModuleDeployedResources(ctx, "mod-1");

        assertEquals(2, result.size());
        verify(appCatalogRegistry).getAppModuleDeployedResources("mod-1");
    }

    @Test
    void getAllApplicationDeploymentsWithAccess_stampsPerDeploymentFlags() throws Exception {
        // Sharing is enabled in this class (see setUp) and the stub grants every userHasAccess check,
        // so each deployment's sharing OWNER/WRITE flags resolve true.
        ApplicationDeploymentDescription d1 = ApplicationDeploymentDescription.newBuilder()
                .setAppDeploymentId("dep-1")
                .build();
        ApplicationDeploymentDescription d2 = ApplicationDeploymentDescription.newBuilder()
                .setAppDeploymentId("dep-2")
                .build();
        when(appCatalogRegistry.getAccessibleApplicationDeployments(eq("testGateway"), anyList(), anyList()))
                .thenReturn(List.of(d1, d2));

        List<ApplicationDeploymentWithAccess> result =
                service.getAllApplicationDeploymentsWithAccess(ctx, "testGateway");

        assertEquals(2, result.size());
        for (ApplicationDeploymentWithAccess d : result) {
            assertTrue(d.getAccess().getIsOwner());
            assertTrue(d.getAccess().getUserHasWriteAccess());
        }
        assertEquals("dep-1", result.get(0).getApplicationDeployment().getAppDeploymentId());
        assertEquals("dep-2", result.get(1).getApplicationDeployment().getAppDeploymentId());
    }

    @Test
    void getAccessibleApplicationDeploymentsWithAccess_stampsPerDeploymentFlags() throws Exception {
        // Sharing is enabled in this class (see setUp) and the stub grants every userHasAccess check,
        // so the deployment's sharing OWNER/WRITE flags resolve true.
        ApplicationDeploymentDescription d1 = ApplicationDeploymentDescription.newBuilder()
                .setAppDeploymentId("dep-1")
                .build();
        when(appCatalogRegistry.getAccessibleApplicationDeployments(eq("testGateway"), anyList(), anyList()))
                .thenReturn(List.of(d1));

        List<ApplicationDeploymentWithAccess> result =
                service.getAccessibleApplicationDeploymentsWithAccess(
                        ctx, "testGateway", ResourcePermissionType.READ);

        assertEquals(1, result.size());
        assertEquals("dep-1", result.get(0).getApplicationDeployment().getAppDeploymentId());
        assertTrue(result.get(0).getAccess().getIsOwner());
        assertTrue(result.get(0).getAccess().getUserHasWriteAccess());
        verify(appCatalogRegistry).getAccessibleApplicationDeployments(eq("testGateway"), anyList(), anyList());
    }

    // -------------------------------------------------------------------------
    // Application Interfaces
    // -------------------------------------------------------------------------

    @Test
    void registerApplicationInterface_delegatesToRegistry() throws Exception {
        ApplicationInterfaceDescription iface = ApplicationInterfaceDescription.newBuilder()
                .setApplicationName("GaussianInterface")
                .build();
        when(appCatalogRegistry.registerApplicationInterface("testGateway", iface))
                .thenReturn("iface-1");

        String result = service.registerApplicationInterface(ctx, "testGateway", iface);

        assertEquals("iface-1", result);
        verify(appCatalogRegistry).registerApplicationInterface("testGateway", iface);
    }

    @Test
    void getApplicationInterface_delegatesToRegistry() throws Exception {
        ApplicationInterfaceDescription iface = ApplicationInterfaceDescription.newBuilder()
                .setApplicationInterfaceId("iface-1")
                .build();
        when(appCatalogRegistry.getApplicationInterface("iface-1")).thenReturn(iface);

        ApplicationInterfaceDescription result = service.getApplicationInterface(ctx, "iface-1");

        assertNotNull(result);
        assertEquals("iface-1", result.getApplicationInterfaceId());
    }

    @Test
    void getApplicationInterfaceWithAccess_stampsGatewayAdminWriteFalseForNonAdmin() throws Exception {
        // Interfaces are not sharing entities: is_owner is always false and write follows gateway-admin
        // status. The default ctx holds no roles, so the caller is not a gateway admin.
        ApplicationInterfaceDescription iface = ApplicationInterfaceDescription.newBuilder()
                .setApplicationInterfaceId("iface-1")
                .build();
        when(appCatalogRegistry.getApplicationInterface("iface-1")).thenReturn(iface);

        ApplicationInterfaceWithAccess result = service.getApplicationInterfaceWithAccess(ctx, "iface-1");

        assertEquals("iface-1", result.getApplicationInterface().getApplicationInterfaceId());
        assertFalse(result.getAccess().getIsOwner());
        assertFalse(result.getAccess().getUserHasWriteAccess());
        verify(appCatalogRegistry).getApplicationInterface("iface-1");
    }

    @Test
    void getApplicationInterfaceWithAccess_stampsWriteTrueForGatewayAdmin() throws Exception {
        ApplicationInterfaceDescription iface = ApplicationInterfaceDescription.newBuilder()
                .setApplicationInterfaceId("iface-1")
                .build();
        when(appCatalogRegistry.getApplicationInterface("iface-1")).thenReturn(iface);
        RequestContext adminCtx = new RequestContext(
                "adminUser",
                "testGateway",
                "token123",
                Map.of("userName", "adminUser", "gatewayId", "testGateway"),
                List.of(Constants.ROLE_GATEWAY_ADMIN));

        ApplicationInterfaceWithAccess result = service.getApplicationInterfaceWithAccess(adminCtx, "iface-1");

        assertFalse(result.getAccess().getIsOwner());
        assertTrue(result.getAccess().getUserHasWriteAccess());
    }

    @Test
    void getAllApplicationInterfacesWithAccess_stampsSameFlagsForEveryInterface() throws Exception {
        ApplicationInterfaceDescription i1 = ApplicationInterfaceDescription.newBuilder()
                .setApplicationInterfaceId("iface-1")
                .build();
        ApplicationInterfaceDescription i2 = ApplicationInterfaceDescription.newBuilder()
                .setApplicationInterfaceId("iface-2")
                .build();
        when(appCatalogRegistry.getAllApplicationInterfaces("testGateway")).thenReturn(List.of(i1, i2));
        RequestContext adminCtx = new RequestContext(
                "adminUser",
                "testGateway",
                "token123",
                Map.of("userName", "adminUser", "gatewayId", "testGateway"),
                List.of(Constants.ROLE_GATEWAY_ADMIN));

        List<ApplicationInterfaceWithAccess> result =
                service.getAllApplicationInterfacesWithAccess(adminCtx, "testGateway");

        assertEquals(2, result.size());
        for (ApplicationInterfaceWithAccess i : result) {
            assertFalse(i.getAccess().getIsOwner());
            assertTrue(i.getAccess().getUserHasWriteAccess());
        }
        verify(appCatalogRegistry).getAllApplicationInterfaces("testGateway");
    }

    @Test
    void cloneApplicationInterface_throwsWhenSourceMissing() throws Exception {
        when(appCatalogRegistry.getApplicationInterface("iface-old")).thenReturn(null);

        assertThrows(
                ServiceException.class,
                () -> service.cloneApplicationInterface(ctx, "iface-old", "NewApp", "testGateway"));
    }

    @Test
    void cloneApplicationInterface_registersNewInterface() throws Exception {
        ApplicationInterfaceDescription iface = ApplicationInterfaceDescription.newBuilder()
                .setApplicationInterfaceId("iface-old")
                .build();
        iface = iface.toBuilder().setApplicationName("OldApp").build();
        when(appCatalogRegistry.getApplicationInterface("iface-old")).thenReturn(iface);
        when(appCatalogRegistry.registerApplicationInterface(eq("testGateway"), any()))
                .thenReturn("iface-new");

        String result = service.cloneApplicationInterface(ctx, "iface-old", "NewApp", "testGateway");

        assertEquals("iface-new", result);
    }

    @Test
    void deleteApplicationInterface_delegatesToRegistry() throws Exception {
        when(appCatalogRegistry.deleteApplicationInterface("iface-1")).thenReturn(true);

        boolean result = service.deleteApplicationInterface(ctx, "iface-1");

        assertTrue(result);
        verify(appCatalogRegistry).deleteApplicationInterface("iface-1");
    }

    @Test
    void getApplicationInputs_delegatesToRegistry() throws Exception {
        List<InputDataObjectType> inputs = List.of(InputDataObjectType.getDefaultInstance());
        when(appCatalogRegistry.getApplicationInputs("iface-1")).thenReturn(inputs);

        List<InputDataObjectType> result = service.getApplicationInputs(ctx, "iface-1");

        assertEquals(1, result.size());
        verify(appCatalogRegistry).getApplicationInputs("iface-1");
    }

    @Test
    void getApplicationOutputs_delegatesToRegistry() throws Exception {
        List<OutputDataObjectType> outputs = List.of(OutputDataObjectType.getDefaultInstance());
        when(appCatalogRegistry.getApplicationOutputs("iface-1")).thenReturn(outputs);

        List<OutputDataObjectType> result = service.getApplicationOutputs(ctx, "iface-1");

        assertEquals(1, result.size());
        verify(appCatalogRegistry).getApplicationOutputs("iface-1");
    }

    @Test
    void getApplicationDeploymentQueues_appliesDeploymentDefaultsToMatchingQueue() throws Exception {
        ApplicationDeploymentDescription dep = ApplicationDeploymentDescription.newBuilder()
                .setAppDeploymentId("dep-1")
                .setComputeHostId("cr-1")
                .setDefaultQueueName("gpu")
                .setDefaultNodeCount(5)
                .setDefaultCpuCount(40)
                .setDefaultWalltime(120)
                .build();
        when(appCatalogRegistry.getApplicationDeployment("dep-1")).thenReturn(dep);

        ComputeResourceDescription cr = ComputeResourceDescription.newBuilder()
                .setComputeResourceId("cr-1")
                .addBatchQueues(BatchQueue.newBuilder()
                        .setQueueName("normal")
                        .setIsDefaultQueue(true)
                        .build())
                .addBatchQueues(
                        BatchQueue.newBuilder().setQueueName("gpu").build())
                .build();
        when(computeRegistry.getComputeResource("cr-1")).thenReturn(cr);

        List<BatchQueue> queues = service.getApplicationDeploymentQueues(ctx, "dep-1");

        assertEquals(2, queues.size());
        BatchQueue normal = queues.stream()
                .filter(q -> q.getQueueName().equals("normal"))
                .findFirst()
                .orElseThrow();
        BatchQueue gpu = queues.stream()
                .filter(q -> q.getQueueName().equals("gpu"))
                .findFirst()
                .orElseThrow();
        // The non-default queue is flagged off; the deployment's default queue is flagged on and
        // carries the deployment's default node/cpu/walltime.
        assertFalse(normal.getIsDefaultQueue());
        assertTrue(gpu.getIsDefaultQueue());
        assertEquals(5, gpu.getDefaultNodeCount());
        assertEquals(40, gpu.getDefaultCpuCount());
        assertEquals(120, gpu.getDefaultWalltime());
    }
}
