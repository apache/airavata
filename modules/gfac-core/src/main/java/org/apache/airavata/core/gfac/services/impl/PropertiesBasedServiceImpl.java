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

package org.apache.airavata.core.gfac.services.impl;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.airavata.common.registry.api.impl.JCRRegistry;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultExecutionContext;
import org.apache.airavata.core.gfac.context.security.impl.GSISecurityContext;
import org.apache.airavata.core.gfac.context.security.impl.SSHSecurityContextImpl;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.ServiceException;
import org.apache.airavata.core.gfac.extension.DataServiceChain;
import org.apache.airavata.core.gfac.extension.ExitableChain;
import org.apache.airavata.core.gfac.extension.PostExecuteChain;
import org.apache.airavata.core.gfac.extension.PreExecuteChain;
import org.apache.airavata.core.gfac.scheduler.Scheduler;
import org.apache.airavata.core.gfac.utils.LogUtils;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.registry.api.impl.AiravataJCRRegistry;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This generic service implementation will load Registry service and Data Catalog from property file using (Apache
 * Commons-Configuration). It selects provider and execute it base on execution context.
 *
 */
public class PropertiesBasedServiceImpl extends AbstractSimpleService {

    private static Logger log = LoggerFactory.getLogger(PropertiesBasedServiceImpl.class);

    /*
     * default properties file location
     */
    private static final String DEFAULT_FILENAME = "service.properties";

    /*
     * context name
     */
    public static final String MYPROXY_SECURITY_CONTEXT = "myproxy";
    public static final String SSH_SECURITY_CONTEXT = "ssh";

    /*
     * Scheduler and chains
     */
    public static final String SCHEDULER_CLASS = "scheduler.class";
    public static final String DATA_CHAIN_CLASS = "datachain.classes";
    public static final String PRE_CHAIN_CLASS = "prechain.classes";
    public static final String POST_CHAIN_CLASS = "postchain.classes";

    /*
     * JCR properties
     */
    public static final String JCR_CLASS = "jcr.class";
    public static final String JCR_USER = "jcr.user";
    public static final String JCR_PASS = "jcr.pass";

    /*
     * SSH properties
     */
    public static final String SSH_PRIVATE_KEY = "ssh.key";
    public static final String SSH_PRIVATE_KEY_PASS = "ssh.keypass";
    public static final String SSH_USER_NAME = "ssh.username";

    /*
     * My proxy properties
     */
    public static final String MYPROXY_SERVER = "myproxy.server";
    public static final String MYPROXY_USER = "myproxy.user";
    public static final String MYPROXY_PASS = "myproxy.pass";
    public static final String MYPROXY_LIFE = "myproxy.life";

    private Scheduler scheduler;
    private PreExecuteChain[] preChain;
    private PostExecuteChain[] postChain;
    private DataServiceChain[] dataChain;
    private AiravataRegistry registryService;

    private String fileName = DEFAULT_FILENAME;
    private Configuration config;

    /**
     * Default constructor
     */
    public PropertiesBasedServiceImpl() {
        log.debug("Create Default PropertiesBasedServiceImpl");
    }

