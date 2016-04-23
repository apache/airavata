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

package org.apache.airavata.cloud.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import org.apache.airavata.cloud.intf.CloudInterface;
import org.apache.airavata.cloud.intf.impl.OpenstackIntfImpl;
import org.junit.Test;
import org.openstack4j.model.compute.Keypair;
import org.openstack4j.model.compute.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudIntfTest {

	/** The properties. */
	private String propertiesFile = "test_data.properties";
	private Properties properties;

	// Initializing Logger
	private Logger logger = LoggerFactory.getLogger(CloudIntfTest.class);

	public CloudIntfTest() {
		try {

			InputStream inputStream = getClass().getClassLoader()
					.getResourceAsStream(propertiesFile);

			if(inputStream != null) {
				properties = new Properties();
				properties.load(inputStream);
			}
			else {
				throw new FileNotFoundException("property file: " + propertiesFile + " not found!");
			}

		}
		catch(Exception ex) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
		}
	}

	/**
	 * Test that will create keypair, create server with keypair, delete server, delete keypair.
	 */
	@Test
	public void jetstreamCreateDeleteServerTest() {
		try {
			CloudInterface cloudIntf = new OpenstackIntfImpl("jetstream_openrc.properties");
			// Sample data. This can be determined by the inputs from Airavata.
			String imageId = properties.getProperty("jetstream_imageId");
			String flavorId = properties.getProperty("jetstream_flavorId");

			// Delay in milliseconds used for waiting for server create and delete.
			Integer delay = 30000;

			/* Create Keypair */
			String publicKeyFile = properties.getProperty("publicKeyFile");
			String keyPairName = "testKey";

			Scanner fileScan = new Scanner(new FileInputStream(publicKeyFile));
			String publicKey = fileScan.nextLine();

			Keypair kp = cloudIntf.getKeyPair(keyPairName);
			if(kp == null) {
				kp = cloudIntf.createKeyPair(keyPairName, publicKey);
			}

			logger.info("Keypair created/ retrieved: " + kp.getFingerprint());

			/* Create Server */
			Server newServer = cloudIntf.createServer("AiravataTest", imageId, flavorId, kp.getName());
			logger.info("Server Created: " + newServer.getId());

			/* Wait 30 seconds until server is active */
			logger.info("Waiting for instance to go ACTIVE...");
			Thread.sleep(delay);

			/* Associate floating ip */
			cloudIntf.addFloatingIP(newServer.getId());

			/* Delete Server */
			cloudIntf.deleteServer(newServer.getId());
			logger.info("Server deleted: " + newServer.getId());

			/* Wait 30 seconds until server is terminated */
			logger.info("Waiting for instance to terminate...");
			Thread.sleep(delay);

			/* Delete Keypair */
			cloudIntf.deleteKeyPair(kp.getName());
			logger.info("Keypair deleted: " + kp.getName());

			Server deleted = cloudIntf.getServer(newServer.getId());

			assertTrue(newServer != null && deleted == null);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			fail();
		}
	}
}
