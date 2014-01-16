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


import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.common.utils.AiravataJobState;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacAPI;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.context.ApplicationContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.scheduler.HostScheduler;
import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.gfac.GFACInstance;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.orchestrator.core.utils.OrchestratorUtils;
import org.apache.airavata.registry.api.JobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the simplest implementation for JobSubmitter,
 * This is calling gfac invocation methods to invoke the gfac embedded mode,so this does not really implement
 * the selectGFACInstance method
 */
public class EmbeddedGFACJobSubmitter implements JobSubmitter {
    private final static Logger logger = LoggerFactory.getLogger(EmbeddedGFACJobSubmitter.class);

    private OrchestratorContext orchestratorContext;


    public void initialize(OrchestratorContext orchestratorContext) throws OrchestratorException {
        this.orchestratorContext = orchestratorContext;
    }

    public GFACInstance selectGFACInstance() throws OrchestratorException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public boolean submitJob(GFACInstance gfac, List<String> experimentIDList) throws OrchestratorException {

        for (int i = 0; i < experimentIDList.size(); i++) {
            try {
                // once its fetched it's status will changed to fetched state
                JobRequest jobRequest = orchestratorContext.getRegistry().fetchAcceptedJob(experimentIDList.get(i));
                launchGfacWithJobRequest(jobRequest);
            } catch (Exception e) {
                logger.error("Error getting job related information");
            }
        }
        return true;
    }

    private void launchGfacWithJobRequest(JobRequest jobRequest) throws OrchestratorException {
        String serviceName = jobRequest.getServiceName();
        if(serviceName == null){
            serviceName = jobRequest.getServiceDescription().getType().getName();
        }


        //todo submit the jobs

        //after successfully submitting the jobs set the status of the job to submitted
        String experimentID = OrchestratorUtils.getUniqueID(jobRequest);
        AiravataAPI airavataAPI = null;
        try {

            airavataAPI = orchestratorContext.getOrchestratorConfiguration().getAiravataAPI();
            HostDescription hostDescription = jobRequest.getHostDescription();
            if (hostDescription == null) {
                List<HostDescription> registeredHosts = new ArrayList<HostDescription>();
                Map<String, ApplicationDescription> applicationDescriptors = airavataAPI.getApplicationManager().getApplicationDescriptors(serviceName);
                for (String hostDescName : applicationDescriptors.keySet()) {
                    registeredHosts.add(airavataAPI.getApplicationManager().getHostDescription(hostDescName));
                }
                Class<? extends HostScheduler> aClass = Class.forName(ServerSettings.getHostScheduler()).asSubclass(HostScheduler.class);
                HostScheduler hostScheduler = aClass.newInstance();
                hostDescription = hostScheduler.schedule(registeredHosts);
            }

            ServiceDescription serviceDescription = jobRequest.getServiceDescription();
            if (serviceDescription == null) {
                try {
                    serviceDescription = airavataAPI.getApplicationManager().getServiceDescription(jobRequest.getServiceName());
                } catch (AiravataAPIInvocationException e) {
                    String error = "Error retrieving document from Registry: " + jobRequest.getServiceName();
                    throw new OrchestratorException(error, e);
                }
            }

            ApplicationDescription applicationDescription = jobRequest.getApplicationDescription();
            if (applicationDescription == null) {
                 applicationDescription = airavataAPI.getApplicationManager().getApplicationDescription(serviceName, hostDescription.getType().getHostName());
            }
            // When we run getInParameters we set the actualParameter object, this has to be fixed
            URL resource = EmbeddedGFACJobSubmitter.class.getClassLoader().getResource("gfac-config.xml");
            Properties configurationProperties = ServerSettings.getProperties();
            GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()), airavataAPI, configurationProperties);

            JobExecutionContext jobExecutionContext = new JobExecutionContext(gFacConfiguration, serviceName);
            //Here we get only the contextheader information sent specific for this node
            //Add security context


            ApplicationContext applicationContext = new ApplicationContext();
            applicationContext.setApplicationDeploymentDescription(applicationDescription);
            applicationContext.setHostDescription(hostDescription);
            applicationContext.setServiceDescription(serviceDescription);

            jobExecutionContext.setApplicationContext(applicationContext);

            jobExecutionContext.setOutMessageContext(new MessageContext(jobRequest.getOutputParameters()));
            jobExecutionContext.setInMessageContext(new MessageContext(jobRequest.getInputParameters()));

            jobExecutionContext.setProperty(Constants.PROP_TOPIC, experimentID);
            jobExecutionContext.setExperimentID(experimentID);
            GFacAPI gfacAPI1 = new GFacAPI();
            gfacAPI1.submitJob(jobExecutionContext);
            orchestratorContext.getRegistry().changeStatus(experimentID, AiravataJobState.State.SUBMITTED);
        } catch (Exception e) {
            throw new OrchestratorException("Error launching the Job", e);
        }
    }

    public boolean directJobSubmit(JobRequest request) throws OrchestratorException {
        try {
            launchGfacWithJobRequest(request);
        } catch (Exception e) {
            String error = "Error launching the job : " + OrchestratorUtils.getUniqueID(request);
            logger.error(error);
            throw new OrchestratorException(error);
        }
        return true;
    }
}
