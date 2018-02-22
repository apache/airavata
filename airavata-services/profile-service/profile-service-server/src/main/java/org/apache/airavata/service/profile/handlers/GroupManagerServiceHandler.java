package org.apache.airavata.service.profile.handlers;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.group.GroupModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.service.profile.groupmanager.cpi.GroupManagerService;
import org.apache.airavata.service.profile.groupmanager.cpi.exception.GroupManagerServiceException;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.airavata.sharing.registry.client.SharingRegistryServiceClientFactory;
import org.apache.airavata.sharing.registry.models.GroupType;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.apache.airavata.sharing.registry.models.UserGroup;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupManagerServiceHandler implements GroupManagerService.Iface {

    private static final Logger logger = LoggerFactory.getLogger(GroupManagerServiceHandler.class);

    public GroupManagerServiceHandler() {

    }

    @Override
    @SecurityCheck
    public String createGroup(AuthzToken authzToken, GroupModel groupModel) throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            //TODO Validations for authorization
            SharingRegistryService.Client sharingClient = getSharingRegistryServiceClient();

            UserGroup sharingUserGroup = new UserGroup();
            sharingUserGroup.setGroupId(UUID.randomUUID().toString());
            sharingUserGroup.setName(groupModel.getName());
            sharingUserGroup.setDescription(groupModel.getDescription());
            sharingUserGroup.setGroupType(GroupType.USER_LEVEL_GROUP);
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            sharingUserGroup.setDomainId(gatewayId);
            String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
            sharingUserGroup.setOwnerId(username + "@" + gatewayId);

            String groupId = sharingClient.createGroup(sharingUserGroup);
            sharingClient.addUsersToGroup(gatewayId, groupModel.getMembers(), groupId);
            return groupId;
        }
        catch (Exception e) {
            String msg = "Error Creating Group" ;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGroup(AuthzToken authzToken, GroupModel groupModel) throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            //TODO Validations for authorization
            SharingRegistryService.Client sharingClient = getSharingRegistryServiceClient();

            UserGroup sharingUserGroup = new UserGroup();
            sharingUserGroup.setGroupId(groupModel.getId());
            sharingUserGroup.setName(groupModel.getName());
            sharingUserGroup.setDescription(groupModel.getDescription());
            sharingUserGroup.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingUserGroup.setDomainId(authzToken.getClaimsMap().get(Constants.GATEWAY_ID));

            //adding and removal of users should be handle separately
            sharingClient.updateGroup(sharingUserGroup);
            return true;
        }
        catch (Exception e) {
            String msg = "Error Updating Group" ;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGroup(AuthzToken authzToken, String groupId, String ownerId) throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            //TODO Validations for authorization
            SharingRegistryService.Client sharingClient = getSharingRegistryServiceClient();

            sharingClient.deleteGroup(authzToken.getClaimsMap().get(Constants.GATEWAY_ID), groupId);
            return true;
        }
        catch (Exception e) {
            String msg = "Error Deleting Group. Group ID: " + groupId ;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public GroupModel getGroup(AuthzToken authzToken, String groupId) throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Client sharingClient = getSharingRegistryServiceClient();
            UserGroup userGroup = sharingClient.getGroup(authzToken.getClaimsMap().get(Constants.GATEWAY_ID), groupId);

            GroupModel groupModel = new GroupModel();
            groupModel.setId(userGroup.getGroupId());
            groupModel.setName(userGroup.getName());
            groupModel.setDescription(userGroup.getDescription());
            groupModel.setOwnerId(userGroup.getOwnerId());

            sharingClient.getGroupMembersOfTypeUser(authzToken.getClaimsMap().get(Constants.GATEWAY_ID), groupId, 0, -1).stream().forEach(user->
                    groupModel.addToMembers(user.getUserId())
            );

            return groupModel;
        }
        catch (Exception e) {
            String msg = "Error Retreiving Group. Group ID: " + groupId ;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<GroupModel> getAllGroupsUserBelongs(AuthzToken authzToken, String userName) throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Client sharingClient = getSharingRegistryServiceClient();
            List<GroupModel> groupModels = new ArrayList<GroupModel>();
            List<UserGroup> userGroups = sharingClient.getAllMemberGroupsForUser(authzToken.getClaimsMap().get(Constants.GATEWAY_ID), userName);

            for (UserGroup userGroup: userGroups) {
                GroupModel groupModel = new GroupModel();
                groupModel.setId(userGroup.getGroupId());
                groupModel.setName(userGroup.getName());
                groupModel.setDescription(userGroup.getDescription());
                groupModel.setOwnerId(userGroup.getOwnerId());

                sharingClient.getGroupMembersOfTypeUser(authzToken.getClaimsMap().get(Constants.GATEWAY_ID), userGroup.getGroupId(), 0, -1).stream().forEach(user->
                        groupModel.addToMembers(user.getUserId()));

                groupModels.add(groupModel);
            }
            return groupModels;
        }
        catch (Exception e) {
            String msg = "Error Retreiving All Groups for User. User ID: " + userName ;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean transferGroupOwnership(AuthzToken authzToken, String groupId, String newOwnerId) throws GroupManagerServiceException, AuthorizationException, TException {
       try{
           SharingRegistryService.Client sharingClient = getSharingRegistryServiceClient();
           return sharingClient.transferGroupOwnership(authzToken.getClaimsMap().get(Constants.GATEWAY_ID), groupId, newOwnerId);
       }
       catch (Exception e) {
           String msg = "Error Transferring Group Ownership";
           logger.error(msg, e);
           GroupManagerServiceException exception = new GroupManagerServiceException();
           exception.setMessage(msg + " More info : " + e.getMessage());
           throw exception;
       }

    }

    @Override
    @SecurityCheck
    public boolean addGroupAdmins(AuthzToken authzToken, String groupId, List<String> adminIds) throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Client sharingClient = getSharingRegistryServiceClient();
            return sharingClient.addGroupAdmins(authzToken.getClaimsMap().get(Constants.GATEWAY_ID), groupId, adminIds);
        }
        catch (Exception e) {
            String msg = "Error Adding Admins to Group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupAdmins(AuthzToken authzToken, String groupId, List<String> adminIds) throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Client sharingClient = getSharingRegistryServiceClient();
            return sharingClient.removeGroupAdmins(authzToken.getClaimsMap().get(Constants.GATEWAY_ID), groupId, adminIds);
        }
        catch (Exception e) {
            String msg = "Error Removing Admins from the Group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean hasAdminAccess(AuthzToken authzToken, String groupId, String adminId) throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Client sharingClient = getSharingRegistryServiceClient();
            return sharingClient.hasAdminAccess(authzToken.getClaimsMap().get(Constants.GATEWAY_ID), groupId, adminId);
        }
        catch (Exception e) {
            String msg = "Error Checking Admin Access for the Group. Group ID: " + groupId + " Admin ID: " + adminId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean hasOwnerAccess(AuthzToken authzToken, String groupId, String ownerId) throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Client sharingClient = getSharingRegistryServiceClient();
            return sharingClient.hasOwnerAccess(authzToken.getClaimsMap().get(Constants.GATEWAY_ID), groupId, ownerId);
        }
        catch (Exception e) {
            String msg = "Error Checking Owner Access for the Group. Group ID: " + groupId + " Owner ID: " + ownerId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    private SharingRegistryService.Client getSharingRegistryServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getSharingRegistryPort());
        final String serverHost = ServerSettings.getSharingRegistryHost();
        try {
            return SharingRegistryServiceClientFactory.createSharingRegistryClient(serverHost, serverPort);
        } catch (SharingRegistryException e) {
            throw new TException("Unable to create sharing registry client...", e);
        }
    }
}
