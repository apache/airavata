package org.apache.airavata.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class which includes security utilities.
 */
public class SecurityUtil {

    /**
     * Creates a hash of given string with the given hash algorithm.
     * @param stringToDigest The string to digest.
     * @param digestingAlgorithm Hash algorithm.
     * @return The digested string.
     * @throws NoSuchAlgorithmException If given hash algorithm doesnt exists.
     */
    public static String digestString(String stringToDigest, String digestingAlgorithm)
            throws NoSuchAlgorithmException {

        if (digestingAlgorithm == null) {
            return stringToDigest;
        }

        MessageDigest messageDigest = MessageDigest.getInstance(digestingAlgorithm);
        return new String(messageDigest.digest(stringToDigest.getBytes()));
    }
}
