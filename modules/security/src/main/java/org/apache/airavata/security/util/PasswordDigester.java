package org.apache.airavata.security.util;

import org.apache.airavata.common.utils.SecurityUtil;
import org.apache.airavata.security.UserStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Contains some utility methods related to security.
 */
public class PasswordDigester {

    protected static Logger log = LoggerFactory.getLogger(PasswordDigester.class);

    private String hashMethod;

    /**
     * Creates password digester
     * @param method The particular hash method. E.g :- MD5, SHA1 etc ...
     */
    public PasswordDigester(String method) throws UserStoreException {
        hashMethod = method;
        validateHashAlgorithm();
    }

    /**
     * Gets the hash value of a password.
     * @param password Password.
     * @return Hashed password.
     * @throws UserStoreException If an invalid hash method is given.
     */
    public String getPasswordHashValue(String password) throws UserStoreException {

        if (hashMethod.equals(SecurityUtil.PASSWORD_HASH_METHOD_PLAINTEXT)) {
            return password;
        } else {
            MessageDigest messageDigest = null;
            try {
                messageDigest = MessageDigest.getInstance(hashMethod);
            } catch (NoSuchAlgorithmException e) {
                throw new UserStoreException("Error creating message digest with hash algorithm - "
                        + hashMethod, e);
            }
            return new String(messageDigest.digest(password.getBytes()));
        }

    }

    private void validateHashAlgorithm() throws UserStoreException {

        if (hashMethod == null) {
            log.warn("Password hash method is not configured. Setting default to plaintext.");
            hashMethod = SecurityUtil.PASSWORD_HASH_METHOD_PLAINTEXT;
        } else {

            // Validating configured hash method is correct.
            if (!hashMethod.equals(SecurityUtil.PASSWORD_HASH_METHOD_PLAINTEXT)) {
                try {
                    MessageDigest.getInstance(hashMethod);
                } catch (NoSuchAlgorithmException e) {
                    String msg = "Invalid hash algorithm - " + hashMethod +
                            ". Use Java style way of specifying hash algorithm. E.g :- MD5";
                    log.error(msg);
                    throw new UserStoreException(msg, e);
                }
            }
        }

    }

    public String getHashMethod() {
        return hashMethod;
    }

    public void setHashMethod(String hashMethod) {
        this.hashMethod = hashMethod;
    }
}
