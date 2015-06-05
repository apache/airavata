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

package org.apache.airavata.gfac.core;

import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.provider.GFacProvider;
import org.apache.airavata.gfac.core.provider.GFacProviderConfig;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;


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
        jobExecutionContext.getGFacConfiguration().setInHandlers(jobExecutionContext.getProvider().getClass().getName(),
                jobExecutionContext.getApplicationName());
        jobExecutionContext.getGFacConfiguration().setOutHandlers(jobExecutionContext.getProvider().getClass().getName(),
        		 jobExecutionContext.getApplicationName());
        jobExecutionContext.getGFacConfiguration().setExecutionMode(getExecutionMode(jobExecutionContext));
    }

    /**
     * Figure out which provider to use based on application configuration.
     * @param jobExecutionContext containing all the required configurations.
     * @return GFacProvider instance.
     */
    private static GFacProvider getProvider(JobExecutionContext jobExecutionContext) throws GFacException {
        String applicationName = jobExecutionContext.getApplicationName();

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

                List<JobSubmissionInterface> jobSubmissionInterfaces = jobExecutionContext.getApplicationContext().getComputeResourceDescription().getJobSubmissionInterfaces();
                JobSubmissionProtocol jobSubmissionProtocol = jobExecutionContext.getPreferredJobSubmissionProtocol();
                SSHJobSubmission sshJobSubmission;
                LOCALSubmission localSubmission;
                UnicoreJobSubmission unicoreSubmission;
                String securityProtocol = null;
                try {
                    AppCatalog appCatalog = jobExecutionContext.getAppCatalog();
                    if (jobSubmissionProtocol == JobSubmissionProtocol.SSH) {
                        sshJobSubmission = appCatalog.getComputeResource().getSSHJobSubmission(
                                jobExecutionContext.getPreferredJobSubmissionInterface().getJobSubmissionInterfaceId());
                        if (sshJobSubmission != null) {
                            securityProtocol  = sshJobSubmission.getSecurityProtocol().toString();
                        }
                    }else if (jobSubmissionProtocol == JobSubmissionProtocol.LOCAL) {
                        localSubmission = appCatalog.getComputeResource().getLocalJobSubmission(jobExecutionContext.getPreferredJobSubmissionInterface().getJobSubmissionInterfaceId());
                    }
                    else if (jobSubmissionProtocol == JobSubmissionProtocol.UNICORE) {
                    	unicoreSubmission = appCatalog.getComputeResource().getUNICOREJobSubmission(jobExecutionContext.getPreferredJobSubmissionInterface().getJobSubmissionInterfaceId());
                    	securityProtocol = unicoreSubmission.getSecurityProtocol().toString(); 
                    }
                    List<Element> elements = GFacUtils.getElementList(GFacConfiguration.getHandlerDoc(), Constants.XPATH_EXPR_PROVIDER_ON_SUBMISSION + jobSubmissionProtocol + "']");
                    for (Element element : elements) {
                        String security = element.getAttribute(Constants.GFAC_CONFIG_SECURITY_ATTRIBUTE);
                        if (security.equals("")) {
                            providerClassName = element.getAttribute(Constants.GFAC_CONFIG_CLASS_ATTRIBUTE);
                        }else if (securityProtocol != null && securityProtocol.equals(security)) {
                            providerClassName = element.getAttribute(Constants.GFAC_CONFIG_CLASS_ATTRIBUTE);
                        }
                    }
                    if (providerClassName == null) {
                        throw new GFacException("Couldn't find provider class");
                    }

                    Class<? extends GFacProvider> aClass1 = Class.forName(providerClassName).asSubclass(GFacProvider.class);
                    provider = aClass1.newInstance();
                    //loading the provider properties
                    aClass = GFacConfiguration.getProviderConfig(GFacConfiguration.getHandlerDoc(), Constants.XPATH_EXPR_PROVIDER_HANDLERS_START +
                            providerClassName + "']", Constants.GFAC_CONFIG_APPLICATION_NAME_ATTRIBUTE);
                    if (!aClass.isEmpty()) {
                        provider.initProperties(aClass.get(0).getProperties());
                    }
                } catch (AppCatalogException e) {
                    throw new GFacException("Couldn't retrieve job submission protocol from app catalog ");
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
            throw new GFacException("Error initializing application specific Handler: " +providerClassName , e);
        } catch (InstantiationException e) {
            log.error("Error initializing application specific Handler: " + providerClassName);
            throw new GFacException("Error initializing Handler", e);
        } catch (IllegalAccessException e) {
            log.error("Error initializing application specific Handler: " + providerClassName);
            throw new GFacException("Error initializing Handler", e);
        }
        return provider;
    }
    public static ExecutionMode getExecutionMode(JobExecutionContext jobExecutionContext)throws GFacException{
        String applicationName = jobExecutionContext.getApplicationContext().getApplicationInterfaceDescription().getApplicationName();
        URL resource = Scheduler.class.getClassLoader().getResource(org.apache.airavata.common.utils.Constants.GFAC_CONFIG_XML);
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        Document handlerDoc = null;
        String jobSubmissionProtocol = jobExecutionContext.getPreferredJobSubmissionProtocol().toString();
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
        String executionMode = "sync";
        try {
            executionMode = GFacConfiguration.getAttributeValue(handlerDoc,
                    Constants.XPATH_EXPR_APPLICATION_HANDLERS_START + applicationName + "']", Constants.GFAC_CONFIG_EXECUTION_MODE_ATTRIBUTE);
            // This should be have a single element only.

            if (executionMode == null || "".equals(executionMode)) {
                String hostClass = jobExecutionContext.getPreferredJobSubmissionProtocol().toString();
                executionMode = GFacConfiguration.getAttributeValue(GFacConfiguration.getHandlerDoc(), Constants.XPATH_EXPR_PROVIDER_ON_HOST + hostClass + "']", Constants.GFAC_CONFIG_EXECUTION_MODE_ATTRIBUTE);
            }

            if (executionMode == null || "".equals(executionMode)) {
                List<Element> elements = GFacUtils.getElementList(GFacConfiguration.getHandlerDoc(), Constants.XPATH_EXPR_PROVIDER_ON_SUBMISSION + jobSubmissionProtocol + "']");
                for (Element element : elements) {
                    executionMode = element.getAttribute(Constants.GFAC_CONFIG_EXECUTION_MODE_ATTRIBUTE);
                }
            }

        } catch (XPathExpressionException e) {
            log.error("Error evaluating XPath expression");  //To change body of catch statement use File | Settings | File Templates.
            throw new GFacException("Error evaluating XPath expression", e);
        }

        return ExecutionMode.fromString(executionMode);
    }

//    private static HostDescription scheduleHost(List<HostDescription> registeredHosts) {
//        //todo implement an algorithm to pick a host among different hosts, ideally this could be configurable in an xml
//        return registeredHosts.get(0);
//    }
}
