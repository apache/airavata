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

import java.util.List;

public class RegistryImpl implements Registry {
    private final static Logger logger = LoggerFactory.getLogger(RegistryImpl.class);
    ExperimentRegistry experimentRegistry = new ExperimentRegistry();

    public void add(TopLevelDataType dataType, Object newObjectToAdd) {
        switch (dataType){
            case EXPERIMENT_BASIC_DATA:
                experimentRegistry.add((BasicMetadata)newObjectToAdd);
                break;
            default:
                logger.error("Unsupported data type", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void add(DependentDataType dataType, Object newObjectToAdd, Object dependentIdentifier) {
        switch (dataType){
            case EXPERIMENT_CONFIGURATION_DATA:
                experimentRegistry.add((ConfigurationData)newObjectToAdd, (String)dependentIdentifier);
                break;
            case EXPERIMENT_SUMMARY:
                break;
            case EXPERIMENT_GENERATED_DATA:
                break;
            case EXECUTION_ERROR:
                break;
            default:
                logger.error("Unsupported data type", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }

    }

    @Override
    public void update(TopLevelDataType dataType, Object newObjectToUpdate) {

    }

    @Override
    public void update(DependentDataType dataType, Object newObjectToUpdate, Object dependentIdentifier) {

    }

    @Override
    public void update(DataType dataType, Object identifier, Object field, Object value) {

    }

    @Override
    public List<Object> get(DataType dataType, Object filteredBy, Object value) {
        return null;
    }

    @Override
    public Object getValue(DataType dataType, Object identifier, Object field) {
        return null;
    }

    @Override
    public void remove(DataType dataType, Object identifier) {

    }

    @Override
    public boolean isExist(DataType dataType, Object identifier) {
        return false;
    }

    public void update(DependentDataType dataType, Object newObjectToUpdate) {

    }

    public void update(DependentDataType dataType, Object identifier, Object field, Object value) {

    }

    public List<Object> get(DependentDataType dataType, Object filteredBy, Object value) {
        return null;
    }

    public Object getValue(DependentDataType dataType, Object identifier, Object field) {
        return null;
    }

    public void remove(DependentDataType dataType, Object identifier) {

    }

    public boolean isExist(DependentDataType dataType, Object identifier) {
        return false;
    }
}
