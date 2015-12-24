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
package org.apache.airavata.file.manager.core.remote.client.gridftp.myproxy;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.globus.gsi.util.CertificateLoadUtil;
import org.globus.util.ClassLoaderUtils;

public class CertificateManager {

    private static X509Certificate[] trustedCertificates;

    /**
     * Load CA certificates from a file included in the XBaya jar.
     *
     * @return The trusted certificates.
     */
    public static X509Certificate[] getTrustedCertificate(String certificate) {
        if (trustedCertificates != null) {
            return trustedCertificates;
        }

        List<X509Certificate> extremeTrustedCertificates = getTrustedCertificates(certificate);

        List<X509Certificate> allTrustedCertificates = new ArrayList<X509Certificate>();
        allTrustedCertificates.addAll(extremeTrustedCertificates);

        trustedCertificates = allTrustedCertificates.toArray(new X509Certificate[allTrustedCertificates.size()]);
        return trustedCertificates;
    }

    private static List<X509Certificate> getTrustedCertificates(String pass) {
        // ClassLoader classLoader = Thread.currentThread().getContextClassLoader(); //**
        // InputStream stream = classLoader.getResourceAsStream(pass); //**
        InputStream stream = ClassLoaderUtils.getResourceAsStream(pass); // **
        if (stream == null) {
            throw new RuntimeException("Failed to get InputStream to " + pass);
        }
        return readTrustedCertificates(stream);
    }

    /**
     * @param stream
     * @return List of X509Certificate
     */
    public static List<X509Certificate> readTrustedCertificates(InputStream stream) {
        ArrayList<X509Certificate> certificates = new ArrayList<X509Certificate>();
        while (true) {
            X509Certificate certificate;
            try {
                certificate = CertificateLoadUtil.loadCertificate(stream);
            } catch (GeneralSecurityException e) {
                String message = "Certificates are invalid";
                throw new RuntimeException(message, e);
            }
            if (certificate == null) {
                break;
            }
            certificates.add(certificate);
        }
        return certificates;
    }

}