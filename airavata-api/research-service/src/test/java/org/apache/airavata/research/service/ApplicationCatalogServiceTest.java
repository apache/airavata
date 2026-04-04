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
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.iam.service.GatewayGroupsInitializer;
import org.apache.airavata.interfaces.AppCatalogRegistry;
import org.apache.airavata.interfaces.CredentialProvider;
import org.apache.airavata.interfaces.RegistryProvider;
import org.apache.airavata.interfaces.ResourceProfileRegistry;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
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
}
