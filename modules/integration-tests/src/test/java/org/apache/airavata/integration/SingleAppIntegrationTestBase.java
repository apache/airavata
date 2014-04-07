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
package org.apache.airavata.integration;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.api.error.AiravataClientException;
import org.apache.airavata.api.error.AiravataSystemException;
import org.apache.airavata.api.error.ExperimentNotFoundException;
import org.apache.airavata.api.error.InvalidRequestException;
import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.airavata.common.utils.ClientSettings;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.model.workspace.experiment.JobStatus;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this class contains the common utils across the single application integration tests
 */
public class SingleAppIntegrationTestBase {

    private final static Logger log = LoggerFactory.getLogger(SingleAppIntegrationTestBase.class);
    private static String THRIFT_SERVER_HOST;
    private static int THRIFT_SERVER_PORT;
    protected AiravataAPI airavataAPI;
    protected Airavata.Client client;
    private final int TRIES = 5;
    private final int TIME_OUT = 10000;

    //initializes the server
    protected void init() {

        try {
            THRIFT_SERVER_HOST = ClientSettings.getSetting("thrift.server.host");
            THRIFT_SERVER_PORT = Integer.parseInt(ClientSettings.getSetting("thrift.server.port"));

            //check the server startup + initialize the thrift client
            initClient();

            //getting the Airavata API ( to add the descriptors
            this.airavataAPI = getAiravataAPI();
        } catch (IOException e) {
            log.error("Error loading client-properties ..." + e.getMessage());
        } catch (AiravataAPIInvocationException e) {
            log.error("Error initializing the Airavata API ... " + e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    protected AiravataAPI getAiravataAPI() throws AiravataAPIInvocationException {
        if (airavataAPI == null) {
            airavataAPI = AiravataAPIFactory.getAPI("default", "admin");
        }
        return airavataAPI;
    }

    /*
    * Check if the thrift server has started.
    * If so, initialize the client
    * */
    protected void initClient() throws Exception {
        int tries = 0;

        while (true) {

            if (tries == TRIES) {
                log("Server not responding. Cannot continue with integration tests ...");
                throw new Exception("Server not responding !");
            }

            log("Checking if the server has started, try - " + tries);

            try {
                this.client = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
            } catch (Exception e) {

            }
            if (this.client == null) {
                log.info("Waiting till server initializes ........");
                Thread.sleep(TIME_OUT);
            } else {
                break;
            }

            ++tries;
        }

    }

    protected String createExperiment(Experiment experiment) throws AiravataSystemException, InvalidRequestException, AiravataClientException, TException {
        return getClient().createExperiment(experiment);
    }

    protected void launchExperiment(String expId) throws ExperimentNotFoundException, AiravataSystemException, InvalidRequestException, AiravataClientException, TException {
        getClient().launchExperiment(expId, "testToken");
    }

    protected Airavata.Client getClient() {
        return client;
    }

    //monitoring the job
    protected void monitorJob(final String expId) {
        Thread monitor = (new Thread() {
            public void run() {
                Map<String, JobStatus> jobStatuses = null;
                while (true) {
                    try {
                        jobStatuses = client.getJobStatuses(expId);
                        Set<String> strings = jobStatuses.keySet();
                        for (String key : strings) {
                            JobStatus jobStatus = jobStatuses.get(key);
                            if (jobStatus == null) {
                                return;
                            } else {
                                if (JobState.COMPLETE.equals(jobStatus.getJobState())) {
                                    log.info("Job completed Job ID: " + key);
                                    return;
                                } else {
                                    log.info("Job ID:" + key + "  Job Status : " + jobStatuses.get(key).getJobState().toString());
                                }
                            }
                        }
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        log.error("Thread interrupted", e.getMessage());
                    }
                }
            }
        });
        monitor.start();
        try {
            monitor.join();
        } catch (InterruptedException e) {
            log.error("Thread interrupted..", e.getMessage());
        }
    }

    public void log(String message) {
        log.info(message);
    }

}
