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

import org.apache.airavata.model.data.resource.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataManagerImpl implements DataManager{
    private final static Logger logger = LoggerFactory.getLogger(DataManagerImpl.class);

    @Override
    public boolean publishResource(ResourceModel resource) {
        return false;
    }

    @Override
    public boolean removeResource(String resourceId) {
        return false;
    }

    @Override
    public boolean copyResource(String resourceId, String destLocation) {
        return false;
    }

    @Override
    public ResourceModel getResource(String resourceId) {
        return null;
    }
}