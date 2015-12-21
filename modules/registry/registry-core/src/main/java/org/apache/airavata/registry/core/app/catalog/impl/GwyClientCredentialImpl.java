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
package org.apache.airavata.registry.core.app.catalog.impl;

import org.apache.airavata.model.security.GatewayClientCredential;
import org.apache.airavata.registry.core.app.catalog.resources.AppCatAbstractResource;
import org.apache.airavata.registry.core.app.catalog.resources.AppCatalogResource;
import org.apache.airavata.registry.core.app.catalog.resources.GatewayClientCredentialResource;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.GwyClientCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GwyClientCredentialImpl implements GwyClientCredential {
    private final static Logger logger = LoggerFactory.getLogger(GwyClientCredentialImpl.class);

    @Override
    public Map.Entry<String, String> generateNewGatewayClientCredential(String gatewayId) throws AppCatalogException {
        try {
            GatewayClientCredentialResource gatewayClientCredentialResource = new GatewayClientCredentialResource();
            gatewayClientCredentialResource.setClientKey(UUID.randomUUID().toString());
            gatewayClientCredentialResource.setClientSecret(UUID.randomUUID().toString());
            gatewayClientCredentialResource.setGatewayId(gatewayId);
            gatewayClientCredentialResource.save();

            Map.Entry<String, String> apiCred = new AbstractMap.SimpleEntry<>(gatewayClientCredentialResource.getClientKey(),
                    gatewayClientCredentialResource.getClientSecret());
            return apiCred;
        } catch (AppCatalogException e) {
            logger.error("Error while creating new gateway client credential...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public Map.Entry<String, String>  getGatewayClientCredential(String clientKey) throws AppCatalogException {
        try {
            GatewayClientCredentialResource gatewayClientCredentialResource = new GatewayClientCredentialResource();
            gatewayClientCredentialResource = (GatewayClientCredentialResource)gatewayClientCredentialResource.get(clientKey);
            if(gatewayClientCredentialResource != null) {
                Map.Entry<String, String> apiCred = new AbstractMap.SimpleEntry<>(gatewayClientCredentialResource.getClientKey(),
                        gatewayClientCredentialResource.getClientSecret());
                return apiCred;
            }else{
                return null;
            }
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving gateway client credential...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void removeGatewayClientCredential(String clientKey) throws AppCatalogException {
        try {
            GatewayClientCredentialResource gatewayClientCredentialResource = new GatewayClientCredentialResource();
            gatewayClientCredentialResource.remove(clientKey);
        } catch (AppCatalogException e) {
            logger.error("Error while removing gateway client credential...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public Map<String, String> getAllGatewayClientCredentials(String gatewayId) throws AppCatalogException {
        try {
            GatewayClientCredentialResource gatewayClientCredentialResource = new GatewayClientCredentialResource();
            List<AppCatalogResource> gatewayClientCredentialResources = gatewayClientCredentialResource
                    .get(AppCatAbstractResource.GatewayClientCredentialConstants.GATEWAY_ID, gatewayId);
            Map<String, String> returnMap = new HashMap<>();
            if(gatewayClientCredentialResources != null && !gatewayClientCredentialResources.isEmpty()) {
                gatewayClientCredentialResources.stream().forEach(cred->{
                    GatewayClientCredentialResource gCred = (GatewayClientCredentialResource)cred;
                    returnMap.put(gCred.getClientKey(), gCred.getClientSecret());
                });
                return returnMap;
            }else{
                return null;
            }
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving gateway client credentials...", e);
            throw new AppCatalogException(e);
        }
    }
}