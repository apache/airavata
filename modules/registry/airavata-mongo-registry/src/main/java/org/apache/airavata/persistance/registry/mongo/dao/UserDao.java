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
import org.apache.airavata.model.workspace.User;
import org.apache.airavata.persistance.registry.mongo.conversion.ModelConversionHelper;
import org.apache.airavata.persistance.registry.mongo.utils.MongoUtil;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private final static Logger logger = LoggerFactory.getLogger(UserDao.class);

    private static final String USERS_COLLECTION_NAME = "users";
    private DBCollection collection;
    private ModelConversionHelper modelConversionHelper;

    private static final String USER_NAME = "user_name";

    public UserDao(){
        collection = MongoUtil.getAiravataRegistry().getCollection(USERS_COLLECTION_NAME);
        modelConversionHelper = new ModelConversionHelper();
        collection.dropIndexes();
        initIndexes();
    }

    /**
     * If indexes are already defined this will simply ignore them
     */
    private void initIndexes(){
        collection.createIndex(new BasicDBObject(USER_NAME, 1), new BasicDBObject("unique", true));
    }

    public List<User> getAllUsers() throws RegistryException{
        List<User> userList = new ArrayList();
        DBCursor cursor = collection.find();
        for(DBObject document: cursor){
            try {
                userList.add((User) modelConversionHelper.deserializeObject(
                        User.class, document.toString()));
            } catch (IOException e) {
                throw new RegistryException(e);
            }
        }
        return userList;
    }

    public void createUser(User user) throws RegistryException{
        try {
            WriteResult result = collection.insert((DBObject) JSON.parse(
                    modelConversionHelper.serializeObject(user)));
            logger.debug("No of inserted results "+ result.getN());
        } catch (JsonProcessingException e) {
            throw new RegistryException(e);
        }
    }

    /**
     * The following operation replaces the document with item equal to
     * the given project id. The newly replaced document will only
     * contain the the _id field and the fields in the replacement document.
     * @param user
     * @throws org.apache.airavata.registry.cpi.RegistryException
     */
    public void updateUser(User user) throws RegistryException{
        try {
            DBObject query = BasicDBObjectBuilder.start().add(
                    USER_NAME, user.getUserName()).get();
            WriteResult result = collection.update(query, (DBObject) JSON.parse(
                    modelConversionHelper.serializeObject(user)));
            logger.debug("No of updated results "+ result.getN());
        } catch (JsonProcessingException e) {
            throw new RegistryException(e);
        }
    }

    public void deleteUser(User user) throws RegistryException{
        DBObject query = BasicDBObjectBuilder.start().add(
                USER_NAME, user.getUserName()).get();
        WriteResult result = collection.remove(query);
        logger.debug("No of removed users " + result.getN());
    }

    public User getUser(String username) throws RegistryException{
        try {
            DBObject criteria = new BasicDBObject(USER_NAME, username);
            DBObject doc = collection.findOne(criteria);
            if(doc != null){
                String json = doc.toString();
                return (User)modelConversionHelper.deserializeObject(
                        User.class, json);
            }
        } catch (IOException e) {
            throw new RegistryException(e);
        }
        return null;
    }
}