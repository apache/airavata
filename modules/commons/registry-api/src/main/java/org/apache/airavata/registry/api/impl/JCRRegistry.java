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

package org.apache.airavata.registry.api.impl;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.sql.Timestamp;
import java.util.*;

import javax.jcr.*;

import org.apache.airavata.registry.api.Axis2Registry;
import org.apache.airavata.registry.api.user.UserManager;
import org.apache.airavata.registry.api.user.UserManagerFactory;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.commons.gfac.type.util.SchemaUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.security.tools.TimestampedSigner;

public class JCRRegistry implements Axis2Registry {

	private static final String SERVICE_NODE_NAME = "SERVICE_HOST";
    private static final String GFAC_INSTANCE_DATA = "GFAC_INSTANCE_DATA";
	private static final String DEPLOY_NODE_NAME = "APP_HOST";
	private static final String HOST_NODE_NAME = "GFAC_HOST";
	private static final String XML_PROPERTY_NAME = "XML";
	private static final String WSDL_PROPERTY_NAME = "WSDL";
    private static final String GFAC_URL_PROPERTY_NAME = "GFAC_URL_LIST";
    private static final String LINK_NAME = "LINK";
    public static final int GFAC_URL_UPDATE_INTERVAL = 1000 * 60 * 60 * 3;

    private Repository repository;
	private Credentials credentials;
	private UserManager userManager;
	
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
			userManager = UserManagerFactory.getUserManager(className);
		} catch (ClassNotFoundException e) {
			log.error("Error class path settting", e);
		} catch (RepositoryException e) {
			log.error("Error connecting Remote Registry instance", e);
		} catch (Exception e) {
			log.error("Error init", e);
		}
	}

    public JCRRegistry(Repository repo,Credentials credentials){
        this.repository = repo;
        this.credentials = credentials;
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
	    Session session = null;
        ArrayList<ServiceDescription> result = new ArrayList<ServiceDescription>();
        try {
            session = getSession();
            Node node = getServiceNode(session);
            NodeIterator nodes = node.getNodes();
            for (; nodes.hasNext();) {
                Node service = nodes.nextNode();
                Property prop = service.getProperty(XML_PROPERTY_NAME);
                result.add((ServiceDescription) SchemaUtil.parseFromXML(prop.getString()));
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

	public List<ApplicationDeploymentDescription> searchDeploymentDescription(
			String serviceName, String hostName) {
		// TODO implementation
		return null;
	}

    public String saveWSDL(String name, String WSDL) {
        Session session = null;
        String result = null;
        try {
            session = getSession();
            Node serviceNode = getServiceNode(session);
            Node node = getOrAddNode(serviceNode, name);
            node.setProperty(WSDL_PROPERTY_NAME, WSDL);
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

    public String saveWSDL(String serviceName, ServiceDescription service) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getWSDL(String serviceName) {
        Session session = null;
        String result = null;
        try {
            session = getSession();
            Node serviceNode = getServiceNode(session);
            Node node = serviceNode.getNode(serviceName);
            Property prop = node.getProperty(WSDL_PROPERTY_NAME);
            result = prop.getString();
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

    public boolean saveGFacDescriptor(String gfacURL) {
        java.util.Date today = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
        Timestamp timestamp = new Timestamp(today.getTime());
        Session session = null;
        try {
            URI uri = new URI(gfacURL);
            String propertyName = uri.getHost() + "-" + uri.getPort();
            session = getSession();
            Node gfacDataNode = getOrAddNode(session.getRootNode(), GFAC_INSTANCE_DATA);
            try {
                Property prop = gfacDataNode.getProperty(propertyName);
                prop.setValue(gfacURL + ";" + timestamp.getTime());
                session.save();
            } catch (PathNotFoundException e) {
                gfacDataNode.setProperty(propertyName, gfacURL + ";" + timestamp.getTime());
                session.save();
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            return false;
            // TODO propagate
        } finally {
            if (session != null && session.isLive()) {
                session.logout();
            }
            return true;
        }
    }

    public boolean deleteGFacDescriptor(String gfacURL) {
        Session session = null;
        try {
            URI uri = new URI(gfacURL);
            String propertyName = uri.getHost() + "-" + uri.getPort();
            session = getSession();
            Node gfacDataNode = getOrAddNode(session.getRootNode(), GFAC_INSTANCE_DATA);
            Property prop = gfacDataNode.getProperty(propertyName);
            prop.setValue((String) null);
            session.save();
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            return false;
            // TODO propagate
        } finally {
            if (session != null && session.isLive()) {
                session.logout();
            }
            return true;
        }
    }

    public List<String> getGFacDescriptorList() {
        Session session = null;
        List<String> urlList = new ArrayList<String>();
        java.util.Date today = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
        Timestamp timestamp = new Timestamp(today.getTime());
        try {
            session = getSession();
            Node gfacNode = getOrAddNode(session.getRootNode(), GFAC_INSTANCE_DATA);
            PropertyIterator propertyIterator = gfacNode.getProperties();
            while (propertyIterator.hasNext()) {
                Property property = propertyIterator.nextProperty();
                if(!"nt:unstructured".equals(property.getString())){
                    Timestamp setTime = new Timestamp(new Long(property.getString().split(";")[1]));
                    if(GFAC_URL_UPDATE_INTERVAL > (timestamp.getTime() - setTime.getTime())){
                        urlList.add(property.getString().split(";")[0]);
                    }
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return urlList;
    }


}
