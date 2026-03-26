package org.apache.airavata.service.credential;

import org.apache.airavata.credential.store.server.CredentialStoreServerHandler;
import org.apache.airavata.model.credential.store.CredentialSummary;
import org.apache.airavata.model.credential.store.SummaryType;
import org.apache.airavata.model.group.ResourcePermissionType;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceAuthorizationException;
import org.apache.airavata.sharing.registry.models.Entity;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock CredentialStoreServerHandler credentialHandler;
    @Mock SharingRegistryServerHandler sharingHandler;

    CredentialService credentialService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        credentialService = new CredentialService(credentialHandler, sharingHandler);
        ctx = new RequestContext("testUser", "testGateway", "token123",
                Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void generateAndRegisterSSHKeys_returnsToken() throws Exception {
        when(credentialHandler.addSSHCredential(any())).thenReturn("ssh-token-1");

        String result = credentialService.generateAndRegisterSSHKeys(ctx, "my ssh key");

        assertEquals("ssh-token-1", result);
        verify(credentialHandler).addSSHCredential(any());
        verify(sharingHandler).createEntity(any());
    }

    @Test
    void generateAndRegisterSSHKeys_rollsBackOnSharingFailure() throws Exception {
        when(credentialHandler.addSSHCredential(any())).thenReturn("ssh-token-1");
        doThrow(new RuntimeException("sharing error")).when(sharingHandler).createEntity(any());

        assertThrows(Exception.class, () -> credentialService.generateAndRegisterSSHKeys(ctx, "my ssh key"));
        verify(credentialHandler).deleteSSHCredential("ssh-token-1", "testGateway");
    }

    @Test
    void registerPwdCredential_returnsToken() throws Exception {
        when(credentialHandler.addPasswordCredential(any())).thenReturn("pwd-token-1");

        String result = credentialService.registerPwdCredential(ctx, "loginUser", "secret", "my pwd");

        assertEquals("pwd-token-1", result);
        verify(credentialHandler).addPasswordCredential(any());
        verify(sharingHandler).createEntity(any());
    }

    @Test
    void registerPwdCredential_rollsBackOnSharingFailure() throws Exception {
        when(credentialHandler.addPasswordCredential(any())).thenReturn("pwd-token-1");
        doThrow(new RuntimeException("sharing error")).when(sharingHandler).createEntity(any());

        assertThrows(Exception.class, () -> credentialService.registerPwdCredential(ctx, "loginUser", "secret", "my pwd"));
        verify(credentialHandler).deletePWDCredential("pwd-token-1", "testGateway");
    }

    @Test
    void getCredentialSummary_delegatesToCredentialHandler() throws Exception {
        when(sharingHandler.userHasAccess(eq("testGateway"), eq("testUser@testGateway"), eq("tok-1"),
                eq("testGateway:" + ResourcePermissionType.OWNER))).thenReturn(true);
        CredentialSummary summary = new CredentialSummary();
        summary.setToken("tok-1");
        when(credentialHandler.getCredentialSummary("tok-1", "testGateway")).thenReturn(summary);

        CredentialSummary result = credentialService.getCredentialSummary(ctx, "tok-1");

        assertNotNull(result);
        assertEquals("tok-1", result.getToken());
        verify(credentialHandler).getCredentialSummary("tok-1", "testGateway");
    }

    @Test
    void getCredentialSummary_throwsAuthorizationExceptionWhenNoAccess() throws Exception {
        when(sharingHandler.userHasAccess(any(), any(), any(), any())).thenReturn(false);

        assertThrows(ServiceAuthorizationException.class, () -> credentialService.getCredentialSummary(ctx, "tok-1"));
        verify(credentialHandler, never()).getCredentialSummary(any(), any());
    }

    @Test
    void deleteSSHPubKey_delegatesToCredentialHandler() throws Exception {
        when(sharingHandler.userHasAccess(eq("testGateway"), eq("testUser@testGateway"), eq("tok-1"),
                eq("testGateway:" + ResourcePermissionType.OWNER))).thenReturn(true);
        when(credentialHandler.deleteSSHCredential("tok-1", "testGateway")).thenReturn(true);

        boolean result = credentialService.deleteSSHPubKey(ctx, "tok-1");

        assertTrue(result);
        verify(credentialHandler).deleteSSHCredential("tok-1", "testGateway");
    }

    @Test
    void deleteSSHPubKey_throwsAuthorizationExceptionWhenNoAccess() throws Exception {
        when(sharingHandler.userHasAccess(any(), any(), any(), any())).thenReturn(false);

        assertThrows(ServiceAuthorizationException.class, () -> credentialService.deleteSSHPubKey(ctx, "tok-1"));
        verify(credentialHandler, never()).deleteSSHCredential(any(), any());
    }

    @Test
    void deletePWDCredential_delegatesToCredentialHandler() throws Exception {
        when(sharingHandler.userHasAccess(eq("testGateway"), eq("testUser@testGateway"), eq("tok-1"),
                eq("testGateway:" + ResourcePermissionType.OWNER))).thenReturn(true);
        when(credentialHandler.deletePWDCredential("tok-1", "testGateway")).thenReturn(true);

        boolean result = credentialService.deletePWDCredential(ctx, "tok-1");

        assertTrue(result);
        verify(credentialHandler).deletePWDCredential("tok-1", "testGateway");
    }

    @Test
    void getAllCredentialSummaries_delegatesToCredentialHandler() throws Exception {
        Entity entity = new Entity();
        entity.setEntityId("tok-1");
        when(sharingHandler.searchEntities(eq("testGateway"), eq("testUser@testGateway"), any(), eq(0), eq(-1)))
                .thenReturn(List.of(entity));
        CredentialSummary summary = new CredentialSummary();
        summary.setToken("tok-1");
        when(credentialHandler.getAllCredentialSummaries(eq(SummaryType.SSH), any(), eq("testGateway")))
                .thenReturn(List.of(summary));

        List<CredentialSummary> result = credentialService.getAllCredentialSummaries(ctx, SummaryType.SSH);

        assertEquals(1, result.size());
        verify(credentialHandler).getAllCredentialSummaries(eq(SummaryType.SSH), any(), eq("testGateway"));
    }
}
