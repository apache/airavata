package org.apache.airavata.security.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Contains some utility methods related to security.
 */
public class SecurityUtil {

    /**
     * Gets the hash value of a password.
     * @param hashMethod The hash method.
     * @param password Password.
     * @return Hashed password.
     * @throws NoSuchAlgorithmException If an invalid hash method is given.
     */
    public static byte[] getHashedPassword (String hashMethod, String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(hashMethod);
        return md.digest(password.getBytes());
    }
}
