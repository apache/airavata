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
package org.apache.airavata.server;

import java.io.File;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.startup.Tomcat;

public class ServerMain {

    private Tomcat embedded = null;
    /**
     * Default Constructor
     */
    public ServerMain() {
    }


    /**
     * This method Starts the Tomcat server.
     */
    public void startTomcat() throws Exception {
        AiravataUtils.setExecutionAsServer();
        String protocol = ServerSettings.isEnableHttps() ? "https" : "http";
        BetterTomcat tomcat = new BetterTomcat(Integer.parseInt(ServerSettings.getTomcatPort(protocol)));
        tomcat.addContext("/axis2", System.getenv("AIRAVATA_HOME"));
        Wrapper axis2Servlet = tomcat.addServlet("/axis2", "AxisServlet", "org.apache.axis2.transport.http.AxisServlet");
        axis2Servlet.addMapping("/servlet/AxisServlet");
        axis2Servlet.addMapping("*.jws");
        axis2Servlet.addMapping("/services/*");
        axis2Servlet.addInitParameter("axis2.repository.path",System.getenv("AIRAVATA_HOME") + File.separator + "repository");
        axis2Servlet.addInitParameter("axis2.xml.path", System.getenv("AIRAVATA_HOME") +
                File.separator + "bin" + File.separator + "axis2.xml");
        axis2Servlet.setLoadOnStartup(1);

        StandardContext context = (StandardContext)tomcat.getTomcat().addContext("/airavata", System.getenv("AIRAVATA_HOME"));
        Wrapper experimentServlet = tomcat.addServlet("/airavata", "Airavata Experiment Service", "com.sun.jersey.spi.container.servlet.ServletContainer");
        experimentServlet.addInitParameter("com.sun.jersey.config.property.packages", "org.apache.airavata.services.experiment;org.codehaus.jackson.jaxrs");
        experimentServlet.setLoadOnStartup(1);
        
        Wrapper configurationServlet = tomcat.addServlet("/airavata", "Airavata Server Configuration Service", "com.sun.jersey.spi.container.servlet.ServletContainer");
        configurationServlet.addInitParameter("com.sun.jersey.config.property.packages", "org.apache.airavata.services.server;org.codehaus.jackson.jaxrs");
        configurationServlet.setLoadOnStartup(1);
        
        Wrapper registryServlet = tomcat.addServlet("/airavata", "Airavata Registry Service", "com.sun.jersey.spi.container.servlet.ServletContainer");
        registryServlet.addInitParameter("com.sun.jersey.config.property.packages", "org.apache.airavata.services.registry.rest;org.codehaus.jackson.jaxrs");
        registryServlet.setLoadOnStartup(1);

        FilterDef filter1definition = new FilterDef();
        filter1definition.setFilterName("AuthenticationFilter");
        filter1definition.setFilterClass("org.apache.airavata.services.registry.rest.security.HttpAuthenticatorFilter");
        filter1definition.addInitParameter("authenticatorConfigurations","authenticators.xml");
        context.addFilterDef(filter1definition);

        FilterMap filter1mapping = new FilterMap();
        filter1mapping.setFilterName("AuthenticationFilter");
        filter1mapping.addURLPattern("/user-store/*");
        filter1mapping.addURLPattern("/services/registry/*");
        filter1mapping.addURLPattern("/services/server/*");
        filter1mapping.addURLPattern("/services/experiment/*");
        context.addFilterMap(filter1mapping);
        registryServlet.addMapping("/services/registry/*");
        configurationServlet.addMapping("/services/server/*");
        experimentServlet.addMapping("/services/experiment/*");
        context.addApplicationListener("org.apache.airavata.rest.mappings.utils.RegistryListener");

        tomcat.start();
    }

    /**
     * This method Stops the Tomcat server.
     */
    public void stopTomcat() throws Exception {
        // Stop the embedded server
        embedded.stop();
    }


    public static void main(String args[]) {
        try {
            new Thread(){
                public void run(){
                    ServerMain tomcat = new ServerMain();
                    try {
                        tomcat.startTomcat();
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }.start();
           while(true){
               Thread.sleep(10000);
           }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}