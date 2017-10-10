package org.apache.airavata.service.profile.iam.admin.services.core.tests;


import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.service.profile.iam.admin.services.core.impl.TenantManagementKeycloakImpl;
import org.apache.airavata.service.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SetupNewGateway {

    private final static Logger logger = LoggerFactory.getLogger(SetupNewGateway.class);

    public static void main(String[] args) {
        findUser();
    }

    public static void setUpGateway(){
        Gateway testGateway = new Gateway();
        testGateway.setGatewayId("maven.test.gateway");
        testGateway.setGatewayName("maven test gateway");
        testGateway.setIdentityServerUserName("mavenTest");
        testGateway.setGatewayAdminFirstName("Maven");
        testGateway.setGatewayAdminLastName("Test");
        testGateway.setGatewayAdminEmail("some.man@gmail.com");
        PasswordCredential superAdminCreds = new PasswordCredential();
        superAdminCreds.setGatewayId(testGateway.getGatewayId());
        superAdminCreds.setDescription("test credentials for IS admin creation");
        superAdminCreds.setLoginUserName("airavataAdmin");
        superAdminCreds.setPassword("Airavata@123");
        superAdminCreds.setPortalUserName("superAdmin");
        TenantManagementKeycloakImpl client = new TenantManagementKeycloakImpl();
        try {
            client.addTenant(superAdminCreds, testGateway);
            if (!client.createTenantAdminAccount(superAdminCreds, testGateway, "Test@123")) {
                logger.error("Admin account creation failed !!, please refer error logs for reason");
            }
            Gateway gatewayWithIdAndSecret = client.configureClient(superAdminCreds, testGateway);
            System.out.println(gatewayWithIdAndSecret.getOauthClientId());
            System.out.println(gatewayWithIdAndSecret.getOauthClientSecret());
        } catch (IamAdminServicesException ex) {
            logger.error("Gateway Setup Failed, reason: " + ex.getCause(), ex);
        }
    }
     public static void UserRegistration(){
         UserProfile user = new UserProfile();
         user.setUserId("testuser");
         user.setFirstName("test-firstname");
         user.setLastName("test-lastname");
         List<String> emails = new ArrayList<>();
         emails.add("some.man@outlook.com");
         user.setGatewayId("maven.test.gateway");
         user.setEmails(emails);
        PasswordCredential tenantAdminCreds = new PasswordCredential();
         tenantAdminCreds.setGatewayId(user.getGatewayId());
         tenantAdminCreds.setDescription("test credentials for tenant admin creation");
         tenantAdminCreds.setLoginUserName("mavenTest");
         tenantAdminCreds.setPassword("Test@1234");
         tenantAdminCreds.setPortalUserName("TenantAdmin");

         TenantManagementKeycloakImpl client = new TenantManagementKeycloakImpl();
         try {
             client.createUser(tenantAdminCreds, user.getGatewayId(), user.getUserId(), user.getEmails().get(0), user.getFirstName(), user.getLastName(),"test@123");
             client.enableUserAccount(tenantAdminCreds, user.getGatewayId(), user.getUserId());
         } catch (IamAdminServicesException e) {
             e.printStackTrace();
         }
     }

//     public static void resetPassword(){
//         UserProfile user = new UserProfile();
//         user.setUserId("testuser");
//         List<String> emails = new ArrayList<>();
//         emails.add("some.man@outlook.com");
//         user.setGatewayId("maven.test.gateway");
//         user.setEmails(emails);
//         TenantManagementKeycloakImpl client = new TenantManagementKeycloakImpl();
//         try {
//             PasswordCredential tenantAdminCreds = new PasswordCredential();
//             tenantAdminCreds.setGatewayId(user.getGatewayId());
//             tenantAdminCreds.setDescription("test credentials for tenant admin creation");
//             tenantAdminCreds.setLoginUserName("mavenTest");
//             tenantAdminCreds.setPassword("Test@1234");
//             tenantAdminCreds.setPortalUserName("TenantAdmin");
//             client.resetUserPassword(tenantAdminCreds,user,"test@123");
//         } catch (IamAdminServicesException e) {
//             e.printStackTrace();
//         }
//     }

     public static void findUser(){
         UserProfile user = new UserProfile();

         List<String> emails = new ArrayList<>();
         emails.add("some.man@outlook.com");
         user.setGatewayId("maven.test.gateway");
         user.setEmails(emails);
         TenantManagementKeycloakImpl client = new TenantManagementKeycloakImpl();
         try {
             PasswordCredential tenantAdminCreds = new PasswordCredential();
             tenantAdminCreds.setGatewayId(user.getGatewayId());
             tenantAdminCreds.setDescription("test credentials for tenant admin creation");
             tenantAdminCreds.setLoginUserName("mavenTest");
             tenantAdminCreds.setPassword("Test@1234");
             tenantAdminCreds.setPortalUserName("TenantAdmin");
             List<UserProfile> list = client.findUser(tenantAdminCreds,"maven.test.gateway","some.man@outlook.com",null);
             System.out.println(list.get(0).getUserId());
         } catch (IamAdminServicesException e) {
             e.printStackTrace();
         }
     }
}
