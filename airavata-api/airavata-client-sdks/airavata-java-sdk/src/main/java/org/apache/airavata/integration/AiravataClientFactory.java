package org.apache.airavata.integration;

import org.apache.airavata.integration.clients.AiravataAPIClient;
import org.apache.airavata.integration.clients.IdentityManagementClient;
import org.apache.airavata.integration.utils.SFTPFileHandler;

public class AiravataClientFactory {


    public static IdentityManagementClient getIdentityManagementClient(String propertiesFilePath) {
        try {
            IdentityManagementClient identityManagementClient = new IdentityManagementClient(propertiesFilePath);
            return identityManagementClient;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static SFTPFileHandler getSFTPHandler(String propertiesFilePath) {
        try {
            SFTPFileHandler sftpFileHandler = new SFTPFileHandler(propertiesFilePath);
            return sftpFileHandler;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static AiravataAPIClient getAiravataAPIClient(String propertiesFilePath) {
        try {
            AiravataAPIClient airavataAPIClient = new AiravataAPIClient(propertiesFilePath);
            return airavataAPIClient;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
