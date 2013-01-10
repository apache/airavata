package org.apache.airavata.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class which includes security utilities.
 */
public class SecurityUtil {

    public static final String PASSWORD_HASH_METHOD_PLAINTEXT = "PLAINTEXT";

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    /**
     * Creates a hash of given string with the given hash algorithm.
     * @param stringToDigest The string to digest.
     * @param digestingAlgorithm Hash algorithm.
     * @return The digested string.
     * @throws NoSuchAlgorithmException If given hash algorithm doesnt exists.
     */
    public static String digestString(String stringToDigest, String digestingAlgorithm)
            throws NoSuchAlgorithmException {

        if (digestingAlgorithm == null || digestingAlgorithm.equals(PASSWORD_HASH_METHOD_PLAINTEXT)) {
            return stringToDigest;
        }

        MessageDigest messageDigest = MessageDigest.getInstance(digestingAlgorithm);
        return new String(messageDigest.digest(stringToDigest.getBytes()));
    }

    /**
     * Sets the truststore for application. Useful when communicating over HTTPS.
     * @param trustStoreFilePath Where trust store is located.
     * @param trustStorePassword The trust store password.
     */
    public static void setTrustStoreParameters(String trustStoreFilePath, String trustStorePassword) {

        logger.info("Setting Java trust store to " + trustStoreFilePath);

        System.setProperty("javax.net.ssl.trustStrore", trustStoreFilePath);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

    }
}
