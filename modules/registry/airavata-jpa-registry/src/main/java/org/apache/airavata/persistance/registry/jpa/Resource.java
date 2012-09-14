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
package org.apache.airavata.persistance.registry.jpa;

import java.util.List;

public interface Resource {
    /**
     * This method will create associate resource objects for the given resource type.
     * @param type child resource type
     * @return associate child resource
     */
    Resource create(ResourceType type);

    /**
     * This method will remove the given child resource from the database
     * @param type child resource type
     * @param name child resource name
     */
    void remove(ResourceType type, Object name);

    /**
     *  This method will return the given child resource from the database
     * @param type child resource type
     * @param name child resource name
     * @return associate child resource
     */
    Resource get(ResourceType type, Object name);

    /**
     * This method will list all the child resources for the given resource type
     * @param type child resource type
     * @return list of child resources of the given child resource type
     */
    List<Resource> get(ResourceType type);

    /**
     * This method will save the resource to the database.
     */
    void save();

    /**
     * This method will check whether an entry from the given resource type and resource name
     * exists in the database
     * @param type child resource type
     * @param name child resource name
     * @return whether the entry exists in the database or not
     */
    boolean isExists(ResourceType type, Object name);

}
