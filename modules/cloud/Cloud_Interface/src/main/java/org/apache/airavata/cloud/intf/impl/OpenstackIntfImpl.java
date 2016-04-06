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

/**
 * Cloud Interface implementation Openstack.
 * @author Mangirish Wagle
 *
 */
public class OpenstackIntfImpl implements CloudInterface {

	/** The properties. */
	private String propertiesFile;
	private Properties properties;

	OSClient os;

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

			// Initialize the OSClient based on API version.

			// API V3
			if( properties.getProperty("OS_IDENTITY_API_VERSION").equals("3") ) {
				os = OS4JClientProvider.getOSClientV3(properties.getProperty("OS_AUTH_URL"),
						properties.getProperty("OS_USERNAME"), 
						properties.getProperty("OS_PASSWORD"),
						properties.getProperty("OS_USER_DOMAIN_NAME"),
						properties.getProperty("OS_PROJECT_DOMAIN_NAME"));
			}
			//API V2
			else if( properties.getProperty("OS_IDENTITY_API_VERSION").equals("2") ) {
				os = OS4JClientProvider.getOSClientV2(properties.getProperty("OS_AUTH_URL"),
						properties.getProperty("OS_USERNAME"), 
						properties.getProperty("OS_PASSWORD"),
						properties.getProperty("OS_USER_DOMAIN_NAME"),
						properties.getProperty("OS_PROJECT_DOMAIN_NAME"));
			}
			else {
				throw new Exception("Non- supported Openstack API version " + properties.getProperty("OS_IDENTITY_API_VERSION"));
			}

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
