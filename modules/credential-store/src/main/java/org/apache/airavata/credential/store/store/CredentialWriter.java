package org.apache.airavata.credential.store.store;

import org.apache.airavata.credential.store.credential.Credential;

/**
 * The entity who's writing credentials to DB will use this interface.
 */
public interface CredentialWriter {

    /**
     * Writes given credentials to a persistent storage.
     * @param credential The credentials implementation.
     */
    void writeCredentials(Credential credential) throws CredentialStoreException;


}
