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
package org.apache.airavata.factory;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.airavata.catalog.sharing.handler.SharingRegistryServerHandler;
import org.apache.airavata.catalog.sharing.service.cpi.SharingRegistryService;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.handler.CredentialStoreServerHandler;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.handler.RegistryServerHandler;

public class AiravataServiceFactory {

    public static RegistryService.Iface getRegistry() {
        try {
            return new RegistryServerHandler();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create registry object. " + e.getMessage());
        }
    }

    public static CredentialStoreService.Iface getCredentialStore() {
        try {
            return new CredentialStoreServerHandler();
        } catch (ApplicationSettingsException
                | IllegalAccessException
                | ClassNotFoundException
                | InstantiationException
                | SQLException
                | IOException e) {
            throw new RuntimeException("Failed to create credential store object. " + e.getMessage());
        }
    }

    public static SharingRegistryService.Iface getSharingRegistry() {
        try {
            return new SharingRegistryServerHandler();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create sharing registry object. " + e.getMessage());
        }
    }
}
