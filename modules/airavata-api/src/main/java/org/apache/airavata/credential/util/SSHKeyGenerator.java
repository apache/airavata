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

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.common.KeyType;
import org.apache.airavata.credential.model.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains some utility methods.
 */
public class SSHKeyGenerator {

    protected static Logger log = LoggerFactory.getLogger(SSHKeyGenerator.class);

    public static SSHCredential generateKeyPair(SSHCredential credential) throws Exception {
        try {
            // Generate RSA key pair using Java's KeyPairGenerator
            var keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            var keyPair = keyGen.generateKeyPair();

            var privateKey = (RSAPrivateKey) keyPair.getPrivate();
            var publicKey = (RSAPublicKey) keyPair.getPublic();

            // Convert to OpenSSH format
            var privateKeyPEM = convertPrivateKeyToOpenSSH(privateKey, credential.getPassphrase());
            var publicKeySSH = convertPublicKeyToSSH(publicKey);

            credential.setPrivateKey(privateKeyPEM);
            credential.setPublicKey(publicKeySSH);
            return credential;
        } catch (Exception e) {
            log.error("Error while creating key pair", e);
            throw new Exception("Error while creating key pair", e);
        }
    }

    /**
     * Convert RSA private key to OpenSSH PEM format using standard Java PKCS8 encoding.
     */
    private static String convertPrivateKeyToOpenSSH(RSAPrivateKey privateKey, String passphrase) throws IOException {
        try {
            // Use Java's standard PKCS8 encoding
            var keySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
            var keyBytes = keySpec.getEncoded();

            // Encode to base64
            var base64Key = Base64.getEncoder().encodeToString(keyBytes);

            // Format as OpenSSH private key (PKCS8 format)
            var pem = new StringBuilder();
            pem.append("-----BEGIN PRIVATE KEY-----\n");
            // Split into 64-character lines
            for (int i = 0; i < base64Key.length(); i += 64) {
                int end = Math.min(i + 64, base64Key.length());
                pem.append(base64Key.substring(i, end)).append("\n");
            }
            pem.append("-----END PRIVATE KEY-----\n");

            return pem.toString();
        } catch (Exception e) {
            throw new IOException("Failed to encode private key", e);
        }
    }

    /**
     * Convert RSA public key to SSH format (ssh-rsa ...) using SSHJ Buffer.
     */
    private static String convertPublicKeyToSSH(RSAPublicKey publicKey) throws IOException {
        try {
            // Use SSHJ's Buffer to encode public key in SSH format
            var buf = new Buffer.PlainBuffer();
            buf.putString(KeyType.RSA.toString());
            // SSH RSA key format (RFC 4253): exponent first, then modulus
            putMPInt(buf, publicKey.getPublicExponent());
            putMPInt(buf, publicKey.getModulus());

            var keyBytes = buf.getCompactData();
            var base64Key = Base64.getEncoder().encodeToString(keyBytes);

            return "ssh-rsa " + base64Key;
        } catch (Exception e) {
            throw new IOException("Failed to encode public key", e);
        }
    }

    /**
     * Helper method to encode BigInteger as SSH mpint format (RFC 4251).
     * Writes 4-byte length followed by the BigInteger's byte array.
     */
    private static void putMPInt(Buffer.PlainBuffer buf, BigInteger value) {
        var bytes = value.toByteArray();
        // Write 4-byte length (unsigned int, big-endian) using putUInt32
        buf.putUInt32(bytes.length);
        // Write the bytes using putRawBytes
        buf.putRawBytes(bytes);
    }
}
