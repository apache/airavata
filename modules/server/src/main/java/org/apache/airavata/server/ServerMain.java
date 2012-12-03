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

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.catalina.Wrapper;
import java.io.File;
import org.apache.catalina.Host;
import org.apache.catalina.startup.Tomcat;

public class ServerMain {

    private String path = null;
    private Tomcat embedded = null;
    private Host host = null;

    /**
     * Default Constructor
     */
    public ServerMain() {
    }


    /**
     * This method Starts the Tomcat server.
     */
    public void startTomcat() throws Exception {

        BetterTomcat tomcat = new BetterTomcat(Integer.parseInt(ServerSettings.getTomcatPort()));
        tomcat.addContext("/axis2", System.getenv("AIRAVATA_HOME"));
        Wrapper axis2Servlet = tomcat.addServlet("/axis2", "AxisServlet", "org.apache.axis2.transport.http.AxisServlet");
        axis2Servlet.addMapping("/servlet/AxisServlet");
        axis2Servlet.addMapping("*.jws");
        axis2Servlet.addMapping("/services/*");
        axis2Servlet.addInitParameter("axis2.repository.path",System.getenv("AIRAVATA_HOME") + File.separator + "repository");
        axis2Servlet.addInitParameter("axis2.xml.path", System.getenv("AIRAVATA_HOME") +
                File.separator + "bin" + File.separator + "axis2.xml");
        axis2Servlet.setLoadOnStartup(1);

//        Wrapper registryServlet = tomcat.addServlet("/registry", "Airavata Web Application", "com.sun.jersey.spi.container.servlet.ServletContainer");
//        registryServlet.addInitParameter("com.sun.jersey.config.property.packages","org.apache.airavata.services.registry.rest;org.codehaus.jackson.jaxrs");
//        registryServlet.setLoadOnStartup(1);
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