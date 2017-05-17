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

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 9/5/13
 * Time: 6:47 PM
 */

public class PrivateKeyStore {

    private Map<String, PrivateKey> privateKeyMap;

    private static PrivateKeyStore privateKeyStore = null;

    private PrivateKeyStore() {
        privateKeyMap = new HashMap<String, PrivateKey>();
    }

    public static PrivateKeyStore getPrivateKeyStore() {

        if (privateKeyStore == null) {
            privateKeyStore = new PrivateKeyStore();
        }

        return privateKeyStore;
    }

    public synchronized void addKey(String tokenId, PrivateKey privateKey) {

        privateKeyMap.put(tokenId, privateKey);
    }

    public synchronized PrivateKey getKey(String tokenId) {

        PrivateKey privateKey = privateKeyMap.get(tokenId);

        if (privateKey != null) {
            privateKeyMap.remove(tokenId);
        }

        return privateKey;
    }


}
