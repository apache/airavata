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
package org.apache.airavata.data.manager.cpi;

import org.apache.airavata.model.data.metadata.MetadataModel;

public interface MetadataCatalogService {

    /**
     * Create new metadata model
     * @param metadataModel
     * @return
     * @throws DataManagerException
     */
    String createMetadata(MetadataModel metadataModel) throws DataManagerException;

    /**
     * Update exisiting metadata model
     * @param metadataModel
     * @throws DataManagerException
     */
    void updateMetadata(MetadataModel metadataModel) throws DataManagerException;

    /**
     * Delete existing metadata model
     * @param metadataId
     * @throws DataManagerException
     */
    void deleteMetadata(String metadataId) throws DataManagerException;

    /**
     * Retrieve metadata model
     * @param metadataId
     * @return
     * @throws DataManagerException
     */
    MetadataModel getMetadata(String metadataId) throws DataManagerException;
}