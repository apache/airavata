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

package org.apache.aiaravata.application.catalog.data.impl;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.airavata.appcatalog.cpi.ApplicationDeployment;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ApplicationDeploymentImpl implements ApplicationDeployment {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationDeploymentImpl.class);

    @Override
    public String addApplicationDeployment(ApplicationDeploymentDescription deploymentDescription) throws AppCatalogException {
        return null;
    }

    @Override
    public String getApplicationDeployement(String deploymentId) throws AppCatalogException {
        return null;
    }

    @Override
    public List<ApplicationDeploymentDescription> getApplicationDeployements(Map<String, String> filters) throws AppCatalogException {
        return null;
    }

    @Override
    public boolean isAppDeploymentExists(String deploymentId) throws AppCatalogException {
        return false;
    }

    @Override
    public void removeAppDeployment(String deploymentId) throws AppCatalogException {

    }
}
