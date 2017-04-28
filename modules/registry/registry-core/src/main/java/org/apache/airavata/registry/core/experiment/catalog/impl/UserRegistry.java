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
package org.apache.airavata.registry.core.experiment.catalog.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.resources.WorkerResource;
import org.apache.airavata.registry.cpi.RegistryException;

public class UserRegistry {
    public WorkerResource getSystemUser() throws ApplicationSettingsException, RegistryException {
        return (WorkerResource) ExpCatResourceUtils.getWorker(ServerSettings.getDefaultUserGateway(), ServerSettings.getDefaultUser());
    }

    public WorkerResource getExistingUser (String gatewayName, String userName) throws RegistryException {
        return (WorkerResource) ExpCatResourceUtils.getWorker(gatewayName, userName);
    }



}
