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
package org.apache.airavata.orchestrator.core.impl;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataJobState;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacAPI;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.ApplicationContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.scheduler.HostScheduler;
import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.gfac.GFACInstance;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.registry.api.JobRequest;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xmlpull.v1.builder.XmlElement;
import xsul.wsif.impl.WSIFMessageElement;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is the simplest implementation for JobSubmitter,
 * This is calling gfac invocation methods to invoke the gfac embedded mode,so this does not really implement
 * the selectGFACInstance method
 */
public class EmbeddedGFACJobSubmitter implements JobSubmitter {
    private final static Logger logger = LoggerFactory.getLogger(EmbeddedGFACJobSubmitter.class);

    private OrchestratorContext orchestratorContext;

    public EmbeddedGFACJobSubmitter(OrchestratorContext orchestratorContext) {
        this.orchestratorContext = orchestratorContext;
    }

    public GFACInstance selectGFACInstance(OrchestratorContext context) {
        return null;
    }


    public boolean submitJob(GFACInstance gfac, List<String> experimentIDList) {

        for (int i = 0; i < experimentIDList.size(); i++) {
            try {
                // once its fetched it's status will changed to fetched state
                JobRequest jobRequest = orchestratorContext.getRegistry().fetchAcceptedJob(experimentIDList.get(i));
                String hostName = jobRequest.getHostDescription().getType().getHostAddress();
                String serviceName = jobRequest.getServiceDescription().getType().getName();
                //todo submit the jobs

                //after successfully submitting the jobs set the status of the job to submitted

                orchestratorContext.getRegistry().changeStatus(experimentIDList.get(i), AiravataJobState.State.SUBMITTED);
                AiravataAPI airavataAPI = orchestratorContext.getOrchestratorConfiguration().getAiravataAPI();
                HostDescription registeredHost = null;
                ServiceDescription serviceDescription = airavataAPI.getApplicationManager().getServiceDescription(serviceName);
                if (hostName == null) {
                    List<HostDescription> registeredHosts = new ArrayList<HostDescription>();
                    Map<String, ApplicationDescription> applicationDescriptors = airavataAPI.getApplicationManager().getApplicationDescriptors(serviceName);
                    for (String hostDescName : applicationDescriptors.keySet()) {
                        registeredHosts.add(airavataAPI.getApplicationManager().getHostDescription(hostDescName));
                    }
                    Class<? extends HostScheduler> aClass = Class.forName(ServerSettings.getHostScheduler()).asSubclass(HostScheduler.class);
                    HostScheduler hostScheduler = aClass.newInstance();
                    registeredHost = hostScheduler.schedule(registeredHosts);
                } else {
                    registeredHost = airavataAPI.getApplicationManager().getHostDescription(hostName);
                }
                ApplicationDescription applicationDescription =
                        airavataAPI.getApplicationManager().getApplicationDescription(serviceName, registeredHost.getType().getHostName());

                // When we run getInParameters we set the actualParameter object, this has to be fixed
                URL resource = EmbeddedGFACJobSubmitter.class.getClassLoader().getResource("gfac-config.xml");
                Properties configurationProperties = ServerSettings.getProperties();
                GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()), airavataAPI, configurationProperties);

                JobExecutionContext jobExecutionContext = new JobExecutionContext(gFacConfiguration, serviceName);
                //Here we get only the contextheader information sent specific for this node
                //Add security context



                jobExecutionContext.setProperty(Constants.PROP_TOPIC, experimentIDList.get(i));
                jobExecutionContext.setProperty(Constants.PROP_BROKER_URL, orchestratorContext.getOrchestratorConfiguration().getBrokerURL().toString());


                ApplicationContext applicationContext = new ApplicationContext();
                applicationContext.setApplicationDeploymentDescription(applicationDescription);
                applicationContext.setHostDescription(registeredHost);
                applicationContext.setServiceDescription(serviceDescription);

                jobExecutionContext.setApplicationContext(applicationContext);

                jobExecutionContext.setOutMessageContext(new MessageContext(jobRequest.getOutputParameters()));
                jobExecutionContext.setInMessageContext(new MessageContext(jobRequest.getInputParameters()));

                //addSecurityContext(registeredHost, configurationProperties, jobExecutionContext,
                  //      configuration.getContextHeader());
                GFacAPI gfacAPI1 = new GFacAPI();
                gfacAPI1.submitJob(jobExecutionContext);
            } catch (Exception e) {
                logger.error("Error getting job related information");
            }
        }
        return true;
    }
}
