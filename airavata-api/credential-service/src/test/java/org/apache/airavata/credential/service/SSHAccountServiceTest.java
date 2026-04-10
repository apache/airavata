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
package org.apache.airavata.credential.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference;
import org.apache.airavata.model.credential.store.proto.SSHCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SSHAccountServiceTest {

    @Mock
    CredentialStoreService credentialHandler;

    @Mock
    SSHAccountManager sshAccountManager;

    SSHAccountService sshAccountService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        sshAccountService = new SSHAccountService(credentialHandler, sshAccountManager);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void doesUserHaveSSHAccount_delegatesToSSHAccountManager() throws Exception {
        when(sshAccountManager.doesUserHaveSSHAccount("testGateway", "cr-001", "testUser"))
                .thenReturn(true);

        boolean result = sshAccountService.doesUserHaveSSHAccount(ctx, "cr-001", "testUser");

        assertTrue(result);
        verify(sshAccountManager).doesUserHaveSSHAccount("testGateway", "cr-001", "testUser");
    }

    @Test
    void doesUserHaveSSHAccount_wrapsException() throws Exception {
        when(sshAccountManager.doesUserHaveSSHAccount(any(), any(), any()))
                .thenThrow(new RuntimeException("connection failed"));

        assertThrows(ServiceException.class, () -> sshAccountService.doesUserHaveSSHAccount(ctx, "cr-001", "testUser"));
    }

    @Test
    void isSSHSetupComplete_fetchesCredentialAndDelegates() throws Exception {
        SSHCredential cred = SSHCredential.getDefaultInstance();
        when(credentialHandler.getSSHCredential("tok-123", "testGateway")).thenReturn(cred);
        when(sshAccountManager.isSSHAccountSetupComplete("testGateway", "cr-001", "testUser", cred))
                .thenReturn(false);

        boolean result = sshAccountService.isSSHSetupCompleteForUserComputeResourcePreference(ctx, "cr-001", "tok-123");

        assertFalse(result);
        verify(credentialHandler).getSSHCredential("tok-123", "testGateway");
    }

    @Test
    void isSSHSetupComplete_wrapsCredentialException() throws Exception {
        when(credentialHandler.getSSHCredential(any(), any())).thenThrow(new RuntimeException("store error"));

        assertThrows(
                ServiceException.class,
                () -> sshAccountService.isSSHSetupCompleteForUserComputeResourcePreference(ctx, "cr-001", "tok-bad"));
    }

    @Test
    void setupSSHAccount_fetchesCredentialAndDelegates() throws Exception {
        SSHCredential cred = SSHCredential.getDefaultInstance();
        when(credentialHandler.getSSHCredential("tok-123", "testGateway")).thenReturn(cred);

        UserComputeResourcePreference pref = UserComputeResourcePreference.newBuilder()
                .setComputeResourceId("cr-001")
                .build();
        when(sshAccountManager.setupSSHAccount("testGateway", "cr-001", "testUser", cred))
                .thenReturn(pref);

        UserComputeResourcePreference result =
                sshAccountService.setupUserComputeResourcePreferencesForSSH(ctx, "cr-001", "testUser", "tok-123");

        assertNotNull(result);
        assertEquals("cr-001", result.getComputeResourceId());
        verify(credentialHandler).getSSHCredential("tok-123", "testGateway");
    }

    @Test
    void setupSSHAccount_wrapsSSHAccountManagerException() throws Exception {
        SSHCredential cred = SSHCredential.getDefaultInstance();
        when(credentialHandler.getSSHCredential("tok-123", "testGateway")).thenReturn(cred);
        when(sshAccountManager.setupSSHAccount(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("setup failed"));

        assertThrows(
                ServiceException.class,
                () -> sshAccountService.setupUserComputeResourcePreferencesForSSH(
                        ctx, "cr-001", "testUser", "tok-123"));
    }
}
