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
package org.apache.airavata.data.manager.core.db.utils;

import org.apache.airavata.data.manager.core.utils.FileManagerConstants;
import org.apache.airavata.data.manager.core.utils.FileManagerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import java.io.IOException;

public class MongoUtils {
    private final static Logger logger = LoggerFactory.getLogger(MongoUtils.class);

    private static int port;
    private static String host;
    private static MongoClient mongoClient = null;
    private static DB fileManagerRegistry;
    private static String FILE_MANAGER_REGISTRY_NAME;

    public static MongoClient getMongoClient() throws IOException {
        if (mongoClient == null) {
            FileManagerProperties fileManagerProperties = FileManagerProperties.getInstance();
            host = fileManagerProperties.getProperty(FileManagerConstants.MONGODB_HOST, "localhost");
            port = Integer.parseInt(fileManagerProperties.getProperty(FileManagerConstants.MONGODB_PORT, "27017"));
            FILE_MANAGER_REGISTRY_NAME = fileManagerProperties.getProperty(FileManagerConstants.MONGODB_DB_NAME,
                    "file-manager-db");
            mongoClient = new MongoClient(host, port);
            logger.debug("New Mongo Client created with [" + host + "] and ["
                    + port + "]");

        }
        return mongoClient;
    }

    public static DB getFileManagerRegistry() throws IOException {
        if (fileManagerRegistry == null) {
            fileManagerRegistry = getMongoClient().getDB(FILE_MANAGER_REGISTRY_NAME);
        }
        return fileManagerRegistry;
    }

    public static void dropFileManagerRegistry() throws IOException {
        getMongoClient().dropDatabase(FILE_MANAGER_REGISTRY_NAME);
        logger.debug("Dropped File Manager Registry");

    }
}