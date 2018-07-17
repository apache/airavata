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
 */
package org.apache.airavata.helix.task.api.support;

import org.apache.airavata.agents.api.*;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.data.movement.DataMovementProtocol;

import java.io.File;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface AdaptorSupport {
    public void initializeAdaptor();

    public AgentAdaptor fetchAdaptor(String gatewayId, String computeResource, JobSubmissionProtocol protocol, String authToken, String userId) throws Exception;
    public StorageResourceAdaptor fetchStorageAdaptor(String gatewayId, String storageResourceId, DataMovementProtocol protocol, String authToken, String userId) throws AgentException;

}
