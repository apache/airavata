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
import org.apache.airavata.security.AiravataSecurityException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrustStoreManager {
    private final static Logger logger = LoggerFactory.getLogger(TrustStoreManager.class);
    public SSLContext initializeTrustStoreManager(String trustStorePath, String trustStorePassword)
            throws AiravataSecurityException {
        try {
            // load and initialize the trust store

            File trustStoreFile = new File(trustStorePath);
            InputStream is;
            if (trustStoreFile.exists()) {
                logger.debug("Loading trust store file from path " + trustStorePath);
                is = new FileInputStream(trustStorePath);
            } else {
                logger.debug("Trying to load trust store file form class path " + trustStorePath);
                is = SecurityUtil.class.getClassLoader().getResourceAsStream(trustStorePath);
                if (is != null) {
                    logger.debug("Trust store file was loaded form class path " + trustStorePath);
                }
            }

            if (is == null) {
                throw new AiravataSecurityException("Could not find a trust store file in path " + trustStorePath);
            }

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

            char[] trustPassword = trustStorePassword.toCharArray();

            trustStore.load(is, trustPassword);

            // initialize a trust manager factory
            TrustManagerFactory trustFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustFactory.init(trustStore);

            // get the trust managers from the factory
            TrustManager[] trustManagers = trustFactory.getTrustManagers();

            // initialize an ssl context to use these managers and set as default
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, null);
            SSLContext.setDefault(sslContext);
            return sslContext;
        } catch (CertificateException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in initializing the trust store.");
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in initializing the trust store.");
        } catch (KeyStoreException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in initializing the trust store.");
        } catch (KeyManagementException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in initializing the trust store.");
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in initializing the trust store.");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in initializing the trust store.");
        }
    }
}
