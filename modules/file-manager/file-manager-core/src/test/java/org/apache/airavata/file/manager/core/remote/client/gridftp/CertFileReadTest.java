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
package org.apache.airavata.file.manager.core.remote.client.gridftp;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.globus.gsi.SigningPolicy;
import org.globus.gsi.SigningPolicyParser;
import org.globus.gsi.util.CertificateIOUtil;
import org.globus.util.GlobusResource;
import org.junit.Ignore;
import org.junit.Test;

import javax.security.auth.x500.X500Principal;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;


@Ignore("This test case used to debug JGlobus-102. No need to run this test with other gridftp tests.")
public class CertFileReadTest extends TestCase {

    private static MessageDigest md5;

    private static String CERT_FILE_LOCATION = "/Users/supun/Work/airavata-sandbox/grid-tools/certificates/";

    @Test
    public void testCertFileRead() throws Exception {

        String path1 = CERT_FILE_LOCATION + "ffc3d59b";
        String path2 = CERT_FILE_LOCATION + "e5cc84c2";


        GlobusResource globusResource1 = new GlobusResource(path1 + ".signing_policy");
        GlobusResource globusResource2 = new GlobusResource(path2 + ".signing_policy");

        // ===== Testing globusResource1 - This should pass (cos no DC components) ================ //
        X509Certificate crt1 = readCertificate(path1 + ".0");
        X500Principal policySubjectCert1 = getPrincipal(globusResource1);

        String certHash1 = CertificateIOUtil.nameHash(crt1.getSubjectX500Principal());
        String principalHash1 = CertificateIOUtil.nameHash(policySubjectCert1);

        System.out.println("======== Printing hashes for 1 ================");
        System.out.println(certHash1);
        System.out.println(principalHash1);

        Assert.assertEquals("Certificate hash value does not match with the hash value generated using principal name.",
                certHash1, principalHash1);

        // ===== Testing globusResource1 - This should fail (cos we have DC components) ================ //
        X509Certificate crt2 = readCertificate(path2 + ".0");
        X500Principal policySubjectCert2 = getPrincipal(globusResource2);

        String certHash2 = CertificateIOUtil.nameHash(crt2.getSubjectX500Principal());
        String principalHash2 = CertificateIOUtil.nameHash(policySubjectCert2);

        System.out.println("======== Printing hashes for 2 ================");
        System.out.println(certHash2);
        System.out.println(principalHash2);

        Assert.assertEquals("Certificate hash value does not match with the hash value generated using principal name.",
                certHash2, principalHash2);
    }

    private X500Principal getPrincipal(GlobusResource globusResource) throws Exception{

        SigningPolicyParser parser = new SigningPolicyParser();

        Reader reader = new InputStreamReader(globusResource.getInputStream());

        Map<X500Principal, SigningPolicy> policies = parser.parse(reader);

        return policies.keySet().iterator().next();

    }

    private X509Certificate readCertificate(String certPath) {
        try {
            FileInputStream fr = new FileInputStream(certPath);
            CertificateFactory cf =
                    CertificateFactory.getInstance("X509");
            X509Certificate crt = (X509Certificate)
                    cf.generateCertificate(fr);
            System.out.println("Read certificate:");
            System.out.println("\tCertificate for: " +
                    crt.getSubjectDN());
            System.out.println("\tCertificate issued by: " +
                    crt.getIssuerDN());
            System.out.println("\tCertificate is valid from " +
                    crt.getNotBefore() + " to " + crt.getNotAfter());
            System.out.println("\tCertificate SN# " +
                    crt.getSerialNumber());
            System.out.println("\tGenerated with " +
                    crt.getSigAlgName());

            return crt;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}