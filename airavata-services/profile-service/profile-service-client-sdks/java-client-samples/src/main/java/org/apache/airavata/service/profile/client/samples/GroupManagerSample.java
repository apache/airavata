package org.apache.airavata.service.profile.client.samples;

import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.service.profile.client.ProfileServiceClientFactory;
import org.apache.airavata.service.profile.client.util.ProfileServiceClientUtil;
import org.apache.airavata.service.profile.groupmanager.cpi.GroupManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GroupManagerSample {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileSample.class);
    private static GroupManagerService.Client groupMangerClient;
    private static String testGatewayId = "test-gateway-465";
    private static AuthzToken authzToken = new AuthzToken("empy_token");

    public static void main(String args[]) throws Exception {
        try {
            String profileServiceServerHost = ProfileServiceClientUtil.getProfileServiceServerHost();
            int profileServiceServerPort = ProfileServiceClientUtil.getProfileServiceServerPort();

            groupMangerClient = ProfileServiceClientFactory.createGroupManagerServiceClient(profileServiceServerHost, profileServiceServerPort);
            Map<String, String> claimsMap = new HashMap<>();
            claimsMap.put(Constants.GATEWAY_ID,testGatewayId);
            authzToken.setClaimsMap(claimsMap);

            //NOTE: Only these two methods can be tested as other methods require us to create a group.
            // createGroup() needs createUser() to be executed. Currently ownerId is set from createUser()
            // method which requires a sharingServiceClient. Hence, verifying only this method for now.
            System.out.println("hasAdminAccess() should return [false]: "+ groupMangerClient.hasAdminAccess(authzToken,"test-group-1", "test-user-1"));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            logger.error("GroupManager client-sample Exception: " + ex, ex);
        }
    }
}
