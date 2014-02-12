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
package org.apache.airavata.gfac.cpi;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.ApplicationContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.scheduler.HostScheduler;
import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.model.experiment.ConfigurationData;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.cpi.DataType;
import org.apache.airavata.registry.cpi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * This is the GFac CPI class for external usage, this simply have a single method to submit a job to
 * the resource, required data for the job has to be stored in registry prior to invoke this object.
 */
public class GFac {
    private static final Logger log = LoggerFactory.getLogger(GFac.class);

    private Registry registry;

    private AiravataAPI airavataAPI;

    private AiravataRegistry2 airavataRegistry2;

    /**
     * Constructor for GFac
     * @param registry
     * @param airavataAPI
     * @param airavataRegistry2
     */
    public GFac(Registry registry, AiravataAPI airavataAPI, AiravataRegistry2 airavataRegistry2) {
        this.registry = registry;
        this.airavataAPI = airavataAPI;
        this.airavataRegistry2 = airavataRegistry2;
    }

    /**
     * This is the job launching method outsiders of GFac can use, this will invoke the GFac handler chain and providers
     * And update the registry occordingly, so the users can query the database to retrieve status and output from Registry
     *
     * @param experimentID
     * @return
     * @throws GFacException
     */
    public boolean submitJob(String experimentID) throws GFacException {
        ConfigurationData configurationData = (ConfigurationData) registry.get(DataType.EXPERIMENT_CONFIGURATION_DATA, experimentID);
        String serviceName = configurationData.getApplicationId();

        if (serviceName == null) {
            throw new GFacException("Error executing the job because there is not Application Name in this Experiment");
        }
        try {
            List<HostDescription> registeredHosts = new ArrayList<HostDescription>();
            Map<String, ApplicationDescription> applicationDescriptors = airavataRegistry2.getApplicationDescriptors(serviceName);
            for (String hostDescName : applicationDescriptors.keySet()) {
                registeredHosts.add(airavataRegistry2.getHostDescriptor(hostDescName));
            }
            Class<? extends HostScheduler> aClass = Class.forName(ServerSettings.getHostScheduler()).asSubclass(HostScheduler.class);
            HostScheduler hostScheduler = aClass.newInstance();
            HostDescription hostDescription = hostScheduler.schedule(registeredHosts);

            ServiceDescription serviceDescription = airavataRegistry2.getServiceDescriptor(serviceName);

            ApplicationDescription applicationDescription = airavataRegistry2.getApplicationDescriptors(serviceName, hostDescription.getType().getHostName());
            URL resource = GFac.class.getClassLoader().getResource("gfac-config.xml");
            Properties configurationProperties = ServerSettings.getProperties();
            GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()), airavataAPI, configurationProperties);

            JobExecutionContext jobExecutionContext = new JobExecutionContext(gFacConfiguration, serviceName);

            ApplicationContext applicationContext = new ApplicationContext();
            applicationContext.setApplicationDeploymentDescription(applicationDescription);
            applicationContext.setHostDescription(hostDescription);
            applicationContext.setServiceDescription(serviceDescription);
            jobExecutionContext.setApplicationContext(applicationContext);


            Map<String, String> experimentInputs = configurationData.getExperimentInputs();
            jobExecutionContext.setInMessageContext(new MessageContext(GFacUtils.getMessageContext(experimentInputs,
                    serviceDescription.getType().getInputParametersArray())));

            HashMap<String, Object> outputData = new HashMap<String, Object>();
            jobExecutionContext.setOutMessageContext(new MessageContext(outputData));

            jobExecutionContext.setProperty(Constants.PROP_TOPIC, experimentID);
            jobExecutionContext.setExperimentID(experimentID);


            GFacAPI gfacAPI1 = new GFacAPI();
            gfacAPI1.submitJob(jobExecutionContext);
        } catch (Exception e) {
            log.error("Error inovoking the job with experiment ID: " + experimentID);
            throw new GFacException(e);
        }
        return true;
    }
}
