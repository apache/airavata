package org.apache.airavata.iam;

import org.apache.airavata.common.ServerSettings;
import org.apache.airavata.iam.service.GatewayService;
import org.apache.airavata.iam.service.SharingService;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.sharing.registry.models.proto.GroupCardinality;
import org.apache.airavata.sharing.registry.models.proto.GroupType;
import org.apache.airavata.sharing.registry.models.proto.UserGroup;
import org.apache.airavata.common.AiravataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Create and save an initial set of user management groups for a gateway.
 */
@Component
public class GatewayGroupsInitializer {

    private static final Logger logger = LoggerFactory.getLogger(GatewayGroupsInitializer.class);

    private final SharingService sharingService;
    private final GatewayService gatewayService;

    public GatewayGroupsInitializer(
            SharingService sharingService, GatewayService gatewayService) {
        this.sharingService = sharingService;
        this.gatewayService = gatewayService;
    }

    public synchronized GatewayGroups initializeGatewayGroups(String gatewayId) {
        try {
            return initialize(gatewayId);
        } catch (Exception e) {
            logger.error("Failed to initialize a GatewayGroups instance for gateway: {}", gatewayId, e);
            throw new RuntimeException("Failed to initialize a GatewayGroups instance for gateway: " + gatewayId, e);
        }
    }

    private GatewayGroups initialize(String gatewayId) throws Exception {

        logger.info("Creating a GatewayGroups instance for gateway " + gatewayId + " ...");

        GatewayGroups.Builder gatewayGroupsBuilder = GatewayGroups.newBuilder().setGatewayId(gatewayId);

        String adminOwnerUsername = getAdminOwnerUsername(gatewayId);
        String ownerId = adminOwnerUsername + "@" + gatewayId;
        if (!sharingService.isUserExists(gatewayId, ownerId)) {
            sharingService.createUser(ownerId, gatewayId, adminOwnerUsername);
        }

        // Gateway Users
        UserGroup gatewayUsersGroup = createGroup(gatewayId, ownerId, "Gateway Users",
                "Default group for users of the gateway.");
        gatewayGroupsBuilder.setDefaultGatewayUsersGroupId(gatewayUsersGroup.getGroupId());
        // Admin Users
        UserGroup adminUsersGroup = createGroup(gatewayId, ownerId, "Admin Users", "Admin users group.");
        gatewayGroupsBuilder.setAdminsGroupId(adminUsersGroup.getGroupId());
        // Read Only Admin Users
        UserGroup readOnlyAdminsGroup = createGroup(gatewayId, ownerId, "Read Only Admin Users",
                "Group of admin users with read-only access.");
        gatewayGroupsBuilder.setReadOnlyAdminsGroupId(readOnlyAdminsGroup.getGroupId());
        GatewayGroups gatewayGroups = gatewayGroupsBuilder.build();

        try {
            gatewayService.createGatewayGroups(gatewayGroups);
        } catch (Exception e) {
            logger.error(
                    "Gateway groups created in Sharing Catalog failed to save GatewayGroups entity in Registry", e);
            throw e;
        }

        return gatewayGroups;
    }

    private UserGroup createGroup(String gatewayId, String ownerId, String groupName, String groupDescription)
            throws Exception {

        UserGroup userGroup = UserGroup.newBuilder()
                .setGroupId(AiravataUtils.getId(groupName))
                .setDomainId(gatewayId)
                .setGroupCardinality(GroupCardinality.MULTI_USER)
                .setCreatedTime(System.currentTimeMillis())
                .setUpdatedTime(System.currentTimeMillis())
                .setName(groupName)
                .setDescription(groupDescription)
                .setOwnerId(ownerId)
                .setGroupType(GroupType.DOMAIN_LEVEL_GROUP)
                .build();
        sharingService.createGroup(userGroup);

        return userGroup;
    }

    private String getAdminOwnerUsername(String gatewayId) throws Exception {
        String defaultUser = ServerSettings.getDefaultUser();
        return defaultUser;
    }
}
