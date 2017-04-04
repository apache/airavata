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
package org.apache.airavata;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.userprofile.cpi.UserProfileService;
import org.apache.airavata.userprofile.cpi.exception.UserProfileServiceException;
import org.apache.airavata.userprofile.cpi.client.UserProfileServiceClientFactory;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;

public class UserProfileAiravataThriftClient {

    public UserProfileService.Client getRegistryServiceClient() throws TException, ApplicationSettingsException {
        // Check the server ports before running migration
        final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
        final String serverHost = ServerSettings.getRegistryServerHost();
        try {
            return UserProfileServiceClientFactory.createRegistryClient(serverHost, serverPort);
        } catch (UserProfileServiceException e) {
            throw new TException("Unable to create registry client...", e);
        }
    }
}
