package org.apache.airavata.service.profile.client.samples;

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
    private static String testGatewayId = "test-client-gateway";

    public static void main(String[] args) throws Exception {
        try {
            String profileServiceServerHost = ProfileServiceClientUtil.getProfileServiceServerHost();
            int profileServiceServerPort = ProfileServiceClientUtil.getProfileServiceServerPort();

            userProfileClient = ProfileServiceClientFactory.createUserProfileServiceClient(profileServiceServerHost, profileServiceServerPort);

            // test add-user-profile
            testUserId = userProfileClient.addUserProfile(getUserProfile());     // (1) run this only once
            assert (testUserId != null) : "User creation failed. Null userId returned!";
            System.out.println("User created with userId: " + testUserId);

            // test find-user-profile
            UserProfile userProfile = userProfileClient.getUserProfileById(testUserId, testGatewayId);
            assert (userProfile != null) : "Could not find user with userId: " + testUserId + ", and gatewayID: " + testGatewayId;
            System.out.println("UserProfile: " + userProfile);

            // test update-user-profile : update name
            userProfile = getUserProfile();
            String newUserName = userProfile.getUserName().replaceAll("username", "username-updated");
            userProfile.setUserName(newUserName);
            boolean updateSuccess = userProfileClient.updateUserProfile(userProfile);
            assert (updateSuccess) : "User update with new userName: [" + newUserName + "], Failed!";
            System.out.println("User update with new userName: [" + newUserName + "], Successful!");

            // test get-all-userprofiles
            List<UserProfile> userProfileList = userProfileClient.getAllUserProfilesInGateway(testGatewayId, 0, 5);
            assert (userProfileList != null && !userProfileList.isEmpty()) : "Failed to retrieve users for gateway!";
            for (UserProfile userProfile1 : userProfileList) {
                System.out.println("User found with userId: " + userProfile1.getUserId());
            }

            // test find-user-profile-by-name
            userProfile = userProfileClient.getUserProfileByName(newUserName, testGatewayId);
            assert (userProfile != null) : "Could not find user with userName: " + newUserName;
            System.out.println("UserProfile: " + userProfile);

            // test delete-user-profile
            boolean deleteSuccess = userProfileClient.deleteUserProfile(testUserId);
            assert (deleteSuccess) : "Delete user failed for userId: " + testUserId;
            System.out.println("Successfully deleted user with userId: " + testUserId);

            // test-check-user-exist
            boolean userExists = userProfileClient.doesUserExist(newUserName, testGatewayId);
            assert (!userExists) : "User should not exist, but it does.";


        } catch (Exception ex) {
            logger.error("UserProfile client-sample Exception: " + ex, ex);
        }
    }

    private static UserProfile getUserProfile() {
        // get random value for userId
        int userIdValue = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);

        // construct userProfile object
        UserProfile userProfile = new UserProfile();
        userProfile.setUserModelVersion("model-" + userIdValue);
        userProfile.setAiravataInternalUserId("test-user-internal-" + userIdValue);
        userProfile.setUserId("test-user-" + userIdValue);
        userProfile.setGatewayId(testGatewayId);
        userProfile.addToEmails("test-user-" + userIdValue + "@domain1.com");
        userProfile.addToEmails("test-user-" + userIdValue + "@domain2.com");
        userProfile.setUserName("test-username-" + userIdValue);
        userProfile.setCreationTime(new Date().getTime());
        userProfile.setLastAccessTime(new Date().getTime());
        userProfile.setValidUntil(new Date().getTime());
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
