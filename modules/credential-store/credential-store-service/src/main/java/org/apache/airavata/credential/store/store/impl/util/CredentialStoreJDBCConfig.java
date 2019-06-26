/*
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

package org.apache.airavata.credential.store.store.impl.util;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.JDBCConfig;
import org.apache.airavata.common.utils.ServerSettings;

public class CredentialStoreJDBCConfig implements JDBCConfig {

    @Override
    public String getURL() {
        try {
            return ServerSettings.getCredentialStoreDBURL();
        } catch (ApplicationSettingsException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDriver() {
        try {
            return ServerSettings.getCredentialStoreDBDriver();
        } catch (ApplicationSettingsException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getUser() {
        try {
            return ServerSettings.getCredentialStoreDBUser();
        } catch (ApplicationSettingsException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPassword() {
        try {
            return ServerSettings.getCredentialStoreDBPassword();
        } catch (ApplicationSettingsException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getValidationQuery() {
        try {
            return ServerSettings.getSetting("credential.store.jdbc.validationQuery");
        } catch (ApplicationSettingsException e) {
            throw new RuntimeException(e);
        }
    }
}
