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
package org.apache.airavata.registry.core.app.catalog.resources;


import org.apache.airavata.registry.cpi.AppCatalogException;

import java.util.List;

public interface AppCatalogResource {

    /**
     * This method will remove the given resource from the database
     *
     * @param identifier identifier that can uniquely identify a single instance of the resource
     */
    void remove(Object identifier) throws AppCatalogException;

    /**
     * This method will return the given resource from the database
     *
     * @param identifier identifier that can uniquely identify a single instance of the resource
     * @return associate resource
     */
    AppCatalogResource get(Object identifier) throws AppCatalogException;

    /**
     * This method will list all the resources according to the filtering criteria
     * @param fieldName field name
     * @param value value of the field
     * @return list of resources
     */
    List<AppCatalogResource> get(String fieldName, Object value) throws AppCatalogException;

    /**
     *
     * @return
     * @throws AppCatalogException
     */
    List<AppCatalogResource> getAll() throws AppCatalogException;

    /**
     *
     * @return
     * @throws AppCatalogException
     */
    List<String> getAllIds() throws AppCatalogException;

    /** This method will return list of resource ids according to given criteria
     * @param fieldName field name
     * @param value value of the field
     * @return list of resource Ids
     * @throws AppCatalogException
     */
    List<String> getIds(String fieldName, Object value) throws AppCatalogException;

    /**
     * This method will save the resource to the database.
     */
    void save() throws AppCatalogException;

    /**
     * This method will check whether an entry from the given resource and resource name
     * exists in the database
     *
     * @param identifier identifier that can uniquely identify a single instance of the resource
     * @return whether the entry exists in the database or not
     */
    boolean isExists(Object identifier) throws AppCatalogException;


}
