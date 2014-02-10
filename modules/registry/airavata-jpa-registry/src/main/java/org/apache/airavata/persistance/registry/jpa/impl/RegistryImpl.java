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

package org.apache.airavata.persistance.registry.jpa.impl;

import org.apache.airavata.model.experiment.BasicMetadata;
import org.apache.airavata.model.experiment.ConfigurationData;
import org.apache.airavata.registry.cpi.DataType;
import org.apache.airavata.registry.cpi.TopLevelDataType;
import org.apache.airavata.registry.cpi.DependentDataType;
import org.apache.airavata.registry.cpi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RegistryImpl implements Registry {
    private final static Logger logger = LoggerFactory.getLogger(RegistryImpl.class);
    ExperimentRegistry experimentRegistry = new ExperimentRegistry();

    /**
     * This method is to add an object in to the registry
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param newObjectToAdd Object which contains the fields that need to be saved in to registry. This object is a
     *                       thrift model object. In experiment case this object can be BasicMetadata, ConfigurationData
     *                       etc
     * @return return the identifier to identify the object
     */
    @Override
    public Object add(TopLevelDataType dataType, Object newObjectToAdd) {
        switch (dataType){
            case EXPERIMENT_BASIC_DATA:
                return experimentRegistry.add((BasicMetadata) newObjectToAdd);
            default:
                logger.error("Unsupported top level type..", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
    }

    /**
     * This method is to add an object in to the registry
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param newObjectToAdd Object which contains the fields that need to be saved in to registry. This object is a
     *                       thrift model object. In experiment case this object can be BasicMetadata, ConfigurationData
     *                       etc
     * @param dependentIdentifier Object which contains the identifier if the object that is going to add is not a top
     *                            level object in the data model. If it is a top level object, programmer can pass it as
     *                            null
     */
    @Override
    public void add(DependentDataType dataType, Object newObjectToAdd, Object dependentIdentifier) {
        switch (dataType){
            case EXPERIMENT_CONFIGURATION_DATA:
                experimentRegistry.add((ConfigurationData)newObjectToAdd, (String)dependentIdentifier);
                break;
            case EXPERIMENT_SUMMARY:
                // no thrift model yet
                break;
            case EXPERIMENT_GENERATED_DATA:
                // no thrift model yet
                break;
            case EXECUTION_ERROR:
                // no thrift model yet
                break;
            default:
                logger.error("Unsupported dependent data type...", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }

    }

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
    @Override
    public void update(DataType dataType, Object newObjectToUpdate, Object identifier) {
        switch (dataType){
            case EXPERIMENT_BASIC_DATA:
                experimentRegistry.update(newObjectToUpdate, (String)identifier);
                break;
            case EXPERIMENT_CONFIGURATION_DATA:
                experimentRegistry.update(newObjectToUpdate, (String)identifier);
                break;
            default:
                logger.error("Unsupported data type...", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }

    }

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
    @Override
    public void update(DataType dataType, Object identifier, String fieldName, Object value) {
        switch (dataType){
            case EXPERIMENT_BASIC_DATA:
                experimentRegistry.updateExpBasicMetadataField((String) identifier, fieldName, value);
                break;
            case EXPERIMENT_CONFIGURATION_DATA:
                experimentRegistry.updateExpConfigDataField((String) identifier, fieldName, value);
                break;
            default:
                logger.error("Unsupported data type...", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
    }

    /**
     * This method is to retrieve object according to the identifier. In the experiment basic data type, if you give the
     * experiment id, this method will return the BasicMetadata object
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @return object according to the given identifier.
     */
    @Override
    public Object get(DataType dataType, Object identifier) {

        switch (dataType){
            case EXPERIMENT_BASIC_DATA:
                return experimentRegistry.getBasicMetaData((String)identifier, null);
            case EXPERIMENT_CONFIGURATION_DATA:
                return experimentRegistry.getConfigData((String)identifier, null);
            default:
                logger.error("Unsupported data type...", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
    }

    /**
     * This method is to retrieve list of objects according to a given criteria
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param fieldName FieldName is the field that filtering should be done. For example, if we want to retrieve all
     *                   the experiments for a given user, filterBy will be "userName"
     * @param value value for the filtering field. In the experiment case, value for "userName" can be "admin"
     * @return List of objects according to the given criteria
     */
    @Override
    public List<Object> get(DataType dataType, String fieldName, Object value) {
        List<Object> result = new ArrayList<Object>();
        switch (dataType){
            case EXPERIMENT_BASIC_DATA:
                List<BasicMetadata> experimentMetaDataList = experimentRegistry.getExperimentMetaDataList(fieldName, value);
                for (BasicMetadata basicMetadata : experimentMetaDataList){
                    result.add(basicMetadata);
                }
                return result;
            case EXPERIMENT_CONFIGURATION_DATA:
                List<ConfigurationData> configurationDataList = experimentRegistry.getConfigurationDataList(fieldName, value);
                for (ConfigurationData configData : configurationDataList){
                    result.add(configData);
                }
                return result;
            default:
                logger.error("Unsupported data type...", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
    }

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
    @Override
    public Object getValue(DataType dataType, Object identifier, String field) {
        switch (dataType){
            case EXPERIMENT_BASIC_DATA:
                return experimentRegistry.getBasicMetaData((String) identifier, field);
            case EXPERIMENT_CONFIGURATION_DATA:
                return experimentRegistry.getConfigData((String) identifier, field);
            default:
                logger.error("Unsupported data type...", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
    }

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
    @Override
    public List<String> getIds(DataType dataType, String fieldName, Object value) {
        switch (dataType){
            case EXPERIMENT_BASIC_DATA:
                return experimentRegistry.getExperimentIDs(fieldName, value);
            case EXPERIMENT_CONFIGURATION_DATA:
                return experimentRegistry.getExperimentIDs(fieldName, value);
            default:
                logger.error("Unsupported data type...", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
    }

    /**
     * This method is to remove a item from the registry
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     */
    @Override
    public void remove(DataType dataType, Object identifier) {
        switch (dataType){
            case EXPERIMENT_BASIC_DATA:
                experimentRegistry.removeExperiment((String)identifier);
                break;
            case EXPERIMENT_CONFIGURATION_DATA:
                experimentRegistry.removeExperimentConfigData((String)identifier);
            default:
                logger.error("Unsupported data type...", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
    }

    /**
     * This method will check whether a given data type which can be identified with the identifier exists or not
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @return whether the given data type exists or not
     */
    @Override
    public boolean isExist(DataType dataType, Object identifier) {
        switch (dataType){
            case EXPERIMENT_BASIC_DATA:
                return experimentRegistry.isExperimentBasicDataExist((String)identifier);
            case EXPERIMENT_CONFIGURATION_DATA:
                return experimentRegistry.isExperimentConfigDataExist((String)identifier);
        }
        return false;
    }
}
