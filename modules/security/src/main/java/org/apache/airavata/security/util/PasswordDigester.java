/**
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
 */
package org.apache.airavata.security.util;

import org.apache.airavata.common.utils.SecurityUtil;
import org.apache.airavata.security.UserStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
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
     * 
     * @param method
     *            The particular hash method. E.g :- MD5, SHA1 etc ...
     */
    public PasswordDigester(String method) throws UserStoreException {
        hashMethod = method;
        validateHashAlgorithm();
    }

    /**
     * Gets the hash value of a password.
     * 
     * @param password
     *            Password.
     * @return Hashed password.
     * @throws UserStoreException
     *             If an invalid hash method is given.
     */
    public String getPasswordHashValue(String password) throws UserStoreException {

        if (hashMethod.equals(SecurityUtil.PASSWORD_HASH_METHOD_PLAINTEXT)) {
            return password;
        } else {
            MessageDigest messageDigest = null;
            try {
                messageDigest = MessageDigest.getInstance(hashMethod);
            } catch (NoSuchAlgorithmException e) {
                throw new UserStoreException("Error creating message digest with hash algorithm - " + hashMethod, e);
            }
            try {
                return new String(messageDigest.digest(password.getBytes("UTF-8")));
            } catch (UnsupportedEncodingException e) {
                throw new UserStoreException("Unable to create password digest", e);
            }
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
                    String msg = "Invalid hash algorithm - " + hashMethod
                            + ". Use Java style way of specifying hash algorithm. E.g :- MD5";
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
