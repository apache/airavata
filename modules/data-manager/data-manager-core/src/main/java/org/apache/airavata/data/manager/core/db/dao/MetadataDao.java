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
package org.apache.airavata.data.manager.core.db.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.apache.airavata.data.manager.core.db.conversion.ModelConversionHelper;
import org.apache.airavata.data.manager.core.db.utils.MongoUtils;
import org.apache.airavata.data.manager.cpi.DataManagerConstants;
import org.apache.airavata.model.data.metadata.MetadataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MetadataDao {
    private final static Logger logger = LoggerFactory.getLogger(MetadataDao.class);

    private static final String METADATA_COLLECTION_NAME = "metadata-models";
    private DBCollection collection;
    private ModelConversionHelper modelConversionHelper;

    private static final String METADATA_ID = "metadata_id";

    public MetadataDao() throws IOException {
        collection = MongoUtils.getFileManagerRegistry().getCollection(METADATA_COLLECTION_NAME);
        modelConversionHelper = new ModelConversionHelper();
        collection.dropIndexes();
        initIndexes();
    }

    /**
     * If indexes are already defined this will simply ignore them
     */
    private void initIndexes() {
        collection.createIndex(new BasicDBObject(METADATA_ID, 1), new BasicDBObject("unique", true));
    }

    public String createMetadata(MetadataModel metadataModel) throws JsonProcessingException {
        metadataModel.setMetadataId(DataManagerConstants.AIRAVATA_METADATA_ID_PREFIX + UUID.randomUUID().toString());
        metadataModel.setCreationTime(System.currentTimeMillis());
        metadataModel.setLastModifiedTime(metadataModel.getCreationTime());
        WriteResult result = collection.insert((DBObject) JSON.parse(
                modelConversionHelper.serializeObject(metadataModel)));
        logger.debug("No of inserted results " + result.getN());
        return metadataModel.getMetadataId();
    }

    public void updateMetadata(MetadataModel metadataModel) throws JsonProcessingException {
        DBObject query = BasicDBObjectBuilder.start().add(
                METADATA_ID, metadataModel.getMetadataId()).get();
        metadataModel.setLastModifiedTime(System.currentTimeMillis());
        WriteResult result = collection.update(query, (DBObject) JSON.parse(
                modelConversionHelper.serializeObject(metadataModel)));
        logger.debug("No of updated results " + result.getN());
    }

    public void deleteMetadata(String metadataId){
        DBObject query = BasicDBObjectBuilder.start().add(
                METADATA_ID,metadataId).get();
        WriteResult result = collection.remove(query);
        logger.debug("No of removed metadata model requests " + result.getN());
    }

    public MetadataModel getMetadata(String metadataId) throws IOException {

        DBObject criteria = new BasicDBObject(METADATA_ID, metadataId);
        DBObject doc = collection.findOne(criteria);
        if (doc != null) {
            String json = doc.toString();
            return (MetadataModel) modelConversionHelper.deserializeObject(
                    MetadataModel.class, json);
        }
        return null;
    }

    public List<MetadataModel> searchMetaDataModels(String username, String gatewayId, String searchText) throws IOException{
        List<MetadataModel> metadataModels = new ArrayList<>();
        DBCursor cursor = collection.find();
        for(DBObject document: cursor){
                metadataModels.add((MetadataModel) modelConversionHelper.deserializeObject(
                        MetadataModel.class, document.toString()));
        }
        return metadataModels;
    }

}