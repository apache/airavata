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
package org.apache.airavata.service.profile.tenant.core.repositories;

import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.service.profile.commons.repositories.AbstractRepository;
import org.apache.airavata.service.profile.commons.tenant.entities.GatewayEntity;
import org.apache.airavata.service.profile.commons.utils.QueryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by goshenoy on 3/8/17.
 */
public class TenantProfileRepository extends AbstractRepository<Gateway, GatewayEntity, String> {

    private final static Logger logger = LoggerFactory.getLogger(TenantProfileRepository.class);

    public TenantProfileRepository(Class<Gateway> thriftGenericClass, Class<GatewayEntity> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
    }

    public Gateway getGateway (String gatewayId) throws Exception {
        Gateway gateway = null;
        try {
            Map<String, Object> queryParam = new HashMap<String, Object>();
            queryParam.put(Gateway._Fields.GATEWAY_ID.getFieldName(), gatewayId);
            List<Gateway> gatewayList = select(QueryConstants.FIND_GATEWAY_BY_ID, 1, 0, queryParam);
            if (!gatewayList.isEmpty()) {
                gateway = gatewayList.get(0);
            }
        } catch (Exception ex) {
            logger.error("Error while getting gateway, reason: " + ex.getMessage(), ex);
            throw ex;
        }
        return gateway;
    }

    public List<Gateway> getAllGateways () throws Exception {
        try {
            List<Gateway> gatewayList = select(QueryConstants.GET_ALL_GATEWAYS);
            return gatewayList;
        } catch (Exception e){
            logger.error("Error while getting all the gateways, reason: ", e);
            throw e;
        }
    }
}
