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
package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;

import java.util.List;

public interface Resource {

        /**
         * This method will create associate resource objects for the given resource type.
         * @return associated resource
         */
        Resource create() throws AppCatalogException;

        /**
         * This method will remove the given resource from the database
         * @param name resource name
         */
        void remove(Object name) throws AppCatalogException;

        /**
         *  This method will return the given resource from the database
         * @param name resource name
         * @return associate resource
         */
        Resource get(Object name) throws AppCatalogException;

        /**
         * This method will list all the child resources for the given resource type
         * @return list of child resources of the given child resource type
         */
        List<Resource> get() throws AppCatalogException;

        /**
         * This method will save the resource to the database.
         */
        void save() throws AppCatalogException;

        /**
         * This method will check whether an entry from the given resource type and resource name
         * exists in the database
         * @param name resource name
         * @return whether the entry exists in the database or not
         */
        boolean isExists(Object name) throws AppCatalogException;


}
