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
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.userprofile.cpi.UserProfileService;
import org.apache.thrift.TException;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class MigrationManager {

    private ArrayList<ISLoginCredentialsDAO> adminCredentials = new ArrayList<ISLoginCredentialsDAO>();

    /*Add the credentials for all the tenants from which the profile should be migrated to Airavata DB*/

    public void setISLoginCredentials(){
        adminCredentials.add(new ISLoginCredentialsDAO("prod.seagrid","UserName","Password"));
        // new credential records here...
    }

    /* Method used to fetch all the user profiles from the registered tenants */

    public List<UserProfileDAO> getUserProfilesFromIS(){
        ArrayList<UserProfileDAO> userProfileList = new ArrayList<UserProfileDAO>();
        for(ISLoginCredentialsDAO creds:adminCredentials){
            RemoteUserStoreManagerServiceStub isClient = IdentityServerClient.getAdminServiceClient(creds.getLoginUserName(),creds.getLoginPassword(),"RemoteUserStoreManagerService");
            String[] userList;
            System.out.println("Fetching User Profiles for " + creds.getGateway() + " tenant ...");
            try {
                userList = isClient.getUserList("http://wso2.org/claims/givenname", "*", "default");
                System.out.println("FirstName\tLastName\tEmail\t\t\tuserName\tCountry\tOrganization\tphone");
                String[] claims = {"http://wso2.org/claims/givenname",
                                    "http://wso2.org/claims/lastname",
                                    "http://wso2.org/claims/emailaddress",
                                    "http://wso2.org/claims/country",
                                    "http://wso2.org/claims/organization",
                                    "http://wso2.org/claims/mobile",
                                    "http://wso2.org/claims/telephone",
                                    "http://wso2.org/claims/streetaddress"};
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
                        }
                    }
                    userProfile.setUserName(user);
                    userProfile.setGatewayID(creds.getGateway());
                    userProfile.setPhones(phones);
                    System.out.println(userProfile.getFirstName()+"\t"+userProfile.getLastName()+"\t"+userProfile.getUserName()+"\t"+userProfile.getEmail()+"\t"+userProfile.getCountry()+"\t"+userProfile.getOrganization() + userProfile.getAddress());
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

    /* Method used to migrate User profiles to Airavata DB by making a call to User profile thrift Service */
    private boolean migrateUserProfilesToAiravata(List<UserProfileDAO> ISProfileList) throws TException, ApplicationSettingsException {
        System.out.println("Initiating migration to Airavata internal DB ...");
        UserProfileAiravataThriftClient objFactory = new UserProfileAiravataThriftClient();
        UserProfileService.Client client = objFactory.getRegistryServiceClient();
        UserProfile airavataUserProfile = new UserProfile();
        // Here are the data associations...
        for(UserProfileDAO ISProfile : ISProfileList){
            airavataUserProfile.setUserName(ISProfile.getFirstName()+" "+ISProfile.getLastName());
            airavataUserProfile.setUserId(ISProfile.getUserName());
            airavataUserProfile.setGatewayId(ISProfile.getGatewayID());
            List<String> emails = new ArrayList<String>();
            emails.add(ISProfile.getEmail());
            airavataUserProfile.setEmails(emails);
            airavataUserProfile.setHomeOrganization(ISProfile.getOrganization());
            airavataUserProfile.setPhones(ISProfile.getPhones());
            airavataUserProfile.setCountry(ISProfile.getCountry());
            client.addUserProfile(airavataUserProfile);
        }
        return false;
    }

    public static void main(String[] args) {
        MigrationManager migrationManager = new MigrationManager();
        migrationManager.setISLoginCredentials();
        List<UserProfileDAO> userProfileList = migrationManager.getUserProfilesFromIS();
        try {
            migrationManager.migrateUserProfilesToAiravata(userProfileList);
        } catch (TException e) {
            e.printStackTrace();
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        }
    }
}
