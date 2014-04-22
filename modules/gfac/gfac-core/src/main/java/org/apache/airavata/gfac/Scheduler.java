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

package org.apache.airavata.gfac;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.GFacProviderConfig;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * Scheduler decides the execution order of handlers based on application description. In addition
 * to that scheduler decides which provider to invoke at the end. Scheduler will set
 * provider instance and in/out handler chains in JobExecutionContext.
 */
public class Scheduler {
    private static Logger log = LoggerFactory.getLogger(Scheduler.class);

    /**
     * Decide which provider to use and execution sequence of handlers based on job request and
     * job configuration.
     * @param jobExecutionContext containing job request as well as all the configurations.
     */
    public static void schedule(JobExecutionContext jobExecutionContext) throws GFacException{
        // Current implementation only support static handler sequence.
        jobExecutionContext.setProvider(getProvider(jobExecutionContext));
        // TODO: Selecting the provider based on application description.
    }

    /**
     * Figure out which provider to use based on application configuration.
     * @param jobExecutionContext containing all the required configurations.
     * @return GFacProvider instance.
     */
    private static GFacProvider getProvider(JobExecutionContext jobExecutionContext) throws GFacException {
        HostDescription hostDescription = jobExecutionContext.getApplicationContext().getHostDescription();
        String applicationName = jobExecutionContext.getServiceName();

        URL resource = Scheduler.class.getClassLoader().getResource(org.apache.airavata.common.utils.Constants.GFAC_CONFIG_XML);
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        Document handlerDoc = null;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            handlerDoc = docBuilder.parse(new File(resource.getPath()));
        } catch (ParserConfigurationException e) {
            throw new GFacException(e);
        } catch (SAXException e) {
            throw new GFacException(e);
        } catch (IOException e) {
            throw new GFacException(e);
        }
        GFacProviderConfig s = null;
        GFacProvider provider = null;
        List<GFacProviderConfig> aClass = null;
        String providerClassName = null;
        try {
            aClass = GFacConfiguration.getProviderConfig(handlerDoc,
                    Constants.XPATH_EXPR_APPLICATION_HANDLERS_START + applicationName + "']", Constants.GFAC_CONFIG_APPLICATION_NAME_ATTRIBUTE);
            // This should be have a single element only.
            if (aClass != null && !aClass.isEmpty()) {
                s = aClass.get(0);
                Class<? extends GFacProvider> aClass1 = Class.forName(s.getClassName()).asSubclass(GFacProvider.class);
                provider = aClass1.newInstance();
                //loading the provider properties
                if(!aClass.isEmpty()){
                    provider.initProperties(aClass.get(0).getProperties());
                }
            }
            // We give higher preference to applications specific provider if configured
            if (provider == null) {
                String hostClass = hostDescription.getType().getClass().getName();
                providerClassName = GFacConfiguration.getProviderClassName(GFacConfiguration.getHandlerDoc(), Constants.XPATH_EXPR_PROVIDER_ON_HOST + hostClass + "']", Constants.GFAC_CONFIG_CLASS_ATTRIBUTE);
                Class<? extends GFacProvider> aClass1 = Class.forName(providerClassName).asSubclass(GFacProvider.class);
                provider = aClass1.newInstance();
                //loading the provider properties
                aClass = GFacConfiguration.getProviderConfig(GFacConfiguration.getHandlerDoc(), Constants.XPATH_EXPR_PROVIDER_HANDLERS_START +
                        providerClassName + "']", Constants.GFAC_CONFIG_APPLICATION_NAME_ATTRIBUTE);
                if(!aClass.isEmpty()){
                    provider.initProperties(aClass.get(0).getProperties());
                }
            }
        } catch (XPathExpressionException e) {
            log.error("Error evaluating XPath expression");  //To change body of catch statement use File | Settings | File Templates.
            throw new GFacException("Error evaluating XPath expression", e);
        } catch (GFacProviderException e) {
            log.error("Error During scheduling");  //To change body of catch statement use File | Settings | File Templates.
            throw new GFacException("Error During scheduling", e);
        }catch (ClassNotFoundException e) {
            log.error("Application Provider class: " + s + "couldn't find");
            throw new GFacException("Error initializing application specific Handler", e);
        } catch (InstantiationException e) {
            log.error("Error initializing application specific Handler");
            throw new GFacException("Error initializing application specific Handler", e);
        } catch (IllegalAccessException e) {
            log.error("Error initializing application specific Handler");
            throw new GFacException("Error initializing application specific Handler", e);
        }
        return provider;
    }

    public static HostDescription pickaHost(AiravataAPI api, String serviceName) throws AiravataAPIInvocationException {
        List<HostDescription> registeredHosts = new ArrayList<HostDescription>();
        Map<String, ApplicationDescription> applicationDescriptors = api.getApplicationManager().getApplicationDescriptors(serviceName);
        for (String hostDescName : applicationDescriptors.keySet()) {
            registeredHosts.add(api.getApplicationManager().getHostDescription(hostDescName));
        }
        return scheduleHost(registeredHosts);
    }

    private static HostDescription scheduleHost(List<HostDescription> registeredHosts) {
        //todo implement an algorithm to pick a host among different hosts, ideally this could be configurable in an xml
        return registeredHosts.get(0);
    }
}
