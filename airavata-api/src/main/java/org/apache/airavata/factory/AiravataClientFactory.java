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
package org.apache.airavata.factory;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.ServiceName;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.service.profile.groupmanager.cpi.GroupManagerService;
import org.apache.airavata.service.profile.groupmanager.cpi.exception.GroupManagerServiceException;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.apache.airavata.service.profile.tenant.cpi.TenantProfileService;
import org.apache.airavata.service.profile.tenant.cpi.exception.TenantProfileServiceException;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.service.profile.user.cpi.exception.UserProfileServiceException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class AiravataClientFactory {

    public static Airavata.Client getAiravata(String serverHost, int serverPort, boolean secure)
            throws AiravataClientException {
        try {
            TTransport transport;
            if (!secure) {
                transport = new TSocket(serverHost, serverPort);
                transport.open();
            } else {
                // TLS enabled client
                var params = new TSSLTransportFactory.TSSLTransportParameters();
                params.setKeyStore(ServerSettings.getKeyStorePath(), ServerSettings.getKeyStorePassword());
                transport = TSSLTransportFactory.getClientSocket(serverHost, serverPort, 10000, params);
            }

            var protocol = new TBinaryProtocol(transport);
            var mp = new TMultiplexedProtocol(protocol, ServiceName.AIRAVATA_API.toString());
            return new Airavata.Client(mp);
        } catch (TTransportException | ApplicationSettingsException e) {
            AiravataClientException exception = new AiravataClientException();
            exception.setParameter("Unable to connect to the server at " + serverHost + ":" + serverPort);
            throw exception;
        }
    }

    public static CredentialStoreService.Iface getCredentialStore(String serverHost, int serverPort)
            throws CredentialStoreException {
        try {
            TTransport transport = new TSocket(serverHost, serverPort);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            protocol = new TMultiplexedProtocol(protocol, ServiceName.CREDENTIAL_STORE.toString());
            return new CredentialStoreService.Client(protocol);
        } catch (TTransportException e) {
            throw new CredentialStoreException(
                    "Unable to connect to the credential store server at " + serverHost + ":" + serverPort);
        }
    }

    public static OrchestratorService.Client createOrchestratorClient(String serverHost, int serverPort)
            throws AiravataClientException {
        try {
            TTransport transport = new TSocket(serverHost, serverPort);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            protocol = new TMultiplexedProtocol(protocol, ServiceName.ORCHESTRATOR.toString());
            return new OrchestratorService.Client(protocol);
        } catch (TTransportException e) {
            throw new AiravataClientException();
        }
    }

    public static UserProfileService.Client createUserProfileServiceClient(String serverHost, int serverPort)
            throws UserProfileServiceException {
        try {
            TTransport transport = new TSocket(serverHost, serverPort);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol multiplexedProtocol =
                    new TMultiplexedProtocol(protocol, ServiceName.USER_PROFILE.toString());
            return new UserProfileService.Client(multiplexedProtocol);
        } catch (TTransportException e) {
            throw new UserProfileServiceException(e.getMessage());
        }
    }

    public static TenantProfileService.Client createTenantProfileServiceClient(String serverHost, int serverPort)
            throws TenantProfileServiceException {
        try {
            TTransport transport = new TSocket(serverHost, serverPort);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol multiplexedProtocol =
                    new TMultiplexedProtocol(protocol, ServiceName.TENANT_PROFILE.toString());
            return new TenantProfileService.Client(multiplexedProtocol);
        } catch (TTransportException e) {
            throw new TenantProfileServiceException(e.getMessage());
        }
    }

    public static IamAdminServices.Client createIamAdminServiceClient(String serverHost, int serverPort)
            throws IamAdminServicesException {
        try {
            TTransport transport = new TSocket(serverHost, serverPort);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol multiplexedProtocol =
                    new TMultiplexedProtocol(protocol, ServiceName.IAM_ADMIN_SERVICES.toString());
            return new IamAdminServices.Client(multiplexedProtocol);
        } catch (TTransportException e) {
            throw new IamAdminServicesException(e.getMessage());
        }
    }

    public static GroupManagerService.Client createGroupManagerServiceClient(String serverHost, int serverPort)
            throws GroupManagerServiceException {
        try {
            TTransport transport = new TSocket(serverHost, serverPort);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol multiplexedProtocol =
                    new TMultiplexedProtocol(protocol, ServiceName.GROUP_MANAGER.toString());
            return new GroupManagerService.Client(multiplexedProtocol);
        } catch (TTransportException e) {
            throw new GroupManagerServiceException(e.getMessage());
        }
    }
}
