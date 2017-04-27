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
        superAdminCreds.setLoginUserName("SomeAdmin");
        superAdminCreds.setPassword("SomePassord");
        superAdminCreds.setPortalUserName("superAdmin");
        TenantManagementKeycloakImpl client = new TenantManagementKeycloakImpl();
        try {
            client.addTenant(superAdminCreds, testGateway);
            if (!client.createTenantAdminAccount(superAdminCreds, testGateway)) {
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
         user.setUserName("Some Man");
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
             client.createUser(tenantAdminCreds,user,"test@123");
             client.enableUserAccount(tenantAdminCreds,user);
         } catch (IamAdminServicesException e) {
             e.printStackTrace();
         }
     }
}
