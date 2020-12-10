package org.apache.airavata.custos.data.migrators;

import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.service.profile.client.ProfileServiceClientFactory;
import org.apache.airavata.service.profile.groupmanager.cpi.GroupManagerService;
import org.apache.airavata.service.profile.groupmanager.cpi.exception.GroupManagerServiceException;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.apache.airavata.service.profile.tenant.cpi.TenantProfileService;
import org.apache.airavata.service.profile.tenant.cpi.exception.TenantProfileServiceException;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.service.profile.user.cpi.exception.UserProfileServiceException;
import org.apache.airavata.sharing.registry.client.SharingRegistryServiceClientFactory;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.TException;

public class Migrator {
    private UserProfileService.Client userprofileClient;
    private TenantProfileService.Client tenantProfileClient;
    private IamAdminServices.Client iamAdminClient;
    private GroupManagerService.Client groupManagerServiceClient;
    private SharingRegistryService.Client sharingRegistryClient;
    private CredentialStoreService.Client credentialStoreServiceClient;


    public Migrator(String profileServiceHost, int profileServicePort, String sharingServiceHost, int sharingServicePort, int credentialStorePort) throws UserProfileServiceException, TenantProfileServiceException, IamAdminServicesException, GroupManagerServiceException, SharingRegistryException, CredentialStoreException {
        userprofileClient = ProfileServiceClientFactory.createUserProfileServiceClient(profileServiceHost, profileServicePort);
        tenantProfileClient = ProfileServiceClientFactory.createTenantProfileServiceClient(profileServiceHost, profileServicePort);
        iamAdminClient = ProfileServiceClientFactory.createIamAdminServiceClient(profileServiceHost, profileServicePort);
        groupManagerServiceClient = ProfileServiceClientFactory.createGroupManagerServiceClient(profileServiceHost, profileServicePort);
        sharingRegistryClient =  SharingRegistryServiceClientFactory.createSharingRegistryClient(sharingServiceHost, sharingServicePort);
        credentialStoreServiceClient = CredentialStoreClientFactory.createAiravataCSClient(sharingServiceHost, credentialStorePort);
    }

    public boolean migrateUserProfile(String gatewayId, String custosId)
            throws TException {
        iamAdminClient.synchronizeWithCustos(gatewayId, custosId);
        return true;
    }

    public boolean migrateGroups(String gatewayId, String custosId)
            throws TException {
        groupManagerServiceClient.synchronizeWithCustos(gatewayId, custosId);
        return true;
    }

    public String createCustosTenantForGateway(String gatewayId) throws TException {
        return tenantProfileClient.synchronizeWithCustos(gatewayId);
    }

    public boolean migrateSharingRegistry(String gatewayId, String custosId) throws TException {
        return sharingRegistryClient.synchronizeWithCustos(gatewayId, custosId);
    }

    public boolean migrateCredentialStore(String gatewayId, String custosId) throws TException {
        return credentialStoreServiceClient.synchronizeWithCustos(gatewayId, custosId);
    }
}
