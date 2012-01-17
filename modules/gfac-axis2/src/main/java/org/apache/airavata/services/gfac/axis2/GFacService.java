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

import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryFactory;
import javax.jcr.SimpleCredentials;

import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.registry.api.impl.AiravataJCRRegistry;
import org.apache.airavata.services.gfac.axis2.dispatchers.GFacURIBasedDispatcher;
import org.apache.airavata.services.gfac.axis2.handlers.AmazonSecurityHandler;
import org.apache.airavata.services.gfac.axis2.handlers.MyProxySecurityHandler;
import org.apache.airavata.services.gfac.axis2.util.WSConstants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.apache.axis2.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GFacService implements ServiceLifeCycle {

    private static final Logger log = LoggerFactory.getLogger(GFacService.class);

    public static final String CONFIGURATION_CONTEXT_REGISTRY = "registry";
    public static final String GFAC_URL = "GFacURL";

    public static final String SECURITY_CONTEXT = "security_context";

    public static final String REPOSITORY_PROPERTIES = "repository.properties";

    public static final int GFAC_URL_UPDATE_INTERVAL = 1000 * 60 * 60 * 3;

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

    private void initializeRepository(ConfigurationContext context) {
        Properties properties = new Properties();
        String port = null;
        try {
            URL url = this.getClass().getClassLoader().getResource(REPOSITORY_PROPERTIES);
            properties.load(url.openStream());
            Map<String, String> map = new HashMap<String, String>((Map) properties);
            AiravataRegistry registry = new AiravataJCRRegistry(new URI(map.get(ORG_APACHE_JACKRABBIT_REPOSITORY_URI)),map.get(JCR_CLASS),map.get(JCR_USER),map.get(JCR_PASS), map);
            String localAddress = Utils.getIpAddress(context.getAxisConfiguration());
            TransportInDescription transportInDescription = context.getAxisConfiguration().getTransportsIn()
                    .get("http");
            if (transportInDescription != null && transportInDescription.getParameter("port") != null) {
                port = (String) transportInDescription.getParameter("port").getValue();
            } else {
                port = map.get("port");
            }
            localAddress = "http://" + localAddress + ":" + port;
            localAddress = localAddress + "/" + context.getContextRoot() + "/" + context.getServicePath() + "/"
                    + WSConstants.GFAC_SERVICE_NAME;
            log.debug("GFAC_ADDRESS:" + localAddress);
            context.setProperty(CONFIGURATION_CONTEXT_REGISTRY, registry);
            context.setProperty(GFAC_URL, localAddress);
            context.setProperty(TRUSTED_CERT_LOCATION,properties.getProperty(TRUSTED_CERT_LOCATION));
            context.setProperty(MYPROXY_USER,properties.getProperty(MYPROXY_USER));
            context.setProperty(MYPROXY_PASS,properties.getProperty(MYPROXY_PASS));
            context.setProperty(MYPROXY_SERVER,properties.getProperty(MYPROXY_SERVER));
            context.setProperty(MYPROXY_LIFE,properties.getProperty(MYPROXY_LIFE));
            

            /*
             * Heart beat message to registry
             */
            thread = new GFacThread(context);
            thread.start();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void shutDown(ConfigurationContext configctx, AxisService service) {
        AiravataRegistry registry = (AiravataJCRRegistry) configctx.getProperty(CONFIGURATION_CONTEXT_REGISTRY);
        String gfacURL = (String) configctx.getProperty(GFAC_URL);
        registry.deleteGFacDescriptor(gfacURL);
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            log.info("GFacURL update thread is interrupted");
        }
    }

    class GFacThread extends Thread {
        private ConfigurationContext context = null;

        GFacThread(ConfigurationContext context) {
            this.context = context;
        }

        public void run() {
            try {
                while (true) {
                    AiravataRegistry registry = (AiravataRegistry) this.context.getProperty(CONFIGURATION_CONTEXT_REGISTRY);
                    String localAddress = (String) this.context.getProperty(GFAC_URL);
                    registry.saveGFacDescriptor(localAddress);
                    log.info("Updated the GFac URL in to Repository");
                    Thread.sleep(GFAC_URL_UPDATE_INTERVAL);
                }
            } catch (InterruptedException e) {
                log.info("GFacURL update thread is interrupted");
            }
        }
    }
}
