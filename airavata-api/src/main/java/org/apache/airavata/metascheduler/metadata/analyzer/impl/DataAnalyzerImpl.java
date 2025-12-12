/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.metascheduler.metadata.analyzer.impl;

import java.util.Map;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.metascheduler.core.engine.DataAnalyzer;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.service.registry.RegistryService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DataAnalyzerImpl implements DataAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAnalyzerImpl.class);
    private static ApplicationContext applicationContext;

    private final AiravataServerProperties properties;
    private final ApplicationContext applicationContextInstance;

    public DataAnalyzerImpl(AiravataServerProperties properties, ApplicationContext applicationContext) {
        this.properties = properties;
        this.applicationContextInstance = applicationContext;
        DataAnalyzerImpl.applicationContext = applicationContext;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            LOGGER.debug("Executing Data Analyzer ....... ");
            RegistryService registryService = applicationContextInstance.getBean(RegistryService.class);

            // TODO: handle multiple gateways
            String gateway = properties.services.parser.enabledGateways;

            JobState state = JobState.SUBMITTED;
            JobStatus jobStatus = new JobStatus();
            jobStatus.setJobState(state);
            double time = properties.services.parser.timeStepSeconds;

            int fiveMinuteCount = registryService.getJobCount(jobStatus, gateway, 5);

            int tenMinuteCount = registryService.getJobCount(jobStatus, gateway, 10);

            int fifteenMinuteCount = registryService.getJobCount(jobStatus, gateway, 15);

            double fiveMinuteAverage = fiveMinuteCount * time / (5 * 60);

            double tenMinuteAverage = tenMinuteCount * time / (10 * 60);

            double fifteenMinuteAverage = fifteenMinuteCount * time / (10 * 60);

            LOGGER.info("service rate: 5 min avg " + fiveMinuteAverage + " 10 min avg " + tenMinuteAverage
                    + " 15 min avg " + fifteenMinuteAverage);

            Map<String, Double> timeDistribution = registryService.getAVGTimeDistribution(gateway, 15);

            String msg = "";
            for (Map.Entry<String, Double> entry : timeDistribution.entrySet()) {
                msg = msg + " avg time " + entry.getKey() + "  : " + entry.getValue();
            }
            LOGGER.info(msg);

        } catch (Exception ex) {
            String msg = "Error occurred while executing data analyzer" + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new JobExecutionException(msg, ex);
        }
    }
}
