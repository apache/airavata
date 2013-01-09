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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.client.tools.PeriodicExecutorThread;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ServiceUtils;
import org.apache.airavata.core.gfac.context.GFacConfiguration;
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

    public static final String GFAC_URL = "GFacURL";

    public static final String SECURITY_CONTEXT = "security_context";

    public static final int JCR_AVAIALABILITY_WAIT_INTERVAL = 1000 * 10;

    public static final String SYSTEM_USER = "system.user";

    public static final String SERVICE_NAME = "GFacService";

    /*
     * Properties for JCR
     */
    public static final String TRUSTED_CERT_LOCATION = "trusted.cert.location";
    public static final String MYPROXY_SERVER = "myproxy.server";
    public static final String MYPROXY_USER = "myproxy.user";
    public static final String MYPROXY_PASS = "myproxy.pass";
    public static final String MYPROXY_LIFE = "myproxy.life";
    public static final String GFAC_CONFIGURATION = "gfacConfiguration";
    public static final String SYSTEM_GATEWAY = "system.gateway";
    public static final String SYSTEM_PASSWORD = "system.password";
//    public static final String REGISTRY_URL = "registry.jdbc.url";

    /*
     * Heart beat thread
     */
    private Thread thread;

    public void startUp(ConfigurationContext configctx, AxisService service) {
    	AiravataUtils.setExecutionAsServer();
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
                String password = null;
                String gatewayName = null;
                AiravataAPI airavataAPI = null;
                try {
                    try {
                        Thread.sleep(JCR_AVAIALABILITY_WAIT_INTERVAL);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    try {
                        if (ServerSettings.getSystemUser() != null) {
                            username = ServerSettings.getSystemUser();
                        }
                        if (ServerSettings.getSystemUserPassword() != null) {
                            password = ServerSettings.getSystemUserPassword();
                        }
                        gatewayName = ServerSettings.getDefaultGatewayId();
                    } catch (ApplicationSettingsException e) {
                        log.error("Unable to read properties", e);
                    }
                    airavataAPI = AiravataAPIFactory.getAPI(ServerSettings.getDefaultGatewayId(), ServerSettings.getSystemUser());
                    context.setProperty(GFAC_URL, ServiceUtils.generateServiceURLFromConfigurationContext(context,SERVICE_NAME));
                    GFacConfiguration gfacConfig = new GFacConfiguration(ServerSettings.getSetting(MYPROXY_SERVER),ServerSettings.getSetting(MYPROXY_USER),
                            ServerSettings.getSetting(MYPROXY_PASS),Integer.parseInt(ServerSettings.getSetting(MYPROXY_LIFE)),airavataAPI,ServerSettings.getSetting(TRUSTED_CERT_LOCATION));
					context.setProperty(GFAC_CONFIGURATION,
							gfacConfig);
					/*
					 * Heart beat message to registry
					 */
					thread = new GFacThread(airavataAPI, context);
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
            AiravataAPI airavataAPI = ((GFacConfiguration) configctx.getProperty(GFAC_CONFIGURATION)).getAiravataAPI();
            String gfacURL = (String) configctx.getProperty(GFAC_URL);
            try {
                airavataAPI.getAiravataManager().removeGFacURI(new URI(gfacURL));
            } catch (AiravataAPIInvocationException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.warn("GFacURL update thread is interrupted");
            }
        }
    }

    class GFacThread extends PeriodicExecutorThread {
        private ConfigurationContext context = null;

        GFacThread(AiravataAPI airavataAPI, ConfigurationContext context) {
            super(airavataAPI);
            this.context = context;
        }

        @Override
        protected void updateRegistry(AiravataAPI airavataAPI) throws Exception {
            URI localAddress = new URI((String) this.context.getProperty(GFAC_URL));
            airavataAPI.getAiravataManager().addGFacURI(localAddress);
            log.debug("Updated GFac service URL in to Repository");
        }
    }
}
