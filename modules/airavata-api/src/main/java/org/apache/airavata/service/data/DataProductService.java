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
package org.apache.airavata.service.data;

import java.util.List;
import org.apache.airavata.common.exception.AiravataErrorType;
import org.apache.airavata.common.exception.AiravataSystemException;
import org.apache.airavata.common.model.DataProductModel;
import org.apache.airavata.common.model.DataReplicaLocationModel;
import org.apache.airavata.registry.exception.RegistryServiceException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * Service for data product and replica management operations.
 */
@Service("dataProductServiceFacade")
@ConditionalOnBean(org.apache.airavata.service.registry.RegistryService.class)
public class DataProductService {
    private static final Logger logger = LoggerFactory.getLogger(DataProductService.class);

    private final RegistryService registryService;

    public DataProductService(RegistryService registryService) {
        this.registryService = registryService;
    }

    private AiravataSystemException airavataSystemException(
            AiravataErrorType errorType, String message, Throwable cause) {
        return org.apache.airavata.common.exception.ExceptionHandlerUtil.wrapAsAiravataException(
                errorType, message, cause);
    }

    public String registerDataProduct(DataProductModel dataProductModel) throws AiravataSystemException {
        try {
            return registryService.registerDataProduct(dataProductModel);
        } catch (RegistryServiceException e) {
            var msg = "Error in registering the data resource" + dataProductModel.getProductName() + ".";
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public DataProductModel getDataProduct(String productUri) throws AiravataSystemException {
        try {
            return registryService.getDataProduct(productUri);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving data product: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel)
            throws AiravataSystemException {
        try {
            return registryService.registerReplicaLocation(replicaLocationModel);
        } catch (RegistryServiceException e) {
            var msg = "Error in retreiving the replica " + replicaLocationModel.getReplicaName() + "." + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public DataProductModel getParentDataProduct(String productUri) throws AiravataSystemException {
        try {
            return registryService.getParentDataProduct(productUri);
        } catch (RegistryServiceException e) {
            var msg = "Error in retreiving the parent data product for " + productUri + "." + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<DataProductModel> getChildDataProducts(String productUri) throws AiravataSystemException {
        try {
            return registryService.getChildDataProducts(productUri);
        } catch (RegistryServiceException e) {
            var msg = "Error in retreiving the child products for " + productUri + "." + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
}
