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
package org.apache.airavata.credential.store.util;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Contains some utility methods.
 */
public class Utility {

    protected static Logger log = LoggerFactory.getLogger(Utility.class);

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

    public static org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential generateKeyPair(SSHCredential credential) throws Exception{
        JSch jsch=new JSch();
        try{
            KeyPair kpair=KeyPair.genKeyPair(jsch, KeyPair.RSA, 2048);
            File file = File.createTempFile("id_rsa", "");
            String fileName = file.getAbsolutePath();

            kpair.writePrivateKey(fileName,credential.getPassphrase().getBytes());
            kpair.writePublicKey(fileName + ".pub"  , "");
            kpair.dispose();
            byte[] priKey = FileUtils.readFileToByteArray(new File(fileName));

            byte[] pubKey = FileUtils.readFileToByteArray(new File(fileName + ".pub"));
            credential.setPrivateKey(priKey);
            credential.setPublicKey(pubKey);
            return credential;
        }
        catch(Exception e){
            log.error("Error while creating key pair", e);
            throw new Exception("Error while creating key pair", e);
        }
    }

}
