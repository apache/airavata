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
package org.apache.airavata.file.manager.core.db.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.apache.airavata.file.manager.core.db.conversion.ModelConversionHelper;
import org.apache.airavata.file.manager.core.db.utils.MongoUtils;
import org.apache.airavata.file.manager.cpi.FileManagerConstants;
import org.apache.airavata.model.file.replica.FileCollectionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class FileCollectionDao {
    private final static Logger logger = LoggerFactory.getLogger(FileCollectionDao.class);

    private static final String FILE_COLLECTION_COLLECTION_NAME = "collection-models";
    private DBCollection collection;
    private ModelConversionHelper modelConversionHelper;

    private static final String COLLECTION_ID = "collection_id";

    public FileCollectionDao() throws IOException {
        collection = MongoUtils.getFileManagerRegistry().getCollection(FILE_COLLECTION_COLLECTION_NAME);
        modelConversionHelper = new ModelConversionHelper();
        collection.dropIndexes();
        initIndexes();
    }

    /**
     * If indexes are already defined this will simply ignore them
     */
    private void initIndexes() {
        collection.createIndex(new BasicDBObject(COLLECTION_ID, 1), new BasicDBObject("unique", true));
    }

    public String createFileCollection(FileCollectionModel fileCollectionModel) throws JsonProcessingException {
        fileCollectionModel.setCollectionId(FileManagerConstants.AIRAVATA_COLLECTION_ID_PREFIX + UUID.randomUUID().toString());
        WriteResult result = collection.insert((DBObject) JSON.parse(
                modelConversionHelper.serializeObject(fileCollectionModel)));
        logger.debug("No of inserted results " + result.getN());
        return fileCollectionModel.getCollectionId();
    }

    public void updateFileCollection(FileCollectionModel fileCollectionModel) throws JsonProcessingException {
        DBObject query = BasicDBObjectBuilder.start().add(
                COLLECTION_ID, fileCollectionModel.getCollectionId()).get();
        WriteResult result = collection.update(query, (DBObject) JSON.parse(
                modelConversionHelper.serializeObject(fileCollectionModel)));
        logger.debug("No of updated results " + result.getN());
    }

    public void deleteFileCollection(String collectionId){
        DBObject query = BasicDBObjectBuilder.start().add(
                COLLECTION_ID,collectionId).get();
        WriteResult result = collection.remove(query);
        logger.debug("No of removed file model requests " + result.getN());
    }

    public FileCollectionModel getFileCollection(String collectionId) throws IOException {

        DBObject criteria = new BasicDBObject(COLLECTION_ID, collectionId);
        DBObject doc = collection.findOne(criteria);
        if (doc != null) {
            String json = doc.toString();
            return (FileCollectionModel) modelConversionHelper.deserializeObject(
                    FileCollectionModel.class, json);
        }
        return null;
    }
}