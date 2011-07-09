/*
 * Copyright (c) 2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: SecurityUtil.java,v 1.3 2008/04/01 21:44:38 echintha Exp $
 */
package org.apache.airavata.xbaya.security;

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

/**
 * @author Satoshi Shirasuna
 */
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

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2007 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */
