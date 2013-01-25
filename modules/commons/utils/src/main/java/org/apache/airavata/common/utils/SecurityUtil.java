/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

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
     * 
     * @param stringToDigest
     *            The string to digest.
     * @param digestingAlgorithm
     *            Hash algorithm.
     * @return The digested string.
     * @throws NoSuchAlgorithmException
     *             If given hash algorithm doesnt exists.
     */
    public static String digestString(String stringToDigest, String digestingAlgorithm) throws NoSuchAlgorithmException {

        if (digestingAlgorithm == null || digestingAlgorithm.equals(PASSWORD_HASH_METHOD_PLAINTEXT)) {
            return stringToDigest;
        }

        MessageDigest messageDigest = MessageDigest.getInstance(digestingAlgorithm);
        return new String(messageDigest.digest(stringToDigest.getBytes()));
    }

    /**
     * Sets the truststore for application. Useful when communicating over HTTPS.
     * 
     * @param trustStoreFilePath
     *            Where trust store is located.
     * @param trustStorePassword
     *            The trust store password.
     */
    public static void setTrustStoreParameters(String trustStoreFilePath, String trustStorePassword) {

        if (System.getProperty("javax.net.ssl.trustStrore") == null) {
            logger.info("Setting Java trust store to " + trustStoreFilePath);
            System.setProperty("javax.net.ssl.trustStrore", trustStoreFilePath);
        }

        if (System.getProperty("javax.net.ssl.trustStorePassword") == null) {
            System.setProperty("javax.net.ssl.trustStorePassword", trustStoreFilePath);
        }

    }
}
