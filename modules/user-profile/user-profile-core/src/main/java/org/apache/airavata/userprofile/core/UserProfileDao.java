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
package org.apache.airavata.userprofile.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.userprofile.cpi.UserProfileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserProfileDao {
    private final static Logger logger = LoggerFactory.getLogger(UserProfileDao.class);

    private static final String USER_PROFILE_COLLECTION_NAME = "user-profiles";
    private DBCollection collection;
    private ModelConversionHelper modelConversionHelper;

    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "user_name";
    private static final String GATEWAY_ID = "gateway_id";

    public UserProfileDao() throws UserProfileException {
        try {
            collection = MongoUtil.getUserProfileRegistry().getCollection(USER_PROFILE_COLLECTION_NAME);
        } catch (ApplicationSettingsException e) {
            throw new UserProfileException(e);
        }
        modelConversionHelper = new ModelConversionHelper();
        //collection.dropIndexes();
        initIndexes();
    }

    /**
     * If indexes are already defined this will simply ignore them
     */
    private void initIndexes(){
        // UserID is the primary key
        collection.createIndex(new BasicDBObject(USER_ID, 1), new BasicDBObject("unique", true));
        // UserName and GatewayID combination is also makes a unique key
        collection.createIndex(new BasicDBObject(USER_NAME, 1).append(GATEWAY_ID, 1), new BasicDBObject("unique", true));
    }

    public List<UserProfile> getAllUserProfilesInGateway(String gatewayId) throws UserProfileException {
        List<UserProfile> userList = new ArrayList();
        DBCursor cursor = collection.find();
        for(DBObject document: cursor){
            try {
                userList.add((UserProfile) modelConversionHelper.deserializeObject(
                        UserProfile.class, document.toString()));
            } catch (IOException e) {
                throw new UserProfileException(e);
            }
        }
        return userList;
    }

    public boolean createUserProfile(UserProfile userProfile) throws UserProfileException{
        try {
            WriteResult result = collection.insert((DBObject) JSON.parse(
                    modelConversionHelper.serializeObject(userProfile)));
            logger.debug("No of inserted results "+ result.getN());
            return true;
        } catch (JsonProcessingException e) {
            throw new UserProfileException(e);
        }
    }

    /**
     * The following operation replaces the document with item equal to
     * the given user id. The newly replaced document will only
     * contain the the _id field and the fields in the replacement document.
     * @param userProfile
     * @throws org.apache.airavata.registry.cpi.UserProfileException
     */
    public boolean updateUserProfile(UserProfile userProfile) throws UserProfileException{
        try {
            DBObject query = BasicDBObjectBuilder.start().add(
                    USER_ID, userProfile.getUserId()).get();
            WriteResult result = collection.update(query, (DBObject) JSON.parse(
                    modelConversionHelper.serializeObject(userProfile)));
            logger.debug("No of updated results "+ result.getN());
            return true;
        } catch (JsonProcessingException e) {
            throw new UserProfileException(e);
        }
    }

    public boolean deleteUserProfile(String userId) throws UserProfileException{
        DBObject query = BasicDBObjectBuilder.start().add(
                USER_ID, userId).get();
        WriteResult result = collection.remove(query);
        logger.debug("No of removed user profiles " + result.getN());
        return true;
    }

    public UserProfile getUserProfileFromUserId(String userId) throws UserProfileException{
        try {
            DBObject criteria = new BasicDBObject(USER_ID, userId);
            DBObject doc = collection.findOne(criteria);
            if(doc != null){
                String json = doc.toString();
                return (UserProfile)modelConversionHelper.deserializeObject(
                        UserProfile.class, json);
            }
        } catch (IOException e) {
            throw new UserProfileException(e);
        }
        return null;
    }

    public UserProfile getUserProfileFromUserName(String userName, String gatewayId) throws UserProfileException{
        try {
            DBObject criteria = new BasicDBObject(USER_NAME, userName).append(GATEWAY_ID, gatewayId);
            DBObject doc = collection.findOne(criteria);
            if(doc != null){
                String json = doc.toString();
                return (UserProfile)modelConversionHelper.deserializeObject(
                        UserProfile.class, json);
            }
        } catch (IOException e) {
            throw new UserProfileException(e);
        }
        return null;
    }

    public boolean userProfileExists(String userName, String gatewayId) throws UserProfileException{
        try {
            DBObject criteria = new BasicDBObject(USER_NAME, userName).append(GATEWAY_ID, gatewayId);
            DBObject doc = collection.findOne(criteria);
            if(doc != null){
                String json = doc.toString();
                return modelConversionHelper.deserializeObject(UserProfile.class, json) !=  null;
            }
            return false;
        } catch (IOException e) {
            throw new UserProfileException(e);
        }
    }
}