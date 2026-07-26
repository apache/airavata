/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.security.service;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.RequestContext;
import org.apache.airavata.iam.SharingHelper;
import org.apache.airavata.iam.service.SharingService;
import org.apache.airavata.models.commons.AccessFlags;
import org.apache.airavata.models.credential.store.CredentialSummary;
import org.apache.airavata.models.credential.store.CredentialSummaryWithAccess;
import org.apache.airavata.models.credential.store.SSHCredential;
import org.apache.airavata.models.credential.store.SummaryType;
import org.apache.airavata.models.group.ResourcePermissionType;
import org.apache.airavata.models.group.ResourceType;
import org.apache.airavata.models.sharing.registry.EntitySearchField;
import org.apache.airavata.models.sharing.registry.SearchCondition;
import org.apache.airavata.models.sharing.registry.SearchCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CredentialService {

    private static final Logger logger = LoggerFactory.getLogger(CredentialService.class);

    private final CredentialStoreService credentialHandler;
    private final SharingService sharingHandler;

    public CredentialService(CredentialStoreService credentialHandler, SharingService sharingHandler) {
        this.credentialHandler = credentialHandler;
        this.sharingHandler = sharingHandler;
    }

    public String generateAndRegisterSSHKeys(RequestContext ctx, String description) throws Exception {
        String gatewayId = ctx.getGatewayId();
        String userName = ctx.getUserId();
        try {
            SSHCredential sshCredential = SSHCredential.newBuilder()
                    .setUsername(userName)
                    .setGatewayId(gatewayId)
                    .setDescription(description)
                    .build();
            String key = credentialHandler.addSSHCredential(sshCredential);
            try {
                sharingHandler.createEntity(
                        key,
                        gatewayId,
                        gatewayId + ":" + ResourceType.CREDENTIAL_TOKEN,
                        userName + "@" + gatewayId,
                        key,
                        description,
                        null);
            } catch (Exception ex) {
                logger.error("Rolling back ssh key creation for user " + userName + " and description [" + description
                        + "]", ex);
                try {
                    credentialHandler.deleteSSHCredential(key, gatewayId);
                } catch (Exception deleteEx) {
                    logger.error("Failed to rollback ssh key creation for user " + userName + " and description ["
                            + description + "]", deleteEx);
                }
                throw new Exception("Failed to create sharing registry record");
            }
            logger.debug("Airavata generated SSH keys for gateway : " + gatewayId + " and for user : " + userName);
            return key;
        } catch (Exception e) {
            throw new Exception(
                    "Error occurred while registering SSH Credential. More info : " + e.getMessage(), e);
        }
    }

    public CredentialSummary getCredentialSummary(RequestContext ctx, String tokenId) throws Exception {
        String gatewayId = ctx.getGatewayId();
        String userName = ctx.getUserId();
        try {
            if (!SharingHelper.userHasAccess(
                    sharingHandler, gatewayId, userName, tokenId, ResourcePermissionType.READ)) {
                logger.info("User " + userName + " not allowed to access credential store token " + tokenId);
                throw new Exception("User does not have permission to access this resource");
            }
            CredentialSummary credentialSummary = credentialHandler.getCredentialSummary(tokenId, gatewayId);
            logger.debug("Airavata retrieved the credential summary for token " + tokenId + " GatewayId: " + gatewayId);
            return credentialSummary;
        } catch (Exception e) {
            throw new Exception(
                    "Error retrieving credential summary for token " + tokenId + ". GatewayId: " + gatewayId
                            + " More info : " + e.getMessage(),
                    e);
        }
    }

    /**
     * {@link #getCredentialSummary} plus the caller's server-computed access flags
     * (additive). Reuses
     * {@code getCredentialSummary} for READ enforcement so a caller can never
     * self-authorize; the flags
     * are derived from the credential's owner (token {@code username}) and the same
     * sharing WRITE check
     * the delete operations use.
     */
    public CredentialSummaryWithAccess getCredentialSummaryWithAccess(RequestContext ctx, String tokenId)
            throws Exception {
        CredentialSummary credentialSummary = getCredentialSummary(ctx, tokenId);
        if (credentialSummary == null) {
            throw new Exception("User does not have permission to access this resource");
        }
        try {
            boolean isOwner = ctx.getUserId().equals(credentialSummary.username())
                    && ctx.getGatewayId().equals(credentialSummary.gatewayId());
            boolean userHasWriteAccess = isOwner;
            if (!isOwner && SharingHelper.isSharingEnabled()) {
                userHasWriteAccess = SharingHelper.userHasAccess(
                        sharingHandler, ctx.getGatewayId(), ctx.getUserId(), tokenId, ResourcePermissionType.WRITE);
            }
            return CredentialSummaryWithAccess.newBuilder()
                    .setCredentialSummary(credentialSummary)
                    .setAccess(AccessFlags.newBuilder()
                            .setIsOwner(isOwner)
                            .setUserHasWriteAccess(userHasWriteAccess)
                            .build())
                    .build();
        } catch (Exception e) {
            throw new Exception("Error while computing credential access: " + e.getMessage(), e);
        }
    }

    public List<CredentialSummary> getAllCredentialSummaries(RequestContext ctx, SummaryType type)
            throws Exception {
        String gatewayId = ctx.getGatewayId();
        String userName = ctx.getUserId();
        try {
            List<SearchCriteria> filters = new ArrayList<>();
            SearchCriteria searchCriteria = SearchCriteria.newBuilder()
                    .setSearchField(EntitySearchField.ENTITY_TYPE_ID)
                    .setSearchCondition(SearchCondition.EQUAL)
                    .setValue(gatewayId + ":" + ResourceType.CREDENTIAL_TOKEN.name())
                    .build();
            filters.add(searchCriteria);
            List<String> accessibleTokenIds = sharingHandler.searchEntityIds(gatewayId, userName + "@" + gatewayId,
                    filters, 0, -1);
            List<CredentialSummary> credentialSummaries = credentialHandler.getAllCredentialSummaries(type,
                    accessibleTokenIds, gatewayId);
            logger.debug("Airavata successfully retrieved credential summaries of type " + type + " GatewayId: "
                    + gatewayId);
            return credentialSummaries;
        } catch (Exception e) {
            throw new Exception(
                    "Error retrieving credential summaries of type " + type + ". GatewayId: " + gatewayId
                            + " More info : " + e.getMessage(),
                    e);
        }
    }

    public boolean deleteSSHPubKey(RequestContext ctx, String airavataCredStoreToken) throws Exception {
        String gatewayId = ctx.getGatewayId();
        String userName = ctx.getUserId();
        try {
            if (!SharingHelper.userHasAccess(
                    sharingHandler, gatewayId, userName, airavataCredStoreToken, ResourcePermissionType.WRITE)) {
                logger.info("User " + userName + " not allowed to delete (no WRITE permission) credential store token "
                        + airavataCredStoreToken);
                throw new Exception("User " + userName + " does not have permission to delete this resource.");
            }
            logger.debug("Airavata deleted SSH pub key for gateway Id : " + gatewayId + " and with token id : "
                    + airavataCredStoreToken);
            return credentialHandler.deleteSSHCredential(airavataCredStoreToken, gatewayId);
        } catch (Exception e) {
            throw new Exception(
                    "Error occurred while deleting SSH credential. More info : " + e.getMessage(), e);
        }
    }
}
