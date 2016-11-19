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
package org.apache.airavata.registry.core.repositories.workflowcatalog;

import org.apache.airavata.model.NodeModel;
import org.apache.airavata.registry.core.entities.workflowcatalog.NodeEntity;
import org.apache.airavata.registry.core.entities.workflowcatalog.NodePK;
import org.apache.airavata.registry.core.repositories.AbstractRepository;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

<<<<<<< HEAD:modules/registry-refactoring/src/main/java/org/apache/airavata/registry/core/repositories/workflowcatalog/NodeRepository.java
public class NodeRepository extends AbstractRepository<NodeModel, NodeEntity, NodePK> {
=======
import java.util.HashMap;
import java.util.List;
import java.util.Map;
>>>>>>> User Profile CPI Server:modules/registry-refactoring/src/main/java/org/apache/airavata/registry/core/repositories/workspacecatalog/UserProfileRepository.java


    private final static Logger logger = LoggerFactory.getLogger(NodeRepository.class);

    public NodeRepository(Class<NodeModel> thriftGenericClass, Class<NodeEntity> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
    }
<<<<<<< HEAD:modules/registry-refactoring/src/main/java/org/apache/airavata/registry/core/repositories/workflowcatalog/NodeRepository.java
}
=======

    public UserProfile getUserProfileByIdAndGateWay(String userId, String gatewayId)   {

        UserProfile userProfile = null;

        Map<String, Object> queryParam = new HashMap<String, Object>();
        queryParam.put(UserProfile._Fields.USER_ID.getFieldName(), userId);
        queryParam.put(UserProfile._Fields.GATEWAY_ID.getFieldName(), gatewayId);
        List<UserProfile> resultList = select(QueryConstants.FIND_USER_PROFILE_BY_USER_ID, 0, 1, queryParam);

        if (resultList != null && resultList.size() > 0)
            userProfile =  resultList.get(0);


        return userProfile;
    }

    public List<UserProfile> getAllUserProfilesInGateway(String gatewayId)  {

        Map<String, Object> queryParam = new HashMap<String, Object>();
        queryParam.put(UserProfile._Fields.GATEWAY_ID.getFieldName(), gatewayId);

        List<UserProfile> resultList = select(QueryConstants.FIND_ALL_USER_PROFILES_BY_GATEWAY_ID, 0, 1, queryParam);

        return  resultList;
    }

    public UserProfile getUserProfileByNameAndGateWay(String name, String gatewayId)   {

        UserProfile userProfile = null;

        Map<String, Object> queryParam = new HashMap<String, Object>();
        queryParam.put(UserProfile._Fields.USER_NAME.getFieldName(), name);
        queryParam.put(UserProfile._Fields.GATEWAY_ID.getFieldName(), gatewayId);
        List<UserProfile> resultList = select(QueryConstants.FIND_USER_PROFILE_BY_USER_NAME, 0, 1, queryParam);

        if (resultList != null && resultList.size() > 0)
            userProfile =  resultList.get(0);


        return userProfile;
    }
}
>>>>>>> User Profile CPI Server:modules/registry-refactoring/src/main/java/org/apache/airavata/registry/core/repositories/workspacecatalog/UserProfileRepository.java
