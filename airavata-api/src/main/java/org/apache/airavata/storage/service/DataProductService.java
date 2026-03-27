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
package org.apache.airavata.storage.service;

import java.util.List;
import org.apache.airavata.execution.handler.RegistryServerHandler;
import org.apache.airavata.execution.service.RequestContext;
import org.apache.airavata.execution.service.ServiceException;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataProductService {

    private static final Logger logger = LoggerFactory.getLogger(DataProductService.class);

    private final RegistryServerHandler registryHandler;

    public DataProductService(RegistryServerHandler registryHandler) {
        this.registryHandler = registryHandler;
    }

    public String registerDataProduct(RequestContext ctx, DataProductModel dataProductModel) throws ServiceException {
        try {
            String result = registryHandler.registerDataProduct(dataProductModel);
            logger.debug("Registered data product {} for gateway {}", result, ctx.getGatewayId());
            return result;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error registering the data product " + dataProductModel.getProductName() + ": " + e.getMessage(),
                    e);
        }
    }

    public DataProductModel getDataProduct(RequestContext ctx, String productUri) throws ServiceException {
        try {
            DataProductModel result = registryHandler.getDataProduct(productUri);
            logger.debug("Retrieved data product {}", productUri);
            return result;
        } catch (Exception e) {
            throw new ServiceException("Error retrieving the data product " + productUri + ": " + e.getMessage(), e);
        }
    }

    public String registerReplicaLocation(RequestContext ctx, DataReplicaLocationModel replicaLocationModel)
            throws ServiceException {
        try {
            String result = registryHandler.registerReplicaLocation(replicaLocationModel);
            logger.debug("Registered replica location {} for gateway {}", result, ctx.getGatewayId());
            return result;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error registering replica " + replicaLocationModel.getReplicaName() + ": " + e.getMessage(), e);
        }
    }

    public DataProductModel getParentDataProduct(RequestContext ctx, String productUri) throws ServiceException {
        try {
            DataProductModel result = registryHandler.getParentDataProduct(productUri);
            logger.debug("Retrieved parent data product for {}", productUri);
            return result;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error retrieving parent data product for " + productUri + ": " + e.getMessage(), e);
        }
    }

    public List<DataProductModel> getChildDataProducts(RequestContext ctx, String productUri) throws ServiceException {
        try {
            List<DataProductModel> result = registryHandler.getChildDataProducts(productUri);
            logger.debug("Retrieved child data products for {}", productUri);
            return result;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error retrieving child data products for " + productUri + ": " + e.getMessage(), e);
        }
    }
}
