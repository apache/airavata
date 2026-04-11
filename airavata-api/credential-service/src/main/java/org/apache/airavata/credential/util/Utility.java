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
package org.apache.airavata.credential.util;

import java.io.FileInputStream;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.common.KeyType;
import org.apache.airavata.model.credential.store.proto.SSHCredential;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains some utility methods.
 */
public class Utility {

    protected static Logger log = LoggerFactory.getLogger(Utility.class);

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
    }

    private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

    public static String convertDateToString(Date date) {

        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return df.format(date);
    }

    public static Date convertStringToDate(String date) throws ParseException {

        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return df.parse(date);
    }

    public static String encrypt(String stringToEncrypt) {
        return null;
    }

    public static KeyStore loadKeyStore(String keyStoreFile) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        // get user password and file input stream
        char[] password = getPassword();

        java.io.FileInputStream fis = null;
        try {
            fis = new FileInputStream(keyStoreFile);
            ks.load(fis, password);

            return ks;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    public static char[] getPassword() {
        return new char[0];
    }

    /**
     * Generates an SSH key pair and returns a new SSHCredential proto with the keys set.
     */
    public static SSHCredential generateKeyPair(SSHCredential credential) throws Exception {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();

            // Encode private key in PEM format (unencrypted — credential store handles encryption at rest)
            StringWriter privateKeyWriter = new StringWriter();
            try (JcaPEMWriter pemWriter = new JcaPEMWriter(privateKeyWriter)) {
                pemWriter.writeObject(new JcaMiscPEMGenerator(kp.getPrivate()));
            }
            String privateKeyPem = privateKeyWriter.toString();

            // Encode public key in OpenSSH format
            String publicKeyOpenSSH = encodePublicKeyOpenSSH((RSAPublicKey) kp.getPublic(), "");

            return credential.toBuilder()
                    .setPrivateKey(privateKeyPem)
                    .setPublicKey(publicKeyOpenSSH)
                    .build();
        } catch (Exception e) {
            log.error("Error while creating key pair", e);
            throw new Exception("Error while creating key pair", e);
        }
    }

    /**
     * Encodes an RSA public key in OpenSSH authorized_keys format.
     */
    private static String encodePublicKeyOpenSSH(RSAPublicKey publicKey, String comment) {
        Buffer.PlainBuffer buf = new Buffer.PlainBuffer();
        buf.putString(KeyType.RSA.toString());
        buf.putMPInt(publicKey.getPublicExponent());
        buf.putMPInt(publicKey.getModulus());
        String encoded = KeyType.RSA.toString() + " " + Base64.getEncoder().encodeToString(buf.getCompactData());
        if (comment != null && !comment.isEmpty()) {
            encoded += " " + comment;
        }
        return encoded + "\n";
    }
}
