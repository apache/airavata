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
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.registry.core.entities.expcatalog.GatewayEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GatewayRepository extends ExpCatAbstractRepository<Gateway, GatewayEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(GatewayRepository.class);

    public GatewayRepository() { super(Gateway.class, GatewayEntity.class); }

    protected String saveGatewayData(Gateway gateway) throws RegistryException {
        GatewayEntity gatewayEntity = saveGateway(gateway);
        return gatewayEntity.getGatewayId();
    }

    protected GatewayEntity saveGateway(Gateway gateway) throws RegistryException {
        String gatewayId = gateway.getGatewayId();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        GatewayEntity gatewayEntity = mapper.map(gateway, GatewayEntity.class);

        if (!isGatewayExist(gatewayId)) {
            logger.debug("Checking if the Gateway already exists");
            gatewayEntity.setRequestCreationTime(new Timestamp(System.currentTimeMillis()));
        }

        return execute(entityManager -> entityManager.merge(gatewayEntity));
    }

    public String addGateway(Gateway gateway) throws RegistryException{
        return saveGatewayData(gateway);
    }

    public void updateGateway(String gatewayId, Gateway updatedGateway) throws RegistryException{
        saveGatewayData(updatedGateway);
    }

    public Gateway getGateway(String gatewayId) throws RegistryException{
        return get(gatewayId);
    }

    public List<Gateway> getAllGateways() throws RegistryException {
        List<Gateway> gatewayList = select(QueryConstants.GET_ALL_GATEWAYS, 0);
        return gatewayList;
    }

    public Gateway getDefaultGateway() throws ApplicationSettingsException, RegistryException {
        String defaultGatewayName = ServerSettings.getDefaultUserGateway();
        return getExistingGateway(defaultGatewayName);
    }

    public Gateway getExistingGateway(String gatewayName) throws RegistryException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.Gateway.GATEWAY_NAME, gatewayName);
        List<Gateway> gatewayList = select(QueryConstants.GET_GATEWAY_FROM_GATEWAY_NAME, -1, 0, queryParameters);

        if (gatewayList != null && !gatewayList.isEmpty()) {
            logger.debug("Return the record (there is only one record)");
            return gatewayList.get(0);
        }

        return null;
    }

    public boolean isGatewayExist(String gatewayId) throws RegistryException{
        return isExists(gatewayId);
    }

    public boolean removeGateway(String gatewayId) throws RegistryException{
        return delete(gatewayId);
    }

}