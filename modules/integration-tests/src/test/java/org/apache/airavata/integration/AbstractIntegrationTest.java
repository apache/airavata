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
package org.apache.airavata.integration;

import java.io.IOException;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.common.utils.ClientSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractIntegrationTest {

	private static String THRIFT_SERVER_HOST;
	private static int THRIFT_SERVER_PORT;
	protected Airavata.Client airavataClient;
	private final int TRIES = 20;
	private final int TIME_OUT = 10000;
    final static Logger log = LoggerFactory.getLogger(AbstractIntegrationTest.class);

	public AbstractIntegrationTest() {
		super();
	}

	protected void init() {
	
	    try {
	        THRIFT_SERVER_HOST = ClientSettings.getSetting("thrift.server.host");
	        THRIFT_SERVER_PORT = Integer.parseInt(ClientSettings.getSetting("thrift.server.port"));
	
	        //check the server startup + initialize the thrift client
	        initClient();
	
	        //getting the Airavata API ( to add the descriptors
	    } catch (IOException e) {
	        log.error("Error loading client-properties ..." + e.getMessage());
	    } catch (Exception e) {
	        log.error(e.getMessage());
	    }
	}

	protected void initClient() throws Exception {
	    int tries = 0;
	    while (airavataClient==null) {
	    	log.info("Waiting till server initializes ........[try "+ (++tries) + " of "+TRIES+"]");
	        try {
                airavataClient = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
	        } catch (Exception e) { 
	        	if (tries == TRIES) {
					log("Server not responding. Cannot continue with integration tests ...");
					throw e;
				} else {
					Thread.sleep(TIME_OUT);
				}
	        }
	    }
	}

	protected Airavata.Client getClient() {
	    return airavataClient;
	}

    public void log(String message) {
        log.info(message);
    }
}