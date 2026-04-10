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
package org.apache.airavata.iam.repository;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.exception.ApplicationSettingsException;
import org.apache.airavata.iam.mapper.GatewayEntityMapper;
import org.apache.airavata.iam.model.GatewayEntity;
import org.apache.airavata.interfaces.GatewayExistenceProvider;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.GatewayApprovalStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GatewayRepository extends AbstractRepository<Gateway, GatewayEntity, String>
        implements GatewayExistenceProvider {
    private static final Logger logger = LoggerFactory.getLogger(GatewayRepository.class);

    public GatewayRepository() {
        super(Gateway.class, GatewayEntity.class);
    }

    @Override
    protected Gateway toModel(GatewayEntity entity) {
        return GatewayEntityMapper.INSTANCE.gatewayToModel(entity);
    }

    @Override
    protected GatewayEntity toEntity(Gateway model) {
        return GatewayEntityMapper.INSTANCE.gatewayToEntity(model);
    }

    protected String saveGatewayData(Gateway gateway) throws RegistryException {
        GatewayEntity gatewayEntity = saveGateway(gateway);
        return gatewayEntity.getGatewayId();
    }

    protected GatewayEntity saveGateway(Gateway gateway) throws RegistryException {
        String gatewayId = gateway.getGatewayId();
        GatewayEntity gatewayEntity = GatewayEntityMapper.INSTANCE.gatewayToEntity(gateway);

        if (!isGatewayExist(gatewayId)) {
            logger.debug("Checking if the Gateway already exists");
            gatewayEntity.setRequestCreationTime(new Timestamp(System.currentTimeMillis()));
        }

        return execute(entityManager -> entityManager.merge(gatewayEntity));
    }

    public String addGateway(Gateway gateway) throws RegistryException {
        return saveGatewayData(gateway);
    }

    public void updateGateway(String gatewayId, Gateway updatedGateway) throws RegistryException {
        saveGatewayData(updatedGateway);
    }

    public Gateway getGateway(String gatewayId) throws RegistryException {
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

    public boolean isGatewayExist(String gatewayId) throws RegistryException {
        return isExists(gatewayId);
    }

    public boolean removeGateway(String gatewayId) throws RegistryException {
        return delete(gatewayId);
    }

    public Gateway getGatewayByInternalId(String airavataInternalGatewayId) throws RegistryException {
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put(DBConstants.Gateway.AIRAVATA_INTERNAL_GATEWAY_ID, airavataInternalGatewayId);
        List<Gateway> gatewayList = select(QueryConstants.FIND_GATEWAY_BY_INTERNAL_ID, 1, 0, queryParam);
        if (!gatewayList.isEmpty()) {
            return gatewayList.get(0);
        }
        return null;
    }

    public List<Gateway> getAllGatewaysForUser(String requesterUsername) throws RegistryException {
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put(DBConstants.Gateway.REQUESTER_USERNAME, requesterUsername);
        return select(QueryConstants.GET_USER_GATEWAYS, -1, 0, queryParam);
    }

    public Gateway getDuplicateGateway(String gatewayId, String gatewayName, String gatewayURL)
            throws RegistryException {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(DBConstants.Gateway.GATEWAY_ID, gatewayId);
        queryParams.put(DBConstants.Gateway.GATEWAY_NAME, gatewayName);
        queryParams.put(DBConstants.Gateway.GATEWAY_URL, gatewayURL);
        queryParams.put(
                DBConstants.Gateway.GATEWAY_APPROVAL_STATUS,
                Arrays.asList(
                        GatewayApprovalStatus.APPROVED.name(),
                        GatewayApprovalStatus.CREATED.name(),
                        GatewayApprovalStatus.DEPLOYED.name()));
        List<Gateway> gatewayList = select(QueryConstants.FIND_DUPLICATE_GATEWAY, 1, 0, queryParams);
        if (!gatewayList.isEmpty()) {
            return gatewayList.get(0);
        }
        return null;
    }
}
