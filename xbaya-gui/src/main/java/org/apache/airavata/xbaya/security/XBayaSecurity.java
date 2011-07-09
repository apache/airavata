/*
 * Copyright (c) 2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: XBayaSecurity.java,v 1.4 2008/04/01 21:44:38 echintha Exp $
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

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.globus.gsi.ptls.PureTLSContext;

import xsul.invoker.puretls.PuretlsInvoker;
import xsul.wsdl.WsdlResolver;
import xsul.wsif.spi.WSIFProviderManager;
import COM.claymoresystems.sslg.SSLPolicyInt;

/**
 * @author Satoshi Shirasuna
 */
public class XBayaSecurity {

    public static final String OGCE_TRUSTED_CERTIFICATES = "/ogce-teragrid-cas.pem";

    public static final String LEAD_TRUSTED_CERTIFICATES = "/lead-trusted-cas.pem";

    private static final String ANONYMOUS_CERTIFICATE = "/hostcertkey-annonymous.pem";

    private static X509Certificate[] trustedCertificates;

    /**
     * Load CA certificates from a file included in the XBaya jar.
     * 
     * @return The trusted certificates.
     */
    public static X509Certificate[] getTrustedCertificates() {
        if (trustedCertificates != null) {
            return trustedCertificates;
        }

        List<X509Certificate> ogceTrustedCertificates = getTrustedCertificates(OGCE_TRUSTED_CERTIFICATES);
        List<X509Certificate> leadTrustedCertificates = getTrustedCertificates(LEAD_TRUSTED_CERTIFICATES);

        List<X509Certificate> allTrustedCertificates = new ArrayList<X509Certificate>();
        allTrustedCertificates.addAll(ogceTrustedCertificates);
        allTrustedCertificates.addAll(leadTrustedCertificates);

        trustedCertificates = allTrustedCertificates.toArray(new X509Certificate[allTrustedCertificates.size()]);
        return trustedCertificates;
    }

    /**
     * Initializes XSUL invokers with SSL without client authentication.
     */
    public static void init() {
        try {
            X509Certificate[] trustedCerts = getTrustedCertificates();

            // Use the class from globus to set trustedCertificates as an
            // argument.
            PureTLSContext sslContext = new PureTLSContext();
            sslContext.setTrustedCertificates(trustedCerts);

            // Load anonymous certificate to access resource catalog.
            // This is a temporary solution
            InputStream anonymousCertificateStream = XBayaSecurity.class.getResourceAsStream(ANONYMOUS_CERTIFICATE);
            if (anonymousCertificateStream == null) {
                throw new XBayaRuntimeException("Failed to get InputStream to " + ANONYMOUS_CERTIFICATE);
            }

            sslContext.loadEAYKeyFile(anonymousCertificateStream, "");

            // Copied from PuretlsInvoker
            SSLPolicyInt policy = new SSLPolicyInt();
            policy.negotiateTLS(true);
            policy.waitOnClose(true);
            sslContext.setPolicy(policy);

            PuretlsInvoker invoker = new PuretlsInvoker(sslContext);
            WSIFProviderManager.getInstance().addProvider(new xsul.wsif_xsul_soap_gsi.Provider(invoker));
            WsdlResolver.getInstance().setSecureInvoker(invoker);

        } catch (GeneralSecurityException e) {
            throw new XBayaRuntimeException(e);
        } catch (IOException e) {
            throw new XBayaRuntimeException(e);
        }

    }

    private static List<X509Certificate> getTrustedCertificates(String pass) {
        InputStream stream = XBayaSecurity.class.getResourceAsStream(pass);
        if (stream == null) {
            throw new XBayaRuntimeException("Failed to get InputStream to " + pass);
        }
        return SecurityUtil.readTrustedCertificates(stream);
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
