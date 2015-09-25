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

package org.apache.airavata.testsuite.multitenantedairavata;

import org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class FrameworkBootstrapping {
    private final static Logger logger = LoggerFactory.getLogger(FrameworkBootstrapping.class);
    private static boolean runAll = false;
    private static boolean regApps = false;
    private static boolean expExec = false;
    private static TestFrameworkProps properties;
    private static Map<String, String> tokens;
    private static ExperimentExecution experimentExecution;

    public static void main(String[] args) {
        parseArguments(args);
        try {
            FrameworkSetup setup = FrameworkSetup.getInstance();
            properties = setup.getTestFrameworkProps();

            if (runAll){
                setup.getGatewayRegister().createGateways();
                logger.info("Gateways created...");
                setup.getGatewayRegister().registerSSHKeys();
                logger.info("Registered SSH keys to each gateway...");
                tokens = readTokens();
                setup.getComputeResourceRegister().addComputeResources();
                setup.getComputeResourceRegister().registerGatewayResourceProfile();
                setup.getApplicationRegister().addApplications();
                logger.info("Applications registered for each each gateway...");
                experimentExecution = new ExperimentExecution(setup.getAiravata(), tokens, properties);
                experimentExecution.createEchoExperiment();
//                experimentExecution.createAmberExperiment();
                experimentExecution.launchExperiments();
                experimentExecution.monitorExperiments();
            }else if (regApps){
                setup.getGatewayRegister().createGateways();
                logger.info("Gateways created...");
                System.out.println("Gateways created...");
                setup.getGatewayRegister().registerSSHKeys();
                logger.info("Registered SSH keys to each gateway...");
                System.out.println("Registered SSH keys to each gateway...");
//                tokens = readTokens();
                setup.getComputeResourceRegister().addComputeResources();
                logger.info("Compute resources saved...");
                System.out.println("Compute resources saved...");
                setup.getComputeResourceRegister().registerGatewayResourceProfile();
                logger.info("Gateway Profiles saved...");
                System.out.println("Gateway Profiles saved...");
                setup.getApplicationRegister().addApplications();
                logger.info("Applications registered for each each gateway...");
            }else if (expExec){
                tokens = readTokens();
                experimentExecution = new ExperimentExecution(setup.getAiravata(), tokens, setup.getTestFrameworkProps());
//                experimentExecution.createUltrascanExperiment();
                experimentExecution.createEchoExperiment();
                experimentExecution.createAmberExperiment();
                experimentExecution.launchExperiments();
                experimentExecution.monitorExperiments();
            }
        } catch (Exception e) {
            logger.error("Error occured while set up", e);
        }
    }

    public static Map<String, String> readTokens () throws Exception{
        Map<String, String> tokens = new HashMap<String, String>();
        String fileLocation = properties.getTokenFileLoc();
        String fileName = TestFrameworkConstants.CredentialStoreConstants.TOKEN_FILE_NAME;
        String path = fileLocation + File.separator + fileName;
        File tokenFile = new File(path);
        if (tokenFile.exists()){
            FileInputStream fis = new FileInputStream(tokenFile);
            //Construct BufferedReader from InputStreamReader
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String line;
            while ((line = br.readLine()) != null) {
                String[] strings = line.split(":");
                tokens.put(strings[0], strings[1]);
            }
            br.close();
        }else {
            throw new Exception("Could not find token file.. Please run application registration step if you haven't run it");
        }
        return tokens;
    }

    public static void parseArguments(String[] args) {
        try{
            Options options = new Options();

            options.addOption("regApps", false , "Gateway ID");
            options.addOption("expExec", false, "Experiment ID");
            options.addOption("runAll", false, "Do application registration and experiment execution together");

            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse( options, args);
            if (cmd.getOptions() == null || cmd.getOptions().length == 0){
                logger.info("You have not specified any options. We assume you need to start from the scratch...");
                runAll= true;
            }
            if (cmd.hasOption("regApps")){
                logger.info("Register Applications only...");
                regApps = true;
            }else if (cmd.hasOption("expExec")){
                logger.info("Execute Experiments only...");
                expExec = true;
            }else {
                runAll = true;
            }
        } catch (ParseException e) {
            logger.error("Error while reading command line parameters" , e);
        }
    }
}
