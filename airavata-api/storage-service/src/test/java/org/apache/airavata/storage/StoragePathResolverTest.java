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
package org.apache.airavata.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.apache.airavata.config.Constants;
import org.apache.airavata.config.UserContext;
import org.apache.airavata.interfaces.GatewayStoragePreferenceProvider;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.apache.airavata.model.security.proto.AuthzToken;
import org.apache.airavata.task.AdaptorSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for {@link StoragePathResolver#resolvePath} — the load-bearing chroot anchoring that
 * must keep matching {@code DataStagingTask.buildDestinationFilePath}: absolute paths are honored
 * as-is, while {@code ~}, {@code ~/...}, and bare-relative paths are anchored under the storage
 * resource's filesystem root.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StoragePathResolverTest {

    private static final String GATEWAY = "testGateway";
    private static final String STORAGE_ID = "storage-1";

    @Mock
    AdaptorSupport adaptorSupport;

    @Mock
    GatewayStoragePreferenceProvider gatewayStoragePreferenceProvider;

    StoragePathResolver resolver;

    @BeforeEach
    void setUp() throws Exception {
        resolver = new StoragePathResolver(adaptorSupport, gatewayStoragePreferenceProvider);

        // Populate the request-scoped UserContext that GrpcRequestContext.current() reads.
        AuthzToken token = AuthzToken.newBuilder()
                .setAccessToken("token123")
                .putClaimsMap(Constants.USER_NAME, "testUser")
                .putClaimsMap(Constants.GATEWAY_ID, GATEWAY)
                .build();
        UserContext.setAuthzToken(token);

        StoragePreference pref = StoragePreference.newBuilder()
                .setStorageResourceId(STORAGE_ID)
                .setFileSystemRootLocation("/storage")
                .build();
        when(gatewayStoragePreferenceProvider.getAllGatewayStoragePreferences(GATEWAY))
                .thenReturn(List.of(pref));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void resolvePath_absoluteHonoredAsIs() throws Exception {
        assertEquals("/storage/project/exp", resolver.resolvePath("/storage/project/exp", STORAGE_ID));
    }

    @Test
    void resolvePath_bareRelativeAnchoredUnderRoot() throws Exception {
        // The chroot-anchoring case: a bare "project/exp" must be anchored under the root.
        assertEquals("/storage/project/exp", resolver.resolvePath("project/exp", STORAGE_ID));
    }

    @Test
    void resolvePath_homeShortcutAnchoredUnderRoot() throws Exception {
        assertEquals("/storage/", resolver.resolvePath("~", STORAGE_ID));
        assertEquals("/storage/project/exp", resolver.resolvePath("~/project/exp", STORAGE_ID));
    }

    @Test
    void resolvePath_nullOrEmptyResolvesToRoot() throws Exception {
        assertEquals("/storage/", resolver.resolvePath(null, STORAGE_ID));
        assertEquals("/storage/", resolver.resolvePath("", STORAGE_ID));
    }

    @Test
    void resolvePath_defaultsRootToSlashWhenNoPreference() throws Exception {
        when(gatewayStoragePreferenceProvider.getAllGatewayStoragePreferences(GATEWAY))
                .thenReturn(List.of());
        // No preference -> root defaults to "/", so a bare relative path is anchored under it.
        assertEquals("/project/exp", resolver.resolvePath("project/exp", STORAGE_ID));
    }

    @Test
    void resolveStorageResourceId_returnsRequestIdWhenSet() throws Exception {
        assertEquals(STORAGE_ID, resolver.resolveStorageResourceId(STORAGE_ID));
    }

    @Test
    void resolveStorageResourceId_fallsBackToGatewayDefault() throws Exception {
        assertEquals(STORAGE_ID, resolver.resolveStorageResourceId(""));
    }
}
