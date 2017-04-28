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
package org.apache.airavata.registry.core.workflow.catalog.resources;

public abstract class WorkflowCatAbstractResource implements WorkflowCatalogResource {
    // table names
    public static final String WORKFLOW = "Workflow";
    public static final String WORKFLOW_INPUT = "WorkflowInput";
    public static final String WORKFLOW_OUTPUT = "WorkflowOutput";
    public static final String EDGE = "Edge";
    public static final String NODE = "Node";
    public static final String PORT = "Port";
    public static final String COMPONENT_STATUS = "ComponentStatus";
    public static final String WORKFLOW_STATUS = "WorkflowStatus";

    public final class WorkflowInputConstants {
        public static final String WF_TEMPLATE_ID = "templateID";
        public static final String INPUT_KEY = "inputKey";
        public static final String INPUT_VALUE = "inputVal";
        public static final String DATA_TYPE = "dataType";
        public static final String METADATA = "metadata";
        public static final String APP_ARGUMENT = "appArgument";
        public static final String USER_FRIENDLY_DESC = "userFriendlyDesc";
        public static final String STANDARD_INPUT = "standardInput";
    }

    public final class WorkflowOutputConstants {
        public static final String WF_TEMPLATE_ID = "templateId";
        public static final String OUTPUT_KEY = "outputKey";
        public static final String OUTPUT_VALUE = "outputVal";
        public static final String DATA_TYPE = "dataType";
    }

    // Workflow Table
    public final class WorkflowConstants {
        public static final String TEMPLATE_ID = "templateId";
        public static final String GATEWAY_ID = "gatewayId";
        public static final String WORKFLOW_NAME = "workflowName";
    }

    public final class ComponentStatusConstants {
        public static final String STATUS_ID = "statusId";
        public static final String TEMPLATE_ID = "templateId";
    }

    public final class WorkflowStatusConstants {
        public static final String STATUS_ID = "statusId";
        public static final String TEMPLATE_ID = "templateId";
    }

    public final class EdgeConstants {
        public static final String STATUS_ID = "statusId";
        public static final String TEMPLATE_ID = "templateId";
        public static final String EDGE_ID = "edgeId";
    }

    public final class PortConstants {
        public static final String STATUS_ID = "statusId";
        public static final String TEMPLATE_ID = "templateId";
        public static final String PORT_ID = "portId";
    }

    public final class NodeConstants {
        public static final String STATUS_ID = "statusId";
        public static final String TEMPLATE_ID = "templateId";
        public static final String NODE_ID = "nodeId";
    }

}
