package org.apache.airavata.cloud.intf;

import org.openstack4j.model.compute.Keypair;
import org.openstack4j.model.compute.Server;

/**
 * Generic Cloud Interface.
 * @author Mangirish Wagle
 *
 */
public interface CloudInterface {

	/**
	 * Method to create Server.
	 * @param serverName
	 * @param imageId
	 * @param flavorId
	 * @param networkId
	 * @param keyPairName
	 * @return Server object.
	 */
	public Server createServer(String serverName, String imageId, String flavorId, String networkId, String keyPairName);

	/**
	 * Returns the Server object pertaining to the serverId.
	 * @param serverId
	 * @return
	 */
	public Server getServer(String serverId);

	/**
	 * Method to delete Server.
	 * @param serverId
	 * @return
	 */
	public void deleteServer(String serverId);

	/**
	 * Creates a public key pair on the cloud
	 * @param publicKey
	 */
	public Keypair createKeyPair(String keyPairName, String publicKey);

	/**
	 * Returns the keypair object associated to the keyPairName.
	 * @param keyPairName
	 * @return
	 */
	public Keypair getKeyPair(String keyPairName);

	/**
	 * Deletes a public key pair on the cloud
	 * @param publicKey
	 */
	public void deleteKeyPair(String keyPairName);
}
