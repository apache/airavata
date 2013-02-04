package org.apache.airavata.credential.store;

/**
 * An exception class for credential store.
 */
public class CredentialStoreException extends Exception {

    public CredentialStoreException() {
        super();
    }

    public CredentialStoreException(String s) {
        super(s);
    }

    public CredentialStoreException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
