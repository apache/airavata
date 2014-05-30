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
package org.apache.airavata.gfac.server;

import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.cpi.GFac;
import org.apache.airavata.gfac.core.cpi.GFacImpl;
import org.apache.airavata.gfac.cpi.GfacService;
import org.apache.airavata.gfac.cpi.gfac_cpi_serviceConstants;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.api.AiravataRegistryFactory;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.exception.RegException;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GfacServerHandler implements GfacService.Iface {
    private final static Logger logger = LoggerFactory.getLogger(GfacServerHandler.class);

    private Registry registry;

    private String registryURL;
    private String gatewayName;
    private String airavataUserName;

    public GfacServerHandler() {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            setGatewayProperties();
        }catch (Exception e){
           logger.error("Error initialising GFAC",e);
        }
    }

    public String getGFACServiceVersion() throws TException {
        return gfac_cpi_serviceConstants.GFAC_CPI_VERSION;
    }

    public boolean submitJob(String experimentId, String taskId) throws TException {
        GFac gfac = getGfac();
        try {
            return gfac.submitJob(experimentId, taskId);
        } catch (GFacException e) {
            throw new TException("Error launching the experiment : " + e.getMessage(), e);
        }
    }

    public boolean cancelJob(String experimentId, String taskId) throws TException {
        throw new TException("Operation not supported");
    }


    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public String getRegistryURL() {
        return registryURL;
    }

    public void setRegistryURL(String registryURL) {
        this.registryURL = registryURL;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getAiravataUserName() {
        return airavataUserName;
    }

    public void setAiravataUserName(String airavataUserName) {
        this.airavataUserName = airavataUserName;
    }
    protected void setGatewayProperties() throws ApplicationSettingsException {
         setAiravataUserName(ServerSettings.getProperties().getProperty("system.user"));
         setGatewayName(ServerSettings.getProperties().getProperty("system.gateway"));
         setRegistryURL(ServerSettings.getProperties().getProperty("airavata.server.url"));
     }

    private GFac getGfac()throws TException{
        try {
            return new GFacImpl(registry, null,
                                AiravataRegistryFactory.getRegistry(new Gateway(getGatewayName()),
                                        new AiravataUser(getAiravataUserName())));
        } catch (RegException e) {
            throw new TException("Error initializing gfac instance",e);
        } catch (AiravataConfigurationException e) {
            throw new TException("Error initializing gfac instance",e);
        }
    }
}
