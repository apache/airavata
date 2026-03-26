package org.apache.airavata.service.ssh;

import org.apache.airavata.credential.store.server.CredentialStoreServerHandler;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.apache.airavata.accountprovisioning.SSHAccountManager;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SSHAccountServiceTest {

    @Mock CredentialStoreServerHandler credentialHandler;

    SSHAccountService sshAccountService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        sshAccountService = new SSHAccountService(credentialHandler);
        ctx = new RequestContext("testUser", "testGateway", "token123",
                Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void doesUserHaveSSHAccount_delegatesToSSHAccountManager() throws Exception {
        try (MockedStatic<SSHAccountManager> mock = mockStatic(SSHAccountManager.class)) {
            mock.when(() -> SSHAccountManager.doesUserHaveSSHAccount("testGateway", "cr-001", "testUser"))
                    .thenReturn(true);

            boolean result = sshAccountService.doesUserHaveSSHAccount(ctx, "cr-001", "testUser");

            assertTrue(result);
            mock.verify(() -> SSHAccountManager.doesUserHaveSSHAccount("testGateway", "cr-001", "testUser"));
        }
    }

    @Test
    void doesUserHaveSSHAccount_wrapsException() {
        try (MockedStatic<SSHAccountManager> mock = mockStatic(SSHAccountManager.class)) {
            mock.when(() -> SSHAccountManager.doesUserHaveSSHAccount(any(), any(), any()))
                    .thenThrow(new RuntimeException("connection failed"));

            assertThrows(ServiceException.class,
                    () -> sshAccountService.doesUserHaveSSHAccount(ctx, "cr-001", "testUser"));
        }
    }

    @Test
    void isSSHSetupComplete_fetchesCredentialAndDelegates() throws Exception {
        SSHCredential cred = new SSHCredential();
        when(credentialHandler.getSSHCredential("tok-123", "testGateway")).thenReturn(cred);

        try (MockedStatic<SSHAccountManager> mock = mockStatic(SSHAccountManager.class)) {
            mock.when(() -> SSHAccountManager.isSSHAccountSetupComplete("testGateway", "cr-001", "testUser", cred))
                    .thenReturn(false);

            boolean result = sshAccountService.isSSHSetupCompleteForUserComputeResourcePreference(
                    ctx, "cr-001", "tok-123");

            assertFalse(result);
            verify(credentialHandler).getSSHCredential("tok-123", "testGateway");
        }
    }

    @Test
    void isSSHSetupComplete_wrapsCredentialException() throws Exception {
        when(credentialHandler.getSSHCredential(any(), any())).thenThrow(new RuntimeException("store error"));

        assertThrows(ServiceException.class,
                () -> sshAccountService.isSSHSetupCompleteForUserComputeResourcePreference(ctx, "cr-001", "tok-bad"));
    }

    @Test
    void setupSSHAccount_fetchesCredentialAndDelegates() throws Exception {
        SSHCredential cred = new SSHCredential();
        when(credentialHandler.getSSHCredential("tok-123", "testGateway")).thenReturn(cred);

        UserComputeResourcePreference pref = new UserComputeResourcePreference();
        pref.setComputeResourceId("cr-001");

        try (MockedStatic<SSHAccountManager> mock = mockStatic(SSHAccountManager.class)) {
            mock.when(() -> SSHAccountManager.setupSSHAccount("testGateway", "cr-001", "testUser", cred))
                    .thenReturn(pref);

            UserComputeResourcePreference result = sshAccountService.setupUserComputeResourcePreferencesForSSH(
                    ctx, "cr-001", "testUser", "tok-123");

            assertNotNull(result);
            assertEquals("cr-001", result.getComputeResourceId());
            verify(credentialHandler).getSSHCredential("tok-123", "testGateway");
        }
    }

    @Test
    void setupSSHAccount_wrapsSSHAccountManagerException() throws Exception {
        SSHCredential cred = new SSHCredential();
        when(credentialHandler.getSSHCredential("tok-123", "testGateway")).thenReturn(cred);

        try (MockedStatic<SSHAccountManager> mock = mockStatic(SSHAccountManager.class)) {
            mock.when(() -> SSHAccountManager.setupSSHAccount(any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("setup failed"));

            assertThrows(ServiceException.class,
                    () -> sshAccountService.setupUserComputeResourcePreferencesForSSH(
                            ctx, "cr-001", "testUser", "tok-123"));
        }
    }
}
