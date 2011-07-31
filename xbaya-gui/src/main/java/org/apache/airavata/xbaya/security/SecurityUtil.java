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

package org.apache.airavata.xbaya.security;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.globus.gsi.CertUtil;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;

public class SecurityUtil {

    /**
     * @param filepath
     * @return List of X509Certificate
     * @throws FileNotFoundException
     */
    public static X509Certificate[] readTrustedCertificates(String filepath) throws FileNotFoundException {
        FileInputStream stream = new FileInputStream(filepath);
        List<X509Certificate> certificates = readTrustedCertificates(stream);
        return certificates.toArray(new X509Certificate[certificates.size()]);
    }

    /**
     * @param stream
     * @return List of X509Certificate
     */
    public static List<X509Certificate> readTrustedCertificates(InputStream stream) {
        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader bufferedReader = new BufferedReader(reader);

        ArrayList<X509Certificate> certificates = new ArrayList<X509Certificate>();
        while (true) {
            X509Certificate certificate;
            try {
                certificate = CertUtil.readCertificate(bufferedReader);
            } catch (IOException e) {
                String message = "Failed to read certificates";
                throw new XBayaRuntimeException(message, e);
            } catch (GeneralSecurityException e) {
                String message = "Certificates are invalid";
                throw new XBayaRuntimeException(message, e);
            }
            if (certificate == null) {
                break;
            }
            certificates.add(certificate);
        }
        return certificates;
    }

    /**
     * @param url
     * @return True if the connection is secure; false otherwise.
     */
    public static boolean isSecureService(URI url) {
        String scheme = url.getScheme();
        if ("http".equalsIgnoreCase(scheme)) {
            return false;
        } else if ("https".equalsIgnoreCase(scheme)) {
            return true;
        } else {
            throw new XBayaRuntimeException("Protocol, " + scheme + ", is not supported");
        }
    }

    /**
     * @param userName
     *            User name of the my proxy server
     * @param password
     *            Password for the my proxy server
     * @param myproxyServer
     *            eg:portal-dev.leadproject.org
     * @return proxy credential
     */
    public static GSSCredential getGSSCredential(String userName, String password, String myproxyServer) {
        MyProxyClient myProxyClient = new MyProxyClient(myproxyServer, XBayaConstants.DEFAULT_MYPROXY_PORT, userName,
                password, XBayaConstants.DEFAULT_MYPROXY_LIFTTIME);
        try {
            myProxyClient.load();
        } catch (MyProxyException e) {
            throw new XBayaRuntimeException("Failed loading the myproxy", e);
        }
        GSSCredential gssCredential = myProxyClient.getProxy();
        return gssCredential;
    }

}