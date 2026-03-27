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
package org.apache.airavata.compute.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.apache.airavata.credential.handler.CredentialStoreServerHandler;
import org.apache.airavata.execution.handler.RegistryServerHandler;
import org.apache.airavata.execution.service.RequestContext;
import org.apache.airavata.execution.service.ServiceException;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.sharing.handler.SharingRegistryServerHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationCatalogServiceTest {

    @Mock
    RegistryServerHandler registryHandler;

    @Mock
    SharingRegistryServerHandler sharingHandler;

    @Mock
    CredentialStoreServerHandler credentialHandler;

    ApplicationCatalogService service;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        service = new ApplicationCatalogService(registryHandler, sharingHandler, credentialHandler);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    // -------------------------------------------------------------------------
    // Application Modules
    // -------------------------------------------------------------------------

    @Test
    void registerApplicationModule_delegatesToRegistry() throws Exception {
        ApplicationModule module = new ApplicationModule();
        module.setAppModuleName("Gaussian");
        when(registryHandler.registerApplicationModule("testGateway", module)).thenReturn("mod-1");

        String result = service.registerApplicationModule(ctx, "testGateway", module);

        assertEquals("mod-1", result);
        verify(registryHandler).registerApplicationModule("testGateway", module);
    }

    @Test
    void getApplicationModule_delegatesToRegistry() throws Exception {
        ApplicationModule module = new ApplicationModule();
        module.setAppModuleId("mod-1");
        module.setAppModuleName("Gaussian");
        when(registryHandler.getApplicationModule("mod-1")).thenReturn(module);

        ApplicationModule result = service.getApplicationModule(ctx, "mod-1");

        assertNotNull(result);
        assertEquals("mod-1", result.getAppModuleId());
        verify(registryHandler).getApplicationModule("mod-1");
    }

    @Test
    void updateApplicationModule_delegatesToRegistry() throws Exception {
        ApplicationModule module = new ApplicationModule();
        module.setAppModuleId("mod-1");
        when(registryHandler.updateApplicationModule("mod-1", module)).thenReturn(true);

        boolean result = service.updateApplicationModule(ctx, "mod-1", module);

        assertTrue(result);
        verify(registryHandler).updateApplicationModule("mod-1", module);
    }

    @Test
    void deleteApplicationModule_delegatesToRegistry() throws Exception {
        when(registryHandler.deleteApplicationModule("mod-1")).thenReturn(true);

        boolean result = service.deleteApplicationModule(ctx, "mod-1");

        assertTrue(result);
        verify(registryHandler).deleteApplicationModule("mod-1");
    }

    @Test
    void getAllAppModules_delegatesToRegistry() throws Exception {
        ApplicationModule m1 = new ApplicationModule();
        m1.setAppModuleId("mod-1");
        ApplicationModule m2 = new ApplicationModule();
        m2.setAppModuleId("mod-2");
        when(registryHandler.getAllAppModules("testGateway")).thenReturn(List.of(m1, m2));

        List<ApplicationModule> result = service.getAllAppModules(ctx, "testGateway");

        assertEquals(2, result.size());
        verify(registryHandler).getAllAppModules("testGateway");
    }

    // -------------------------------------------------------------------------
    // Application Deployments (with sharing logic)
    // -------------------------------------------------------------------------

    @Test
    void getApplicationDeployment_returnsDeploymentWhenSharingDisabled() throws Exception {
        // ServerSettings.isEnableSharing() returns false in test env, so no sharing check is done
        ApplicationDeploymentDescription dep = new ApplicationDeploymentDescription();
        dep.setAppDeploymentId("dep-1");
        when(registryHandler.getApplicationDeployment("dep-1")).thenReturn(dep);

        ApplicationDeploymentDescription result = service.getApplicationDeployment(ctx, "dep-1");

        assertNotNull(result);
        assertEquals("dep-1", result.getAppDeploymentId());
        verify(registryHandler).getApplicationDeployment("dep-1");
    }

    @Test
    void updateApplicationDeployment_delegatesToRegistry() throws Exception {
        ApplicationDeploymentDescription dep = new ApplicationDeploymentDescription();
        dep.setAppDeploymentId("dep-1");
        when(registryHandler.updateApplicationDeployment("dep-1", dep)).thenReturn(true);

        // sharing disabled in test env — no access check
        boolean result = service.updateApplicationDeployment(ctx, "dep-1", dep);

        assertTrue(result);
        verify(registryHandler).updateApplicationDeployment("dep-1", dep);
    }

    @Test
    void getAppModuleDeployedResources_delegatesToRegistry() throws Exception {
        when(registryHandler.getAppModuleDeployedResources("mod-1")).thenReturn(List.of("dep-1", "dep-2"));

        List<String> result = service.getAppModuleDeployedResources(ctx, "mod-1");

        assertEquals(2, result.size());
        verify(registryHandler).getAppModuleDeployedResources("mod-1");
    }

    // -------------------------------------------------------------------------
    // Application Interfaces
    // -------------------------------------------------------------------------

    @Test
    void registerApplicationInterface_delegatesToRegistry() throws Exception {
        ApplicationInterfaceDescription iface = new ApplicationInterfaceDescription();
        iface.setApplicationName("GaussianInterface");
        when(registryHandler.registerApplicationInterface("testGateway", iface)).thenReturn("iface-1");

        String result = service.registerApplicationInterface(ctx, "testGateway", iface);

        assertEquals("iface-1", result);
        verify(registryHandler).registerApplicationInterface("testGateway", iface);
    }

    @Test
    void getApplicationInterface_delegatesToRegistry() throws Exception {
        ApplicationInterfaceDescription iface = new ApplicationInterfaceDescription();
        iface.setApplicationInterfaceId("iface-1");
        when(registryHandler.getApplicationInterface("iface-1")).thenReturn(iface);

        ApplicationInterfaceDescription result = service.getApplicationInterface(ctx, "iface-1");

        assertNotNull(result);
        assertEquals("iface-1", result.getApplicationInterfaceId());
    }

    @Test
    void cloneApplicationInterface_throwsWhenSourceMissing() throws Exception {
        when(registryHandler.getApplicationInterface("iface-old")).thenReturn(null);

        assertThrows(
                ServiceException.class,
                () -> service.cloneApplicationInterface(ctx, "iface-old", "NewApp", "testGateway"));
    }

    @Test
    void cloneApplicationInterface_registersNewInterface() throws Exception {
        ApplicationInterfaceDescription iface = new ApplicationInterfaceDescription();
        iface.setApplicationInterfaceId("iface-old");
        iface.setApplicationName("OldApp");
        when(registryHandler.getApplicationInterface("iface-old")).thenReturn(iface);
        when(registryHandler.registerApplicationInterface(eq("testGateway"), any()))
                .thenReturn("iface-new");

        String result = service.cloneApplicationInterface(ctx, "iface-old", "NewApp", "testGateway");

        assertEquals("iface-new", result);
    }

    @Test
    void deleteApplicationInterface_delegatesToRegistry() throws Exception {
        when(registryHandler.deleteApplicationInterface("iface-1")).thenReturn(true);

        boolean result = service.deleteApplicationInterface(ctx, "iface-1");

        assertTrue(result);
        verify(registryHandler).deleteApplicationInterface("iface-1");
    }

    @Test
    void getApplicationInputs_delegatesToRegistry() throws Exception {
        List<InputDataObjectType> inputs = List.of(new InputDataObjectType());
        when(registryHandler.getApplicationInputs("iface-1")).thenReturn(inputs);

        List<InputDataObjectType> result = service.getApplicationInputs(ctx, "iface-1");

        assertEquals(1, result.size());
        verify(registryHandler).getApplicationInputs("iface-1");
    }

    @Test
    void getApplicationOutputs_delegatesToRegistry() throws Exception {
        List<OutputDataObjectType> outputs = List.of(new OutputDataObjectType());
        when(registryHandler.getApplicationOutputs("iface-1")).thenReturn(outputs);

        List<OutputDataObjectType> result = service.getApplicationOutputs(ctx, "iface-1");

        assertEquals(1, result.size());
        verify(registryHandler).getApplicationOutputs("iface-1");
    }
}