    /**
     * Constructor with passing file
     *
     * @param prop
     */
    public PropertiesBasedServiceImpl(String fileName) {
        this.fileName = fileName;
        log.debug("Create PropertiesBasedServiceImpl with Filename");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.airavata.core.gfac.services.GenericService#init(org.apache .airavata.core.gfac.context.
     * InvocationContext)
     */
    public void init() throws GfacException {
        try {

            /*
             * Load properties only it is not loaded
             */
            if (this.config == null || this.config.isEmpty()) {
                this.config = new PropertiesConfiguration(this.fileName);

                log.info("Properties loaded");
                LogUtils.displayProperties(log, getProperties());
            }
        } catch (ConfigurationException e) {
            throw new GfacException("Error initialize the PropertiesBasedServiceImpl", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.airavata.core.gfac.services.GenericService#dispose(org.apache .airavata.core.gfac.context.
     * InvocationContext)
     */
    public void dispose() throws GfacException {
    }

    @Override
    public void preProcess(InvocationContext context) throws ServiceException {
        /*
         * Check Gram header
         */
        if (context.getSecurityContext(MYPROXY_SECURITY_CONTEXT) == null) {
            String proxyServer = loadFromProperty(MYPROXY_SERVER, false);
            String proxyUser = loadFromProperty(MYPROXY_USER, false);
            String proxyPass = loadFromProperty(MYPROXY_PASS, false);
            String proxyTime = loadFromProperty(MYPROXY_LIFE, false);
            if (proxyServer != null && proxyUser != null && proxyPass != null) {
                GSISecurityContext gsi = new GSISecurityContext();
                gsi.setMyproxyServer(proxyServer);
                gsi.setMyproxyUserName(proxyUser);
                gsi.setMyproxyPasswd(proxyPass);
                if (proxyTime != null) {
                    gsi.setMyproxyLifetime(Integer.parseInt(proxyTime));
                }
                context.addSecurityContext(MYPROXY_SECURITY_CONTEXT, gsi);
            }
        }

        /*
         * Check SSH properties
         */
        if (context.getSecurityContext(SSH_SECURITY_CONTEXT) == null) {
            String key = loadFromProperty(SSH_PRIVATE_KEY, false);
            String pass = loadFromProperty(SSH_PRIVATE_KEY_PASS, false);
            String user = loadFromProperty(SSH_USER_NAME, false);
            if (key != null && user != null) {
                SSHSecurityContextImpl ssh = new SSHSecurityContextImpl();
                ssh.setKeyPass(pass);
                ssh.setPrivateKeyLoc(key);
                ssh.setUsername(user);
                context.addSecurityContext(SSH_SECURITY_CONTEXT, ssh);
            }
        }

        /*
         * Check registry
         */
        if (context.getExecutionContext() == null || context.getExecutionContext().getRegistryService() == null) {

            if (this.registryService == null) {
                log.info("try to create default registry service (JCR Implementation)");

                // JCR
                String jcrClass = loadFromProperty(JCR_CLASS, true);
                String userName = loadFromProperty(JCR_USER, false);
                String password = loadFromProperty(JCR_PASS, false);

                /*
                 * Remove unnecessary key
                 */
                Map<String, String> map = new HashMap<String, String>((Map) getProperties());
                map.remove(JCR_CLASS);
                map.remove(JCR_USER);
                map.remove(JCR_PASS);

                map.remove(SCHEDULER_CLASS);
                map.remove(DATA_CHAIN_CLASS);
                map.remove(PRE_CHAIN_CLASS);
                map.remove(POST_CHAIN_CLASS);

                map.remove(MYPROXY_SERVER);
                map.remove(MYPROXY_USER);
                map.remove(MYPROXY_PASS);
                map.remove(MYPROXY_LIFE);

                map.remove(SSH_USER_NAME);
                map.remove(SSH_PRIVATE_KEY);
                map.remove(SSH_PRIVATE_KEY_PASS);

                if (map.size() == 0)
                    map = null;

                try {
                    // TODO pass the url of the registry as the first parameter
                    this.registryService = new AiravataJCRRegistry(null, jcrClass, userName, password, map);
                } catch (javax.jcr.RepositoryException e) {
                    e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
                }

                log.info("Default registry service is created");
            }

            /*
             * If there is no specific registry service, use the default one.
             */
            ((DefaultExecutionContext) context.getExecutionContext()).setRegistryService(this.registryService);
        }
    }

    @Override
    public void postProcess(InvocationContext context) throws ServiceException {
        if(this.registryService != null)
        ((JCRRegistry)this.registryService).closeConnection();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.airavata.core.gfac.services.GenericService#getScheduler(org .apache.airavata.core.gfac.context
     * .InvocationContext)
     */
    public Scheduler getScheduler(InvocationContext context) throws ServiceException {
        String className = null;
        if (this.scheduler == null) {
            log.info("try to create scheduler");

            /*
             * get class names
             */
            className = loadFromProperty(SCHEDULER_CLASS, true);

            /*
             * init instance of that class
             */
            try {

                Class spiClass = Class.forName(className).asSubclass(Scheduler.class);

                this.scheduler = (Scheduler) spiClass.newInstance();

                log.info("Scheduler:" + className + " is loaded");

            } catch (ClassNotFoundException ex) {
                throw new ServiceException("Scheduler " + className + " not found", ex);
            } catch (Exception ex) {
                throw new ServiceException("Scheduler " + className + " could not be instantiated: " + ex, ex);
            }
        }
        return this.scheduler;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.airavata.core.gfac.services.GenericService#getPreExecutionSteps (org.ogce.gfac
     * .context.InvocationContext)
     */
    public PreExecuteChain[] getPreExecutionSteps(InvocationContext context) throws ServiceException {
        if (this.preChain == null) {
            log.info("try to load pre-execution chain");
            this.preChain = loadClassFromProperties(PRE_CHAIN_CLASS, PreExecuteChain.class);
        }
        return preChain;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.airavata.core.gfac.services.GenericService#getPostExecuteSteps (org.ogce.gfac
     * .context.InvocationContext)
     */
    public PostExecuteChain[] getPostExecuteSteps(InvocationContext context) throws ServiceException {
        if (this.postChain == null) {
            log.info("try to load post-execution chain");
            this.postChain = loadClassFromProperties(POST_CHAIN_CLASS, PostExecuteChain.class);
        }
        return postChain;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.airavata.core.gfac.services.impl.OGCEGenericService#getDataChains
     * (org.apache.airavata.core.gfac.context .InvocationContext)
     */
    public DataServiceChain[] getDataChains(InvocationContext context) throws ServiceException {
        if (this.dataChain == null) {
            log.info("try to load data chain");
            this.dataChain = loadClassFromProperties(DATA_CHAIN_CLASS, DataServiceChain.class);
        }
        return dataChain;
    }

    private Properties getProperties() {
        Properties prop = new Properties();
        for (Iterator iterator = this.config.getKeys(); iterator.hasNext();) {
            String key = (String) iterator.next();
            prop.put(key, this.config.getString(key));
        }
        return prop;
    }

    /**
     *
     * @param propertyName
     * @param required
     * @return
     * @throws GfacException
     */
    private String loadFromProperty(String propertyName, boolean required) throws ServiceException {
        String propValue = this.config.getString(propertyName);
        if (propValue == null) {
            if (required)
                throw new ServiceException("Property \"" + propertyName + "\" is not found");
            return null;
        }
        return propValue;
    }

    /**
	 *
	 */
    @SuppressWarnings("unchecked")
    private <T> T[] loadClassFromProperties(String propertyName, Class<? extends ExitableChain> type)
            throws ServiceException {

        // property is not set
        String propValue = loadFromProperty(propertyName, false);
        if (propValue == null) {
            return null;
        }

        /*
         * get class names
         */
        String classNames[] = this.config.getStringArray(propertyName);

        /*
         * init instance of that class
         */
        T[] chain = (T[]) Array.newInstance(type, classNames.length);
        for (int i = 0; i < classNames.length; i++) {

            String className = classNames[i].trim();

            try {
                Class<? extends ExitableChain> spiClass;
                spiClass = Class.forName(className).asSubclass(ExitableChain.class);
                chain[i] = (T) spiClass.newInstance();

                log.info(type.getName() + " : " + className + " is loaded");

            } catch (ClassNotFoundException ex) {
                throw new ServiceException("Cannot find the class: " + className, ex);
            } catch (IllegalAccessException ex) {
                throw new ServiceException("Cannot access the class: " + className, ex);
            } catch (InstantiationException ex) {
                throw new ServiceException("Cannot init the class: " + className, ex);
            }
        }
        return chain;
    }
}
