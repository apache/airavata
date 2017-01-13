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
package org.apache.airavata.cloud.intf;

/**
 * The Interface CloudInterface.
 */
public interface CloudInterface {

	/**
	 * Method to create Server.
	 *
	 * @param serverName the server name
	 * @param imageId the image id
	 * @param flavorId the flavor id
	 * @param keyPairName the key pair name
	 * @return Server object.
	 */
	public Object createServer(String serverName, String imageId, String flavorId, String keyPairName);

	/**
	 * Returns the Server object pertaining to the serverId.
	 *
	 * @param serverId the server id
	 * @return the server
	 */
	public Object getServer(String serverId);

	/**
	 * Method to delete Server.
	 *
	 * @param serverId the server id
	 */
	public void deleteServer(String serverId);

	/**
	 * Creates a public key pair on the cloud.
	 *
	 * @param keyPairName the key pair name
	 * @param publicKey the public key
	 * @return the keypair
	 */
	public Object createKeyPair(String keyPairName, String publicKey);

	/**
	 * Returns the keypair object associated to the keyPairName.
	 *
	 * @param keyPairName the key pair name
	 * @return the key pair
	 */
	public Object getKeyPair(String keyPairName);

	/**
	 * Deletes a public key pair on the cloud.
	 *
	 * @param keyPairName the key pair name
	 */
	public void deleteKeyPair(String keyPairName);

	/**
	 * Associates a floating ip to the instance indicated by serverId.
	 *
	 * @param serverId the server id
	 */
	public void addFloatingIP(String serverId);

	/**
	 * Creates the router.
	 *
	 * @param routerName the router name
	 * @param externalGatewayName the external gateway name
	 * @return the object
	 */
	public Object createRouter(String routerName, String externalGatewayName);

	/**
	 * Creates the subnet.
	 *
	 * @param subnetName the subnet name
	 * @param networkName the network name
	 * @param subnetCIDR the subnet cidr
	 * @param ipVersion the ip version
	 * @return the object
	 */
	public Object createSubnet(String subnetName, String networkName, String subnetCIDR, int ipVersion);

	/**
	 * Creates the network.
	 *
	 * @param networkName the network name
	 * @return the object
	 */
	public Object createNetwork(String networkName);

	/**
	 * Creates the router subnet interface.
	 *
	 * @param routerName the router name
	 * @param subnetName the subnet name
	 * @return the object
	 */
	public Object createRouterSubnetInterface(String routerName, String subnetName);

	/**
	 * Delete router subnet interface.
	 *
	 * @param routerName the router name
	 * @param subnetName the subnet name
	 */
	public void deleteRouterSubnetInterface(String routerName, String subnetName);

	/**
	 * Delete subnet.
	 *
	 * @param subnetName the subnet name
	 */
	public void deleteSubnet(String subnetName);

	/**
	 * Delete router.
	 *
	 * @param routerName the router name
	 */
	public void deleteRouter(String routerName);

	/**
	 * Delete network.
	 *
	 * @param networkName the network name
	 */
	public void deleteNetwork(String networkName);
}
