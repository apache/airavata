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
package org.apache.airavata.service.profile.client.samples;

import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.*;
import org.apache.airavata.model.workspace.User;
import org.apache.airavata.service.profile.client.ProfileServiceClientFactory;
import org.apache.airavata.service.profile.client.util.ProfileServiceClientUtil;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by goshenoy on 3/23/17.
 */
public class UserProfileSample {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileSample.class);
    private static UserProfileService.Client userProfileClient;
    private static String testUserId = null;
    private static String testGatewayId = "test-gateway-465";
    private static AuthzToken authzToken = new AuthzToken("empy_token");

    /**
     * Performs the following operations in sequence:
     *  1. create new user
     *  2. find user created
     *  3. update created user's name
     *  4. find all users in gateway
     *  5. find created user by name
     *  6. delete created user
     *  7. check if user exists
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        try {
            String profileServiceServerHost = ProfileServiceClientUtil.getProfileServiceServerHost();
            int profileServiceServerPort = ProfileServiceClientUtil.getProfileServiceServerPort();

            userProfileClient = ProfileServiceClientFactory.createUserProfileServiceClient(profileServiceServerHost, profileServiceServerPort);

            // test add-user-profile
            testUserId = userProfileClient.addUserProfile(authzToken, getUserProfile(null));
            assert (testUserId != null) : "User creation failed. Null userId returned!";
            System.out.println("User created with userId: " + testUserId);

            // test find-user-profile
            UserProfile userProfile = userProfileClient.getUserProfileById(authzToken, testUserId, testGatewayId);
            assert (userProfile != null) : "Could not find user with userId: " + testUserId + ", and gatewayID: " + testGatewayId;
            System.out.println("UserProfile: " + userProfile);

            // test update-user-profile : update name
            userProfile = getUserProfile(testUserId);
            String newFName = userProfile.getFirstName().replaceAll("fname", "fname-updated");
            userProfile.setFirstName(newFName);
            boolean updateSuccess = userProfileClient.updateUserProfile(authzToken, userProfile);
            assert (updateSuccess) : "User update with new firstName: [" + newFName + "], Failed!";
            System.out.println("User update with new firstName: [" + newFName + "], Successful!");

            // test get-all-userprofiles
            List<UserProfile> userProfileList = userProfileClient.getAllUserProfilesInGateway(authzToken, testGatewayId, 0, 5);
            assert (userProfileList != null && !userProfileList.isEmpty()) : "Failed to retrieve users for gateway!";
            System.out.println("Printing userList retrieved..");
            for (UserProfile userProfile1 : userProfileList) {
                System.out.println("\t [UserProfile] userId: " + userProfile1.getUserId());
            }

            // test delete-user-profile
            boolean deleteSuccess = userProfileClient.deleteUserProfile(authzToken, testUserId, testGatewayId);
            assert (deleteSuccess) : "Delete user failed for userId: " + testUserId;
            System.out.println("Successfully deleted user with userId: " + testUserId);

            // test-check-user-exist
            boolean userExists = userProfileClient.doesUserExist(authzToken, testUserId, testGatewayId);
            assert (!userExists) : "User should not exist, but it does.";
            System.out.println("User was deleted, hence does not exist!");
            System.out.println("*** DONE ***");
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("UserProfile client-sample Exception: " + ex, ex);
        }
    }

    private static UserProfile getUserProfile(String userId) {
        // get random value for userId
        int userIdValue = ThreadLocalRandom.current().nextInt(1000);

        if (userId != null) {
            userIdValue = Integer.parseInt(userId.replaceAll("test-user-", ""));
        }
        // construct userProfile object
        UserProfile userProfile = new UserProfile();
        userProfile.setUserModelVersion("model-" + userIdValue);
        userProfile.setAiravataInternalUserId("test-user-internal-" + userIdValue);
        userProfile.setUserId("test-user-" + userIdValue);
        userProfile.setFirstName("test-user-fname");
        userProfile.setLastName("test-user-lname");
        userProfile.setGatewayId(testGatewayId);
        userProfile.addToEmails("test-user-" + userIdValue + "@domain1.com");
        userProfile.addToEmails("test-user-" + userIdValue + "@domain2.com");
        userProfile.setCreationTime(System.currentTimeMillis());
        userProfile.setLastAccessTime(System.currentTimeMillis());
        userProfile.setValidUntil(System.currentTimeMillis());
        userProfile.setState(Status.ACTIVE);
        userProfile.setNsfDemographics(getNSFDemographics(userIdValue));
        return userProfile;
    }

    private static NSFDemographics getNSFDemographics(int userIdValue) {
        // construct nsfdemographics object
        NSFDemographics nsfDemographics = new NSFDemographics();
        nsfDemographics.setAiravataInternalUserId("test-user-internal-" + userIdValue);
        nsfDemographics.setGender("male");
        nsfDemographics.setUsCitizenship(USCitizenship.US_CITIZEN);
        nsfDemographics.addToEthnicities(ethnicity.NOT_HISPANIC_LATINO);
        nsfDemographics.addToRaces(race.AMERICAN_INDIAN_OR_ALASKAN_NATIVE);
        return nsfDemographics;
    }
}
