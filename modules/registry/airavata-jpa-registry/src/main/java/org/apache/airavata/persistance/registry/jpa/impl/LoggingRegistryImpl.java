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

import org.apache.airavata.registry.cpi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class LoggingRegistryImpl implements Registry {
    private final static Logger logger = LoggerFactory.getLogger(LoggingRegistryImpl.class);

    public Object add(ChildDataType dataType, Object newObjectToAdd, Object dependentIdentifiers) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object add(ParentDataType dataType, Object newObjectToAdd) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void update(RegistryModelType dataType, Object newObjectToUpdate, Object identifier) throws RegistryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void update(RegistryModelType dataType, Object identifier, String fieldName, Object value) throws RegistryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object get(RegistryModelType dataType, Object identifier) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Object> get(RegistryModelType dataType, String fieldName, Object value) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Object> search(RegistryModelType dataType, Map<String, String> filters) throws RegistryException {
        return null;
    }

    public Object getValue(RegistryModelType dataType, Object identifier, String field) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> getIds(RegistryModelType dataType, String fieldName, Object value) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void remove(RegistryModelType dataType, Object identifier) throws RegistryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isExist(RegistryModelType dataType, Object identifier) throws RegistryException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
