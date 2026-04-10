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
package org.apache.airavata.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import org.apache.airavata.interfaces.GatewayGroupsProvider;
import org.apache.airavata.interfaces.RegistryProvider;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.model.group.proto.ResourcePermissionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SharingHelperTest {

    @Mock
    SharingFacade sharingHandler;

    @Mock
    RegistryProvider registryHandler;

    @Mock
    GatewayGroupsProvider gatewayGroupsInitializer;

    @Test
    void isSharingEnabled_returnsTrueWhenConfigured() {
        // airavata-server.properties on classpath has enable.sharing=true
        assertTrue(SharingHelper.isSharingEnabled());
    }

    @Test
    void userHasAccess_ownerPermission_returnsTrueWhenOwner() throws Exception {
        when(sharingHandler.userHasAccess("gw1", "alice@gw1", "entity1", "gw1:OWNER"))
                .thenReturn(true);

        boolean result =
                SharingHelper.userHasAccess(sharingHandler, "gw1", "alice", "entity1", ResourcePermissionType.OWNER);

        assertTrue(result);
    }

    @Test
    void userHasAccess_ownerPermission_returnsFalseWhenNotOwner() throws Exception {
        when(sharingHandler.userHasAccess("gw1", "alice@gw1", "entity1", "gw1:OWNER"))
                .thenReturn(false);

        boolean result =
                SharingHelper.userHasAccess(sharingHandler, "gw1", "alice", "entity1", ResourcePermissionType.OWNER);

        assertFalse(result);
    }

    @Test
    void userHasAccess_readPermission_returnsTrueWhenOwner() throws Exception {
        when(sharingHandler.userHasAccess("gw1", "alice@gw1", "entity1", "gw1:OWNER"))
                .thenReturn(true);

        boolean result =
                SharingHelper.userHasAccess(sharingHandler, "gw1", "alice", "entity1", ResourcePermissionType.READ);

        assertTrue(result);
        // should not check READ permission when OWNER already grants access
        verify(sharingHandler, never()).userHasAccess("gw1", "alice@gw1", "entity1", "gw1:READ");
    }

    @Test
    void userHasAccess_readPermission_fallsBackToReadWhenNotOwner() throws Exception {
        when(sharingHandler.userHasAccess("gw1", "alice@gw1", "entity1", "gw1:OWNER"))
                .thenReturn(false);
        when(sharingHandler.userHasAccess("gw1", "alice@gw1", "entity1", "gw1:READ"))
                .thenReturn(true);

        boolean result =
                SharingHelper.userHasAccess(sharingHandler, "gw1", "alice", "entity1", ResourcePermissionType.READ);

        assertTrue(result);
    }

    @Test
    void userHasAccess_readPermission_returnsFalseWhenNeitherOwnerNorReader() throws Exception {
        when(sharingHandler.userHasAccess("gw1", "alice@gw1", "entity1", "gw1:OWNER"))
                .thenReturn(false);
        when(sharingHandler.userHasAccess("gw1", "alice@gw1", "entity1", "gw1:READ"))
                .thenReturn(false);

        boolean result =
                SharingHelper.userHasAccess(sharingHandler, "gw1", "alice", "entity1", ResourcePermissionType.READ);

        assertFalse(result);
    }

    @Test
    void userHasAccess_qualifiedUserId_usedAsIs() throws Exception {
        when(sharingHandler.userHasAccess("gw1", "alice@gw1", "entity1", "gw1:OWNER"))
                .thenReturn(true);

        boolean result = SharingHelper.userHasAccess(
                sharingHandler, "gw1", "alice@gw1", "entity1", ResourcePermissionType.OWNER);

        assertTrue(result);
    }

    @Test
    void userHasAccess_throwsRuntimeExceptionOnFailure() throws Exception {
        when(sharingHandler.userHasAccess(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(
                RuntimeException.class,
                () -> SharingHelper.userHasAccess(
                        sharingHandler, "gw1", "alice", "entity1", ResourcePermissionType.OWNER));
    }

    @Test
    void retrieveGatewayGroups_returnsExistingGroups() throws Exception {
        GatewayGroups groups = GatewayGroups.newBuilder()
                .setGatewayId("gw1")
                .setAdminsGroupId("admins")
                .build();
        when(registryHandler.isGatewayGroupsExists("gw1")).thenReturn(true);
        when(registryHandler.getGatewayGroups("gw1")).thenReturn(groups);

        GatewayGroups result = SharingHelper.retrieveGatewayGroups(registryHandler, gatewayGroupsInitializer, "gw1");

        assertEquals("gw1", result.getGatewayId());
        assertEquals("admins", result.getAdminsGroupId());
    }

    @Test
    void createManageSharingPermissionTypeIfMissing_createsWhenNotExists() throws Exception {
        when(sharingHandler.isPermissionExists("gw1", "gw1:MANAGE_SHARING")).thenReturn(false);

        SharingHelper.createManageSharingPermissionTypeIfMissing(sharingHandler, "gw1");

        verify(sharingHandler)
                .createPermissionType("gw1:MANAGE_SHARING", "gw1", "MANAGE_SHARING", "Manage sharing permission type");
    }

    @Test
    void createManageSharingPermissionTypeIfMissing_skipsWhenExists() throws Exception {
        when(sharingHandler.isPermissionExists("gw1", "gw1:MANAGE_SHARING")).thenReturn(true);

        SharingHelper.createManageSharingPermissionTypeIfMissing(sharingHandler, "gw1");

        verify(sharingHandler, never()).createPermissionType(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shareEntityWithAdminGatewayGroups_sharesWithAdminAndReadOnlyGroups() throws Exception {
        GatewayGroups groups = GatewayGroups.newBuilder()
                .setGatewayId("gw1")
                .setAdminsGroupId("admins")
                .setReadOnlyAdminsGroupId("readOnlyAdmins")
                .build();
        when(registryHandler.isGatewayGroupsExists("gw1")).thenReturn(true);
        when(registryHandler.getGatewayGroups("gw1")).thenReturn(groups);
        when(sharingHandler.isPermissionExists("gw1", "gw1:MANAGE_SHARING")).thenReturn(true);

        SharingHelper.shareEntityWithAdminGatewayGroups(
                sharingHandler, registryHandler, gatewayGroupsInitializer, "gw1", "entity1");

        verify(sharingHandler)
                .shareEntityWithGroups("gw1", "entity1", Arrays.asList("admins"), "gw1:MANAGE_SHARING", true);
        verify(sharingHandler).shareEntityWithGroups("gw1", "entity1", Arrays.asList("admins"), "gw1:WRITE", true);
        verify(sharingHandler)
                .shareEntityWithGroups("gw1", "entity1", Arrays.asList("admins", "readOnlyAdmins"), "gw1:READ", true);
    }
}
