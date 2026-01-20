/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.common.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base class for all messaging events.
 * Uses Jackson polymorphic type handling to support JSON serialization/deserialization
 * of event subclasses through Dapr Pub/Sub.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ExperimentStatusChangeEvent.class, name = "experimentStatus"),
    @JsonSubTypes.Type(value = ExperimentSubmitEvent.class, name = "experimentSubmit"),
    @JsonSubTypes.Type(value = ProcessStatusChangeEvent.class, name = "processStatus"),
    @JsonSubTypes.Type(value = ProcessStatusChangeRequestEvent.class, name = "processStatusRequest"),
    @JsonSubTypes.Type(value = ProcessSubmitEvent.class, name = "processSubmit"),
    @JsonSubTypes.Type(value = ProcessTerminateEvent.class, name = "processTerminate"),
    @JsonSubTypes.Type(value = JobStatusChangeEvent.class, name = "jobStatus"),
    @JsonSubTypes.Type(value = JobStatusChangeRequestEvent.class, name = "jobStatusRequest"),
    @JsonSubTypes.Type(value = TaskStatusChangeEvent.class, name = "taskStatus"),
    @JsonSubTypes.Type(value = TaskStatusChangeRequestEvent.class, name = "taskStatusRequest"),
    @JsonSubTypes.Type(value = TaskOutputChangeEvent.class, name = "taskOutput")
})
public class MessagingEvent {}
