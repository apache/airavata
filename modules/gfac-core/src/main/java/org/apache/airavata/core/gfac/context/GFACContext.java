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

package org.apache.airavata.core.gfac.context;

import org.apache.airavata.core.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.core.gfac.type.HostDescription;
import org.apache.airavata.core.gfac.type.ServiceDescription;


/**
 * This class provides access to the Host, Application Deployment, and Service descriptions, which
 * are XML<->JavaBean serializations. 
 */ 
public class GFACContext{
    private HostDescription host;
    private ApplicationDeploymentDescription app;
    private ServiceDescription service;
    
    public HostDescription getHost() {
        return host;
    }
    public void setHost(HostDescription host) {
        this.host = host;
    }
    public ApplicationDeploymentDescription getApp() {
        return app;
    }
    public void setApp(ApplicationDeploymentDescription app) {
        this.app = app;
    }
    public ServiceDescription getService() {
        return service;
    }
    public void setService(ServiceDescription service) {
        this.service = service;
    }
}
