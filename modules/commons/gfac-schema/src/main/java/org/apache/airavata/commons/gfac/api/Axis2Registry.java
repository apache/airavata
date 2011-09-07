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


package org.apache.airavata.commons.gfac.api;

import org.apache.airavata.commons.gfac.type.ServiceDescription;


public interface Axis2Registry extends Registry {
    
    /**
     * Save WSDL for Axis2
     * 
     * @param service
     * @param WSDL
     */
    public String saveWSDL(String serviceName, String WSDL);
    
    /**
     * Save WSDL for Axis2
     * 
     * @param serviceName
     * @param service
     */
    public String saveWSDL(String serviceName, ServiceDescription service);
    
    
    /**
     * Load WSDL for Axis2
     * 
     * @param serviceName
     * @return WSDL
     */
    public String getWSDL(String serviceName);
}
