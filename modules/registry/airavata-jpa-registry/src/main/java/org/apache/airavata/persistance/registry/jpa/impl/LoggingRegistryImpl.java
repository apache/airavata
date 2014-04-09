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

import java.util.List;

public class LoggingRegistryImpl implements Registry {
    public Object add(ChildDataType dataType, Object newObjectToAdd, Object dependentIdentifiers) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object add(ParentDataType dataType, Object newObjectToAdd) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void update(DataType dataType, Object newObjectToUpdate, Object identifier) throws RegistryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void update(DataType dataType, Object identifier, String fieldName, Object value) throws RegistryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object get(DataType dataType, Object identifier) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Object> get(DataType dataType, String fieldName, Object value) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getValue(DataType dataType, Object identifier, String field) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> getIds(DataType dataType, String fieldName, Object value) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void remove(DataType dataType, Object identifier) throws RegistryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isExist(DataType dataType, Object identifier) throws RegistryException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
