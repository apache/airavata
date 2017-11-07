/**
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
 */
package org.apache.airavata.sharing.registry.db.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConstants {
    private final static Logger logger = LoggerFactory.getLogger(DBConstants.class);

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
    
    //User allocation details table
    public static class UserAllocationDetailsTable{
    	 public static final String PROJECT_ID =  "projectId";
    	 public static final String APPLICATIONS_TO_BE_USED = "applicationsToBeUsed";
    	 public static final String DISK_USAGE_RANGE_PER_JOB = "diskUsageRangePerJob";
    	 public static final String EXTERNAL_ALLOCATION_ACCESS_MECHANISMS = "externalAllocationAccessMechanisms";
    	 public static final String EXTERNAL_ALLOCATION_ACCOUNT_PASSWORD = "externalAllocationAccountPassword";
    	 public static final String EXTERNAL_ALLOCATION_ACCOUNT_USERNAME = "externalAllocationAccountUsername";
    	 public static final String EXTERNAL_ALLOCATION_ORGANIZATION_NAME = "externalAllocationOrganizationName";
    	 public static final String EXTERNAL_ALLOCATION_PROJECT_ID = "externalAllocationProjectId";
    	 public static final String EXTERNAL_ALLOCATION_RESOURCE_NAME = "externalAllocationResourceName";
    	 public static final String FIELD_OF_SCIENCE = "fieldOfScience";
    	 public static final String MAX_MEMORY_PER_CPU = "maxMemoryPerCpu";
    	 public static final String NUMBER_OF_CPU_PER_JOB = "numberOfCpuPerJob";
    	 public static final String PRINCIPAL_INVISTIGATOR_EMAIL = "principalInvistigatorEmail";
    	 public static final String PRINCIPAL_INVISTIGATOR_NAME = "principalInvistigatorName";
    	 public static final String PROJECT_DESCRIPTION = "projectDescription";
    	 public static final String PROJECT_REVIEWED_AND_FUNDED_BY = "projectReviewedAndFundedBy";
    	 public static final String SERVICE_UNITS = "serviceUnits";
    	 public static final String SPECIFIC_RESOURCE_SELECTION = "specificResourceSelection";
    	 public static final String TYPE_OF_ALLOCATION = "typeOfAllocation";
    	 public static final String TYPICAL_SU_PER_JOB = "typicalSuPerJob";
    }
}