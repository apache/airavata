package org.apache.airavata.service.credential;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.server.CredentialStoreServerHandler;
import org.apache.airavata.model.credential.store.CredentialSummary;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.model.credential.store.SummaryType;
import org.apache.airavata.model.group.ResourcePermissionType;
import org.apache.airavata.model.group.ResourceType;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceAuthorizationException;
import org.apache.airavata.service.exception.ServiceException;
import org.apache.airavata.sharing.registry.models.Entity;
import org.apache.airavata.sharing.registry.models.EntitySearchField;
import org.apache.airavata.sharing.registry.models.SearchCondition;
import org.apache.airavata.sharing.registry.models.SearchCriteria;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CredentialService {

    private static final Logger logger = LoggerFactory.getLogger(CredentialService.class);

    private final CredentialStoreServerHandler credentialHandler;
    private final SharingRegistryServerHandler sharingHandler;

    public CredentialService(CredentialStoreServerHandler credentialHandler, SharingRegistryServerHandler sharingHandler) {
        this.credentialHandler = credentialHandler;
        this.sharingHandler = sharingHandler;
    }

    public String generateAndRegisterSSHKeys(RequestContext ctx, String description) throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        String userName = ctx.getUserId();
        try {
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setUsername(userName);
            sshCredential.setGatewayId(gatewayId);
            sshCredential.setDescription(description);
            String key = credentialHandler.addSSHCredential(sshCredential);
            try {
                Entity entity = new Entity();
                entity.setEntityId(key);
                entity.setDomainId(gatewayId);
                entity.setEntityTypeId(gatewayId + ":" + ResourceType.CREDENTIAL_TOKEN);
                entity.setOwnerId(userName + "@" + gatewayId);
                entity.setName(key);
                entity.setDescription(description);
                sharingHandler.createEntity(entity);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                logger.error("Rolling back ssh key creation for user " + userName + " and description [" + description + "]");
                credentialHandler.deleteSSHCredential(key, gatewayId);
                throw new ServiceException("Failed to create sharing registry record");
            }
            logger.debug("Airavata generated SSH keys for gateway : " + gatewayId + " and for user : " + userName);
            return key;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error occurred while registering SSH Credential. More info : " + e.getMessage(), e);
        }
    }

    public String registerPwdCredential(RequestContext ctx, String loginUserName, String password, String description)
            throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        String userName = ctx.getUserId();
        try {
            PasswordCredential pwdCredential = new PasswordCredential();
            pwdCredential.setPortalUserName(userName);
            pwdCredential.setLoginUserName(loginUserName);
            pwdCredential.setPassword(password);
            pwdCredential.setDescription(description);
            pwdCredential.setGatewayId(gatewayId);
            String key = credentialHandler.addPasswordCredential(pwdCredential);
            try {
                Entity entity = new Entity();
                entity.setEntityId(key);
                entity.setDomainId(gatewayId);
                entity.setEntityTypeId(gatewayId + ":" + ResourceType.CREDENTIAL_TOKEN);
                entity.setOwnerId(userName + "@" + gatewayId);
                entity.setName(key);
                entity.setDescription(description);
                sharingHandler.createEntity(entity);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                logger.error("Rolling back password registration for user " + userName + " and description [" + description + "]");
                credentialHandler.deletePWDCredential(key, gatewayId);
                throw new ServiceException("Failed to create sharing registry record");
            }
            logger.debug("Airavata generated PWD credential for gateway : " + gatewayId + " and for user : " + loginUserName);
            return key;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error occurred while registering PWD Credential. More info : " + e.getMessage(), e);
        }
    }

    public CredentialSummary getCredentialSummary(RequestContext ctx, String tokenId) throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        String userName = ctx.getUserId();
        try {
            if (!userHasAccess(gatewayId, userName, tokenId, ResourcePermissionType.READ)) {
                logger.info("User " + userName + " not allowed to access credential store token " + tokenId);
                throw new ServiceAuthorizationException("User does not have permission to access this resource");
            }
            CredentialSummary credentialSummary = credentialHandler.getCredentialSummary(tokenId, gatewayId);
            logger.debug("Airavata retrieved the credential summary for token " + tokenId + " GatewayId: " + gatewayId);
            return credentialSummary;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error retrieving credential summary for token " + tokenId + ". GatewayId: " + gatewayId + " More info : " + e.getMessage(), e);
        }
    }

    public List<CredentialSummary> getAllCredentialSummaries(RequestContext ctx, SummaryType type) throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        String userName = ctx.getUserId();
        try {
            List<SearchCriteria> filters = new ArrayList<>();
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
            searchCriteria.setSearchCondition(SearchCondition.EQUAL);
            searchCriteria.setValue(gatewayId + ":" + ResourceType.CREDENTIAL_TOKEN.name());
            filters.add(searchCriteria);
            List<String> accessibleTokenIds =
                    sharingHandler.searchEntities(gatewayId, userName + "@" + gatewayId, filters, 0, -1).stream()
                            .map(p -> p.getEntityId())
                            .collect(Collectors.toList());
            List<CredentialSummary> credentialSummaries =
                    credentialHandler.getAllCredentialSummaries(type, accessibleTokenIds, gatewayId);
            logger.debug("Airavata successfully retrieved credential summaries of type " + type + " GatewayId: " + gatewayId);
            return credentialSummaries;
        } catch (Exception e) {
            throw new ServiceException("Error retrieving credential summaries of type " + type + ". GatewayId: " + gatewayId + " More info : " + e.getMessage(), e);
        }
    }

    public boolean deleteSSHPubKey(RequestContext ctx, String airavataCredStoreToken) throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        String userName = ctx.getUserId();
        try {
            if (!userHasAccess(gatewayId, userName, airavataCredStoreToken, ResourcePermissionType.WRITE)) {
                logger.info("User " + userName + " not allowed to delete (no WRITE permission) credential store token " + airavataCredStoreToken);
                throw new ServiceAuthorizationException("User does not have permission to delete this resource.");
            }
            logger.debug("Airavata deleted SSH pub key for gateway Id : " + gatewayId + " and with token id : " + airavataCredStoreToken);
            return credentialHandler.deleteSSHCredential(airavataCredStoreToken, gatewayId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error occurred while deleting SSH credential. More info : " + e.getMessage(), e);
        }
    }

    public boolean deletePWDCredential(RequestContext ctx, String airavataCredStoreToken) throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        String userName = ctx.getUserId();
        try {
            if (!userHasAccess(gatewayId, userName, airavataCredStoreToken, ResourcePermissionType.WRITE)) {
                logger.info("User " + userName + " not allowed to delete (no WRITE permission) credential store token " + airavataCredStoreToken);
                throw new ServiceAuthorizationException("User does not have permission to delete this resource.");
            }
            logger.debug("Airavata deleted PWD credential for gateway Id : " + gatewayId + " and with token id : " + airavataCredStoreToken);
            return credentialHandler.deletePWDCredential(airavataCredStoreToken, gatewayId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error occurred while deleting PWD credential. More info : " + e.getMessage(), e);
        }
    }

    private boolean userHasAccess(String gatewayId, String userName, String entityId, ResourcePermissionType permissionType) {
        String userId = userName + "@" + gatewayId;
        try {
            boolean hasOwnerAccess = sharingHandler.userHasAccess(
                    gatewayId, userId, entityId, gatewayId + ":" + ResourcePermissionType.OWNER);
            if (permissionType.equals(ResourcePermissionType.WRITE)) {
                return hasOwnerAccess
                        || sharingHandler.userHasAccess(gatewayId, userId, entityId, gatewayId + ":" + ResourcePermissionType.WRITE);
            } else if (permissionType.equals(ResourcePermissionType.READ)) {
                return hasOwnerAccess
                        || sharingHandler.userHasAccess(gatewayId, userId, entityId, gatewayId + ":" + ResourcePermissionType.READ);
            } else if (permissionType.equals(ResourcePermissionType.OWNER)) {
                return hasOwnerAccess;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Unable to check if user has access", e);
        }
    }

    private boolean isSharingEnabled() {
        try {
            return ServerSettings.isEnableSharing();
        } catch (Exception e) {
            return false;
        }
    }
}
