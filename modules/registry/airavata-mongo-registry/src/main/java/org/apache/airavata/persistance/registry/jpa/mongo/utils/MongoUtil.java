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
package org.apache.airavata.persistance.registry.jpa.mongo.utils;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoUtil {
    private final static Logger logger = LoggerFactory.getLogger(MongoUtil.class);

    private static final int port = 27017;
    private static final String host = "localhost";
    private static MongoClient mongoClient = null;
    private static DB airavataRegistry;
    public static String AIRAVATA_REGISTRY_NAME = "airavata-registry";

    public static MongoClient getMongoClient() {
        if (mongoClient == null) {
            try {
                mongoClient = new MongoClient(host, port);
                logger.debug("New Mongo Client created with [" + host + "] and ["
                        + port + "]");
            } catch (MongoException e) {
                logger.error(e.getMessage());
            }
        }
        return mongoClient;
    }

    public static DB getAiravataRegistry(){
        if (airavataRegistry == null) {
            try {
                airavataRegistry = getMongoClient().getDB(AIRAVATA_REGISTRY_NAME);
            } catch (MongoException e) {
                logger.error(e.getMessage());
            }
        }
        return airavataRegistry;
    }

    public static void dropAiravataRegistry(){
        try {
            getMongoClient().dropDatabase(AIRAVATA_REGISTRY_NAME);
            logger.debug("Dropped Airavata Registry");
        } catch (MongoException e) {
            logger.error(e.getMessage());
        }
    }
}