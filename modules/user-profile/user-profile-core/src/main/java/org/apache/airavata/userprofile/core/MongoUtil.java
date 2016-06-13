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

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoUtil {
    private final static Logger logger = LoggerFactory.getLogger(MongoUtil.class);

    private static MongoClient mongoClient = null;
    private static DB userProfileRegistry;
    public static String USER_PROFILE_REGISTRY_NAME = "user-profile-registry";

    public static MongoClient getMongoClient() throws ApplicationSettingsException {
        if (mongoClient == null) {
            String host = ServerSettings.getUserProfileMongodbHost();
            int port = ServerSettings.getUserProfileMongodbPort();
            mongoClient = new MongoClient(host, port);
            logger.debug("New Mongo Client created with [" + host + "] and ["
                    + port + "]");
        }
        return mongoClient;
    }

    public static DB getUserProfileRegistry() throws ApplicationSettingsException {
        if (userProfileRegistry == null) {
            userProfileRegistry = getMongoClient().getDB(USER_PROFILE_REGISTRY_NAME);
        }
        return userProfileRegistry;
    }

    public static void dropUserProfileRegistry() throws ApplicationSettingsException {
        getMongoClient().dropDatabase(USER_PROFILE_REGISTRY_NAME);
        logger.debug("Dropped User Profile Registry");
    }
}