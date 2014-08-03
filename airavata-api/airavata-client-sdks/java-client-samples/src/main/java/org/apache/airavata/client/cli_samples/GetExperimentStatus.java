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

package org.apache.airavata.client.cli_samples;

import org.apache.airavata.model.error.*;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.thrift.TException;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import java.io.File;
import java.io.IOException;

public class GetExperimentStatus {
    public static String THRIFT_SERVER_HOST = "";
    public static int THRIFT_SERVER_PORT = 0;
    private static Airavata.Client client;
       
    public static void readConfigFile(){
    	Ini ini = null;
		try {
			ini = new Ini(new File("src/main/resources/airavata-client-properties.ini"));
		} catch (InvalidFileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	THRIFT_SERVER_HOST = ini.get("airavata", "AIRAVATA_SERVER");
    	THRIFT_SERVER_PORT = Integer.parseInt(ini.get("airavata", "AIRAVATA_PORT"));
    }
    
    public static String getExperimentStatus(String expId){
    	AiravataUtils.setExecutionAsClient();
    	ExperimentStatus status = null;
        try {
			client = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
		} catch (AiravataClientConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	try {
			status = client.getExperimentStatus(expId);
		} catch (InvalidRequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExperimentNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AiravataClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AiravataSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LaunchValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return status.getExperimentState().toString();
    }
    
    public static void main(String[] args) {
    	if(args.length!=1){
    		System.out.println("Inputs required: experimentId");
    		return;
    	}
    	readConfigFile();
    	String expId = args[0];
    	String status = getExperimentStatus(expId);    
    	System.out.println("The status is experiment id : " + expId + " is : "+status);
    }

}
