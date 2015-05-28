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

package org.apache.airavata.persistance.registry.mongo.repository;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.persistance.registry.mongo.dao.GatewayDao;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GatewayRepository {

    private final static Logger logger = LoggerFactory.getLogger(GatewayRepository.class);

    private GatewayDao gatewayDao;

    public GatewayRepository(){
        this.gatewayDao = new GatewayDao();
    }

    public Gateway getDefaultGateway () throws ApplicationSettingsException, RegistryException {
        return gatewayDao.getGatewayByName(ServerSettings.getDefaultUserGateway());
    }

    public Gateway getExistingGateway (String gatewayName) throws RegistryException {
        return gatewayDao.getGatewayByName(gatewayName);
    }

    public String addGateway(Gateway gateway) throws RegistryException{
        try {
            gatewayDao.createGateway(gateway);
            return gateway.getGatewayId();
        }catch (RegistryException e){
            logger.error("Error while saving gateway to registry", e);
            throw new RegistryException(e);
        }
    }

    public void updateGateway (String gatewayId, Gateway updatedGateway) throws RegistryException{
        try {
            updatedGateway.setGatewayId(gatewayId);
            gatewayDao.updateGateway(updatedGateway);
        }catch (RegistryException e){
            logger.error("Error while updating gateway to registry", e);
            throw new RegistryException(e);
        }
    }

    public Gateway getGateway (String gatewayId) throws RegistryException{
        try {
            return gatewayDao.getGateway(gatewayId);
        }catch (RegistryException e){
            logger.error("Error while getting gateway", e);
            throw new RegistryException(e);
        }
    }

    public boolean isGatewayExists (String gatewayId) throws RegistryException{
        try {
            return gatewayDao.getGateway(gatewayId) != null;
        }catch (RegistryException e){
            logger.error("Error while getting gateway", e);
            throw new RegistryException(e);
        }
    }

    public boolean isGatewayExist (String gatewayId) throws RegistryException{
        try {
            return gatewayDao.getGateway(gatewayId) != null;
        }catch (RegistryException e){
            logger.error("Error while checking gateway exists", e);
            throw new RegistryException(e);
        }
    }

    public boolean removeGateway (String gatewayId) throws RegistryException{
        try {
            Gateway gateway = new Gateway();
            gateway.setGatewayId(gatewayId);
            gatewayDao.deleteGateway(gateway);
            return true;
        }catch (Exception e){
            logger.error("Error while removing the gateway", e);
            throw new RegistryException(e);
        }
    }

    public List<Gateway> getAllGateways () throws RegistryException {
        try {
            return gatewayDao.getAllGateways();
        }catch (Exception e){
            logger.error("Error while getting all the gateways", e);
            throw new RegistryException(e);
        }
    }

}
