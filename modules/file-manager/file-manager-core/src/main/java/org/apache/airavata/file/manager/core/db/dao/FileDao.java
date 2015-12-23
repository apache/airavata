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
import org.apache.airavata.model.file.replica.FileModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class FileDao {
    private final static Logger logger = LoggerFactory.getLogger(FileDao.class);

    private static final String FILE_MODELS_COLLECTION_NAME = "file-models";
    private DBCollection collection;
    private ModelConversionHelper modelConversionHelper;

    private static final String FILE_ID = "file_id";

    public FileDao() throws IOException {
        collection = MongoUtils.getFileManagerRegistry().getCollection(FILE_MODELS_COLLECTION_NAME);
        modelConversionHelper = new ModelConversionHelper();
        collection.dropIndexes();
        initIndexes();
    }

    /**
     * If indexes are already defined this will simply ignore them
     */
    private void initIndexes() {
        collection.createIndex(new BasicDBObject(FILE_ID, 1), new BasicDBObject("unique", true));
    }

    public String createFile(FileModel fileModel) throws JsonProcessingException {
        fileModel.setFileId(FileManagerConstants.AIRAVATA_FILE_ID_PREFIX + UUID.randomUUID().toString());
        WriteResult result = collection.insert((DBObject) JSON.parse(
                modelConversionHelper.serializeObject(fileModel)));
        logger.debug("No of inserted results " + result.getN());
        return fileModel.getFileId();
    }

    public void updateFile(FileModel fileModel) throws JsonProcessingException {
        DBObject query = BasicDBObjectBuilder.start().add(
                FILE_ID, fileModel.getFileId()).get();
        WriteResult result = collection.update(query, (DBObject) JSON.parse(
                modelConversionHelper.serializeObject(fileModel)));
        logger.debug("No of updated results " + result.getN());
    }

    public void deleteFile(String fileId){
        DBObject query = BasicDBObjectBuilder.start().add(
                FILE_ID,fileId).get();
        WriteResult result = collection.remove(query);
        logger.debug("No of removed file model requests " + result.getN());
    }

    public FileModel getFile(String fileId) throws IOException {

        DBObject criteria = new BasicDBObject(FILE_ID, fileId);
        DBObject doc = collection.findOne(criteria);
        if (doc != null) {
            String json = doc.toString();
            return (FileModel) modelConversionHelper.deserializeObject(
                    FileModel.class, json);
        }
        return null;
    }
}