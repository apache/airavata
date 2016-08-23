package org.apache.airavata.grouper;

import org.apache.airavata.grouper.group.Group;
import org.apache.airavata.grouper.permission.PermissionAction;
import org.apache.airavata.grouper.resource.Resource;
import org.apache.airavata.grouper.resource.ResourceType;

import java.util.List;
import java.util.Set;

public interface GroupManagerCPI {
    void createResource(Resource projectResource);

    boolean isResourceRegistered(String resourceId, org.apache.airavata.grouper.resource.ResourceType resourceType);

    void grantPermission(String userId, SubjectType subjectType, String resourceId, ResourceType resourceType,
                         PermissionAction permissionAction);

    void revokePermission(String userId, SubjectType subjectType, String resourceId, ResourceType resourceType,
                          PermissionAction action);

    Set<String> getAllAccessibleUsers(String resourceId, ResourceType resourceType, PermissionAction permissionType);

    List<String> getAccessibleResourcesForUser(String userId, ResourceType resourceType, PermissionAction permissionAction);

    void createGroup(Group group);

    void updateGroup(Group group);

    void deleteGroup(String groupId, String s);

    Group getGroup(String groupId);

    List<Group> getAllGroupsUserBelongs(String userId);
}
