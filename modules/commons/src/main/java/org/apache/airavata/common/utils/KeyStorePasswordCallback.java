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
package org.apache.airavata.common.utils;/*
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

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 10/11/13
 * Time: 11:30 AM
 */

/**
 * An interface to get keystore password in a form of a callback.
 */
public interface KeyStorePasswordCallback {

    /**
     * Caller should implement the interface. Should return the password for
     * the keystore. This should return the keystore password. i.e. password used to open the keystore.
     * Instead of the actual file.
     * @return The password to open the keystore.
     */
    char[] getStorePassword() throws RuntimeException;

    /**
     * Caller should implement the interface. Should return the pass phrase for
     * the secret key.
     * Instead of the actual file.
     * @param keyAlias The alias of the key
     * @return The pass phrase for the secret key.
     */
    char[] getSecretKeyPassPhrase(String keyAlias);

}
