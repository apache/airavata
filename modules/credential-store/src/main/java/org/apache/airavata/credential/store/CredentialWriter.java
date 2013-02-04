package org.apache.airavata.credential.store;

/**
 * The entity who's writing credentials to DB will use this interface.
 */
public interface CredentialWriter {

    /**
     * Writes given credentials to a persistent storage.
     * @param credential The credentials implementation.
     */
    void writeCredentials(Credential credential) throws CredentialStoreException;

    /**
     * Writes community user information.
     * @param communityUser Writes community user information to a persistent storage.
     * @throws CredentialStoreException If an error occurred while writing community user.
     */
    void writeCommunityUser(CommunityUser communityUser) throws CredentialStoreException;
}
