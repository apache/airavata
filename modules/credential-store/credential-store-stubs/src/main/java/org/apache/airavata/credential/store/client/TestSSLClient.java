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
package org.apache.airavata.credential.store.client;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.model.credential.store.CertificateCredential;
import org.apache.airavata.model.credential.store.CommunityUser;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.commons.codec.binary.Base64;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class TestSSLClient {
    private void invoke() {
//        TTransport transport;
        try {

//            TSSLTransportFactory.TSSLTransportParameters params =
//                    new TSSLTransportFactory.TSSLTransportParameters();
//            String keystorePath = ServerSettings.getCredentialStoreThriftServerKeyStorePath();
//            String keystorePWD = ServerSettings.getCredentialStoreThriftServerKeyStorePassword();
//            params.setTrustStore(keystorePath, keystorePWD);
            final int serverPort = Integer.parseInt(ServerSettings.getCredentialStoreServerPort());
            final String serverHost = ServerSettings.getCredentialStoreServerHost();
            TTransport transport = new TSocket(serverHost, serverPort);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
//            transport = TSSLTransportFactory.getClientSocket(serverHost, serverPort, 10000, params);
//            TProtocol protocol = new TBinaryProtocol(transport);

            CredentialStoreService.Client client = new CredentialStoreService.Client(protocol);
            testSSHCredential(client);
            testCertificateCredential(client);
            transport.close();
        } catch (TTransportException e) {
            e.printStackTrace();
        }catch (ApplicationSettingsException e) {
            e.printStackTrace();
        }
    }

    public static void testSSHCredential (CredentialStoreService.Client client){
        try {
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setUsername("test");
            sshCredential.setGatewayId("testGateway");
            sshCredential.setPassphrase("mypassphrase");
            String token = client.addSSHCredential(sshCredential);
            System.out.println("SSH Token :" + token);
            SSHCredential credential = client.getSSHCredential(token, "testGateway");
            System.out.println("private key : " + credential.getPrivateKey());
            System.out.println("public key : " + credential.getPublicKey());
        }catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public static void testCertificateCredential (CredentialStoreService.Client client){
        try {
            CertificateCredential certificateCredential = new CertificateCredential();
            CommunityUser communityUser = new CommunityUser("testGateway", "test", "test@ddsd");
            certificateCredential.setCommunityUser(communityUser);
            X509Certificate[] x509Certificates = new X509Certificate[1];
            KeyStore ks = KeyStore.getInstance("JKS");
            File keyStoreFile = new File("/Users/smarru/code/airavata-master/modules/configuration/server/src/main/resources/airavata.jks");
            FileInputStream fis = new FileInputStream(keyStoreFile);
            char[] password = "airavata".toCharArray();
            ks.load(fis,password);
            x509Certificates[0] = (X509Certificate) ks.getCertificate("airavata");
            Base64 encoder = new Base64(64);
            String cert_begin = "-----BEGIN CERTIFICATE-----\n";
            String end_cert = "-----END CERTIFICATE-----";
            byte[] derCert = x509Certificates[0].getEncoded();
            String pemCertPre = new String(encoder.encode(derCert));
            String pemCert = cert_begin + pemCertPre + end_cert;
            certificateCredential.setX509Cert(pemCert);
            String token = client.addCertificateCredential(certificateCredential);
            System.out.println("Certificate Token :" + token);
            CertificateCredential credential = client.getCertificateCredential(token, "testGateway");
            System.out.println("certificate : " + credential.getX509Cert());
            System.out.println("gateway name  : " + credential.getCommunityUser().getGatewayName());
        }catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TestSSLClient c = new TestSSLClient();
        c.invoke();

    }
}
