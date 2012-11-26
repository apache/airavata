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

package org.apache.airavata.services.registry.rest.utils;

import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.rest.mappings.utils.RestServicesConstants;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class RegistryListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            ServletContext servletContext = servletContextEvent.getServletContext();

            URL url = this.getClass().getClassLoader().
                    getResource(RestServicesConstants.AIRAVATA_SERVER_PROPERTIES);
            Properties properties = new Properties();
            try {
                properties.load(url.openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String gatewayID = properties.getProperty(RestServicesConstants.GATEWAY_ID);
            Gateway gateway =  new Gateway(gatewayID);

            servletContext.setAttribute(RestServicesConstants.GATEWAY, gateway);
            servletContext.setAttribute(RestServicesConstants.AIRAVATA_REGISTRY_POOL,new RegistryInstancesPool(100));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
