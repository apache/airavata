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

package org.apache.airavata.client.samples;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.api.error.AiravataClientException;
import org.apache.airavata.api.error.AiravataSystemException;
import org.apache.airavata.api.error.InvalidRequestException;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.thrift.TException;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateLaunchExperiment {

    //FIXME: Read from a config file
    public static final String THRIFT_SERVER_HOST = "localhost";
    public static final int THRIFT_SERVER_PORT = 8930;
    private static Airavata.Client airavata;
    private final static Logger logger = LoggerFactory.getLogger(CreateLaunchExperiment.class);

    public static void main(String[] args) {
        airavata = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
        try {
            System.out.println("API version is " + airavata.GetAPIVersion());
            createExperiment(airavata);
        } catch (TException e) {
           logger.error("Error while connecting with server", e.getMessage());
        }
    }

    public static String createExperiment (Airavata.Client client){
        try{
            Experiment experiment = ExperimentModelUtil.createSimpleExperiment("testProject", "admin", "experiment1", "testDescription", "testApp", null);
            String expId = client.createExperiment(experiment);
            System.out.println("Experiment ID : " + expId);
            return expId;
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        }catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }
}
