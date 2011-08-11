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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.airavata.core.gfac.api.Registry;
import org.apache.airavata.core.gfac.api.impl.JCRRegistry;
import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.GfacException.FaultCode;
import org.apache.airavata.core.gfac.extension.DataServiceChain;
import org.apache.airavata.core.gfac.extension.ExitableChain;
import org.apache.airavata.core.gfac.extension.PostExecuteChain;
import org.apache.airavata.core.gfac.extension.PreExecuteChain;
import org.apache.airavata.core.gfac.scheduler.Scheduler;

/**
 * This generic service implementation will load Registry service and Data Catalog from property file. It selects
 * provider and execute it base on execution context.
 * 
 */
public class PropertiesBasedServiceImpl extends AbstractSimpleService {

    private static final String FILENAME = "service.properties";
    public static final String SCHEDULER_CLASS = "scheduler.class";
    public static final String DATA_CHAIN_CLASS = "datachain.classes";
    public static final String PRE_CHAIN_CLASS = "prechain.classes";
    public static final String POST_CHAIN_CLASS = "postchain.classes";
    
    /*
     * JCR properties
     */
    public static final String JCR_CLASS = "jcr.class";

    private Properties properties;
    private Scheduler scheduler;
    private PreExecuteChain[] preChain;
    private PostExecuteChain[] postChain;
    private DataServiceChain[] dataChain;

    private Registry registryService;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.airavata.core.gfac.services.GenericService#init(org.apache.airavata.core.gfac.context.
     * InvocationContext)
     */
    public void init() throws GfacException {
        /*
         * Load properties and create XRegistry service
         */
        try {
            URL url = ClassLoader.getSystemResource(FILENAME);

            this.properties = new Properties();
            this.properties.load(url.openStream());
            
            //JCR
            String jcrClass = loadFromProperty(JCR_CLASS, true);
            
            /*
             * Remove unnecessary key
             */
            Map<String, String> map = new HashMap<String, String>((Map) this.properties);
            map.remove(JCR_CLASS);
            map.remove(SCHEDULER_CLASS);
            map.remove(DATA_CHAIN_CLASS);
            map.remove(PRE_CHAIN_CLASS);
            map.remove(POST_CHAIN_CLASS);
            if(map.size() == 0)
            	map = null;

            this.registryService = new JCRRegistry(jcrClass, "admin", "admin", map);

        } catch (Exception e) {
        	e.printStackTrace();
            throw new GfacException("Error initialize the generic service", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.airavata.core.gfac.services.GenericService#dispose(org.apache.airavata.core.gfac.context.
     * InvocationContext)
     */
    public void dispose() throws GfacException {
        // TODO Auto-generated method stub

    }

    @Override
    public void preProcess(InvocationContext context) throws GfacException {
        // set Fix Registry location for every requests
        context.getExecutionContext().setRegistryService(this.registryService);
    }

    @Override
    public void postProcess(InvocationContext context) throws GfacException {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.airavata.core.gfac.services.GenericService#getScheduler(org.apache.airavata.core.gfac.context
     * .InvocationContext)
     */
    public Scheduler getScheduler(InvocationContext context) throws GfacException {
        String className = null;
        if (this.scheduler == null) {
            /*
             * get class names
             */
            className = loadFromProperty(SCHEDULER_CLASS, true);

            /*
             * init instance of that class
             */
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Class<? extends Scheduler> spiClass;

                if (classLoader == null) {
                    spiClass = Class.forName(className).asSubclass(Scheduler.class);
                } else {
                    spiClass = classLoader.loadClass(className).asSubclass(Scheduler.class);
                }

                this.scheduler = spiClass.newInstance();

            } catch (ClassNotFoundException ex) {
                throw new GfacException("Scheduler " + className + " not found", ex);
            } catch (Exception ex) {
                throw new GfacException("Scheduler " + className + " could not be instantiated: " + ex, ex);
            }
        }
        return this.scheduler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.airavata.core.gfac.services.GenericService#getPreExecutionSteps(org.ogce.gfac
     * .context.InvocationContext)
     */
    public PreExecuteChain[] getPreExecutionSteps(InvocationContext context) throws GfacException {
        if (this.preChain == null) {
            this.preChain = loadClassFromProperties(PRE_CHAIN_CLASS, PreExecuteChain.class);
        }
        return preChain;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.airavata.core.gfac.services.GenericService#getPostExecuteSteps(org.ogce.gfac
     * .context.InvocationContext)
     */
    public PostExecuteChain[] getPostExecuteSteps(InvocationContext context) throws GfacException {
        if (this.postChain == null) {
            this.postChain = loadClassFromProperties(POST_CHAIN_CLASS, PostExecuteChain.class);
        }
        return postChain;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.airavata.core.gfac.services.impl.OGCEGenericService#getDataChains(org.apache.airavata.core.gfac.context
     * .InvocationContext)
     */
    public DataServiceChain[] getDataChains(InvocationContext context) throws GfacException {
        if (this.dataChain == null) {
            this.dataChain = loadClassFromProperties(DATA_CHAIN_CLASS, DataServiceChain.class);
        }
        return dataChain;
    }

    /**
     * 
     * @param propertyName
     * @param required
     * @return
     * @throws GfacException
     */
    private String loadFromProperty(String propertyName, boolean required) throws GfacException {
        String propValue = this.properties.getProperty(propertyName);
        if (propValue == null) {
            if (required)
                throw new GfacException("Property \"" + propertyName + "\" is not found", FaultCode.InvalidConfig);
            return null;
        }
        return propValue;
    }

    /**
	 * 
	 */
    @SuppressWarnings("unchecked")
    private <T> T[] loadClassFromProperties(String propertyName, Class<? extends ExitableChain> type)
            throws GfacException {

        // property is not set
        String propValue = loadFromProperty(propertyName, false);
        if (propValue == null) {
            return null;
        }

        /*
         * get class names
         */
        String classNames[] = this.properties.getProperty(propertyName).split(",");

        /*
         * init instance of that class
         */
        T[] chain = (T[]) Array.newInstance(type, classNames.length);
        for (int i = 0; i < classNames.length; i++) {

            String className = classNames[i];
            System.out.println(className);

            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Class<? extends ExitableChain> spiClass;

                if (classLoader == null) {
                    spiClass = Class.forName(className).asSubclass(ExitableChain.class);
                } else {
                    spiClass = classLoader.loadClass(className).asSubclass(ExitableChain.class);
                }

                chain[i] = (T) spiClass.newInstance();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                // TODO proper throw out
            } catch (Exception ex) {
                ex.printStackTrace();
                // TODO proper throw out
            }
        }
        return chain;
    }
}
