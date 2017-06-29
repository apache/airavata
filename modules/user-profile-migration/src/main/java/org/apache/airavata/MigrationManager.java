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

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.Status;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.service.profile.client.ProfileServiceClientFactory;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.apache.airavata.service.profile.tenant.cpi.TenantProfileService;
import org.apache.airavata.service.profile.tenant.cpi.exception.TenantProfileServiceException;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.thrift.TException;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

public class MigrationManager {

    private ArrayList<Wso2ISLoginCredentialsDAO> adminCredentials = new ArrayList<Wso2ISLoginCredentialsDAO>();
    private static AuthzToken authzToken = new AuthzToken("empty_token");

    // Default values
    private String profileServiceServerHost = "localhost";
    private int profileServiceServerPort = 8962;
    private String airavataServiceServerHost = "localhost";
    private int airavataServiceServerPort = 8930;
    private boolean airavataServiceSecure = false;
    private Map<String,String> roleConversionMap = createDefaultRoleConversionMap();
    private String gatewayId = "gateway-id";
    private String wso2ISAdminUsername = "username";
    private String wso2ISAdminPassword = "password";
    private String keycloakServiceURL = "https://iam.scigap.org/auth";
    private String keycloakAdminUsername = "username";
    private String keycloakAdminPassword = "password";
    private String keycloakTrustStorePath = "../../modules/configuration/server/src/main/resources/client_truststore.jks";
    private String keycloakTrustStorePassword = "password";
    private String keycloakTemporaryUserPassword = "tempPassword";
    // For some gateways in the legacy gateways table, the following information is missing and needs to be provided
    private String gatewayURL = "http://localhost";
    private String gatewayAdminUsername = "admin";
    private String gatewayAdminFirstName = "Admin";
    private String gatewayAdminLastName = "User";
    private String gatewayAdminEmailAddress = "sgg@iu.edu";

    // Names of properties in user-profile-migration.properties.template
    private final static String GATEWAY_ID = "gateway-id";
    private final static String GATEWAY_URL = "gateway.url";
    private final static String GATEWAY_ADMIN_USERNAME = "gateway.admin.username";
    private final static String GATEWAY_ADMIN_FIRST_NAME = "gateway.admin.first.name";
    private final static String GATEWAY_ADMIN_LAST_NAME = "gateway.admin.last.name";
    private final static String GATEWAY_ADMIN_EMAIL_ADDRESS = "gateway.admin.email.address";
    private final static String WSO2IS_ADMIN_USERNAME = "wso2is.admin.username";
    private final static String WSO2IS_ADMIN_PASSWORD = "wso2is.admin.password";
    private final static String WSO2IS_ADMIN_ROLENAME = "wso2is.admin.rolename";
    private final static String WSO2IS_ADMIN_READ_ONLY_ROLENAME = "wso2is.admin-read-only.rolename";
    private final static String WSO2IS_GATEWAY_USER_ROLENAME = "wso2is.gateway-user.rolename";
    private final static String WSO2IS_USER_PENDING_ROLENAME = "wso2is.user-pending.rolename";
    private final static String WSO2IS_GATEWAY_PROVIDER_ROLENAME = "wso2is.gateway-provider.rolename";
    private final static String AIRAVATA_SERVICE_HOST = "airavata.service.host";
    private final static String AIRAVATA_SERVICE_PORT = "airavata.service.port";
    private final static String AIRAVATA_SERVICE_SECURE = "airavata.service.secure";
    private final static String PROFILE_SERVICE_HOST = "profile.service.host";
    private final static String PROFILE_SERVICE_PORT = "profile.service.port";
    private final static String KEYCLOAK_ADMIN_USERNAME = "keycloak.admin.username";
    private final static String KEYCLOAK_ADMIN_PASSWORD = "keycloak.admin.password";
    private final static String KEYCLOAK_SERVICE_URL = "keycloak.service-url";
    private final static String KEYCLOAK_TRUSTSTORE_PATH = "keycloak.truststore.path";
    private final static String KEYCLOAK_TRUSTSTORE_PASSWORD = "keycloak.truststore.password";
    private final static String KEYCLOAK_USER_TEMP_PASSWORD = "keycloak.user.temp.password";


