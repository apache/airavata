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

package org.apache.airavata.persistance.registry.jpa.resources;

public class DBC {
    public static final class ExperimentData{
    	public static final String TABLE="Experiment_Data";
        public static final String EXPERIMENT_ID = "experiment_ID";
        public static final String EXPERIMENT_NAME = "name";
        public static final String USER_NAME = "username";
    }
    
    public static final class ExperimentMetadata{
    	public static final String TABLE="Experiment_Metadata";
        public static final String EXPERIMENT_ID = "experiment_ID";
        public static final String METADATA = "metadata";
    }
    
    public static final class WorkflowData {
    	public static final String TABLE="Workflow_Data";
        public static final String EXPERIMENT_ID = "experiment_ID";
        public static final String INSTANCE_ID = "workflow_instanceID";
        public static final String TEMPLATE_NAME = "template_name";
        public static final String STATUS = "status";
        public static final String START_TIME = "start_time";
        public static final String LAST_UPDATED = "last_update_time";
    }
    public static final class NodeData {
    	public static final String TABLE="Node_Data";
        public static final String WORKFLOW_INSTANCE_ID = "workflow_instanceID";
        public static final String NODE_ID = "node_id";
        public static final String TYPE = "node_type";
        public static final String INPUTS = "inputs";
        public static final String OUTPUTS = "outputs";
        public static final String STATUS = "status";
        public static final String START_TIME = "start_time";
        public static final String LAST_UPDATED = "last_update_time";
    }
    public static final class GramData {
    	public static final String TABLE="Gram_Data";
        public static final String WORKFLOW_INSTANCE_ID = "workflow_instanceID";
        public static final String NODE_ID = "node_id";
        public static final String RSL = "rsl";
        public static final String INVOKED_HOST = "invoked_host";
    }
    
}
