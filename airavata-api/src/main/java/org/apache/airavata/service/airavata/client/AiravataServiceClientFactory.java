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
package org.apache.airavata.service.airavata.client;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.service.profile.groupmanager.cpi.GroupManagerService;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.tenant.cpi.TenantProfileService;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client factory for creating multiplexed thrift clients that connect to the unified AiravataService.
 * This factory creates clients that use TMultiplexedProtocol to access specific services within the unified server.
 */
public class AiravataServiceClientFactory {
    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(AiravataServiceClientFactory.class);

    /**
     * Create a multiplexed protocol for a specific service
     */
    private static TProtocol createMultiplexedProtocol(String host, int port, String serviceName)
            throws TTransportException {
        TTransport transport = new TSocket(host, port);
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        return new TMultiplexedProtocol(protocol, serviceName);
    }

    /**
     * Create Airavata API client
     */
    public static Airavata.Client createAiravataClient(String host, int port) throws TException {
        TProtocol protocol = createMultiplexedProtocol(host, port, "Airavata");
        return new Airavata.Client(protocol);
    }

    /**
     * Create Registry Service client
     */
    public static RegistryService.Client createRegistryClient(String host, int port) throws TException {
        TProtocol protocol = createMultiplexedProtocol(host, port, "RegistryService");
        return new RegistryService.Client(protocol);
    }

    /**
     * Create Credential Store Service client
     */
    public static CredentialStoreService.Client createCredentialStoreClient(String host, int port) throws TException {
        TProtocol protocol = createMultiplexedProtocol(host, port, "CredentialStoreService");
        return new CredentialStoreService.Client(protocol);
    }

    /**
     * Create Sharing Registry Service client
     */
    public static SharingRegistryService.Client createSharingRegistryClient(String host, int port) throws TException {
        TProtocol protocol = createMultiplexedProtocol(host, port, "SharingRegistryService");
        return new SharingRegistryService.Client(protocol);
    }

    /**
     * Create Orchestrator Service client
     */
    public static OrchestratorService.Client createOrchestratorClient(String host, int port) throws TException {
        TProtocol protocol = createMultiplexedProtocol(host, port, "OrchestratorService");
        return new OrchestratorService.Client(protocol);
    }

    /**
     * Create Workflow Service client
     * TODO: Uncomment when WorkflowModel thrift stubs are generated
     */
    // public static Workflow.Client createWorkflowClient(String host, int port) throws TException {
    //     TProtocol protocol = createMultiplexedProtocol(host, port, "Workflow");
    //     return new Workflow.Client(protocol);
    // }

    /**
     * Create User Profile Service client
     */
    public static UserProfileService.Client createUserProfileClient(String host, int port) throws TException {
        TProtocol protocol = createMultiplexedProtocol(host, port, "UserProfileService");
        return new UserProfileService.Client(protocol);
    }

    /**
     * Create Tenant Profile Service client
     */
    public static TenantProfileService.Client createTenantProfileClient(String host, int port) throws TException {
        TProtocol protocol = createMultiplexedProtocol(host, port, "TenantProfileService");
        return new TenantProfileService.Client(protocol);
    }

    /**
     * Create IAM Admin Services client
     */
    public static IamAdminServices.Client createIamAdminServicesClient(String host, int port) throws TException {
        TProtocol protocol = createMultiplexedProtocol(host, port, "IamAdminServices");
        return new IamAdminServices.Client(protocol);
    }

    /**
     * Create Group Manager Service client
     */
    public static GroupManagerService.Client createGroupManagerClient(String host, int port) throws TException {
        TProtocol protocol = createMultiplexedProtocol(host, port, "GroupManagerService");
        return new GroupManagerService.Client(protocol);
    }

    /**
     * Create all service clients with default settings
     * Uses localhost and default port 9930
     */
    public static AiravataServiceClients createAllClients() throws TException {
        return createAllClients("localhost", 9930);
    }

    /**
     * Create all service clients with specified host and port
     * TODO: Add workflow client when thrift stubs are generated
     */
    public static AiravataServiceClients createAllClients(String host, int port) throws TException {
        return new AiravataServiceClients(
                createAiravataClient(host, port),
                createRegistryClient(host, port),
                createCredentialStoreClient(host, port),
                createSharingRegistryClient(host, port),
                createOrchestratorClient(host, port),
                null, // createWorkflowClient(host, port),  // TODO: Uncomment when WorkflowModel thrift stubs are
                // generated
                createUserProfileClient(host, port),
                createTenantProfileClient(host, port),
                createIamAdminServicesClient(host, port),
                createGroupManagerClient(host, port));
    }

    /**
     * Container class for all service clients
     */
    public static class AiravataServiceClients {
        public final Airavata.Client airavata;
        public final RegistryService.Client registry;
        public final CredentialStoreService.Client credentialStore;
        public final SharingRegistryService.Client sharingRegistry;
        public final OrchestratorService.Client orchestrator;
        // public final Workflow.Client workflow;  // TODO: Uncomment when WorkflowModel thrift stubs are generated
        public final Object workflow; // Placeholder
        public final UserProfileService.Client userProfile;
        public final TenantProfileService.Client tenantProfile;
        public final IamAdminServices.Client iamAdmin;
        public final GroupManagerService.Client groupManager;

        public AiravataServiceClients(
                Airavata.Client airavata,
                RegistryService.Client registry,
                CredentialStoreService.Client credentialStore,
                SharingRegistryService.Client sharingRegistry,
                OrchestratorService.Client orchestrator,
                Object workflow, // Workflow.Client workflow,  // TODO: Uncomment when WorkflowModel thrift stubs are
                // generated
                UserProfileService.Client userProfile,
                TenantProfileService.Client tenantProfile,
                IamAdminServices.Client iamAdmin,
                GroupManagerService.Client groupManager) {
            this.airavata = airavata;
            this.registry = registry;
            this.credentialStore = credentialStore;
            this.sharingRegistry = sharingRegistry;
            this.orchestrator = orchestrator;
            this.workflow = workflow;
            this.userProfile = userProfile;
            this.tenantProfile = tenantProfile;
            this.iamAdmin = iamAdmin;
            this.groupManager = groupManager;
        }
    }
}
