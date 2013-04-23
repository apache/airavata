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

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.handler.GFacHandlerConfig;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.GFacProviderConfig;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.gfac.provider.impl.BESProvider;
import org.apache.airavata.gfac.provider.impl.EC2Provider;
import org.apache.airavata.gfac.provider.impl.GramProvider;
import org.apache.airavata.gfac.provider.impl.LocalProvider;
import org.apache.airavata.gfac.provider.impl.SSHProvider;
import org.apache.airavata.schemas.gfac.Ec2HostType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.UnicoreHostType;
import org.apache.airavata.schemas.gfac.SSHHostType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.xpath.XPathExpressionException;
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
    }

    /**
     * Figure out which provider to use based on application configuration.
     * @param jobExecutionContext containing all the required configurations.
     * @return GFacProvider instance.
     */
    private static GFacProvider getProvider(JobExecutionContext jobExecutionContext) throws GFacException{
        HostDescription hostDescription = jobExecutionContext.getApplicationContext().getHostDescription();
        String applicationName = jobExecutionContext.getApplicationContext().
                getApplicationDeploymentDescription().getType().getApplicationName().getStringValue();
        GFacProviderConfig s = null;
        GFacProvider provider = null;
        List<GFacProviderConfig> aClass = null;
        try {
             aClass = GFacConfiguration.getProviderConfig(GFacConfiguration.getHandlerDoc(),
                    Constants.XPATH_EXPR_APPLICATION_HANDLERS_START + applicationName + "']", Constants.GFAC_CONFIG_APPLICATION_NAME_ATTRIBUTE);
            // This should be have a single element only.
            if (!aClass.isEmpty()) {
                s = aClass.get(0);
                Class<? extends GFacProvider> aClass1 = Class.forName(s.getClassName()).asSubclass(GFacProvider.class);
                provider = aClass1.newInstance();
            }
        } catch (XPathExpressionException e) {
           log.error("Error configuring gfac-config.xml for application specific configuration");
            throw new GFacException("Error configuring gfac-config.xml for application specific configuration", e);
        } catch (ClassNotFoundException e) {
            log.error("Application Provider class: " + s + "couldn't find");
            throw new GFacException("Error initializing application specific Handler", e);
        } catch (InstantiationException e) {
            log.error("Error initializing application specific Handler");
            throw new GFacException("Error initializing application specific Handler", e);
        } catch (IllegalAccessException e) {
            log.error("Error initializing application specific Handler");
            throw new GFacException("Error initializing application specific Handler", e);
        }
        if(hostDescription.getType() instanceof GlobusHostType){
            provider = new GramProvider();
        }
        else if (hostDescription.getType() instanceof UnicoreHostType) {
        	provider = new BESProvider();
        }
        else if (hostDescription.getType() instanceof Ec2HostType) {
            provider = new EC2Provider();
        }
        else if (hostDescription.getType() instanceof SSHHostType) {
            provider = new SSHProvider();
        }
        else {
            provider = new LocalProvider();
        }
        String providerName = provider.getClass().getName();
        try {
            aClass = GFacConfiguration.getProviderConfig(GFacConfiguration.getHandlerDoc(), Constants.XPATH_EXPR_PROVIDER_HANDLERS_START +
                    providerName + "']", Constants.GFAC_CONFIG_APPLICATION_NAME_ATTRIBUTE);
            if(!aClass.isEmpty()){
                provider.initProperties(aClass.get(0).getProperties());
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (GFacProviderException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return provider;

    }


}
