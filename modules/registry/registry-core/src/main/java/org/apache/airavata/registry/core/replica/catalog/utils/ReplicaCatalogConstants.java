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
package org.apache.airavata.registry.core.replica.catalog.utils;

public class ReplicaCatalogConstants {
	// table names
	public static final String DATA_RESOURCE = "DataProductInterface";
	public static final String DATA_REPLICA_LOCATION = "DataReplicaLocationInterface";
	public static final String CONFIGURATION = "Configuration";

	// DataProductInterface Table
	public final class DataResourceConstants {
		public static final String RESOURCE_ID = "resourceId";
		public static final String RESOURCE_NAME = "resourceName";
		public static final String RESOURCE_DESCRIPTION = "resourceDescription";
		public static final String RESOURCE_SIZE = "resourceSize";
        public static final String CREATION_TIME = "creationTime";
        public static final String LAST_MODIFIED_TIME = "lastModifiedTime";
	}

	// Users table
	public final class DataReplicaLocationConstants {
        public static final String REPLICA_ID = "replicaId";
        public static final String RESOURCE_ID = "resourceId";
        public static final String DATA_LOCATIONS = "dataLocations";
        public static final String REPLICA_NAME = "replicaName";
        public static final String REPLICA_DESCRIPTION = "replicaDescription";
        public static final String CREATION_TIME = "creationTime";
        public static final String LAST_MODIFIED_TIME = "lastModifiedTime";
	}
}
