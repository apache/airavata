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

import org.apache.airavata.config.ServerProperties;
import org.springframework.stereotype.Component;

/**
 * Provides keystore password callbacks for encryption/decryption operations.
 */
@Component
public class KeyStorePasswordCallback {

    private final ServerProperties properties;

    public KeyStorePasswordCallback(ServerProperties properties) {
        this.properties = properties;
    }

    public char[] getStorePassword() {
        String password = getVaultKeystorePassword();
        return password.toCharArray();
    }

    public char[] getSecretKeyPassPhrase(String keyAlias) {
        String password = getVaultKeystorePassword();
        return password.toCharArray();
    }

    private String getVaultKeystorePassword() {
        // Handle null at any level of the nested records
        if (properties == null || properties.security() == null) {
            return "airavata"; // Default for tests
        }
        var vault = properties.security().vault();
        if (vault == null || vault.keystore() == null || vault.keystore().password() == null) {
            return "airavata"; // Default for tests
        }
        return vault.keystore().password();
    }
}
