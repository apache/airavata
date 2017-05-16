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
package org.apache.airavata.registry.core.impl;

import org.apache.airavata.registry.core.app.catalog.impl.AppCatalogImpl;
import org.apache.airavata.registry.core.replica.catalog.impl.ReplicaCatalogImpl;
import org.apache.airavata.registry.core.experiment.catalog.impl.ExperimentCatalogImpl;
import org.apache.airavata.registry.cpi.*;

public class RegistryImpl implements Registry {
    @Override
    public ExperimentCatalog getExperimentCatalog() throws RegistryException {
        return new ExperimentCatalogImpl();
    }

    @Override
    public ExperimentCatalog getExperimentCatalog(String gatewayId, String username, String password) throws RegistryException {
        return new ExperimentCatalogImpl(gatewayId, username, password);
    }

    @Override
    public AppCatalog getAppCatalog() throws RegistryException {
        return new AppCatalogImpl();
    }

    @Override
    public ReplicaCatalog getReplicaCatalog() throws RegistryException {
        return new ReplicaCatalogImpl();
    }
}
