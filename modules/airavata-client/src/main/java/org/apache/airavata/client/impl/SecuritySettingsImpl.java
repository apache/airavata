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

import org.apache.airavata.client.api.*;
import org.apache.airavata.client.api.exception.AiravataAPIUnimplementedException;

public class SecuritySettingsImpl implements SecuritySettings {
    private AmazonWebServicesSettings amazonWebServicesSettings = new AmazonWebServicesSettingsImpl();
    private CredentialStoreSecuritySettings credentialStoreSecuritySettings = new CredentialStoreSecuritySettingsImpl();

    public AmazonWebServicesSettings getAmazonWSSettings() {
        return amazonWebServicesSettings;
    }

    public CredentialStoreSecuritySettings getCredentialStoreSecuritySettings() throws AiravataAPIUnimplementedException {
        return credentialStoreSecuritySettings;
    }

    public GridMyProxyRepositorySettings getGridMyProxyRepositorySettings() throws AiravataAPIUnimplementedException {
        throw new AiravataAPIUnimplementedException("Customizing security is not supported by the client in this binary!!!");
    }

    public SSHAuthenticationSettings getSSHAuthenticationSettings() throws AiravataAPIUnimplementedException {
        throw new AiravataAPIUnimplementedException("Customizing security is not supported by the client in this binary!!!");
    }
}
