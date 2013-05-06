package org.apache.airavata.credential.store.store;

import org.apache.airavata.credential.store.credential.AuditInfo;

/**
 * This interface provides an API for Credential Store.
 * Provides methods to manipulate credential store data.
 */
public interface CredentialReader {

    /**
     * Gets the admin portal user name who  retrieved given community user for
     * given portal user name.
     * @param gatewayName The gateway name
     * @param tokenId The issued token id.
     * @return The portal user name who requested given community user credentials.
     */
    String getPortalUser(String gatewayName, String tokenId) throws CredentialStoreException;

    /**
     * Gets audit information related to given gateway name and community
     * user name.
     * @param gatewayName The gateway name.
     * @param tokenId The community user name.
     * @return CertificateAuditInfo object.
     */
    AuditInfo getAuditInfo(String gatewayName, String tokenId) throws CredentialStoreException;

    /**
     * Updates the community user contact email address.
     * @param gatewayName The gateway name.
     * @param communityUser The community user name.
     * @param email The new email address.
     */
    void updateCommunityUserEmail(String gatewayName, String communityUser, String email) throws CredentialStoreException;

    /**
     * Will remove credentials for the given gateway id and community user.
     * @param gatewayName The gateway Id
     * @param tokenId The issued token id.
     * @throws CredentialStoreException If an error occurred while retrieving data.
     */
    void removeCredentials(String gatewayName, String tokenId) throws CredentialStoreException;


}
