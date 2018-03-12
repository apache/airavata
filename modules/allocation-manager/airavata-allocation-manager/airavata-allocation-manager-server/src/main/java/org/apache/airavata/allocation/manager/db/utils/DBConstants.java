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
package org.apache.airavata.allocation.manager.db.utils;

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

    // Added the ProjectReviewer table fields
    public static class ProjectReviewerTable {

        public static final String PROJECTID = "projectId";
        public static final String REVIEWER = "reviewer";
    }

    public static class UserAllocationDetailTable {

        public static final String PROJECTID = "projectId";
        public static final String USERNAME = "username";
        public static final String ISPRIMARYOWNER = "isPrimaryOwner";
    }

    public static class UserDetailTable {

        public static final String USERTYPE = "userType";
    }

    public static class UserType {

        public static final String REVIEWER = "REVIEWER";
        public static final String ADMIN = "ADMIN";
        public static final String USER = "USER";
    }

    public static class RequestStatus {
    	public static final String PARTIALLYAPPROVED  = "PARTIALLY_APPROVED";
        public static final String PENDING = "PENDING";
        public static final String UNDER_REVIEW = "UNDER_REVIEW";
        public static final String APPROVED = "APPROVED";
         public static final String REJECTED = "REJECTED";
    }
}
