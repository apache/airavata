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

package org.apache.airavata.credential.store.util;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Contains some utility methods.
 */
public class Utility {

    private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

    public static String convertDateToString(Date date) {

        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return df.format(date);
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

}
