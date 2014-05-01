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
package org.apache.airavata.gfac.monitor.util;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

public class X509Helper {

    static {
        // parsing of RSA key fails without this
        java.security.Security.addProvider(new BouncyCastleProvider());
    }



    public static KeyStore keyStoreFromPEM(String proxyFile,
                                           String keyPassPhrase) throws IOException,
            CertificateException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            KeyStoreException {
        return keyStoreFromPEM(proxyFile,proxyFile,keyPassPhrase);
    }

    public static KeyStore keyStoreFromPEM(String certFile,
                                           String keyFile,
                                           String keyPassPhrase) throws IOException,
                                                                        CertificateException,
                                                                        NoSuchAlgorithmException,
                                                                        InvalidKeySpecException,
                                                                        KeyStoreException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate)cf.generateCertificate(new FileInputStream(certFile));
        //System.out.println(cert.toString());

        // this works for proxy files, too, since it skips over the certificate
        BufferedReader reader = new BufferedReader(new FileReader(keyFile));
        String line = null;
        StringBuilder builder = new StringBuilder();
        boolean inKey = false;
        while((line=reader.readLine()) != null) {
            if (line.contains("-----BEGIN RSA PRIVATE KEY-----")) {
                inKey = true;
            }
            if (inKey) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            if (line.contains("-----END RSA PRIVATE KEY-----")) {
                inKey = false;
            }
        }
        String privKeyPEM = builder.toString();
        //System.out.println(privKeyPEM);

        // using BouncyCastle
        PEMReader pemParser = new PEMReader(new StringReader(privKeyPEM));
        Object object = pemParser.readObject();

        PrivateKey privKey = null;
        if(object instanceof KeyPair){
            privKey = ((KeyPair)object).getPrivate();
        }
        // PEMParser from BouncyCastle is good for reading PEM files, but I didn't want to add that dependency
        /*
        // Base64 decode the data
        byte[] encoded = javax.xml.bind.DatatypeConverter.parseBase64Binary(privKeyPEM);

        // PKCS8 decode the encoded RSA private key
        java.security.spec.PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);
        //RSAPrivateKey privKey = (RSAPrivateKey)kf.generatePrivate(keySpec);
        */
        //System.out.println(privKey.toString());

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null,null);

        KeyStore.PrivateKeyEntry entry =
            new KeyStore.PrivateKeyEntry(privKey,
                                         new java.security.cert.Certificate[] {(java.security.cert.Certificate)cert});
        KeyStore.PasswordProtection prot = new KeyStore.PasswordProtection(keyPassPhrase.toCharArray());
        keyStore.setEntry(cert.getSubjectX500Principal().getName(), entry, prot);

        return keyStore;
    }


    public static KeyStore trustKeyStoreFromCertDir() throws IOException,
                                                             KeyStoreException,
                                                             CertificateException,
                                                             NoSuchAlgorithmException, ApplicationSettingsException {
        return trustKeyStoreFromCertDir(ServerSettings.getSetting("trusted.cert.location"));
    }

    public static KeyStore trustKeyStoreFromCertDir(String certDir) throws IOException,
                                                                           KeyStoreException,
                                                                           CertificateException,
                                                                           NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null,null);

        File dir = new File(certDir);
        for(File file : dir.listFiles()) {
            if (!file.isFile()) {
                continue;
            }
            if (!file.getName().endsWith(".0")) {
                continue;
            }

            try {
                //System.out.println("reading file "+file.getName());
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert = (X509Certificate) cf.generateCertificate(new FileInputStream(file));
                //System.out.println(cert.toString());

                KeyStore.TrustedCertificateEntry entry = new KeyStore.TrustedCertificateEntry(cert);

                ks.setEntry(cert.getSubjectX500Principal().getName(), entry, null);
            } catch (KeyStoreException e) {
            } catch (CertificateParsingException e) {
                continue;
            }

        }

        return ks;
    }
}

