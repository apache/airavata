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

import org.apache.airavata.core.gfac.services.GenericService;
import org.apache.airavata.registry.api.impl.JCRRegistry;
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

    public static final String SECURITY_CONTEXT = "security_context";
    public static final String REPOSITORY_PROPERTIES = "repository.properties";

    /*
     * Properties for JCR
     */
    public static final String JCR_CLASS = "jcr.class";
    public static final String JCR_USER = "jcr.user";
    public static final String JCR_PASS = "jcr.pass";

    public static GenericService service;

    public void startUp(ConfigurationContext configctx, AxisService service){
        AxisConfiguration config = null;
        List<Phase> phases = null;
        config = service.getAxisConfiguration();
        phases = config.getInFlowPhases();

        initializeRepository(configctx);

        for (Iterator<Phase> iterator = phases.iterator(); iterator.hasNext();) {
            Phase phase = (Phase) iterator.next();
            if ("Security".equals(phase.getPhaseName())) {
                phase.addHandler(new MyProxySecurityHandler());
                phase.addHandler(new AmazonSecurityHandler());
                return;
            }
        }
    }

    private void initializeRepository(ConfigurationContext context) {
        Properties properties = new Properties();
        try {
            URL url = this.getClass().getClassLoader().getResource(REPOSITORY_PROPERTIES);
            properties.load(url.openStream());
            Map<String, String> map = new HashMap<String, String>((Map) properties);
            Class registryRepositoryFactory = Class.forName(map.get(JCR_CLASS));
            Constructor c = registryRepositoryFactory.getConstructor();
            RepositoryFactory repositoryFactory = (RepositoryFactory) c.newInstance();
            Repository repository = repositoryFactory.getRepository(map);
            Credentials credentials = new SimpleCredentials(map.get(JCR_USER), map.get(JCR_PASS).toCharArray());
            context.setProperty(CONFIGURATION_CONTEXT_REGISTRY, new JCRRegistry(repository, credentials));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void shutDown(ConfigurationContext configctx, AxisService service) {
    }
}
