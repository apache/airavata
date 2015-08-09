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

import java.util.Date;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.ExperimentNotFoundException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.ExperimentState;
import org.apache.airavata.model.workspace.experiment.ExperimentStatus;
import org.apache.thrift.TException;
import org.junit.Assert;

/**
 * this class contains the common utils across the single application integration tests
 */
public class SingleAppIntegrationTestBase extends AbstractIntegrationTest {

    protected String createExperiment(Experiment experiment) throws AiravataSystemException, InvalidRequestException, TException, ApplicationSettingsException {
        return getClient().createExperiment("default", experiment);
    }

    protected void launchExperiment(String expId) throws ExperimentNotFoundException, AiravataSystemException, InvalidRequestException, TException {
        getClient().launchExperiment(expId, "testToken");
    }

    //monitoring the job
    protected void monitorJob(final String expId) {
        Thread monitor = (new Thread() {
            public void run() {
            	long previousUpdateTime=-1;
                ExperimentStatus experimentStatus = null;
                do {
                    try {
                    	experimentStatus = airavataClient.getExperimentStatus(expId);
						if (previousUpdateTime!=experimentStatus.getTimeOfStateChange()) {
							previousUpdateTime=experimentStatus.getTimeOfStateChange();
							log.info(expId
									+ " : " + experimentStatus.getExperimentState().toString()
									+ " ["+new Date(previousUpdateTime).toString()+"]");
							
						}
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        log.error("Thread interrupted", e);
                    }
                    Assert.assertFalse(experimentStatus.getExperimentState().equals(ExperimentState.FAILED));
                }while(!experimentStatus.getExperimentState().equals(ExperimentState.COMPLETED));
            }
        });
        monitor.start();
        try {
            monitor.join();
        } catch (InterruptedException e) {
            log.error("Thread interrupted..", e.getMessage());
        }
    }

}
