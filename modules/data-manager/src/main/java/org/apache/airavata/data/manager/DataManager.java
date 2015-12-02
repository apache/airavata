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
package org.apache.airavata.data.manager;

import org.apache.airavata.model.data.resource.DataResourceModel;

public interface DataManager {

    /**
     * To create a replica entry for an already existing file(s). This is how the system comes to know about already
     * existing resources
     * @param resource
     * @return
     */
    boolean publishResource(DataResourceModel resource);

    /**
     * To remove a resource entry from the replica catalog
     * @param resourceId
     * @return
     */
    boolean removeResource(String resourceId);

    /**
     * To copy an already existing resource to a specified location. After successful copying the new location will be
     * added to the available replica locations of the resource
     * @param resourceId
     * @param destLocation
     * @return
     */
    boolean copyResource(String resourceId, String destLocation);

    /**
     * To retrieve a resource object providing the resourceId
     * @param resourceId
     * @return
     */
    DataResourceModel getResource(String resourceId);
}
