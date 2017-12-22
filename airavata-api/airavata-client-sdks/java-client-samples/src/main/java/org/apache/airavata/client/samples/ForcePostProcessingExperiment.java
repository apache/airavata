/**
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
 */
package org.apache.airavata.client.samples;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.commons.io.IOUtils;
import org.apache.thrift.TException;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * This class will demonstrate an example of force post processing of an experiment, Using this, you can manually trigger
 * post processing tasks of the experiment.
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ForcePostProcessingExperiment {

    private static final String THRIFT_SERVER_HOST = "127.0.0.1";
    private static final int THRIFT_SERVER_PORT = 8930;

    private Airavata.Client airavataClient;
    private String experimentId = "dummy-echo-experiment_03e7de4f-04fe-4e0e-9402-7f46289105f2";
    private String gatewayId = "default";

    public static void main(String args[]) throws TException, IOException {
        ForcePostProcessingExperiment fppExperiment = new ForcePostProcessingExperiment();
        fppExperiment.register();
    }

    public void register() throws TException {
        airavataClient = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
        forcePostProcess();
        System.out.println("Force post processing started for experiment " + experimentId);
    }

    private void forcePostProcess() throws TException {
        airavataClient.executePostProcessing(new AuthzToken(""), experimentId, gatewayId);
    }
}
