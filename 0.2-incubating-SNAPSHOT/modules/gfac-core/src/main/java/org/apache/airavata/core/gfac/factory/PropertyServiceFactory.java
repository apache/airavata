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

package org.apache.airavata.core.gfac.factory;

import org.apache.airavata.core.gfac.services.GenericService;
import org.apache.airavata.core.gfac.services.impl.PropertiesBasedServiceImpl;

/**
 * Factory for {@link PropertiesBasedServiceImpl}
 * 
 */
public class PropertyServiceFactory extends AbstractServiceFactory {

    private GenericService service;
    private String fileName;

    /**
     * Default constructor with used file "service.properties"
     */
    public PropertyServiceFactory() {
    }

    /**
     * Construct the {@link PropertiesBasedServiceImpl} with a given property file
     */
    public PropertyServiceFactory(String fileName) {
        this.fileName = fileName;
    }

    public GenericService getGenericService() {
        if (service == null) {
            if (this.fileName == null) {
                service = new PropertiesBasedServiceImpl();
            } else {
                service = new PropertiesBasedServiceImpl(this.fileName);
            }
        }
        return service;
    }
}
