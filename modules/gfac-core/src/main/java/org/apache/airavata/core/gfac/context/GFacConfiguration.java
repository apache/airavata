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

import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.registry.api.Axis2Registry;

public class GFacConfiguration {

    private String myProxyServer;

    private String myProxyUser;

    private String myProxyPassphrase;

    private int myProxyLifeCycle;


    private AiravataRegistry registry;

    private String trustedCertLocation;

    public GFacConfiguration(String myProxyServer, String myProxyUser, String myProxyPassphrase, int myProxyLifeCycle, AiravataRegistry axis2Registry, String trustedCertLocation) {
        this.myProxyServer = myProxyServer;
        this.myProxyUser = myProxyUser;
        this.myProxyPassphrase = myProxyPassphrase;
        this.myProxyLifeCycle = myProxyLifeCycle;
        this.registry = axis2Registry;
        this.trustedCertLocation = trustedCertLocation;
    }

    public String getMyProxyServer() {
        return myProxyServer;
    }

    public String getMyProxyUser() {
        return myProxyUser;
    }

    public String getMyProxyPassphrase() {
        return myProxyPassphrase;
    }

    public int getMyProxyLifeCycle() {
        return myProxyLifeCycle;
    }

    public AiravataRegistry getRegistry() {
        return registry;
    }

    public String getTrustedCertLocation() {
        return trustedCertLocation;
    }
}
