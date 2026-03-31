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
package org.apache.airavata.credential.repository.db;

import java.security.Key;
import java.sql.*;
import org.apache.airavata.common.server.KeyStorePasswordCallback;
import org.apache.airavata.security.util.SecurityUtil;

/**
 * One-time migration: re-encrypts all CREDENTIALS rows from legacy AES/CBC
 * (static zero IV) to AES/GCM (random IV). Run before deploying the GCM-only code.
 *
 * Usage: java MigrateCredentialEncryption <jdbcUrl> <dbUser> <dbPass> <keystorePath> <keyAlias> <keystorePass>
 */
public class MigrateCredentialEncryption {

    public static void main(String[] args) throws Exception {
        if (args.length != 6) {
            System.err.println(
                    "Usage: MigrateCredentialEncryption <jdbcUrl> <dbUser> <dbPass> <keystorePath> <keyAlias> <keystorePass>");
            System.exit(1);
        }

        String jdbcUrl = args[0], dbUser = args[1], dbPass = args[2];
        String keystorePath = args[3], keyAlias = args[4];
        char[] keystorePass = args[5].toCharArray();

        KeyStorePasswordCallback cb = new KeyStorePasswordCallback() {
            public char[] getStorePassword() {
                return keystorePass;
            }

            public char[] getSecretKeyPassPhrase(String alias) {
                return keystorePass;
            }
        };

        Key key = SecurityUtil.getSymmetricKey(keystorePath, keyAlias, cb);

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
            conn.setAutoCommit(false);

            int migrated = 0, skipped = 0;

            try (PreparedStatement select =
                            conn.prepareStatement("SELECT GATEWAY_ID, TOKEN_ID, CREDENTIAL FROM CREDENTIALS");
                    PreparedStatement update = conn.prepareStatement(
                            "UPDATE CREDENTIALS SET CREDENTIAL = ? WHERE GATEWAY_ID = ? AND TOKEN_ID = ?")) {

                ResultSet rs = select.executeQuery();
                while (rs.next()) {
                    String gatewayId = rs.getString("GATEWAY_ID");
                    String tokenId = rs.getString("TOKEN_ID");
                    byte[] blob = rs.getBytes("CREDENTIAL");

                    // Try GCM first — if it works, already migrated
                    try {
                        SecurityUtil.decrypt(blob, key);
                        skipped++;
                        continue;
                    } catch (Exception ignored) {
                        // Not GCM, proceed with migration
                    }

                    // Decrypt with legacy CBC, re-encrypt with GCM
                    byte[] plaintext = SecurityUtil.decryptLegacy(blob, key);
                    byte[] gcmEncrypted = SecurityUtil.encrypt(plaintext, key);

                    update.setBytes(1, gcmEncrypted);
                    update.setString(2, gatewayId);
                    update.setString(3, tokenId);
                    update.addBatch();
                    migrated++;
                }

                if (migrated > 0) {
                    update.executeBatch();
                }
            }

            conn.commit();
            System.out.printf("Migration complete: %d migrated, %d already GCM%n", migrated, skipped);
        }
    }
}
