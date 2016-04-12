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

package org.apache.airavata.cloud.intf.impl;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.airavata.cloud.intf.CloudInterface;
import org.apache.airavata.cloud.openstack.OS4JClientProvider;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Keypair;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;

public class OpenstackIntfImpl implements CloudInterface {

	/** The properties. */
	private String propertiesFile;
	private Properties properties;

	OSClient os = null;

	/**
	 * Default Constructor
	 * Initializing the properties.
	 */
	public OpenstackIntfImpl(String propFile) {
		try {

			this.propertiesFile = propFile;
			InputStream inputStream = getClass().getClassLoader()
					.getResourceAsStream(propertiesFile);

			if(inputStream != null) {
				properties = new Properties();
				properties.load(inputStream);
			}
			else {
				throw new FileNotFoundException("property file: " + propertiesFile + " not found!");
			}

			// Initialize the OSClient.
			os = OS4JClientProvider.getOSClient(properties);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
		}
	}

	@Override
	public Server createServer(String serverName, String imageId, String flavorId, String networkId, String keyPairName) {
		try {


			List<String> srvNet = new LinkedList<String>();
			srvNet.add(networkId);

			ServerCreate sc = Builders.server()
					.name(serverName)
					.flavor(flavorId)
					.image(imageId)
					.networks(srvNet)
					.keypairName(keyPairName)
					.addPersonality("/etc/motd", "Welcome to the new VM! Restricted access only")
					.build();

			//Boot the Server
			Server newServer = os.compute().servers().boot(sc);

			return newServer;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			return null;
		}
	}

	@Override
	public Server getServer(String serverId) {
		try {

			Server server = os.compute().servers().get(serverId);

			return server;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			return null;
		}
	}

	@Override
	public void deleteServer(String serverId) {
		try {

			os.compute().servers().delete(serverId);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
		}
	}

	@Override
	public Keypair createKeyPair(String keyPairName, String publicKey) {
		try {

			Keypair keyp = os.compute().keypairs().create(keyPairName, publicKey);

			return keyp;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			return null;
		}
	}

	@Override
	public Keypair getKeyPair(String keyPairName) {
		try {

			Keypair keyp = os.compute().keypairs().get(keyPairName);

			return keyp;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			return null;
		}
	}

	@Override
	public void deleteKeyPair(String keyPairName) {
		try {

			os.compute().keypairs().delete(keyPairName);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
		}
	}

}
