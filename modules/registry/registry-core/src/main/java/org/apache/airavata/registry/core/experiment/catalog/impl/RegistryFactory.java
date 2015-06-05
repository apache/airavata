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

package org.apache.airavata.registry.core.experiment.catalog.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryFactory {
    private static ExperimentCatalog experimentCatalog;
    private static Logger logger = LoggerFactory.getLogger(RegistryFactory.class);

    public static ExperimentCatalog getRegistry(String gateway, String username, String password) throws RegistryException {
        try {
            if (experimentCatalog == null) {
                experimentCatalog = new ExperimentCatalogImpl(gateway, username, password);
            }
        } catch (RegistryException e) {
            logger.error("Unable to create registry instance", e);
            throw new RegistryException(e);
        }
        return experimentCatalog;
    }

    public static ExperimentCatalog getRegistry(String gateway) throws RegistryException {
        try {
            if (experimentCatalog == null) {
                experimentCatalog = new ExperimentCatalogImpl(gateway, ServerSettings.getDefaultUser(), ServerSettings.getDefaultUserPassword());
            }
        } catch (RegistryException e) {
            logger.error("Unable to create registry instance", e);
            throw new RegistryException(e);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to create registry instance", e);
            throw new RegistryException(e);
        }
        return experimentCatalog;
    }

    public static ExperimentCatalog getDefaultRegistry () throws RegistryException {
        try {
            if (experimentCatalog == null) {
                experimentCatalog = new ExperimentCatalogImpl();
            }
        } catch (RegistryException e) {
            logger.error("Unable to create registry instance", e);
            throw new RegistryException(e);
        }
        return experimentCatalog;
    }

    public static ExperimentCatalog getLoggingRegistry() {
        if(experimentCatalog == null) {
            experimentCatalog = new LoggingExperimentCatalogImpl();
        }
        return experimentCatalog;
    }
}
