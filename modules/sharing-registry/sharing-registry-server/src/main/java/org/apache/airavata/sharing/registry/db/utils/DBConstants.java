/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.sharing.registry.db.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConstants {
    private final static Logger logger = LoggerFactory.getLogger(DBConstants.class);

    public static int SELECT_MAX_ROWS = 1000;

    public static class DomainTable {
        public static String DOMAIN_ID = "domainId";
        public static String NAME = "name";
        public static String DESCRIPTION = "description";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
    }

    public static class UserTable {
        public static String USER_ID = "userId";
        public static String DOMAIN_ID = "domainId";
        public static String USER_NAME = "userName";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
    }

    public static class UserGroupTable {
        public static String GROUP_ID = "groupId";
        public static String DOMAIN_ID = "domainId";
        public static String NAME = "name";
        public static String DESCRIPTION = "description";
        public static String OWNER_ID = "ownerId";
        public static String GROUP_TYPE = "groupType";
        public static String GROUP_CARDINALITY = "groupCardinality";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
    }

    public static class GroupMembershipTable {
        public static String PARENT_ID = "parentId";
        public static String CHILD_ID = "childId";
        public static String CHILD_TYPE = "childType";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
    }

    public static class EntityTypeTable {
        public static String ENTITY_TYPE_ID = "entityTypeId";
        public static String DOMAIN_ID = "domainId";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
    }

    public static class PermissionTypeTable {
        public static String ENTITY_TYPE_ID = "permissionTypeId";
        public static String DOMAIN_ID = "domainId";
        public static String NAME = "name";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
    }

    public static class EntityTable {
        public static String ENTITY_ID = "entityId";
        public static String PARENT_ENTITY_ID = "parentEntityId";
        public static String ENTITY_TYPE_ID = "entityTypeId";
        public static String NAME = "name";
        public static String DESCRIPTION = "description";
        public static String FULL_TEXT = "fullText";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
    }

    public static class SharingTable {
        public static String PERMISSION_TYPE_ID = "permissionTypeId";
        public static String ENTITY_ID = "entityId";
        public static String GROUP_ID = "groupId";
        public static String INHERITED_PARENT_ID = "inheritedParentId";
        public static final String SHARING_TYPE = "sharingType";
        public static final String CREATED_TIME = "createdTime";
        public static final String UPDATED_TIME = "updatedTime";
    }
}