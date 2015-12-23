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
import org.apache.airavata.model.file.FileTransferRequest;
import org.apache.airavata.registry.core.experiment.catalog.model.Gateway;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileTransferRequestDao {
    private final static Logger logger = LoggerFactory.getLogger(FileTransferRequestDao.class);

    private static final String FILE_TRANSFER_REQUESTS_COLLECTION_NAME = "file-transfer-requests";
    private DBCollection collection;
    private ModelConversionHelper modelConversionHelper;

    private static final String TRANSFER_ID = "transfer_id";

    public FileTransferRequestDao() throws IOException {
        collection = MongoUtils.getFileManagerRegistry().getCollection(FILE_TRANSFER_REQUESTS_COLLECTION_NAME);
        modelConversionHelper = new ModelConversionHelper();
        collection.dropIndexes();
        initIndexes();
    }

    /**
     * If indexes are already defined this will simply ignore them
     */
    private void initIndexes() {
        collection.createIndex(new BasicDBObject(TRANSFER_ID, 1), new BasicDBObject("unique", true));
    }

    public List<FileTransferRequest> getAllFileTransferRequests() throws IOException {
        List<FileTransferRequest> fileTransferRequestList = new ArrayList();
        DBCursor cursor = collection.find();
        for (DBObject document : cursor) {
            fileTransferRequestList.add((FileTransferRequest) modelConversionHelper.deserializeObject(
                    FileTransferRequest.class, document.toString()));
        }
        return fileTransferRequestList;
    }

    public String createFileTransferRequest(FileTransferRequest fileTransferRequest) throws JsonProcessingException {
        fileTransferRequest.setTransferId(UUID.randomUUID().toString());
        WriteResult result = collection.insert((DBObject) JSON.parse(
                modelConversionHelper.serializeObject(fileTransferRequest)));
        logger.debug("No of inserted results " + result.getN());
        return fileTransferRequest.getTransferId();
    }

    public void updateFileTransferRequest(FileTransferRequest fileTransferRequest) throws JsonProcessingException {
        DBObject query = BasicDBObjectBuilder.start().add(
                TRANSFER_ID, fileTransferRequest.getTransferId()).get();
        WriteResult result = collection.update(query, (DBObject) JSON.parse(
                modelConversionHelper.serializeObject(fileTransferRequest)));
        logger.debug("No of updated results " + result.getN());
    }

    public void deleteFileTransferRequest(FileTransferRequest fileTransferRequest){
        DBObject query = BasicDBObjectBuilder.start().add(
                TRANSFER_ID, fileTransferRequest.getTransferId()).get();
        WriteResult result = collection.remove(query);
        logger.debug("No of removed file transfer requests " + result.getN());
    }

    public FileTransferRequest getFileTransferRequest(String transferId) throws IOException {

        DBObject criteria = new BasicDBObject(TRANSFER_ID, transferId);
        DBObject doc = collection.findOne(criteria);
        if (doc != null) {
            String json = doc.toString();
            return (FileTransferRequest) modelConversionHelper.deserializeObject(
                    FileTransferRequest.class, json);
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        FileTransferRequest fileTransferRequest = new FileTransferRequest();
        fileTransferRequest.setSrcHostCredToken("djkalbsbdaslfbalsfbslf");
        fileTransferRequest.setSrcFilePath("test-file-path");
        FileTransferRequestDao fileTransferRequestDao = new FileTransferRequestDao();
        String transferId = fileTransferRequestDao.createFileTransferRequest(fileTransferRequest);
        fileTransferRequest = fileTransferRequestDao.getFileTransferRequest(transferId);
        System.out.println("Transfer Id:" + fileTransferRequest.getTransferId());
    }
}