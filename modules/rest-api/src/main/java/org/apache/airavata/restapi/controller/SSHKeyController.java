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
package org.apache.airavata.restapi.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ssh-keygen")
@Tag(name = "SSH Keys")
public class SSHKeyController {

    @PostMapping
    public Map<String, String> generateSSHKeyPair(@RequestParam(defaultValue = "2048") int keySize) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(keySize);
        KeyPair keyPair = keyGen.generateKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Convert to PEM format
        String privateKeyPEM = formatPrivateKeyPEM(privateKey.getEncoded());
        String publicKeySSH = formatPublicKeySSH(publicKey.getEncoded());

        Map<String, String> result = new HashMap<>();
        result.put("privateKey", privateKeyPEM);
        result.put("publicKey", publicKeySSH);
        result.put("keySize", String.valueOf(keySize));

        return result;
    }

    private String formatPrivateKeyPEM(byte[] keyBytes) {
        String base64 = Base64.getEncoder().encodeToString(keyBytes);
        StringBuilder pem = new StringBuilder("-----BEGIN RSA PRIVATE KEY-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            pem.append(base64.substring(i, Math.min(i + 64, base64.length()))).append("\n");
        }
        pem.append("-----END RSA PRIVATE KEY-----");
        return pem.toString();
    }

    private String formatPublicKeySSH(byte[] keyBytes) {
        // For SSH public key, we use a simplified format
        // In production, use proper ASN.1 parsing
        String base64 = Base64.getEncoder().encodeToString(keyBytes);
        return "ssh-rsa " + base64 + " generated@airavata";
    }
}
