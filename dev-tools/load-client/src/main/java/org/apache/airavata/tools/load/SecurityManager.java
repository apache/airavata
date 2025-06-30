/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.tools.load;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import javax.net.ssl.*;

public class SecurityManager {

    private String trustStoreName = "airavata.jks";
    private String trustStorePassword = "airavata";

    public void loadCertificate(String host, int port)
            throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException,
                    KeyManagementException, URISyntaxException {

        // It is not secure!
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
            }
        };

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        SSLSocket socket = (SSLSocket) sslContext.getSocketFactory().createSocket(host, port);
        socket.startHandshake();
        SSLSession sslSession = socket.getSession();
        Certificate[] certificates = sslSession.getPeerCertificates();

        FileInputStream is = new FileInputStream(getTrustStorePath());

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(is, trustStorePassword.toCharArray());
        is.close();

        File keystoreFile = new File(getTrustStorePath());

        String certificateAlias = host;
        keystore.setCertificateEntry(certificateAlias, certificates[0]);

        FileOutputStream out = new FileOutputStream(keystoreFile);
        keystore.store(out, trustStorePassword.toCharArray());
        out.close();

        System.out.println("Certificates successfully loaded for " + host + ":" + port);
    }

    public String getTrustStorePath() throws URISyntaxException {
        URL trustStoreUrl = SecurityManager.class.getClassLoader().getResource(trustStoreName);

        String trustStorePath;
        if (trustStoreUrl.toURI().getPath() != null) {
            trustStorePath = trustStoreUrl.toURI().getPath();
        } else {
            trustStorePath = System.getProperty("airavata.home") + "/bin/" + trustStoreName;
        }
        return trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }
}
