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
package org.apache.airavata.thriftapi.client;

import org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException;
import org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException;
import org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException;
import org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException;
import org.apache.airavata.thriftapi.profile.model.GroupManagerService;
import org.apache.airavata.thriftapi.profile.model.IamAdminServices;
import org.apache.airavata.thriftapi.profile.model.TenantProfileService;
import org.apache.airavata.thriftapi.profile.model.UserProfileService;
import org.apache.airavata.thriftapi.profile.model.group_manager_cpiConstants;
import org.apache.airavata.thriftapi.profile.model.iam_admin_services_cpiConstants;
import org.apache.airavata.thriftapi.profile.model.profile_tenant_cpiConstants;
import org.apache.airavata.thriftapi.profile.model.profile_user_cpiConstants;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

/**
 * Created by goshenoy on 03/08/2017.
 */
public class ProfileServiceClientFactory {
    private static final String PROFILE_SERVICE_NAME = "ProfileService";

    public static UserProfileService.Client createUserProfileServiceClient(String serverHost, int serverPort)
            throws UserProfileServiceException {
        try {
            var transport = new TSocket(serverHost, serverPort);
            transport.open();
            var protocol = new TBinaryProtocol(transport);
            // Use prefixed service name for Profile sub-services
            var multiplexedProtocol = new TMultiplexedProtocol(
                    protocol, PROFILE_SERVICE_NAME + "." + profile_user_cpiConstants.USER_PROFILE_CPI_NAME);
            return new UserProfileService.Client(multiplexedProtocol);
        } catch (TTransportException e) {
            throw new UserProfileServiceException(e.getMessage());
        }
    }

    public static TenantProfileService.Client createTenantProfileServiceClient(String serverHost, int serverPort)
            throws TenantProfileServiceException {
        try {
            var transport = new TSocket(serverHost, serverPort);
            transport.open();
            var protocol = new TBinaryProtocol(transport);
            // Use prefixed service name for Profile sub-services
            var multiplexedProtocol = new TMultiplexedProtocol(
                    protocol, PROFILE_SERVICE_NAME + "." + profile_tenant_cpiConstants.TENANT_PROFILE_CPI_NAME);
            return new TenantProfileService.Client(multiplexedProtocol);
        } catch (TTransportException e) {
            throw new TenantProfileServiceException(e.getMessage());
        }
    }

    public static IamAdminServices.Client createIamAdminServiceClient(String serverHost, int serverPort)
            throws IamAdminServicesException {
        try {
            var transport = new TSocket(serverHost, serverPort);
            transport.open();
            var protocol = new TBinaryProtocol(transport);
            // Use prefixed service name for Profile sub-services
            var multiplexedProtocol = new TMultiplexedProtocol(
                    protocol, PROFILE_SERVICE_NAME + "." + iam_admin_services_cpiConstants.IAM_ADMIN_SERVICES_CPI_NAME);
            return new IamAdminServices.Client(multiplexedProtocol);
        } catch (TTransportException e) {
            throw new IamAdminServicesException(e.getMessage());
        }
    }

    public static GroupManagerService.Client createGroupManagerServiceClient(String serverHost, int serverPort)
            throws GroupManagerServiceException {
        try {
            var transport = new TSocket(serverHost, serverPort);
            transport.open();
            var protocol = new TBinaryProtocol(transport);
            // Use prefixed service name for Profile sub-services
            var multiplexedProtocol = new TMultiplexedProtocol(
                    protocol, PROFILE_SERVICE_NAME + "." + group_manager_cpiConstants.GROUP_MANAGER_CPI_NAME);
            return new GroupManagerService.Client(multiplexedProtocol);
        } catch (TTransportException e) {
            throw new GroupManagerServiceException(e.getMessage());
        }
    }
}