    private Map<String,String> createDefaultRoleConversionMap() {
        Map<String,String> roleConversionMap = new HashMap<>();
        roleConversionMap.put("admin", "admin");
        roleConversionMap.put("admin-read-only", "admin-read-only");
        roleConversionMap.put("gateway-user", "gateway-user");
        roleConversionMap.put("user-pending", "user-pending");
        roleConversionMap.put("gateway-provider", "gateway-provider");
        return roleConversionMap;
    }
    /*Add the credentials for all the tenants from which the profile should be migrated to Airavata DB*/

    public void setISLoginCredentials(){
        adminCredentials.add(new Wso2ISLoginCredentialsDAO(this.gatewayId, this.wso2ISAdminUsername, this.wso2ISAdminPassword));
        // new credential records here...
    }

    /* Method used to fetch all the user profiles from the registered tenants */

    public List<UserProfileDAO> getUserProfilesFromWso2IS(){
        ArrayList<UserProfileDAO> userProfileList = new ArrayList<UserProfileDAO>();
        for(Wso2ISLoginCredentialsDAO creds:adminCredentials){
            RemoteUserStoreManagerServiceStub isClient = Wso2IdentityServerClient.getAdminServiceClient(creds.getLoginUserName(),creds.getLoginPassword(),"RemoteUserStoreManagerService");
            String[] userList;
            System.out.println("Fetching User Profiles for " + creds.getGateway() + " tenant ...");
            try {
                userList = isClient.getUserList("http://wso2.org/claims/givenname", "*", "default");
                System.out.println("FirstName\tLastName\tEmail\t\t\tuserName\tCountry\tOrganization\tphone\tRoles");
                String[] claims = {"http://wso2.org/claims/givenname",
                        "http://wso2.org/claims/lastname",
                        "http://wso2.org/claims/emailaddress",
                        "http://wso2.org/claims/country",
                        "http://wso2.org/claims/organization",
                        "http://wso2.org/claims/mobile",
                        "http://wso2.org/claims/telephone",
                        "http://wso2.org/claims/streetaddress",
                        "http://wso2.org/claims/role",
                        "http://wso2.org/claims/identity/accountLocked"};
                for (String user : userList) {
                    UserProfileDAO userProfile = new UserProfileDAO();
                    ClaimValue[] retrievedClaimValues = isClient.getUserClaimValuesForClaims(user, claims, null);
                    List<String> phones = new ArrayList<String>();
                    for(ClaimValue claim:retrievedClaimValues){
                        if(claim.getClaimURI().equals(claims[0])){
                            userProfile.setFirstName(claim.getValue());
                        }else if(claim.getClaimURI().equals(claims[1])){
                            userProfile.setLastName(claim.getValue());
                        }else if(claim.getClaimURI().equals(claims[2])){
                            userProfile.setEmail(claim.getValue());
                        }else if(claim.getClaimURI().equals(claims[3])){
                            userProfile.setCountry(claim.getValue());
                        }else if(claim.getClaimURI().equals(claims[4])){
                            userProfile.setOrganization(claim.getValue());
                        }else if(claim.getClaimURI().equals(claims[5]) || claim.getClaimURI().equals(claims[6])){
                            phones.add(claim.getValue());
                        } else if(claim.getClaimURI().equals(claims[7])){
                            userProfile.setAddress(claim.getValue());
                        } else if(claim.getClaimURI().equals(claims[8])){
                            userProfile.setRoles(convertCommaSeparatedRolesToList(claim.getValue()));
                        } else if(claim.getClaimURI().equals(claims[9])){
                            userProfile.setAccountLocked(claim.getValue().equals("true"));
                        }
                    }
                    // Lowercase all usernames as required by Keycloak and User Profile service
                    userProfile.setUserName(user.toLowerCase());
                    userProfile.setGatewayID(creds.getGateway());
                    userProfile.setPhones(phones);
                    if (!userProfile.isAccountLocked()) {
                        System.out.println(userProfile.getFirstName() + "\t" + userProfile.getLastName() + "\t" + userProfile.getUserName() + "\t" + userProfile.getEmail() + "\t" + userProfile.getCountry() + "\t" + userProfile.getOrganization() + "\t" + userProfile.getAddress() + "\t" + userProfile.getRoles());
                        userProfileList.add(userProfile);
                    } else {
                        System.out.println("Skipping locked account for user " + user + "!");
                    }
                }
            } catch (RemoteException e) {
                System.out.println(e.getMessage());
                System.out.println(e.getCause());
                e.printStackTrace();
            } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
                System.out.println(e.getMessage());
                System.out.println(e.getCause());
                e.printStackTrace();
            }
        }
        System.out.println("User profiles from all the tenant are retrieved ...");
        return userProfileList;
    }

    private List<String> convertCommaSeparatedRolesToList(String roles) {

        return Arrays.stream(roles.split(","))
                .filter(s -> !"Internal/everyone".equals(s))
                .filter(s -> !"Internal/identity".equals(s))
                .collect(Collectors.toList());
    }

    private TenantProfileService.Client getTenantProfileServiceClient() throws TenantProfileServiceException {

        return ProfileServiceClientFactory.createTenantProfileServiceClient(profileServiceServerHost, profileServiceServerPort);
    }

    private Airavata.Client getAiravataClient() throws AiravataClientException {
        return AiravataClientFactory.createAiravataClient(airavataServiceServerHost, airavataServiceServerPort);
    }

    private Airavata.Client getAiravataSecureClient() throws AiravataClientException {
        return AiravataClientFactory.createAiravataSecureClient(airavataServiceServerHost, airavataServiceServerPort, keycloakTrustStorePath, keycloakTrustStorePassword, 10000);
    }

    private IamAdminServices.Client getIamAdminServicesClient() throws IamAdminServicesException {
        return ProfileServiceClientFactory.createIamAdminServiceClient(profileServiceServerHost, profileServiceServerPort);
    }

    private PasswordCredential getPasswordCredential() {
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setGatewayId("dummy");
        passwordCredential.setPortalUserName("dummy");
        passwordCredential.setLoginUserName(keycloakAdminUsername);
        passwordCredential.setPassword(keycloakAdminPassword);
        return passwordCredential;
    }

    private boolean migrateGatewayProfileToAiravata() throws TException {

        TenantProfileService.Client tenantProfileServiceClient = getTenantProfileServiceClient();
        Airavata.Client airavataClient = airavataServiceSecure ? getAiravataSecureClient() : getAiravataClient();
        IamAdminServices.Client iamAdminServicesClient = getIamAdminServicesClient();

        // Get Gateway from Airavata API
        Gateway gateway = airavataClient.getGateway(authzToken, gatewayId);

        if (!GatewayApprovalStatus.APPROVED.equals(gateway.getGatewayApprovalStatus())) {
            throw new RuntimeException("Gateway " + gatewayId + " is not APPROVED! Status is " + gateway.getGatewayApprovalStatus());
        }
        // Add Gateway through TenantProfileService
        if (!tenantProfileServiceClient.isGatewayExist(authzToken, gatewayId)) {

            System.out.println("Gateway [" + gatewayId + "] doesn't exist, adding in Profile Service...");
            String airavataInternalGatewayId = tenantProfileServiceClient.addGateway(authzToken, gateway);
            gateway.setAiravataInternalGatewayId(airavataInternalGatewayId);
        } else {

            System.out.println("Gateway [" + gatewayId + "] already exists in Profile Service");
            gateway = tenantProfileServiceClient.getGateway(authzToken, gatewayId);
        }

        // Gateway URL is required by IAM Admin Services
        if (gateway.getGatewayURL() == null) {
            gateway.setGatewayURL(this.gatewayURL);
        }
        // Following are also required by IAM Admin Services in order to create an admin user for the realm
        if (gateway.getIdentityServerUserName() == null) {
            gateway.setIdentityServerUserName(this.gatewayAdminUsername);
        }
        if (gateway.getGatewayAdminFirstName() == null) {
            gateway.setGatewayAdminFirstName(this.gatewayAdminFirstName);
        }
        if (gateway.getGatewayAdminLastName() == null) {
            gateway.setGatewayAdminLastName(this.gatewayAdminLastName);
        }
        if (gateway.getGatewayAdminEmail() == null) {
            gateway.setGatewayAdminEmail(this.gatewayAdminEmailAddress);
        }

        // Add Keycloak Tenant for Gateway
        System.out.println("Creating Keycloak Tenant for gateway ...");
        Gateway gatewayWithIdAndSecret = iamAdminServicesClient.setUpGateway(authzToken, gateway);

        // Update Gateway profile with the client id and secret
        System.out.println("Updating gateway with OAuth client id and secret ...");
        tenantProfileServiceClient.updateGateway(authzToken, gatewayWithIdAndSecret);

        KeycloakIdentityServerClient keycloakIdentityServerClient = getKeycloakIdentityServerClient();
        // Set the admin user's password to the same as it was for wso2IS
        keycloakIdentityServerClient.setUserPassword(gatewayId, this.gatewayAdminUsername, this.wso2ISAdminPassword);

        // Create password credential for admin username and password
        String passwordToken = airavataClient.registerPwdCredential(authzToken, gatewayId, this.gatewayAdminUsername, this.gatewayAdminUsername, this.wso2ISAdminPassword, "Keycloak admin password for realm " + gatewayId);

        // Update gateway resource profile with tenant id (gatewayId) and admin user password token
        GatewayResourceProfile gatewayResourceProfile = airavataClient.getGatewayResourceProfile(authzToken, gatewayId);
        gatewayResourceProfile.setIdentityServerTenant(gatewayId);
        gatewayResourceProfile.setIdentityServerPwdCredToken(passwordToken);
        airavataClient.updateGatewayResourceProfile(authzToken, gatewayId, gatewayResourceProfile);
        return true;
    }

    /* Method used to migrate User profiles to Airavata DB by making a call to User profile thrift Service */
    private boolean migrateUserProfilesToAiravata(List<UserProfileDAO> ISProfileList) throws TException, ApplicationSettingsException {
        System.out.println("Initiating migration to Airavata internal DB ...");
        UserProfileService.Client client = ProfileServiceClientFactory.createUserProfileServiceClient(profileServiceServerHost, profileServiceServerPort);
        UserProfile airavataUserProfile = new UserProfile();
        // Here are the data associations...
        for(UserProfileDAO ISProfile : ISProfileList){
            airavataUserProfile.setAiravataInternalUserId(ISProfile.getUserName() + "@" + ISProfile.getGatewayID());
            airavataUserProfile.setFirstName(ISProfile.getFirstName());
            airavataUserProfile.setLastName(ISProfile.getLastName());
            airavataUserProfile.setUserId(ISProfile.getUserName());
            airavataUserProfile.setGatewayId(ISProfile.getGatewayID());
            List<String> emails = new ArrayList<String>();
            emails.add(ISProfile.getEmail());
            airavataUserProfile.setEmails(emails);
            airavataUserProfile.setHomeOrganization(ISProfile.getOrganization());
            airavataUserProfile.setPhones(ISProfile.getPhones());
            airavataUserProfile.setCountry(ISProfile.getCountry());
            airavataUserProfile.setCreationTime(new Date().getTime());
            airavataUserProfile.setLastAccessTime(new Date().getTime());
            airavataUserProfile.setValidUntil(-1);
            airavataUserProfile.setState(Status.ACTIVE);
            //TODO: fix authtzToken, for now we are using empty token, but need to properly populate claims map
            AuthzToken authzToken = new AuthzToken("dummy_token");
            Map<String,String> claimsMap = new HashMap<>();
            claimsMap.put(Constants.USER_NAME, ISProfile.getUserName());
            claimsMap.put(Constants.GATEWAY_ID, ISProfile.getGatewayID());
            authzToken.setClaimsMap(claimsMap);
            client.addUserProfile(authzToken, airavataUserProfile);
        }
        return false;
    }

    private void migrateUserProfilesToKeycloak(List<UserProfileDAO> Wso2ISProfileList){
        KeycloakIdentityServerClient client = getKeycloakIdentityServerClient();
        client.migrateUserStore(Wso2ISProfileList, this.gatewayId, this.keycloakTemporaryUserPassword, this.roleConversionMap);
    }

    private KeycloakIdentityServerClient getKeycloakIdentityServerClient() {
        return new KeycloakIdentityServerClient(this.keycloakServiceURL,
                    this.keycloakAdminUsername,
                    this.keycloakAdminPassword,
                    this.keycloakTrustStorePath,
                    this.keycloakTrustStorePassword);
    }

    private void loadConfigFile(String filename) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(filename));
            // Load values from properties if they exist, otherwise will just use default values
            this.gatewayId = properties.getProperty(GATEWAY_ID, this.gatewayId);
            this.gatewayURL = properties.getProperty(GATEWAY_URL, this.gatewayURL);
            this.gatewayAdminUsername = properties.getProperty(GATEWAY_ADMIN_USERNAME, this.gatewayAdminUsername);
            this.gatewayAdminFirstName = properties.getProperty(GATEWAY_ADMIN_FIRST_NAME, this.gatewayAdminFirstName);
            this.gatewayAdminLastName = properties.getProperty(GATEWAY_ADMIN_LAST_NAME, this.gatewayAdminLastName);
            this.gatewayAdminEmailAddress = properties.getProperty(GATEWAY_ADMIN_EMAIL_ADDRESS, this.gatewayAdminEmailAddress);
            this.wso2ISAdminUsername = properties.getProperty(WSO2IS_ADMIN_USERNAME, this.wso2ISAdminUsername);
            this.wso2ISAdminPassword = properties.getProperty(WSO2IS_ADMIN_PASSWORD, this.wso2ISAdminPassword);
            this.airavataServiceServerHost = properties.getProperty(AIRAVATA_SERVICE_HOST, this.airavataServiceServerHost);
            this.airavataServiceServerPort = Integer.valueOf(properties.getProperty(AIRAVATA_SERVICE_PORT, Integer.toString(this.airavataServiceServerPort)));
            this.airavataServiceSecure = Boolean.valueOf(properties.getProperty(AIRAVATA_SERVICE_SECURE, "false"));
            this.profileServiceServerHost = properties.getProperty(PROFILE_SERVICE_HOST, this.profileServiceServerHost);
            this.profileServiceServerPort = Integer.valueOf(properties.getProperty(PROFILE_SERVICE_PORT, Integer.toString(this.profileServiceServerPort)));
            this.keycloakServiceURL = properties.getProperty(KEYCLOAK_SERVICE_URL, this.keycloakServiceURL);
            this.keycloakAdminUsername = properties.getProperty(KEYCLOAK_ADMIN_USERNAME, this.keycloakAdminUsername);
            this.keycloakAdminPassword = properties.getProperty(KEYCLOAK_ADMIN_PASSWORD, this.keycloakAdminPassword);
            this.keycloakTrustStorePath = properties.getProperty(KEYCLOAK_TRUSTSTORE_PATH, this.keycloakTrustStorePath);
            this.keycloakTrustStorePassword = properties.getProperty(KEYCLOAK_TRUSTSTORE_PASSWORD, this.keycloakTrustStorePassword);
            this.keycloakTemporaryUserPassword = properties.getProperty(KEYCLOAK_USER_TEMP_PASSWORD, this.keycloakTemporaryUserPassword);
            // Custom role names
            this.roleConversionMap.put(properties.getProperty(WSO2IS_ADMIN_ROLENAME, "admin"), "admin");
            this.roleConversionMap.put(properties.getProperty(WSO2IS_ADMIN_READ_ONLY_ROLENAME, "admin-read-only"), "admin-read-only");
            this.roleConversionMap.put(properties.getProperty(WSO2IS_GATEWAY_USER_ROLENAME, "gateway-user"), "gateway-user");
            this.roleConversionMap.put(properties.getProperty(WSO2IS_USER_PENDING_ROLENAME, "user-pending"), "user-pending");
            this.roleConversionMap.put(properties.getProperty(WSO2IS_GATEWAY_PROVIDER_ROLENAME, "gateway-provider"), "gateway-provider");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        MigrationManager migrationManager = new MigrationManager();
        if (args.length > 0) {
            String configFilename = args[0];
            migrationManager.loadConfigFile(configFilename);
        }
        migrationManager.setISLoginCredentials();
        List<UserProfileDAO> userProfileList = migrationManager.getUserProfilesFromWso2IS();
        try {
            migrationManager.migrateGatewayProfileToAiravata();
            // Must migrate profiles to Keycloak first because Profile Service will attempt to keep user profiles
            // in since with Keycloak user profiles
            migrationManager.migrateUserProfilesToKeycloak(userProfileList);
            migrationManager.migrateUserProfilesToAiravata(userProfileList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}