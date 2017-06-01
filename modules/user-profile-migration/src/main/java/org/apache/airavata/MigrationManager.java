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
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.Status;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.thrift.TException;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

public class MigrationManager {

    private ArrayList<Wso2ISLoginCredentialsDAO> adminCredentials = new ArrayList<Wso2ISLoginCredentialsDAO>();
    private static AuthzToken authzToken = new AuthzToken("empy_token");
    private String profileServiceServerHost = "localhost";
    private int profileServiceServerPort = 8962;
    private Map<String,String> roleConversionMap = createDefaultRoleConversionMap();

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
        adminCredentials.add(new Wso2ISLoginCredentialsDAO("gateway-id","username","password"));
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
                        "http://wso2.org/claims/role"};
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
                        }
                    }
                    userProfile.setUserName(user);
                    userProfile.setGatewayID(creds.getGateway());
                    userProfile.setPhones(phones);
                    System.out.println(userProfile.getFirstName()+"\t"+userProfile.getLastName()+"\t"+userProfile.getUserName()+"\t"+userProfile.getEmail()+"\t"+userProfile.getCountry()+"\t"+userProfile.getOrganization() + "\t" + userProfile.getAddress() + "\t" + userProfile.getRoles());
                    userProfileList.add(userProfile);
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

    /* Method used to migrate User profiles to Airavata DB by making a call to User profile thrift Service */
    private boolean migrateUserProfilesToAiravata(List<UserProfileDAO> ISProfileList) throws TException, ApplicationSettingsException {
        System.out.println("Initiating migration to Airavata internal DB ...");
        UserProfileAiravataThriftClient objFactory = new UserProfileAiravataThriftClient();
        UserProfileService.Client client = objFactory.getUserProfileServiceClient(profileServiceServerHost, profileServiceServerPort);
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
            //TODO: fix authtzToken, for now we are using empty token
            client.addUserProfile(authzToken, airavataUserProfile);
        }
        return false;
    }

    private void migrateUserProfilesToKeycloak(List<UserProfileDAO> Wso2ISProfileList){
        KeycloakIdentityServerClient client = new KeycloakIdentityServerClient("https://iam.scigap.org/auth",
                "master",
                "SuperRealmUsername",
                "MasterRealmPassword",
                "trustStorePath",
                "trustStorePassword");
        client.migrateUserStore(Wso2ISProfileList,"keycloakTargetRealm","tempPassword", roleConversionMap);
    }

    public static void main(String[] args) {
        MigrationManager migrationManager = new MigrationManager();
        migrationManager.setISLoginCredentials();
        List<UserProfileDAO> userProfileList = migrationManager.getUserProfilesFromWso2IS();
        try {
            migrationManager.migrateUserProfilesToAiravata(userProfileList);
            migrationManager.migrateUserProfilesToKeycloak(userProfileList);
        } catch (TException e) {
            e.printStackTrace();
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        }
    }
}