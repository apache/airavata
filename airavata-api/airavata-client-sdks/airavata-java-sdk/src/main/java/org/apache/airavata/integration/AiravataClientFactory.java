package org.apache.airavata.integration;

import org.apache.airavata.integration.clients.AiravataAPIClient;
import org.apache.airavata.integration.clients.IdentityManagementClient;
import org.apache.airavata.integration.utils.SFTPFileHandler;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.security.AiravataSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AiravataClientFactory {
    private static Logger LOGGER = LoggerFactory.getLogger(AiravataAPIClient.class);

    public static IdentityManagementClient getIdentityManagementClient(String propertiesFilePath) throws IOException, AiravataSecurityException {
        try {
            IdentityManagementClient identityManagementClient = new IdentityManagementClient(propertiesFilePath);
            return identityManagementClient;
        } catch (Exception ex) {
            String msg = "Error occurred while creating Identity client, " +
                    "properties file may not exist " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw ex;
        }
    }

    public static SFTPFileHandler getSFTPHandler(String propertiesFilePath) throws IOException {
        try {
            SFTPFileHandler sftpFileHandler = new SFTPFileHandler(propertiesFilePath);
            return sftpFileHandler;
        } catch (Exception ex) {
            String msg = "Error occurred while creating SFTP handler, " +
                    "properties file may not exist " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw ex;
        }
    }

    public static AiravataAPIClient getAiravataAPIClient(String propertiesFilePath) throws IOException, AiravataClientException {
        try {
            AiravataAPIClient airavataAPIClient = new AiravataAPIClient(propertiesFilePath);
            return airavataAPIClient;
        } catch (Exception ex) {
            String msg = "Error occurred while retreving Airvata api client, " +
                    "properties file may not exist " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw ex;
        }
    }
}
