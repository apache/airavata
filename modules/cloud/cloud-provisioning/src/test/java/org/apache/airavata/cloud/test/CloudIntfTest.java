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
package org.apache.airavata.cloud.test;

import org.apache.airavata.cloud.intf.CloudInterface;
import org.apache.airavata.cloud.intf.impl.OpenstackIntfImpl;
import org.apache.airavata.cloud.util.Constants;
import org.junit.Ignore;
import org.junit.Test;
import org.openstack4j.model.compute.Keypair;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Router;
import org.openstack4j.model.network.RouterInterface;
import org.openstack4j.model.network.Subnet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
	@Ignore
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

			Keypair kp = (Keypair) cloudIntf.getKeyPair(keyPairName);
			if(kp == null) {
				kp = (Keypair) cloudIntf.createKeyPair(keyPairName, publicKey);
			}

			logger.info("Keypair created/ retrieved: " + kp.getFingerprint());

			/* Create Server */
			Server newServer = (Server) cloudIntf.createServer("AiravataTest", imageId, flavorId, kp.getName());
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

			Server deleted = (Server) cloudIntf.getServer(newServer.getId());

			assertTrue(newServer != null && deleted == null);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			fail();
		}
	}

	/**
	 * Jetstream create delete network test.
	 */
	@Test
	@Ignore
	public void jetstreamCreateDeleteNetworkTest() {
		try {
			CloudInterface cloudIntf = new OpenstackIntfImpl("jetstream_openrc.properties");

			/* fetch sample data from properties file */
			String networkName = properties.getProperty("jetstream_network_name");
			String subnetCIDR = properties.getProperty("jetstream_subnet_cidr");
			Integer ipVersion = Integer.valueOf(properties.getProperty("jetstream_ip_version", 
					Constants.OS_IP_VERSION_DEFAULT.toString()));
			String externalGateway = properties.getProperty("jetstream_public_network_name");

			/* build router and subnet names */
			String subnetName = "subnet-" + networkName;
			String routerName = "router-" + networkName;

			/*  create network */
			logger.info("Creating network with name = " + networkName);
			Network network = (Network) cloudIntf.createNetwork(networkName);
			assertTrue(network != null && network.getName().equals(networkName));

			/* create subnet for network */
			logger.info("Creating subnet with name = " + subnetName + ", and CIDR = " + subnetCIDR + ", and version = " + ipVersion);
			Subnet subnet = (Subnet) cloudIntf.createSubnet(subnetName, networkName, subnetCIDR, ipVersion);
			assertTrue(subnet != null 
					&& subnet.getName().equals(subnetName) 
					&& subnet.getCidr().equals(subnetCIDR) 
					&& subnet.getIpVersion().getVersion() == ipVersion.intValue());

			/* create router for external gateway */
			logger.info("Creating router with name = " + routerName + ", and external gateway = " + externalGateway);
			Router router = (Router) cloudIntf.createRouter(routerName, externalGateway);
			assertTrue(router != null && router.getName().equals(routerName));

			/* create router-subnet interface */
			logger.info("Creating interface between router = " + routerName + ", and subnet = " + subnetName);
			RouterInterface iface = (RouterInterface) cloudIntf.createRouterSubnetInterface(routerName, subnetName);
			assertTrue(iface != null && iface.getSubnetId().equals(subnet.getId()));

			/* delete router-subnet interface */
			logger.info("Deleting interface between router = " + routerName + ", and subnet = " + subnetName);
			cloudIntf.deleteRouterSubnetInterface(routerName, subnetName);

			/* delete router for external gateway */
			logger.info("Creating router with name = " + routerName);
			cloudIntf.deleteRouter(routerName);

			/* delete subnet for network */
			logger.info("Creating subnet with name = " + subnetName);
			cloudIntf.deleteSubnet(subnetName);

			/* delete network */
			logger.info("Deleting network with name = " + networkName);
			cloudIntf.deleteNetwork(networkName);
		} catch( Exception ex ) {
			ex.printStackTrace();
			fail();
		}

	}
}
