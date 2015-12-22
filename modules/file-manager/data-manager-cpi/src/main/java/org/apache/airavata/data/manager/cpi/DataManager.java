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

import org.apache.airavata.model.data.resource.DataReplicaLocationModel;
import org.apache.airavata.model.data.resource.DataResourceModel;

import java.util.List;

public interface DataManager {

    /**
     * To create a new dataResourceModel. This is how the system comes to know about already
     * existing resources
     * @param dataResourceModel
     * @return
     */
    String registerResource(DataResourceModel dataResourceModel) throws DataManagerException;

    /**
     * To remove a resource entry from the replica catalog
     * @param resourceId
     * @return
     */
    boolean removeResource(String resourceId) throws DataManagerException;


    /**
     * To update an existing data resource model
     * @param dataResourceModel
     * @return
     * @throws DataManagerException
     */
    boolean updateResource(DataResourceModel dataResourceModel) throws DataManagerException;

    /**
     * To retrieve a resource object providing the resourceId
     * @param resourceId
     * @return
     */
    DataResourceModel getResource(String resourceId) throws DataManagerException;

    /**
     * To create a new data replica location. This is how the system comes to know about already
     * existing resources
     * @param dataReplicaLocationModel
     * @return
     */
    String registerReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws DataManagerException;

    /**
     * To remove a replica entry from the replica catalog
     * @param replicaId
     * @return
     */
    boolean removeReplicaLocation(String replicaId) throws DataManagerException;

    /**
     * To update an existing data replica model
     * @param dataReplicaLocationModel
     * @return
     * @throws DataManagerException
     */
    boolean updateReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws DataManagerException;

    /**
     * To retrieve a replica object providing the replicaId
     * @param replicaId
     * @return
     */
    DataReplicaLocationModel getReplicaLocation(String replicaId) throws DataManagerException;

    /**
     * To retrieve all the replica entries for a given resource id
     * @param resourceId
     * @return
     * @throws DataCatalogException
     */
    List<DataReplicaLocationModel> getAllReplicaLocations(String resourceId) throws DataManagerException;


    /**
     * API method to copy a resource to the provided destination storage resource. Only resources of type FILE can be
     * copied using this API method. Method returns the new replicaId.
     * @param dataResourceId
     * @param destStorageResourceId
     * @param destinationParentPath
     * @return
     */
    String copyResource(String dataResourceId, String destStorageResourceId, String destinationParentPath) throws DataManagerException;

    /**
     * API method to copy the specified replica to the provided destination storage resource. Only resources of type FILE
     * can be copied using this API method. Method returns the new replicaId
     * @param dataResourceId
     * @param replicaId
     * @param destStorageResourceId
     * @param destinationParentPath
     * @return
     * @throws DataManagerException
     */
    String copyReplica(String dataResourceId, String replicaId, String destStorageResourceId, String destinationParentPath) throws DataManagerException;
}
