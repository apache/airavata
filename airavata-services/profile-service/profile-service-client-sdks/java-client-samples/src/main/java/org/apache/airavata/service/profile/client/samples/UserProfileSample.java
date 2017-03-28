package org.apache.airavata.service.profile.client.samples;

import org.apache.airavata.model.user.*;
import org.apache.airavata.model.workspace.User;
import org.apache.airavata.service.profile.client.ProfileServiceClientFactory;
import org.apache.airavata.service.profile.client.util.ProfileServiceClientUtil;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by goshenoy on 3/23/17.
 */
public class UserProfileSample {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileSample.class);
    private static UserProfileService.Client userProfileClient;

    public static void main(String[] args) throws Exception {
        try {
            String profileServiceServerHost = ProfileServiceClientUtil.getProfileServiceServerHost();
            int profileServiceServerPort = ProfileServiceClientUtil.getProfileServiceServerPort();

            userProfileClient = ProfileServiceClientFactory.createUserProfileServiceClient(profileServiceServerHost, profileServiceServerPort);
            userProfileClient.addUserProfile(getUserProfile());

        } catch (Exception ex) {
            logger.error("UserProfile client-sample Exception: " + ex, ex);
        }
    }

    private static UserProfile getUserProfile() {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserModelVersion("model-001");
        userProfile.setAiravataInternalUserId("test-user-internal-001");
        userProfile.setUserId("test-user-001");
        userProfile.setGatewayId("test-gateway-001");
        userProfile.addToEmails("test-user-001@domain1.com");
        userProfile.addToEmails("test-user-002@domain1.com");
        userProfile.setUserName("test-username-001");
        userProfile.setCreationTime(new Date().getTime());
        userProfile.setLastAccessTime(new Date().getTime());
        userProfile.setValidUntil(new Date().getTime());
        userProfile.setState(Status.ACTIVE);
        userProfile.setNsfDemographics(getNSFDemographics());
        return userProfile;
    }

    private static NSFDemographics getNSFDemographics() {
        NSFDemographics nsfDemographics = new NSFDemographics();
        nsfDemographics.setAiravataInternalUserId("test-user-internal-001");
        nsfDemographics.setGender("male");
        nsfDemographics.setUsCitizenship(USCitizenship.US_CITIZEN);
        nsfDemographics.addToEthnicities(ethnicity.NOT_HISPANIC_LATINO);
        nsfDemographics.addToRaces(race.AMERICAN_INDIAN_OR_ALASKAN_NATIVE);
        return nsfDemographics;
    }

}
