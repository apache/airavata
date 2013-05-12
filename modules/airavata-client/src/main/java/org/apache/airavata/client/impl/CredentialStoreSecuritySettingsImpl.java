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

package org.apache.airavata.client.impl;

import org.apache.airavata.client.api.CredentialStoreSecuritySettings;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 5/7/13
 * Time: 2:44 PM
 */

/**
 * Implementation of credential store security settings class.
 */
public class CredentialStoreSecuritySettingsImpl implements CredentialStoreSecuritySettings {

    private String tokenId;

    public CredentialStoreSecuritySettingsImpl(String tokenId) {
        this.tokenId = tokenId;
    }

    public CredentialStoreSecuritySettingsImpl() {
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }


    public String getTokenId() {
        return tokenId;
    }

}
