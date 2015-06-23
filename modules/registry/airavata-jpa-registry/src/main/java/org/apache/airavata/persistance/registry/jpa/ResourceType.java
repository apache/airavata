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
package org.apache.airavata.persistance.registry.jpa;

public enum ResourceType {
    GATEWAY,
    PROJECT,
    USER,
    PROJECT_USER,
    CONFIGURATION,
    GATEWAY_WORKER,
    EXPERIMENT,
    EXPERIMENT_SUMMARY,
    NOTIFICATION_EMAIL,
    EXPERIMENT_INPUT,
    EXPERIMENT_OUTPUT,
    WORKFLOW_NODE_DETAIL,
    TASK_DETAIL,
    ERROR_DETAIL,
    APPLICATION_INPUT,
    APPLICATION_OUTPUT,
    NODE_INPUT,
    NODE_OUTPUT,
    JOB_DETAIL,
    DATA_TRANSFER_DETAIL,
    STATUS,
    CONFIG_DATA,
    COMPUTATIONAL_RESOURCE_SCHEDULING,
    ADVANCE_INPUT_DATA_HANDLING,
    ADVANCE_OUTPUT_DATA_HANDLING,
    QOS_PARAM
}
