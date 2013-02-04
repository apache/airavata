package org.apache.airavata.credential.store;

/**
 * Represents the community user.
 */
public class CommunityUser {

    private String gatewayName;
    private String userName;
    private String userEmail;

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public CommunityUser(String gatewayName, String userName, String userEmail) {
        this.gatewayName = gatewayName;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public CommunityUser(String gatewayName, String userName) {
        this.gatewayName = gatewayName;
        this.userName = userName;
    }
}
