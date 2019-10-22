/**
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
 */
package org.apache.airavata.sharing.registry.db.repositories;

import org.apache.airavata.sharing.registry.db.entities.EntityEntity;
import org.apache.airavata.sharing.registry.db.entities.EntityPK;
import org.apache.airavata.sharing.registry.db.utils.DBConstants;
import org.apache.airavata.sharing.registry.db.utils.SharingRegistryJDBCConfig;
import org.apache.airavata.sharing.registry.models.*;

import java.util.*;

public class EntityRepository extends AbstractRepository<Entity, EntityEntity, EntityPK> {

    public EntityRepository() {
        super(Entity.class, EntityEntity.class);
    }

    public List<Entity> getChildEntities(String domainId, String parentId) throws SharingRegistryException {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(DBConstants.EntityTable.DOMAIN_ID, domainId);
        filters.put(DBConstants.EntityTable.PARENT_ENTITY_ID, parentId);
        return select(filters, 0, -1);
    }

    //TODO Replace with prepared statements
    public List<Entity> searchEntities(String domainId, List<String> groupIds, List<SearchCriteria> filters,
                                       int offset, int limit) throws SharingRegistryException {
        String groupIdString = "'";
        for(String groupId : groupIds)
            groupIdString += groupId + "','";
        groupIdString = groupIdString.substring(0, groupIdString.length()-2);

        String query = "SELECT ENTITY.* FROM ENTITY WHERE ENTITY.ENTITY_ID IN (SELECT DISTINCT E.ENTITY_ID FROM ENTITY AS E INNER JOIN SHARING AS S ON (E.ENTITY_ID=S.ENTITY_ID AND E.DOMAIN_ID=S.DOMAIN_ID) WHERE " +
                "E.DOMAIN_ID = '" + domainId + "' AND " + "S.GROUP_ID IN(" + groupIdString + ") AND ";

        for(SearchCriteria searchCriteria : filters){
            if(searchCriteria.getSearchField().equals(EntitySearchField.NAME)){
                if (searchCriteria.getSearchCondition() != null && searchCriteria.getSearchCondition().equals(SearchCondition.NOT)) {
                    query += "E.NAME != '" + searchCriteria.getValue() + "' AND ";
                } else {
                    query += "E.NAME LIKE '%" + searchCriteria.getValue() + "%' AND ";
                }
            }else if(searchCriteria.getSearchField().equals(EntitySearchField.DESCRIPTION)){
                query += "E.DESCRIPTION LIKE '%" + searchCriteria.getValue() + "%' AND ";
            }else if(searchCriteria.getSearchField().equals(EntitySearchField.PERMISSION_TYPE_ID)){
                if (searchCriteria.getSearchCondition() != null && searchCriteria.getSearchCondition().equals(SearchCondition.NOT)) {
                    query += "S.PERMISSION_TYPE_ID != '" + searchCriteria.getValue() + "' AND ";
                } else {
                    query += "S.PERMISSION_TYPE_ID IN ('" + searchCriteria.getValue() + "', '"
                            + (new PermissionTypeRepository()).getOwnerPermissionTypeIdForDomain(domainId) + "') AND ";
                }
            }else if(searchCriteria.getSearchField().equals(EntitySearchField.FULL_TEXT)){
                if (new SharingRegistryJDBCConfig().getDriver().contains("derby")) {
                    query += "E.FULL_TEXT LIKE '%" + searchCriteria.getValue() + "%' AND ";
                } else {
                    // FULL TEXT Search with Query Expansion
                    String queryTerms = "";
                    for (String word : searchCriteria.getValue().trim().replaceAll(" +", " ").split(" ")) {
                        queryTerms += queryTerms + " +" + word;
                    }
                    queryTerms = queryTerms.trim();
                    query += "MATCH(E.FULL_TEXT) AGAINST ('" + queryTerms + "' IN BOOLEAN MODE) AND ";
                }
            }else if(searchCriteria.getSearchField().equals(EntitySearchField.PARRENT_ENTITY_ID)){
                if (searchCriteria.getSearchCondition() != null && searchCriteria.getSearchCondition().equals(SearchCondition.NOT)) {
                    query += "E.PARENT_ENTITY_ID != '" + searchCriteria.getValue() + "' AND ";
                } else {
                    query += "E.PARENT_ENTITY_ID = '" + searchCriteria.getValue() + "' AND ";
                }
            }else if(searchCriteria.getSearchField().equals(EntitySearchField.OWNER_ID)){
                if (searchCriteria.getSearchCondition() != null && searchCriteria.getSearchCondition().equals(SearchCondition.NOT)) {
                    query += "E.OWNER_ID != '" + searchCriteria.getValue() + "' AND ";
                } else {
                    query += "E.OWNER_ID = '" + searchCriteria.getValue() + "' AND ";
                }
            } else if (searchCriteria.getSearchField().equals(EntitySearchField.ENTITY_TYPE_ID)) {
                if (searchCriteria.getSearchCondition() != null && searchCriteria.getSearchCondition().equals(SearchCondition.NOT)) {
                    query += "E.ENTITY_TYPE_ID != '" + searchCriteria.getValue() + "' AND ";
                } else {
                    query += "E.ENTITY_TYPE_ID = '" + searchCriteria.getValue() + "' AND ";
                }
            }else if(searchCriteria.getSearchField().equals(EntitySearchField.CREATED_TIME)){
                if(searchCriteria.getSearchCondition().equals(SearchCondition.GTE)){
                    query += "E.CREATED_TIME >= " + Long.parseLong(searchCriteria.getValue().trim()) + " AND ";
                }else{
                    query += "E.CREATED_TIME <= " + Long.parseLong(searchCriteria.getValue().trim()) + " AND ";
                }
            }else if(searchCriteria.getSearchField().equals(EntitySearchField.UPDATED_TIME)){
                if(searchCriteria.getSearchCondition().equals(SearchCondition.GTE)){
                    query += "E.UPDATED_TIME >= " + Long.parseLong(searchCriteria.getValue().trim()) + " AND ";
                }else{
                    query += "E.UPDATED_TIME <= " + Long.parseLong(searchCriteria.getValue().trim()) + " AND ";
                }
            } else if (searchCriteria.getSearchField().equals(EntitySearchField.SHARED_COUNT)) {
                if (searchCriteria.getSearchCondition().equals(SearchCondition.GTE)) {
                    query += "E.SHARED_COUNT >= " + Integer.parseInt(searchCriteria.getValue().trim()) + " AND ";
                } else {
                    query += "E.SHARED_COUNT <= " + Integer.parseInt(searchCriteria.getValue().trim()) + " AND ";
                }
            }
        }

        query = query.substring(0, query.length() - 5);
        query += ") ORDER BY ENTITY.CREATED_TIME DESC";

        final String nativeQuery = query;
        int newLimit = limit < 0 ? DBConstants.SELECT_MAX_ROWS: limit;

        List<Object[]> temp = execute(entityManager -> entityManager.createNativeQuery(nativeQuery).setFirstResult(offset)
                .setMaxResults(newLimit).getResultList());
        List<Entity> resultSet = new ArrayList<>();

        HashMap<String, Object> keys = new HashMap<>();

        temp.stream().forEach(rs->{
            Entity entity = new Entity();
            entity.setEntityId((String)(rs[0]));
            entity.setDomainId((String) (rs[1]));
            entity.setEntityTypeId((String) (rs[2]));
            entity.setOwnerId((String) (rs[3]));
            entity.setParentEntityId((String) (rs[4]));
            entity.setName((String) (rs[5]));
            entity.setDescription((String)(rs[6]));
            entity.setBinaryData((byte[]) (rs[7]));
            entity.setFullText((String) (rs[8]));
            entity.setSharedCount((long) rs[9]);
            entity.setOriginalEntityCreationTime((long) (rs[10]));
            entity.setCreatedTime((long) (rs[11]));
            entity.setUpdatedTime((long) (rs[12]));

            //Removing duplicates. Another option is to change the query to remove duplicates.
            if (!keys.containsKey(entity + domainId + "," + entity.getEntityId())) {
                resultSet.add(entity);
                keys.put(entity + domainId + "," + entity.getEntityId(), null);
            }
        });

        return resultSet;
    }

    public String getSelectQuery(Map<String, String> filters){
        String query = "SELECT p from " + EntityEntity.class.getSimpleName() + " as p";
        if(filters != null && filters.size() != 0){
            query += " WHERE ";
            for(String k : filters.keySet()){
                query += "p." + k + " = '" + filters.get(k) + "' AND ";
            }
            query = query.substring(0, query.length()-5);
        }

        query += " ORDER BY p."+DBConstants.EntityTable.ORIGINAL_ENTITY_CREATION_TIME+" DESC";

        return query;
    }
}
