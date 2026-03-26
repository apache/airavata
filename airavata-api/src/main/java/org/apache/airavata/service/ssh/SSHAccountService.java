package org.apache.airavata.service.ssh;

import org.apache.airavata.accountprovisioning.SSHAccountManager;
import org.apache.airavata.credential.store.server.CredentialStoreServerHandler;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSHAccountService {

    private static final Logger logger = LoggerFactory.getLogger(SSHAccountService.class);

    private final CredentialStoreServerHandler credentialHandler;

    public SSHAccountService(CredentialStoreServerHandler credentialHandler) {
        this.credentialHandler = credentialHandler;
    }

    public boolean doesUserHaveSSHAccount(RequestContext ctx, String computeResourceId, String userId)
            throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        try {
            return SSHAccountManager.doesUserHaveSSHAccount(gatewayId, computeResourceId, userId);
        } catch (Exception e) {
            String msg = "Error occurred while checking if [" + userId + "] has an SSH Account on ["
                    + computeResourceId + "].";
            logger.error(msg, e);
            throw new ServiceException(msg + " More info: " + e.getMessage(), e);
        }
    }

    public boolean isSSHSetupCompleteForUserComputeResourcePreference(
            RequestContext ctx, String computeResourceId, String airavataCredStoreToken)
            throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        String userId = ctx.getUserId();

        SSHCredential sshCredential;
        try {
            sshCredential = credentialHandler.getSSHCredential(airavataCredStoreToken, gatewayId);
        } catch (Exception e) {
            String msg = "Error occurred while retrieving SSH Credential.";
            logger.error(msg, e);
            throw new ServiceException(msg + " More info: " + e.getMessage(), e);
        }

        try {
            return SSHAccountManager.isSSHAccountSetupComplete(gatewayId, computeResourceId, userId, sshCredential);
        } catch (Exception e) {
            String msg = "Error occurred while checking if setup of SSH account is complete for user [" + userId + "].";
            logger.error(msg, e);
            throw new ServiceException(msg + " More info: " + e.getMessage(), e);
        }
    }

    public UserComputeResourcePreference setupUserComputeResourcePreferencesForSSH(
            RequestContext ctx, String computeResourceId, String userId, String airavataCredStoreToken)
            throws ServiceException {
        String gatewayId = ctx.getGatewayId();

        SSHCredential sshCredential;
        try {
            sshCredential = credentialHandler.getSSHCredential(airavataCredStoreToken, gatewayId);
        } catch (Exception e) {
            String msg = "Error occurred while retrieving SSH Credential.";
            logger.error(msg, e);
            throw new ServiceException(msg + " More info: " + e.getMessage(), e);
        }

        try {
            return SSHAccountManager.setupSSHAccount(gatewayId, computeResourceId, userId, sshCredential);
        } catch (Exception e) {
            String msg = "Error occurred while automatically setting up SSH account for user [" + userId + "].";
            logger.error(msg, e);
            throw new ServiceException(msg + " More info: " + e.getMessage(), e);
        }
    }
}
