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
package org.apache.airavata.credential.utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import java.io.File;
import java.nio.file.Files;
import java.security.KeyStore;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.apache.airavata.credential.model.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains some utility methods.
 */
public class Utility {

    protected static Logger log = LoggerFactory.getLogger(Utility.class);

    private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    public static String convertDateToString(Date date) {
        return FORMATTER.format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    public static Date convertStringToDate(String dateStr) {
        LocalDateTime ldt = LocalDateTime.parse(dateStr, FORMATTER);
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String encrypt(String stringToEncrypt) {
        return null;
    }

    public static KeyStore loadKeyStore(String keyStoreFile) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        char[] password = getPassword();
        try (var fis = Files.newInputStream(java.nio.file.Path.of(keyStoreFile))) {
            ks.load(fis, password);
            return ks;
        }
    }

    public static char[] getPassword() {
        return new char[0];
    }

    public static SSHCredential generateKeyPair(SSHCredential credential) throws Exception {
        JSch jsch = new JSch();
        try {
            KeyPair kpair = KeyPair.genKeyPair(jsch, KeyPair.RSA, 2048);
            File file = File.createTempFile("id_rsa", "");
            String fileName = file.getAbsolutePath();

            kpair.writePrivateKey(fileName, credential.getPassphrase().getBytes());
            kpair.writePublicKey(fileName + ".pub", "");
            kpair.dispose();
            byte[] priKey = Files.readAllBytes(new File(fileName).toPath());
            byte[] pubKey = Files.readAllBytes(new File(fileName + ".pub").toPath());
            credential.setPrivateKey(new String(priKey));
            credential.setPublicKey(new String(pubKey));
            return credential;
        } catch (Exception e) {
            log.error("Error while creating key pair", e);
            throw new Exception("Error while creating key pair", e);
        }
    }
}
