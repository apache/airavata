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
package org.apache.airavata.registry.core.experiment.catalog.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.registry.core.app.catalog.impl.AppCatalogImpl;
import org.apache.airavata.registry.core.replica.catalog.impl.ReplicaCatalogImpl;
import org.apache.airavata.registry.core.impl.RegistryImpl;
import org.apache.airavata.registry.cpi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryFactory {
    private static ExperimentCatalog experimentCatalog;
    private static AppCatalog appCatalog;
    private static ReplicaCatalog replicaCatalog;
    private static Registry registry;
    private static Logger logger = LoggerFactory.getLogger(RegistryFactory.class);

    public static Registry getRegistry() throws RegistryException {
        try {
            if (registry == null) {
                registry = new RegistryImpl();
            }
        } catch (Exception e) {
            logger.error("Unable to create registry instance", e);
            throw new RegistryException(e);
        }
        return registry;
    }

    public static ExperimentCatalog getExperimentCatalog(String gateway, String username, String password) throws RegistryException {
        try {
            if (experimentCatalog == null) {
                experimentCatalog = new ExperimentCatalogImpl(gateway, username, password);
            }
        } catch (RegistryException e) {
            logger.error("Unable to create experiment catalog instance", e);
            throw new RegistryException(e);
        }
        return experimentCatalog;
    }

    public static ExperimentCatalog getExperimentCatalog(String gateway) throws RegistryException {
        try {
            if (experimentCatalog == null) {
                experimentCatalog = new ExperimentCatalogImpl(gateway, ServerSettings.getDefaultUser(), ServerSettings.getDefaultUserPassword());
            }
        } catch (RegistryException e) {
            logger.error("Unable to create experiment catalog instance", e);
            throw new RegistryException(e);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to create experiment catalog instance", e);
            throw new RegistryException(e);
        }
        return experimentCatalog;
    }

    public static ExperimentCatalog getDefaultExpCatalog() throws RegistryException {
        try {
            if (experimentCatalog == null) {
                experimentCatalog = new ExperimentCatalogImpl();
            }
        } catch (RegistryException e) {
            logger.error("Unable to create experiment catalog instance", e);
            throw new RegistryException(e);
        }
        return experimentCatalog;
    }

    public static AppCatalog getAppCatalog() throws AppCatalogException {
        try {
            if (appCatalog == null) {
                appCatalog = new AppCatalogImpl();
            }
        } catch (Exception e) {
            logger.error("Unable to create app catalog instance", e);
            throw new AppCatalogException(e);
        }
        return appCatalog;
    }

    public static ReplicaCatalog getReplicaCatalog() throws ReplicaCatalogException {
        try {
            if (replicaCatalog == null) {
                replicaCatalog = new ReplicaCatalogImpl();
            }
        } catch (Exception e) {
            logger.error("Unable to create data catalog instance", e);
            throw new ReplicaCatalogException(e);
        }
        return replicaCatalog;
    }

}
