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

package org.apache.airavata.cloud.intf;

import org.openstack4j.model.compute.Keypair;
import org.openstack4j.model.compute.Server;

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
	public Server createServer(String serverName, String imageId, String flavorId, String keyPairName);

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

	/**
	 * Associates a floating ip to the instance indicated by serverId.
	 * @param serverId
	 */
	public void addFloatingIP(String serverId);
}
