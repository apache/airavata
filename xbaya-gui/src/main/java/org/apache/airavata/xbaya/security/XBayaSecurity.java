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