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

package org.apache.airavata.services.gfac.axis2;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.common.utils.ServiceUtils;
import org.apache.airavata.core.gfac.context.GFacConfiguration;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.AiravataRegistryFactory;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.util.RegistryUtils;
import org.apache.airavata.services.gfac.axis2.dispatchers.GFacURIBasedDispatcher;
import org.apache.airavata.services.gfac.axis2.handlers.AmazonSecurityHandler;
import org.apache.airavata.services.gfac.axis2.handlers.MyProxySecurityHandler;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GFacService implements ServiceLifeCycle {

    private static final Logger log = LoggerFactory.getLogger(GFacService.class);

    public static final String CONFIGURATION_CONTEXT_REGISTRY = "registry";
    public static final String GFAC_URL = "GFacURL";

    public static final String SECURITY_CONTEXT = "security_context";

    public static final String REPOSITORY_PROPERTIES = "airavata-server.properties";

    public static final int GFAC_URL_UPDATE_INTERVAL = 1000 * 60 * 60 * 3;

    public static final int JCR_AVAIALABILITY_WAIT_INTERVAL = 1000 * 10;

    public static final String REGISTRY_USER = "registry.user";

    public static final String SERVICE_NAME = "GFacService";

    /*
     * Properties for JCR
     */
    public static final String JCR_CLASS = "jcr.class";
    public static final String JCR_USER = "jcr.user";
    public static final String JCR_PASS = "jcr.pass";
    public static final String ORG_APACHE_JACKRABBIT_REPOSITORY_URI = "org.apache.jackrabbit.repository.uri";
    public static final String TRUSTED_CERT_LOCATION = "trusted.cert.location";
    public static final String MYPROXY_SERVER = "myproxy.server";
    public static final String MYPROXY_USER = "myproxy.user";
    public static final String MYPROXY_PASS = "myproxy.pass";
    public static final String MYPROXY_LIFE = "myproxy.life";
    public static final String GFAC_CONFIGURATION = "gfacConfiguration";
    public static final String GATEWAY_ID = "gateway.id";

    /*
     * Heart beat thread
     */
    private Thread thread;

    public void startUp(ConfigurationContext configctx, AxisService service) {
        AxisConfiguration config = null;
        List<Phase> phases = null;
        config = service.getAxisConfiguration();
        phases = config.getInFlowPhases();

        /*
         * Add dispatcher and security handler to inFlowPhases
         */
        for (Iterator<Phase> iterator = phases.iterator(); iterator.hasNext();) {
            Phase phase = (Phase) iterator.next();
            if ("Security".equals(phase.getPhaseName())) {
                phase.addHandler(new MyProxySecurityHandler());
                phase.addHandler(new AmazonSecurityHandler());
            } else if ("Dispatch".equals(phase.getPhaseName())) {
                phase.addHandler(new GFacURIBasedDispatcher(), 0);
            }
        }

        initializeRepository(configctx);
    }

    private void initializeRepository(final ConfigurationContext context) {
    	//Allow the initialization to run on a thread, orelse this will create the chiken and egg prob
    	//(orelse the main thread will wait for the response from jackrabbit which actually starts after GFac webapp
    	new Thread(){
    		@Override
    		public void run() {
                String port = null;
                String username = null;
                AiravataRegistry2 registry = null;
                try {
                    URL url = this.getClass().getClassLoader().getResource(REPOSITORY_PROPERTIES);
                    try {
                        Thread.sleep(JCR_AVAIALABILITY_WAIT_INTERVAL);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    Properties properties = new Properties();
                    try {
                        properties.load(url.openStream());
                        if (properties.get(REGISTRY_USER) != null) {
                            username = (String) properties.get(REGISTRY_USER);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    registry = RegistryUtils.getRegistryFromConfig(url);
                    context.setProperty(GFAC_URL, ServiceUtils.generateServiceURLFromConfigurationContext(context,SERVICE_NAME));
                    GFacConfiguration gfacConfig = new GFacConfiguration(properties.getProperty(MYPROXY_SERVER),properties.getProperty(MYPROXY_USER),
                            properties.getProperty(MYPROXY_PASS),Integer.parseInt(properties.getProperty(MYPROXY_LIFE)),registry,properties.getProperty(TRUSTED_CERT_LOCATION));
					context.setProperty(GFAC_CONFIGURATION,
							gfacConfig);
					/*
					 * Heart beat message to registry
					 */
					thread = new GFacThread(context);
					thread.start();
    	        } catch (Exception e) {
    	            log.error(e.getMessage(), e);
    	        }
    		}
    	}.start();
    }

    public void shutDown(ConfigurationContext configctx, AxisService service) {
        //following nullchecks will avoid the exceptions when user press Ctrl-C before the server start properly
        if (configctx.getProperty(GFAC_CONFIGURATION) != null) {
            AiravataRegistry2 registry = ((GFacConfiguration) configctx.getProperty(GFAC_CONFIGURATION)).getRegistry();
            String gfacURL = (String) configctx.getProperty(GFAC_URL);
            try {
                registry.removeGFacURI(new URI(gfacURL));
            } catch (URISyntaxException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.info("GFacURL update thread is interrupted");
            }
        }
    }

    class GFacThread extends Thread {
        private ConfigurationContext context = null;

        GFacThread(ConfigurationContext context) {
            this.context = context;
        }

         public void run() {
            try{
                while (true) {
                    try {
                        AiravataRegistry2 registry = ((GFacConfiguration)context.getProperty(GFAC_CONFIGURATION)).getRegistry();
                        URI localAddress = new URI((String)this.context.getProperty(GFAC_URL));
                        registry.addGFacURI(localAddress);
                        log.info("Updated Workflow Interpreter service URL in to Repository");
                        Thread.sleep(GFAC_URL_UPDATE_INTERVAL);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }catch (Exception e){
                try {
                    Thread.sleep(JCR_AVAIALABILITY_WAIT_INTERVAL);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    return;
                }
                log.error(e.getMessage());
                log.error("Workflow Interpreter Service URL update thread is interrupted");
            }
        }
    }
}
