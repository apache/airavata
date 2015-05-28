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
package org.apache.airavata.persistance.registry.mongo.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.persistance.registry.mongo.conversion.ModelConversionHelper;
import org.apache.airavata.persistance.registry.mongo.utils.MongoUtil;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GatewayDao {
    private final static Logger logger = LoggerFactory.getLogger(GatewayDao.class);

    private static final String GATEWAYS_COLLECTION_NAME = "gateways";
    private DBCollection collection;
    private ModelConversionHelper modelConversionHelper;

    private static final String GATEWAY_ID = "gateway_id";
    private static final String GATEWAY_NAME = "gateway_name";
    private static final String DOMAIN = "domain";
    private static final String EMAIL_ADDRESS = "email_address";

    public GatewayDao(){
        collection = MongoUtil.getAiravataRegistry().getCollection(GATEWAYS_COLLECTION_NAME);
        modelConversionHelper = new ModelConversionHelper();
        collection.dropIndexes();
        initIndexes();
    }

    /**
     * If indexes are already defined this will simply ignore them
     */
    private void initIndexes(){
        collection.createIndex(new BasicDBObject(GATEWAY_ID, 1), new BasicDBObject("unique", true));
        collection.createIndex(new BasicDBObject(GATEWAY_NAME, 1), new BasicDBObject("unique", true));
        collection.createIndex(new BasicDBObject(EMAIL_ADDRESS, 1));
        collection.createIndex(new BasicDBObject(DOMAIN, 1));
    }

    public List<Gateway> getAllGateways() throws RegistryException{
        List<Gateway> gatewayList = new ArrayList();
        DBCursor cursor = collection.find();
        for(DBObject document: cursor){
            try {
                gatewayList.add((Gateway) modelConversionHelper.deserializeObject(
                        Gateway.class, document.toString()));
            } catch (IOException e) {
                throw new RegistryException(e);
            }
        }
        return gatewayList;
    }

    public void createGateway(Gateway gateway) throws RegistryException{
        try {
            WriteResult result = collection.insert((DBObject) JSON.parse(
                    modelConversionHelper.serializeObject(gateway)));
            logger.debug("No of inserted results "+ result.getN());
        } catch (JsonProcessingException e) {
            throw new RegistryException(e);
        }
    }

    /**
     * The following operation replaces the document with item equal to
     * the given gateway id. The newly replaced document will only
     * contain the the _id field and the fields in the replacement document.
     * @param gateway
     * @throws org.apache.airavata.registry.cpi.RegistryException
     */
    public void updateGateway(Gateway gateway) throws RegistryException{
        try {
            DBObject query = BasicDBObjectBuilder.start().add(
                    GATEWAY_ID, gateway.getGatewayId()).get();
            WriteResult result = collection.update(query, (DBObject) JSON.parse(
                    modelConversionHelper.serializeObject(gateway)));
            logger.debug("No of updated results "+ result.getN());
        } catch (JsonProcessingException e) {
            throw new RegistryException(e);
        }
    }

    public void deleteGateway(Gateway gateway) throws RegistryException{
        DBObject query = BasicDBObjectBuilder.start().add(
                GATEWAY_ID, gateway.getGatewayId()).get();
        WriteResult result = collection.remove(query);
        logger.debug("No of removed experiments " + result.getN());
    }

    public Gateway getGateway(String gatewayId) throws RegistryException{
        try {
            DBObject criteria = new BasicDBObject(GATEWAY_ID, gatewayId);
            DBObject doc = collection.findOne(criteria);
            if(doc != null){
                String json = doc.toString();
                return (Gateway)modelConversionHelper.deserializeObject(
                        Gateway.class, json);
            }
        } catch (IOException e) {
            throw new RegistryException(e);
        }
        return null;
    }

    public Gateway getGatewayByName(String gatewayName) throws RegistryException{
        try {
            DBObject criteria = new BasicDBObject(GATEWAY_NAME, gatewayName);
            DBObject doc = collection.findOne(criteria);
            if(doc != null){
                String json = doc.toString();
                return (Gateway)modelConversionHelper.deserializeObject(
                        Gateway.class, json);
            }
        } catch (IOException e) {
            throw new RegistryException(e);
        }
        return null;
    }

    public List<Gateway> searchGateways(Map<String, String> filters, int limit, int offset) throws RegistryException{
        List<Gateway> gatewayList = new ArrayList();
        BasicDBObjectBuilder queryBuilder = BasicDBObjectBuilder.start();
        for (String field : filters.keySet()) {
//            if (field.equals(Constants.FieldConstants.ProjectConstants.GATEWAY_NAME)){
//                fil.put(AbstractResource.ProjectConstants.GATEWAY_NAME, filters.get(field));
//            }else if (field.equals(Constants.FieldConstants.ProjectConstants.OWNER)){
//                fil.put(AbstractResource.ProjectConstants.USERNAME, filters.get(field));
//            }else if (field.equals(Constants.FieldConstants.ProjectConstants.DESCRIPTION)){
//                fil.put(AbstractResource.ProjectConstants.DESCRIPTION, filters.get(field));
//            }else if (field.equals(Constants.FieldConstants.ProjectConstants.GATEWAY_ID)){
//                fil.put(AbstractResource.ProjectConstants.GATEWAY_ID, filters.get(field));
//            }
        }

        //handling pagination.
        DBCursor cursor;
        if(limit > 0 && offset >= 0) {
                cursor = collection.find(queryBuilder.get()).skip(offset).limit(limit);
        }else{
                cursor = collection.find(queryBuilder.get());
        }
        for(DBObject document: cursor){
            try {
                gatewayList.add((Gateway) modelConversionHelper.deserializeObject(
                        Gateway.class, document.toString()));
            } catch (IOException e) {
                throw new RegistryException(e);
            }
        }
        return gatewayList;
    }
}