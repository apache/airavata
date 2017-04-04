/**
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
 */
package org.apache.airavata.registry.cpi;

import java.util.List;
import java.util.Map;

/**
 * This is the interface for Registry CPI
 */
public interface ExperimentCatalog {

    /**
     * This method is to add an object in to the registry
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param newObjectToAdd Object which contains the fields that need to be saved in to registry. This object is a
     *                       thrift model object. In experiment case this object can be BasicMetadata, ConfigurationData
     *                       etc
     * @return return the identifier to identify the object
     */
    public Object add(ExpCatParentDataType dataType, Object newObjectToAdd, String gatewayId) throws RegistryException ;

    /**
     * This method is to add an object in to the registry
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param newObjectToAdd Object which contains the fields that need to be saved in to registry. This object is a
     *                       thrift model object. In experiment case this object can be BasicMetadata, ConfigurationData
     *                       etc
     * @param dependentIdentifiers contains the identifier if the object that is going to add is not a top
     *                            level object in the data model. This object can be a simple string or a
     *                             org.apache.airavata.registry.cpi.CompositeIdentifier type if it is a child element
     *                             with multiple identifiers
     * @return return the identifier to identify the object
     */
    public Object add(ExpCatChildDataType dataType, Object newObjectToAdd, Object dependentIdentifiers) throws RegistryException;

    /**
     * This method is to update the whole object in registry
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param newObjectToUpdate Object which contains the fields that need to be updated in to registry. This object is a
     *                       thrift model object. In experiment case this object can be BasicMetadata, ConfigurationData
     *                       etc. CPI programmer can only fill necessary fields that need to be updated. He does not
     *                       have to fill the whole object. He needs to only fill the mandatory fields and whatever the
     *                       other fields that need to be updated.
     */
    public void update(ExperimentCatalogModelType dataType, Object newObjectToUpdate, Object identifier) throws RegistryException;


    /**
     * This method is to update a specific field of the data model
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @param fieldName Field which need to be updated in the registry. In Experiment_Basic_Type, if you want to update the
     *              description, field will be "description". Field names are defined in
     *              org.apache.airavata.registry.cpi.utils.Constants
     * @param value Value by which the given field need to be updated. If the field is "description", that field will be
     *              updated by given value
     */
    public void update(ExperimentCatalogModelType dataType, Object identifier, String fieldName, Object value) throws RegistryException;

    /**
     * This method is to retrieve object according to the identifier. In the experiment basic data type, if you give the
     * experiment id, this method will return the BasicMetadata object
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @return object according to the given identifier.
     */
    public Object get(ExperimentCatalogModelType dataType, Object identifier) throws RegistryException;

    /**
     * This method is to retrieve list of objects according to a given criteria
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param fieldName FieldName is the field that filtering should be done. For example, if we want to retrieve all
     *                   the experiments for a given user, filterBy will be "userName"
     * @param value value for the filtering field. In the experiment case, value for "userName" can be "admin"
     * @return List of objects according to the given criteria
     */
    public List<Object> get(ExperimentCatalogModelType dataType, String fieldName, Object value) throws RegistryException;

    /**
     * This method is to retrieve list of objects according to a given criteria with pagination and ordering
     *
     * @param dataType  Data type is a predefined type which the programmer should choose according to the object he
     *                  is going to save in to registry
     * @param fieldName FieldName is the field that filtering should be done. For example, if we want to retrieve all
     *                  the experiments for a given user, filterBy will be "userName"
     * @param value     value for the filtering field. In the experiment case, value for "userName" can be "admin"
     * @param limit     Size of the results to be returned
     * @param offset    Start position of the results to be retrieved
     * @param orderByIdentifier     Named of the column in which the ordering is based
     * @param resultOrderType       Type of ordering i.e ASC or DESC
     * @return
     * @throws RegistryException
     */
    public List<Object> get(ExperimentCatalogModelType dataType, String fieldName, Object value, int limit,
                            int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException ;
    /**
     * This method is to retrieve list of objects according to a given criteria
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param filters filters is a map of field name and value that you need to use for search filtration
     * @return List of objects according to the given criteria
     */
    public List<Object> search(ExperimentCatalogModelType dataType, Map<String, String> filters) throws RegistryException;

    /**
     * This method is to retrieve list of objects with pagination according to a given criteria sorted
     * according by the specified  identified and specified ordering (i.e either ASC or DESC)
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param filters            filters is a map of field name and value that you need to use for search filtration
     * @param limit              amount of the results to be returned
     * @param offset             offset of the results from the sorted list to be fetched from
     * @param orderByIdentifier  identifier (i.e the column) which will be used as the basis to sort the results
     * @param resultOrderType    The type of ordering (i.e ASC or DESC) that has to be used when retrieving the results
     * @return List of objects according to the given criteria
     */
    public List<Object> search(ExperimentCatalogModelType dataType, Map<String, String> filters,
                                             int limit, int offset, Object orderByIdentifier,
                                             ResultOrderType resultOrderType) throws RegistryException;

    /**
     * This method search all the accessible resources given the set of ids of all accessible resource IDs.
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param accessibleIds      list of string IDs of all accessible resources
     * @param filters            filters is a map of field name and value that you need to use for search filtration
     * @param limit              amount of the results to be returned
     * @param offset             offset of the results from the sorted list to be fetched from
     * @param orderByIdentifier  identifier (i.e the column) which will be used as the basis to sort the results
     * @param resultOrderType    The type of ordering (i.e ASC or DESC) that has to be used when retrieving the results
     * @return List of objects according to the given criteria
     */
    public List<Object> searchAllAccessible(ExperimentCatalogModelType dataType,List<String> accessibleIds, Map<String, String> filters,
                               int limit, int offset, Object orderByIdentifier,
                               ResultOrderType resultOrderType) throws RegistryException;

    /**
     * This method is to retrieve a specific value for a given field.
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @param field field that filtering should be done. For example, if we want to execution user for a given
     *              experiment, field will be "userName"
     * @return return the value for the specific field where data model is identified by the unique identifier that has
     *         given
     */
    public Object getValue (ExperimentCatalogModelType dataType, Object identifier, String field) throws RegistryException;

    /**
     * This method is to retrieve all the identifiers according to given filtering criteria. For an example, if you want
     * to get all the experiment ids for a given gateway, your field name will be "gateway" and the value will be the
     * name of the gateway ("default"). Similar manner you can retrieve all the experiment ids for a given user.
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param fieldName FieldName is the field that filtering should be done. For example, if we want to retrieve all
     *                the experiments for a given user, filterBy will be "userName"
     * @param value value for the filtering field. In the experiment case, value for "userName" can be "admin"
     * @return id list according to the filtering criteria
     */
    public List<String> getIds (ExperimentCatalogModelType dataType, String fieldName, Object value) throws RegistryException;

    /**
     * This method is to remove a item from the registry
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     */
    public void remove (ExperimentCatalogModelType dataType, Object identifier) throws RegistryException;

    /**
     * This method will check whether a given data type which can be identified with the identifier exists or not
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @return whether the given data type exists or not
     */
    public boolean isExist(ExperimentCatalogModelType dataType, Object identifier) throws RegistryException;


}
