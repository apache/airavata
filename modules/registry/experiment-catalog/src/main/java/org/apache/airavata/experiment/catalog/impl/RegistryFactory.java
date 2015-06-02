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

package org.apache.airavata.experiment.catalog.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryFactory {
    private static Registry registry;
    private static Logger logger = LoggerFactory.getLogger(RegistryFactory.class);

    public static Registry getRegistry(String gateway, String username, String password) throws RegistryException {
        try {
            if (registry == null) {
                registry = new RegistryImpl(gateway, username, password);
            }
        } catch (RegistryException e) {
            logger.error("Unable to create registry instance", e);
            throw new RegistryException(e);
        }
        return registry;
    }

    public static Registry getRegistry(String gateway) throws RegistryException {
        try {
            if (registry == null) {
                registry = new RegistryImpl(gateway, ServerSettings.getDefaultUser(), ServerSettings.getDefaultUserPassword());
            }
        } catch (RegistryException e) {
            logger.error("Unable to create registry instance", e);
            throw new RegistryException(e);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to create registry instance", e);
            throw new RegistryException(e);
        }
        return registry;
    }

    public static Registry getDefaultRegistry () throws RegistryException {
        try {
            if (registry == null) {
                registry = new RegistryImpl();
            }
        } catch (RegistryException e) {
            logger.error("Unable to create registry instance", e);
            throw new RegistryException(e);
        }
        return registry;
    }

    public static Registry getLoggingRegistry() {
        if(registry == null) {
            registry = new LoggingRegistryImpl();
        }
        return registry;
    }
}
