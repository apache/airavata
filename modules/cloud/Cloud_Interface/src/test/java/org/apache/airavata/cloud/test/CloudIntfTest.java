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

/**
 * JUnit test class for Cloud Interface.
 * @author Mangirish Wagle
 *
 */
public class CloudIntfTest {

	/** The properties. */
	private String propertiesFile = "test_data.properties";
	private Properties properties;

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
			String networkId = properties.getProperty("jetstream_networkId");

			/* Create Keypair */
			String publicKeyFile = properties.getProperty("publicKeyFile");
			String keyPairName = "testKey";

			Scanner fileScan = new Scanner(new FileInputStream(publicKeyFile));
			String publicKey = fileScan.nextLine();

			Keypair kp = cloudIntf.getKeyPair(keyPairName);
			if(kp == null) {
				kp = cloudIntf.createKeyPair(keyPairName, publicKey);
			}

			System.out.println("Keypair created/ retrieved: " + kp.getFingerprint());

			/* Create Server */
			Server newServer = cloudIntf.createServer("AiravataTest", imageId, flavorId, networkId, kp.getName());
			System.out.println("Server Created: " + newServer.getId());

			/* Delete Server */
			cloudIntf.deleteServer(newServer.getId());
			System.out.println("Server deleted: " + newServer.getId());

			Server deleted = cloudIntf.getServer(newServer.getId());

			/* Delete Keypair */
			cloudIntf.deleteKeyPair(kp.getName());
			System.out.println("Keypair deleted: " + kp.getName());

			assertTrue(newServer != null && deleted == null);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			fail();
		}
	}
}
