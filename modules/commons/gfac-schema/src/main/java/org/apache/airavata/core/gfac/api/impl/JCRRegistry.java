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

package org.apache.airavata.core.gfac.api.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;

import org.apache.airavata.core.gfac.api.Registry;
import org.apache.airavata.core.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.core.gfac.type.HostDescription;
import org.apache.airavata.core.gfac.type.ServiceDescription;
import org.apache.airavata.core.gfac.type.util.SchemaUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JCRRegistry implements Registry {

	private static final String SERVICE_NODE_NAME = "SERVICE_HOST";
	private static final String DEPLOY_NODE_NAME = "APP_HOST";
	private static final String HOST_NODE_NAME = "GFAC_HOST";
	private static final String XML_PROPERTY_NAME = "XML";
	private static final String LINK_NAME = "LINK";

	private Repository repository;
	private Credentials credentials;

	private static Log log = LogFactory.getLog(JCRRegistry.class);

	public JCRRegistry(String className, String user, String pass,
			Map<String, String> map) {
		try {
			/*
			 * Load the configuration from properties file at this level and
			 * create the object
			 */
			Class registryRepositoryFactory = Class.forName(className);
			Constructor c = registryRepositoryFactory.getConstructor();
			RepositoryFactory repositoryFactory = (RepositoryFactory) c
					.newInstance();

			repository = repositoryFactory.getRepository(map);
			credentials = new SimpleCredentials(user,
					new String(pass).toCharArray());

		} catch (ClassNotFoundException e) {
			log.error("Error class path settting", e);
		} catch (RepositoryException e) {
			log.error("Error connecting Remote Registry instance", e);
		} catch (Exception e) {
			log.error("Error init", e);
		}
	}

	private Session getSession() throws RepositoryException {
		return repository.login(credentials);
	}

	private Node getServiceNode(Session session) throws RepositoryException {
		return getOrAddNode(session.getRootNode(), SERVICE_NODE_NAME);
	}

	private Node getDeploymentNode(Session session) throws RepositoryException {
		return getOrAddNode(session.getRootNode(), DEPLOY_NODE_NAME);
	}

	private Node getHostNode(Session session) throws RepositoryException {
		return getOrAddNode(session.getRootNode(), HOST_NODE_NAME);
	}

	private Node getOrAddNode(Node node, String name)
			throws RepositoryException {
		Node node1 = null;
		try {
			node1 = node.getNode(name);
		} catch (PathNotFoundException pnfe) {
			node1 = node.addNode(name);
		} catch (RepositoryException e) {
			String msg = "failed to resolve the path of the given node ";
			log.debug(msg);
			throw new RepositoryException(msg, e);
		}
		return node1;
	}

	public List<HostDescription> getServiceLocation(String serviceName) {
		Session session = null;
		ArrayList<HostDescription> result = new ArrayList<HostDescription>();
		try {
			session = getSession();
			Node node = getServiceNode(session);
			Node serviceNode = node.getNode(serviceName);
			if (serviceNode.hasProperty(LINK_NAME)) {
				Property prop = serviceNode.getProperty(LINK_NAME);
				Value[] vals = prop.getValues();
				for (Value val : vals) {					
					Node host = session.getNodeByIdentifier(val.getString());
					Property hostProp = host.getProperty(XML_PROPERTY_NAME);
					result.add((HostDescription) SchemaUtil
							.parseFromXML(hostProp.getString()));
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			// TODO propagate
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}
		return result;
	}

	public ServiceDescription getServiceDescription(String serviceName) {
		Session session = null;
		ServiceDescription result = null;
		try {
			session = getSession();
			Node serviceNode = getServiceNode(session);
			Node node = serviceNode.getNode(serviceName);
			Property prop = node.getProperty(XML_PROPERTY_NAME);
			result = (ServiceDescription) SchemaUtil.parseFromXML(prop
					.getString());
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			// TODO propagate
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}
		return result;
	}

	public ApplicationDeploymentDescription getDeploymentDescription(
			String serviceName, String host) {
		Session session = null;
		ApplicationDeploymentDescription result = null;
		try {
			session = getSession();
			Node deploymentNode = getDeploymentNode(session);
			Node serviceNode = deploymentNode.getNode(serviceName);
			Node hostNode = serviceNode.getNode(host);
			NodeIterator nodes = hostNode.getNodes();
			for (; nodes.hasNext();) {
				Node app = nodes.nextNode();
				Property prop = app.getProperty(XML_PROPERTY_NAME);
				result = (ApplicationDeploymentDescription) SchemaUtil
						.parseFromXML(prop.getString());
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			// TODO propagate
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}
		return result;
	}

	public HostDescription getHostDescription(String name) {
		Session session = null;
		HostDescription result = null;
		try {
			session = getSession();
			Node hostNode = getHostNode(session);
			Node node = hostNode.getNode(name);
			Property prop = node.getProperty(XML_PROPERTY_NAME);
			result = (HostDescription) SchemaUtil
					.parseFromXML(prop.getString());
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			// TODO propagate
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}
		return result;
	}

	public String saveHostDescription(String name, HostDescription host) {
		Session session = null;
		String result = null;
		try {
			session = getSession();
			Node hostNode = getHostNode(session);
			Node node = getOrAddNode(hostNode, name);
			node.setProperty(XML_PROPERTY_NAME, SchemaUtil.toXML(host));
			session.save();

			result = node.getIdentifier();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			// TODO propagate
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}
		return result;
	}

	public String saveServiceDescription(String name, ServiceDescription service) {
		Session session = null;
		String result = null;
		try {
			session = getSession();
			Node serviceNode = getServiceNode(session);
			Node node = getOrAddNode(serviceNode, name);
			node.setProperty(XML_PROPERTY_NAME, SchemaUtil.toXML(service));
			session.save();

			result = node.getIdentifier();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			// TODO propagate
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}
		return result;
	}

	public String saveDeploymentDescription(String service, String host,
			ApplicationDeploymentDescription app) {
		Session session = null;
		String result = null;
		try {
			session = getSession();
			Node deployNode = getDeploymentNode(session);
			Node serviceNode = getOrAddNode(deployNode, service);
			Node hostNode = getOrAddNode(serviceNode, host);
			Node appName = getOrAddNode(hostNode, app.getName());
			appName.setProperty(XML_PROPERTY_NAME, SchemaUtil.toXML(app));
			session.save();

			result = appName.getIdentifier();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			// TODO propagate
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}
		return result;
	}

	public boolean deployServiceOnHost(String serviceName, String hostName) {
		Session session = null;
		try {
			session = getSession();
			Node serviceRoot = getServiceNode(session);
			Node hostRoot = getHostNode(session);

			Node serviceNode = serviceRoot.getNode(serviceName);
			Node hostNode = hostRoot.getNode(hostName);
						
			if (!serviceNode.hasProperty(LINK_NAME)) {				
				serviceNode.setProperty(LINK_NAME,
						new String[] { hostNode.getIdentifier() });
			} else {
				Property prop = serviceNode.getProperty(LINK_NAME);
				Value[] vals = prop.getValues();
				ArrayList<String> s = new ArrayList<String>();
				for (Value val : vals) {
					s.add(val.getString());
				}

				if (s.contains(hostNode.getIdentifier())) {
					return false;
				}
				
				s.add(hostNode.getIdentifier());
				serviceNode.setProperty(LINK_NAME, s.toArray(new String[0]));
			}
			
			session.save();
			return true;
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			// TODO propagate
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}
		return false;
	}

	public List<HostDescription> searchHostDescription(String name) {
		// TODO implementation
		return null;
	}

	public List<ServiceDescription> searchServiceDescription(String name) {
		// TODO implementation
		return null;
	}

	public List<ApplicationDeploymentDescription> searchDeploymentDescription(
			String serviceName, String hostName) {
		// TODO implementation
		return null;
	}
}
