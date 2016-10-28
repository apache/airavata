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
package org.apache.airavata.cloud.intf.impl;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.airavata.cloud.intf.CloudInterface;
import org.apache.airavata.cloud.openstack.OS4JClientProvider;
import org.apache.airavata.cloud.util.Constants;
import org.apache.airavata.cloud.util.IPType;
import org.apache.airavata.cloud.util.OpenstackIntfUtil;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.ActionResponse;
import org.openstack4j.model.compute.Address;
import org.openstack4j.model.compute.FloatingIP;
import org.openstack4j.model.compute.Keypair;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.network.AttachInterfaceType;
import org.openstack4j.model.network.IPVersionType;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Router;
import org.openstack4j.model.network.RouterInterface;
import org.openstack4j.model.network.Subnet;
import org.openstack4j.openstack.compute.domain.NovaAddresses.NovaAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenstackIntfImpl implements CloudInterface {

	/** The properties. */
	private String propertiesFile;
	private Properties properties;

	// Initializing Logger
	private Logger logger = LoggerFactory.getLogger(OpenstackIntfImpl.class);

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
	public Server createServer(String serverName, String imageId, String flavorId, String keyPairName) {
		try {

			Server newServer = null;
			String networkId = null;

			// Adhering to openstack format of subnet names 'subnet-<name>'.
			String networkName = "subnet-" + properties.getProperty(Constants.OS_NETWORK_NAME);

			for( Subnet net : os.networking().subnet().list() ) {
				if(net.getName().equals(networkName)) {
					networkId = net.getNetworkId();
					logger.info("Using network " + networkName + " with ID: " + networkId);
					break;
				}
			}

			if(networkId != null) {

				List<String> srvNet = new LinkedList<String>();
				srvNet.add(networkId);

				ServerCreate sc = Builders.server()
						.name(serverName)
						.flavor(flavorId)
						.image(imageId)
						.networks(srvNet)
						.keypairName(keyPairName)
						.build();

				//Boot the Server
				newServer = os.compute().servers().boot(sc);

				logger.info("New server created with ID: " + newServer.getId());

			}
			else {
				logger.error("Network with name " + networkName + " not found.");
			}
			return newServer;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to create server.");
			return null;
		}
	}

	@Override
	public Server getServer(String serverId) {
		try {

			Server server = os.compute().servers().get(serverId);

			logger.info("Server retrieved successfully for ID: " + serverId);

			return server;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to retrieve server for ID: " + serverId);
			return null;
		}
	}

	@Override
	public void deleteServer(String serverId) {
		try {

			Server server = this.getServer(serverId);

			// Get Floating IP if there is one associated.
			String floatingIpAddr = null;
			for(Address novaAddress : server.getAddresses().getAddresses().get(properties.getProperty(Constants.OS_NETWORK_NAME))) {
				novaAddress = (NovaAddress) novaAddress;
				if(novaAddress.getType().equals(IPType.FLOATING.toString())) {
					floatingIpAddr = novaAddress.getAddr();
					break;
				}
			}

			if(server != null) {
				os.compute().servers().delete(serverId);

				// Deallocating Floating IP.
				if(floatingIpAddr != null) {
					for(FloatingIP floatIp : os.compute().floatingIps().list()) {
						if(floatIp.getFloatingIpAddress().equals(floatingIpAddr)) {
							os.compute().floatingIps().deallocateIP(floatIp.getId());
						}
					}
				}

				logger.info("Server deleted successfully for ID: " + serverId);
			}

		}
		catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to delete server with ID: " + serverId);
		}
	}

	@Override
	public Keypair createKeyPair(String keyPairName, String publicKey) {
		try {

			Keypair keyp = os.compute().keypairs().create(keyPairName, publicKey);

			logger.info("Keypair created successfully: " + keyp.getName());

			return keyp;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to create keypair: " + keyPairName);
			return null;
		}
	}

	@Override
	public Keypair getKeyPair(String keyPairName) {
		try {

			Keypair keyp = os.compute().keypairs().get(keyPairName);

			if(keyp != null){
				logger.info("Keypair retrieved successfully: " + keyp.getName());				
			}

			return keyp;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to retrieve keypair: " + keyPairName);
			return null;
		}
	}

	@Override
	public void deleteKeyPair(String keyPairName) {
		try {

			os.compute().keypairs().delete(keyPairName);

			logger.info("Keypair deleted successfully: " + keyPairName);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to delete keypair: " + keyPairName);
		}
	}

	@Override
	public void addFloatingIP(String serverId) {

		try {
			Server server = this.getServer(serverId);

			// Floating IP to allocate.
			FloatingIP floatIp = null;

			if(server != null) {
				List<? extends FloatingIP> floatIPList = os.compute().floatingIps().list();

				// Iterate through the floating ips from the pool present if any.
				if(floatIPList.size() > 0) {
					for(FloatingIP ip : floatIPList) {
						logger.info("Checking if floating ip : " + ip.getFloatingIpAddress() + " is free to use.");
						Boolean isFloatingIpUsed = OpenstackIntfUtil.isFloatingIPUsed(ip);
						if( isFloatingIpUsed != null && !isFloatingIpUsed ) {
							floatIp = ip;
							logger.info("Floating ip " + ip.getFloatingIpAddress() + " found to be free.");
							break;
						}
					}
				}
				// If all floating IPs are used, or there are no free floating ips, create new one.
				if(floatIp == null){
					floatIp = os.compute().floatingIps().allocateIP(properties.getProperty(Constants.OS_FLOATING_IP_POOL));
					logger.info("Created new floating ip " + floatIp.getFloatingIpAddress());
				}

				if(floatIp != null) {
					String ipAddr = floatIp.getFloatingIpAddress();

					if(ipAddr != null) {
						ActionResponse response = os.compute().floatingIps().addFloatingIP(server, ipAddr);
						logger.info(response.isSuccess() + ":" + response.getCode() + ":" + response.getFault() + ":" + response.toString());

						if(response.isSuccess()) {
							logger.info("Floating IP "+ ipAddr + " assigned successfully to server with ID: " + serverId);
						}
						else {
							logger.error("Failed to associate Floating IP.");
						}
					}
				}

			}
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to associate floating IP to server with ID: " + serverId);
		}
	}

	@Override
	public Object createNetwork(String networkName) {
		Network network = null;
		try {
			network = os.networking().network().create(Builders.network()
					.name(networkName)
					.adminStateUp(true)
					.build());
			logger.info("Created a new network : " + network);
		} catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to create network: " + networkName + ". Exception: " + ex.getMessage(), ex);
		}
		return network;
	}

	@Override
	public Object createRouter(String routerName, String externalGatewayName) {
		String publicNetId = null;
		Router router = null;
		try {
			for(Network net : os.networking().network().list()) {
				if(net.getName().equals(externalGatewayName)) {
					publicNetId = net.getId();
				}
			}
			if(publicNetId != null) {
				router = os.networking().router().create(Builders.router()
						.name(routerName)
						.adminStateUp(true)
						.externalGateway(publicNetId)
						.build());
				logger.info("Created a new router " + router + " for external gateway : [" + externalGatewayName + "]");
			} else {
				logger.error("Failed to create router because external gateway [ " + externalGatewayName + "] is not found!");
			}	
		} catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to create network: " + routerName + ". Exception: " + ex.getMessage(), ex);
		}
		return router;
	}

	@Override
	public Object createSubnet(String subnetName, String networkName, String subnetCIDR, int ipVersion) {
		String networkId = null;
		Subnet subnet = null;
		try {
			// get network id
			for(Network network : os.networking().network().list()) {
				if(network.getName().equals(networkName)) {
					networkId = network.getId();
				}
			}

			if(networkId != null) {
				subnet = os.networking().subnet().create(Builders.subnet()
						.enableDHCP(true)
						.name(subnetName)
						.networkId(networkId)
						.ipVersion(IPVersionType.valueOf(ipVersion))
						.cidr(subnetCIDR)
						.build());
				logger.info("Created a subnet : " + subnetName + " for network [ " + networkName + "]");
			} else {
				logger.error("Failed to create subnet because network [ " + networkName + "] is not found!");
			}
		} catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to create subnet: " + subnetName + ". Exception: " + ex.getMessage(), ex);
		}
		return subnet;
	}

	@Override
	public Object createRouterSubnetInterface(String routerName, String subnetName) {
		String subnetId = null, routerId = null;
		RouterInterface iface = null;
		try {
			// get subnetid from name
			for(Subnet subnet : os.networking().subnet().list()) {
				if(subnet.getName().equals(subnetName)) {
					subnetId = subnet.getId();
				}
			}

			// get routerid from name
			for(Router router : os.networking().router().list()) {
				if(router.getName().equals(routerName)) {
					routerId = router.getId();
				}
			}

			if(routerId != null && subnetId != null) {
				// attach external interface to gateway
				iface = os.networking().router()
						.attachInterface(routerId, AttachInterfaceType.SUBNET, subnetId);
				logger.info("Attached external interface to router : " + iface);
			} else {
				logger.error("Either router or network is not found. Kindly re-check and try again.");
			}
		} catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to create subnet-router interface. Exception: " + ex.getMessage(), ex);
		}
		return iface;
	}

	@Override
	public void deleteRouterSubnetInterface(String routerName, String subnetName) {
		String routerId = null, subnetId = null;
		try {
			// get subnet id
			for(Subnet subnet : os.networking().subnet().list()) {
				if(subnet.getName().equals(subnetName)) {
					subnetId = subnet.getId();
				}
			}
			// get router id
			for(Router router : os.networking().router().list()) {
				if(router.getName().equals(routerName)) {
					routerId = router.getId();
				}
			}
			// detach the interface
			if(routerId != null && subnetId != null) {
				os.networking().router().detachInterface(routerId, subnetId, null);
			} else {
				logger.error("Failed to delete router subnet interface. Either router/subnet not found.");
			}
		} catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to delete subnet: " + subnetName + ". Exception: " + ex.getMessage(), ex);
		}
	}

	@Override
	public void deleteSubnet(String subnetName) {
		try {
			for(Subnet subnet : os.networking().subnet().list()) {
				if(subnet.getName().equals(subnetName)) {
					os.networking().subnet().delete(subnet.getId());
					logger.info("Deleted Subnet [" + subnet.getName() + "] Successfully.");
				}
			}
		} catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to delete subnet: " + subnetName + ". Exception: " + ex.getMessage(), ex);
		}

	}

	@Override
	public void deleteRouter(String routerName) {
		try {
			for(Router router : os.networking().router().list()) {
				if(router.getName().equals(routerName)) {
					os.networking().router().delete(router.getId());
					logger.info("Deleted Router [" + router.getName() + "] Successfully.");
				}
			}
		} catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to delete router: " + routerName + ". Exception: " + ex.getMessage(), ex);
		}

	}

	@Override
	public void deleteNetwork(String networkName) {
		try {
			for(Network network : os.networking().network().list()) {
				if(network.getName().equals(networkName)) {
					os.networking().network().delete(network.getId());
					logger.info("Deleted Network [" + network.getName() + "] Successfully.");
				}
			}
		} catch( Exception ex ) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
			logger.error("Failed to delete network: " + networkName + ". Exception: " + ex.getMessage(), ex);
		}
	}
}
