package org.apache.airavata.service.resourceprofile;

import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserResourceProfileServiceTest {

    @Mock RegistryServerHandler registryHandler;

    UserResourceProfileService service;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        service = new UserResourceProfileService(registryHandler);
        ctx = new RequestContext("testUser", "testGateway", "token123",
                Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void registerUserResourceProfile_returnsId() throws Exception {
        UserResourceProfile profile = new UserResourceProfile();
        profile.setUserId("testUser");
        profile.setGatewayID("testGateway");
        when(registryHandler.registerUserResourceProfile(profile)).thenReturn("testUser");

        String result = service.registerUserResourceProfile(ctx, profile);

        assertEquals("testUser", result);
    }

    @Test
    void isUserResourceProfileExists_delegatesToRegistry() throws Exception {
        when(registryHandler.isUserResourceProfileExists("testUser", "testGateway")).thenReturn(true);

        boolean result = service.isUserResourceProfileExists(ctx, "testUser", "testGateway");

        assertTrue(result);
    }

    @Test
    void getUserResourceProfile_delegatesToRegistry() throws Exception {
        UserResourceProfile profile = new UserResourceProfile();
        profile.setUserId("testUser");
        when(registryHandler.getUserResourceProfile("testUser", "testGateway")).thenReturn(profile);

        UserResourceProfile result = service.getUserResourceProfile(ctx, "testUser", "testGateway");

        assertNotNull(result);
        assertEquals("testUser", result.getUserId());
    }

    @Test
    void deleteUserResourceProfile_delegatesToRegistry() throws Exception {
        when(registryHandler.deleteUserResourceProfile("testUser", "testGateway")).thenReturn(true);

        boolean result = service.deleteUserResourceProfile(ctx, "testUser", "testGateway");

        assertTrue(result);
    }

    @Test
    void addUserComputeResourcePreference_delegatesToRegistry() throws Exception {
        UserComputeResourcePreference pref = new UserComputeResourcePreference();
        when(registryHandler.addUserComputeResourcePreference(
                "testUser", "testGateway", "compute-1", pref)).thenReturn(true);

        boolean result = service.addUserComputeResourcePreference(ctx, "testUser", "testGateway", "compute-1", pref);

        assertTrue(result);
    }

    @Test
    void getAllUserComputeResourcePreferences_delegatesToRegistry() throws Exception {
        List<UserComputeResourcePreference> prefs = List.of(new UserComputeResourcePreference());
        when(registryHandler.getAllUserComputeResourcePreferences("testUser", "testGateway")).thenReturn(prefs);

        List<UserComputeResourcePreference> result =
                service.getAllUserComputeResourcePreferences(ctx, "testUser", "testGateway");

        assertEquals(1, result.size());
    }

    @Test
    void getAllUserResourceProfiles_delegatesToRegistry() throws Exception {
        List<UserResourceProfile> profiles = List.of(new UserResourceProfile());
        when(registryHandler.getAllUserResourceProfiles()).thenReturn(profiles);

        List<UserResourceProfile> result = service.getAllUserResourceProfiles(ctx);

        assertEquals(1, result.size());
    }

    @Test
    void getLatestQueueStatuses_delegatesToRegistry() throws Exception {
        List<QueueStatusModel> statuses = List.of(new QueueStatusModel());
        when(registryHandler.getLatestQueueStatuses()).thenReturn(statuses);

        List<QueueStatusModel> result = service.getLatestQueueStatuses(ctx);

        assertEquals(1, result.size());
    }

    @Test
    void addUserStoragePreference_delegatesToRegistry() throws Exception {
        UserStoragePreference pref = new UserStoragePreference();
        when(registryHandler.addUserStoragePreference("testUser", "testGateway", "storage-1", pref)).thenReturn(true);

        boolean result = service.addUserStoragePreference(ctx, "testUser", "testGateway", "storage-1", pref);

        assertTrue(result);
    }

    @Test
    void registryException_wrappedAsServiceException() throws Exception {
        when(registryHandler.getUserResourceProfile("bad-user", "testGateway"))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(ServiceException.class, () ->
                service.getUserResourceProfile(ctx, "bad-user", "testGateway"));
    }
}
