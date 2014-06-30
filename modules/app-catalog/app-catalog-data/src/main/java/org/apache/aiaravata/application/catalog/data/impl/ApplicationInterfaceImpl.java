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
import org.airavata.appcatalog.cpi.ApplicationInterface;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;

import java.util.List;
import java.util.Map;

public class ApplicationInterfaceImpl implements ApplicationInterface {

    @Override
    public String addApplicationModule(ApplicationModule applicationModule) throws AppCatalogException {
        return null;
    }

    @Override
    public String addApplicationInterface(ApplicationInterfaceDescription applicationInterfaceDescription) throws AppCatalogException {
        return null;
    }

    @Override
    public void addApplicationModuleMapping(String moduleId, String interfaceId) throws AppCatalogException {

    }

    @Override
    public ApplicationModule getApplicationModule(String moduleId) throws AppCatalogException {
        return null;
    }

    @Override
    public ApplicationInterfaceDescription getApplicationInterface(String interfaceId) throws AppCatalogException {
        return null;
    }

    @Override
    public List<ApplicationModule> getApplicationModules(Map<String, String> filters) throws AppCatalogException {
        return null;
    }

    @Override
    public List<ApplicationInterfaceDescription> getApplicationInterfaces(Map<String, String> filters) throws AppCatalogException {
        return null;
    }

    @Override
    public void removeApplicationInterface(String interfaceId) throws AppCatalogException {

    }

    @Override
    public void removeApplicationModule(String moduleId) throws AppCatalogException {

    }

    @Override
    public boolean isApplicationInterfaceExists(String interfaceId) throws AppCatalogException {
        return false;
    }

    @Override
    public boolean isApplicationModuleExists(String moduleId) throws AppCatalogException {
        return false;
    }
}
