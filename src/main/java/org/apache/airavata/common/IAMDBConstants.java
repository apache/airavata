package org.apache.airavata.common;

public class IAMDBConstants {

    public static int SELECT_MAX_ROWS = 1000;

    public static class DomainTable {
        public static final String DOMAIN_ID = "domainId";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
    }

    public static class UserTable {
        public static final String USER_ID = "userId";
        public static final String DOMAIN_ID = "domainId";
        public static final String USER_NAME = "userName";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
    }

    public static class UserGroupTable {
        public static final String GROUP_ID = "groupId";
        public static final String DOMAIN_ID = "domainId";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String OWNER_ID = "ownerId";
        public static final String GROUP_TYPE = "groupType";
        public static final String GROUP_CARDINALITY = "groupCardinality";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
    }

    public static class GroupMembershipTable {
        public static final String PARENT_ID = "parentId";
        public static final String CHILD_ID = "childId";
        public static final String CHILD_TYPE = "childType";
        public static final String DOMAIN_ID = "domainId";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
    }

    public static class EntityTypeTable {
        public static final String ENTITY_TYPE_ID = "entityTypeId";
        public static final String DOMAIN_ID = "domainId";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
    }

    public static class PermissionTypeTable {
        public static final String ENTITY_TYPE_ID = "permissionTypeId";
        public static final String DOMAIN_ID = "domainId";
        public static final String NAME = "name";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
    }

    public static class EntityTable {
        public static final String ENTITY_ID = "entityId";
        public static final String PARENT_ENTITY_ID = "parentEntityId";
        public static final String ENTITY_TYPE_ID = "entityTypeId";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String FULL_TEXT = "fullText";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
        public static final String DOMAIN_ID = "domainId";
        public static final String ORIGINAL_ENTITY_CREATION_TIME = "originalEntityCreationTime";
        public static final String SHARED = "shared";
    }

    public static class SharingTable {
        public static final String DOMAIN_ID = "domainId";
        public static final String PERMISSION_TYPE_ID = "permissionTypeId";
        public static final String ENTITY_ID = "entityId";
        public static final String GROUP_ID = "groupId";
        public static final String INHERITED_PARENT_ID = "inheritedParentId";
        public static final String SHARING_TYPE = "sharingType";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
    }
}
