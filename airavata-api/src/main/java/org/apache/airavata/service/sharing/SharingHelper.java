package org.apache.airavata.service.sharing;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.group.ResourcePermissionType;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.security.GatewayGroupsInitializer;
import org.apache.airavata.sharing.registry.models.Entity;
import org.apache.airavata.sharing.registry.models.PermissionType;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.apache.thrift.TException;

import java.util.Arrays;

public class SharingHelper {

    private SharingHelper() {
        // utility class
    }

    public static boolean isSharingEnabled() {
        try {
            return ServerSettings.isEnableSharing();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean userHasAccess(SharingRegistryServerHandler sharingHandler,
            String gatewayId, String userId, String entityId, ResourcePermissionType permissionType) {
        String qualifiedUserId = userId.contains("@") ? userId : userId + "@" + gatewayId;
        try {
            boolean hasOwnerAccess = sharingHandler.userHasAccess(
                    gatewayId, qualifiedUserId, entityId, gatewayId + ":" + ResourcePermissionType.OWNER);
            if (permissionType.equals(ResourcePermissionType.OWNER)) return hasOwnerAccess;
            return hasOwnerAccess || sharingHandler.userHasAccess(
                    gatewayId, qualifiedUserId, entityId, gatewayId + ":" + permissionType);
        } catch (Exception e) {
            throw new RuntimeException("Unable to check if user has access", e);
        }
    }

    public static GatewayGroups retrieveGatewayGroups(RegistryServerHandler registryHandler, String gatewayId) throws TException {
        if (registryHandler.isGatewayGroupsExists(gatewayId)) {
            return registryHandler.getGatewayGroups(gatewayId);
        }
        return GatewayGroupsInitializer.initializeGatewayGroups(gatewayId);
    }

    public static void shareEntityWithAdminGatewayGroups(
            SharingRegistryServerHandler sharingHandler,
            RegistryServerHandler registryHandler,
            Entity entity) throws TException {
        String domainId = entity.getDomainId();
        GatewayGroups gatewayGroups = retrieveGatewayGroups(registryHandler, domainId);
        createManageSharingPermissionTypeIfMissing(sharingHandler, domainId);
        sharingHandler.shareEntityWithGroups(domainId, entity.getEntityId(),
                Arrays.asList(gatewayGroups.getAdminsGroupId()), domainId + ":MANAGE_SHARING", true);
        sharingHandler.shareEntityWithGroups(domainId, entity.getEntityId(),
                Arrays.asList(gatewayGroups.getAdminsGroupId()), domainId + ":WRITE", true);
        sharingHandler.shareEntityWithGroups(domainId, entity.getEntityId(),
                Arrays.asList(gatewayGroups.getAdminsGroupId(), gatewayGroups.getReadOnlyAdminsGroupId()),
                domainId + ":READ", true);
    }

    public static void createManageSharingPermissionTypeIfMissing(
            SharingRegistryServerHandler sharingHandler, String domainId) throws TException {
        String permissionTypeId = domainId + ":MANAGE_SHARING";
        if (!sharingHandler.isPermissionExists(domainId, permissionTypeId)) {
            PermissionType permissionType = new PermissionType();
            permissionType.setPermissionTypeId(permissionTypeId);
            permissionType.setDomainId(domainId);
            permissionType.setName("MANAGE_SHARING");
            permissionType.setDescription("Manage sharing permission type");
            sharingHandler.createPermissionType(permissionType);
        }
    }
}
