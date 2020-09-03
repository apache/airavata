package org.apache.airavata.service.profile.handlers;

import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.CustosUtils;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.group.GroupModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.service.profile.groupmanager.cpi.GroupManagerService;
import org.apache.airavata.service.profile.groupmanager.cpi.exception.GroupManagerServiceException;
import org.apache.airavata.service.profile.groupmanager.cpi.group_manager_cpiConstants;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.custos.group.management.client.GroupManagementClient;
import org.apache.custos.iam.service.GroupRepresentation;
import org.apache.custos.iam.service.GroupsResponse;
import org.apache.custos.iam.service.OperationStatus;
import org.apache.custos.user.profile.service.DefaultGroupMembershipTypes;
import org.apache.custos.user.profile.service.GetAllGroupsResponse;
import org.apache.custos.user.profile.service.GetAllUserProfilesResponse;
import org.apache.custos.user.profile.service.Group;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GroupManagerServiceHandler implements GroupManagerService.Iface {

    private static final Logger logger = LoggerFactory.getLogger(GroupManagerServiceHandler.class);

    private GroupManagementClient groupManagementClient;

    public GroupManagerServiceHandler() {
        try {
            groupManagementClient = CustosUtils.getCustosClientProvider().getGroupManagementClient();

        } catch (Exception ex) {
            logger.error("Error while initiating Custos Group Management Client ");
        }
    }

    @Override
    public String getAPIVersion() throws TException {
        return group_manager_cpiConstants.GROUP_MANAGER_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public String createGroup(AuthzToken authzToken, GroupModel groupModel) throws GroupManagerServiceException, AuthorizationException, TException {
        try {

            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            GroupRepresentation groupRepresentation = GroupRepresentation
                    .newBuilder()
                    .setName(groupModel.getName())
                    .setDescription(groupModel.getDescription())
                    .setOwnerId(getUserId(authzToken))
                    .build();

            GroupRepresentation[] representations = {groupRepresentation};

            GroupsResponse response = groupManagementClient.createGroup(custosId, representations);
            String groupId = response.getGroupsList().get(0).getId();

            for (String id : groupModel.getMembers()) {
                groupManagementClient.addUserToGroup(custosId, id, groupId, DefaultGroupMembershipTypes.MEMBER.name());
            }

            for (String id : groupModel.getAdmins()) {
                groupManagementClient.addUserToGroup(custosId, id, groupId, DefaultGroupMembershipTypes.ADMIN.name());
            }

            return groupId;

        } catch (Exception e) {
            String msg = "Error Creating Group";
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
            String userId = getUserId(authzToken);
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            OperationStatus status = groupManagementClient.hasAccess(custosId, groupModel.getId(), userId,
                    DefaultGroupMembershipTypes.OWNER.name());

            OperationStatus exStatus = groupManagementClient.hasAccess(custosId, groupModel.getId(), userId,
                    DefaultGroupMembershipTypes.ADMIN.name());

            if (!(status.getStatus() || exStatus.getStatus())) {
                throw new GroupManagerServiceException("User does not have access to update the group");
            }

            GroupRepresentation groupRepresentation = GroupRepresentation
                    .newBuilder()
                    .setName(groupModel.getName())
                    .setDescription(groupModel.getDescription())
                    .setId(groupModel.getId())
                    .setOwnerId(groupModel.getOwnerId())
                    .build();

            groupManagementClient.updateGroup(custosId, groupRepresentation);

            return true;

        } catch (Exception e) {
            String msg = "Error Updating Group";
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
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
            String userId = getUserId(authzToken);

            OperationStatus status = groupManagementClient.hasAccess(custosId, groupId, userId,
                    DefaultGroupMembershipTypes.OWNER.name());

            if (!status.getStatus()) {
                throw new GroupManagerServiceException("User does not have access to delete group");
            }

            GroupRepresentation groupRepresentation = GroupRepresentation.newBuilder().setId(groupId).build();

            OperationStatus st = groupManagementClient.deleteGroup(custosId, groupRepresentation);

            return st.getStatus();

        } catch (Exception e) {
            String msg = "Error Deleting Group. Group ID: " + groupId;
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

            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            GroupRepresentation groupRepresentation = groupManagementClient.findGroup(custosId, null, groupId);

            GroupModel groupModel = convertToAiravataGroupModel(custosId, groupRepresentation);

            return groupModel;
        } catch (Exception e) {
            String msg = "Error Retreiving Group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<GroupModel> getGroups(AuthzToken authzToken) throws GroupManagerServiceException, AuthorizationException, TException {
        final String domainId = getDomainId(authzToken);
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

        try {

            GroupsResponse response = groupManagementClient.getAllGroups(custosId);

            List<GroupRepresentation> groupRepresentations = response.getGroupsList();

            return convertToAiravataGroupModels(custosId, groupRepresentations);
        } catch (Exception e) {
            String msg = "Error Retrieving Groups. Domain ID: " + domainId;
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

            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            GetAllGroupsResponse response = groupManagementClient.getAllGroupsOfUser(custosId, userName);

            List<GroupModel> groupModels = new ArrayList<GroupModel>();

            groupModels = convertToAiravataGroupModels(custosId, response.getGroupsList());

            return groupModels;
        } catch (Exception e) {
            String msg = "Error Retreiving All Groups for User. User ID: " + userName;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean addUsersToGroup(AuthzToken authzToken, List<String> userIds, String groupId) throws GroupManagerServiceException, AuthorizationException, TException {
        try {

            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
            String userId = getUserId(authzToken);

            OperationStatus status = groupManagementClient.hasAccess(custosId, groupId, userId, "OWNER");

            OperationStatus exStatus = groupManagementClient.hasAccess(custosId, groupId, userId, "ADMIN");

            if (!(status.getStatus() || exStatus.getStatus())) {
                throw new GroupManagerServiceException("User does not have access to add users to the group");
            }

            for (String usr : userIds) {

                groupManagementClient.addUserToGroup(custosId, usr, groupId, DefaultGroupMembershipTypes.MEMBER.name());
            }

            return true;

        } catch (Exception e) {
            String msg = "Error adding users to group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean removeUsersFromGroup(AuthzToken authzToken, List<String> userIds, String groupId) throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            String userId = getUserId(authzToken);
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            OperationStatus status = groupManagementClient.hasAccess(custosId, groupId, userId, "OWNER");


            OperationStatus exStatus = groupManagementClient.hasAccess(custosId, groupId, userId, "ADMIN");

            if (!(status.getStatus() || exStatus.getStatus())) {
                throw new GroupManagerServiceException("User does not have access to remove users to the group");
            }

            for (String id : userIds) {
                groupManagementClient.removeUserFromGroup(custosId, id, groupId);
            }

            return true;
        } catch (Exception e) {
            String msg = "Error remove users to group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean transferGroupOwnership(AuthzToken authzToken, String groupId, String newOwnerId) throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            String userId = getUserId(authzToken);
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            OperationStatus status = groupManagementClient.hasAccess(custosId, groupId, userId,
                    DefaultGroupMembershipTypes.OWNER.name());

            if (!status.getStatus()) {
                throw new GroupManagerServiceException("User does not have access to transfer ownership group");
            }
            OperationStatus st = groupManagementClient.changeUserMembershipType(custosId, newOwnerId, groupId,
                    DefaultGroupMembershipTypes.OWNER.name());
            return st.getStatus();
        } catch (Exception e) {
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
            String userId = getUserId(authzToken);
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            OperationStatus status = groupManagementClient.hasAccess(custosId, groupId, userId,
                    DefaultGroupMembershipTypes.OWNER.name());

            if (!status.getStatus()) {
                throw new GroupManagerServiceException("User does not have access to transfer ownership group");
            }

            for (String id : adminIds) {

                GetAllGroupsResponse response = groupManagementClient.getAllGroupsOfUser(custosId, id);

                boolean exist = false;
                if (response.getGroupsList() != null && !response.getGroupsList().isEmpty()) {
                    for (Group group : response.getGroupsList()) {

                        if (group.getId().equals(groupId)) {
                            exist = true;
                            break;
                        }
                    }
                }

                if (exist) {
                    groupManagementClient.changeUserMembershipType(custosId, id, groupId, DefaultGroupMembershipTypes.ADMIN.name());
                } else {
                    groupManagementClient.addUserToGroup(custosId, id, groupId, DefaultGroupMembershipTypes.ADMIN.name());
                }
            }

            return true;
        } catch (Exception e) {
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
            String userId = getUserId(authzToken);
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            OperationStatus status = groupManagementClient.hasAccess(custosId, groupId, userId,
                    DefaultGroupMembershipTypes.OWNER.name());

            if (!status.getStatus()) {
                throw new GroupManagerServiceException("User does not have access to transfer ownership group");
            }

            for (String id : adminIds) {
                groupManagementClient.changeUserMembershipType(custosId,
                        id, groupId, DefaultGroupMembershipTypes.MEMBER.name());
            }
            return true;
        } catch (Exception e) {
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
            String userId = getUserId(authzToken);
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            OperationStatus status = groupManagementClient.hasAccess(custosId, groupId, adminId,
                    DefaultGroupMembershipTypes.ADMIN.name());

            return status.getStatus();

        } catch (Exception e) {
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
            String userId = getUserId(authzToken);
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            OperationStatus status = groupManagementClient.hasAccess(custosId, groupId, ownerId,
                    DefaultGroupMembershipTypes.OWNER.name());

            return status.getStatus();
        } catch (Exception e) {
            String msg = "Error Checking Owner Access for the Group. Group ID: " + groupId + " Owner ID: " + ownerId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }


    private String getDomainId(AuthzToken authzToken) {
        return authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
    }

    private String getUserId(AuthzToken authzToken) {
        return authzToken.getClaimsMap().get(Constants.USER_NAME);
    }


    private List<GroupModel> convertToAiravataGroupModels(String custosId, List userGroups) throws TException {

        List<GroupModel> groupModels = new ArrayList<>();

        if (userGroups != null && !userGroups.isEmpty()) {

            for (Object userGroup : userGroups) {
                GroupModel groupModel = null;
                if (userGroup instanceof GroupRepresentation) {
                    groupModel = convertToAiravataGroupModel(custosId, (GroupRepresentation) userGroup);
                } else {
                    groupModel = convertToAiravataGroupModel(custosId, (Group) userGroup);
                }

                groupModels.add(groupModel);
            }
        }
        return groupModels;
    }


    private GroupModel convertToAiravataGroupModel(String cutosId, GroupRepresentation userGroup) throws TException {
        GroupModel groupModel = new GroupModel();
        groupModel.setId(userGroup.getId());
        groupModel.setName(userGroup.getName());
        groupModel.setDescription(userGroup.getDescription());
        groupModel.setOwnerId(userGroup.getOwnerId());

        GetAllUserProfilesResponse response = groupManagementClient.getAllChildUsers(cutosId, userGroup.getId());
        List<String> admins = new ArrayList<>();
        List<String> member = new ArrayList<>();

        if (response.getProfilesList() != null && !response.getProfilesList().isEmpty()) {
            for (org.apache.custos.user.profile.service.UserProfile profile : response.getProfilesList()) {
                if (profile.getMembershipType().equals(DefaultGroupMembershipTypes.ADMIN)) {
                    admins.add(profile.getUsername());
                    member.add(profile.getUsername());
                } else if (profile.getMembershipType().equals(DefaultGroupMembershipTypes.MEMBER)) {
                    member.add(profile.getUsername());
                }
            }
        }

        member.add(userGroup.getOwnerId());
        groupModel.setAdmins(admins);
        groupModel.setMembers(member);

        return groupModel;
    }


    private GroupModel convertToAiravataGroupModel(String custosId, Group userGroup) throws TException {
        GroupModel groupModel = new GroupModel();
        groupModel.setId(userGroup.getId());
        groupModel.setName(userGroup.getName());
        groupModel.setDescription(userGroup.getDescription());
        groupModel.setOwnerId(userGroup.getOwnerId());


        GetAllUserProfilesResponse response = groupManagementClient.getAllChildUsers(custosId, userGroup.getId());
        List<String> admins = new ArrayList<>();
        List<String> member = new ArrayList<>();

        if (response.getProfilesList() != null && !response.getProfilesList().isEmpty()) {
            for (org.apache.custos.user.profile.service.UserProfile profile : response.getProfilesList()) {
                if (profile.getMembershipType().equals(DefaultGroupMembershipTypes.ADMIN)) {
                    admins.add(profile.getUsername());
                } else if (profile.getMembershipType().equals(DefaultGroupMembershipTypes.MEMBER)) {
                    member.add(profile.getUsername());
                }
            }
        }
        member.add(userGroup.getOwnerId());
        groupModel.setAdmins(admins);
        groupModel.setMembers(member);
        return groupModel;
    }


}
