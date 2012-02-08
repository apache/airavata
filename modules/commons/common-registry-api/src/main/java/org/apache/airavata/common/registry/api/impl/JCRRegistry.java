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

package org.apache.airavata.common.registry.api.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.airavata.common.registry.api.Registry;
import org.apache.airavata.common.registry.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCRRegistry extends Observable implements Registry{

    private static final String XML_PROPERTY_NAME = "XML";

    public static final String PUBLIC = "PUBLIC";

    private Repository repository;
    private Credentials credentials;
    private UserManager userManager;
    private String username;
    private URI repositoryURI;
    private Class registryRepositoryFactory;
    private Map<String,String> connectionMap;
    private String password;
    private Session defaultSession=null;
    private boolean sessionKeepAlive=false;
    private EventListener workspaceChangeEventListener;
    
	private Thread sessionManager;
    private static final int SESSION_TIME_OUT = 60000;
    private static final int DEFINITE_SESSION_TIME_OUT = 300000;
    private static Logger log = LoggerFactory.getLogger(JCRRegistry.class);
    private Map<Node,Map<String,Node>> sessionNodes;
    private Map<Node,List<Node>> sessionNodeChildren;
    private Map<Session,Integer> currentSessionUseCount=new HashMap<Session, Integer>();
    private boolean threadRun = true;
    
    public JCRRegistry(URI repositoryURI, String className, String user, String pass, Map<String, String> map)
            throws RepositoryException {
        try {
            /*
             * Load the configuration from properties file at this level and create the object
             */
            connectionMap = map;
            registryRepositoryFactory = Class.forName(className);
            Constructor c = registryRepositoryFactory.getConstructor();
            RepositoryFactory repositoryFactory = (RepositoryFactory) c.newInstance();
            setRepositoryURI(repositoryURI);
            repository = repositoryFactory.getRepository(connectionMap);
            setUsername(user);
            setPassword(pass);
            credentials = new SimpleCredentials(getUsername(), new String(pass).toCharArray());
            definiteSessionTimeout();
            workspaceChangeEventListener=new EventListener() {
				
				public void onEvent(EventIterator events) {
					for(;events.hasNext();){
						Event event=events.nextEvent();
						try {
							String path = event.getPath();
							synchronized (sessionSynchronousObject) {
								System.out.println("something happened: " + event.getType() + " " + path);
								List<Node> nodesToRemove=new ArrayList<Node>();
								Set<Node> nodeIterator = getSessionNodes().keySet();
								for (Node node : nodeIterator) {
									if (node == null) {
										if (path.equals("/")) {
											nodesToRemove.add(node);
										}
									} else {
										if (node.getSession().isLive() && (node.getPath().startsWith(path)
												|| path.startsWith(node
														.getPath()))) {
											nodesToRemove.add(node);
										}
									}
								}
								for(Node node:nodesToRemove){
									getSessionNodes().remove(node);
								}
								nodeIterator = getSessionNodeChildren().keySet();
								nodesToRemove.clear();
								for (Node node : nodeIterator) {
									if (node.getSession().isLive() && (node.getPath().startsWith(path)
											|| path.startsWith(node.getPath()))) {
										nodesToRemove.add(node);
									}
								}
								for(Node node:nodesToRemove){
									getSessionNodeChildren().remove(node);
								}
							}
							triggerObservers(this);
						} catch (RepositoryException e) {
							e.printStackTrace();
						}
					}
					
				}
			};
        } catch (ClassNotFoundException e) {
            log.error("Error class path settting", e);
        } catch (RepositoryException e) {
            log.error("Error connecting Remote Registry instance", e);
            throw e;
        } catch (Exception e) {
            log.error("Error init", e);
        }
    }
    
    private void definiteSessionTimeout(){
    	Thread m=new Thread(new Runnable() {
			public void run() {
				while (threadRun){
					int timeoutCount=0;
					int shortStep=10000;
					Session currentSession=defaultSession;
					while(timeoutCount<DEFINITE_SESSION_TIME_OUT){
						try {
							Thread.sleep(shortStep);
						} catch (InterruptedException e) {
							//life sucks anyway, so who cares if this exception is thrown
						}
						timeoutCount=timeoutCount+shortStep;
						if (currentSession!=defaultSession){
							//reset start from begining since its a new session
							currentSession=defaultSession;
							timeoutCount=0;
						}
					}
					reallyCloseSession(defaultSession);
				}
				
			}
		});
    	m.start();
    }
    
    private void setupSessionManagement(){
    	stopSessionManager();
        setSessionKeepAlive(true);
    	sessionManager=new Thread(new Runnable() {
			public void run() {
				while (!isSessionInvalid() && isSessionKeepAlive()){
					try {
						setSessionKeepAlive(false);
						Thread.sleep(SESSION_TIME_OUT);
					} catch (InterruptedException e) {
						//no issue
					}
				}
				reallyCloseSession(defaultSession);
			}
		});
    	sessionManager.start();
    }
    
    private void stopSessionManager(){
    	if (sessionManager!=null) {
			sessionManager.interrupt();
		}
    }
    protected boolean isSessionKeepAlive() {
		return sessionKeepAlive;
	}

	public JCRRegistry(Repository repo, Credentials credentials) {
        this.repository = repo;
        this.credentials = credentials;
    }

	protected Node getRootNode(Session session) throws RepositoryException {
		String ROOT_NODE_TEXT = "root";
		if (!getSessionNodes().containsKey(null)){
			getSessionNodes().put(null, new HashMap<String, Node>());
			getSessionNodes().get(null).put(ROOT_NODE_TEXT, session.getRootNode());
		}
		return getOrAddNode(null, ROOT_NODE_TEXT);
	}
	
	public Session getSession() throws RepositoryException {
    	if (isSessionInvalid()){
    		reallyCloseSession(defaultSession);
        	synchronized (sessionSynchronousObject) {
	    		System.out.println("session created");
		        Session session = null;
		        try {
		            session = repository.login(credentials);
		            if (session == null) {
		                session = resetSession(session);
		            }
		        } catch (Exception e) {
		            session = resetSession(session);
		        }
		        defaultSession=session;
				if (defaultSession!=null) {
					defaultSession
							.getWorkspace()
							.getObservationManager()
							.addEventListener(
									getWorkspaceChangeEventListener(),
									Event.NODE_ADDED | Event.NODE_REMOVED
											| Event.NODE_MOVED, "/", true,
									null, null, false);
					currentSessionUseCount.put(session, 1);
				}
        	}
	        setupSessionManagement();
    	}else{
            setSessionKeepAlive(true);
            synchronized (sessionSynchronousObject) {
		        currentSessionUseCount.put(defaultSession, currentSessionUseCount.get(defaultSession)+1);
        	}
    	}
        return defaultSession;
    }

    protected Session resetSession(Session session){
        try {
            Constructor c = registryRepositoryFactory.getConstructor();
            RepositoryFactory repositoryFactory = (RepositoryFactory) c.newInstance();
            setRepositoryURI(repositoryURI);
            repository = repositoryFactory.getRepository(connectionMap);
            setUsername(username);
            credentials = new SimpleCredentials(getUsername(), getPassword().toCharArray());
            session = repository.login(credentials);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return session;
    }

    protected Node getOrAddNode(Node node, String name) throws RepositoryException {
    	Map<Node, Map<String, Node>> sessionNodes = getSessionNodes();
    	if (sessionNodes.containsKey(node)){
    		if (sessionNodes.get(node)!=null && sessionNodes.get(node).containsKey(name)){
    			return sessionNodes.get(node).get(name);
    		}
    	}else{
    		sessionNodes.put(node,new HashMap<String, Node>());
    	}
        Node node1 = null;
        try {
        	System.out.println("node extracted");
            node1 = node.getNode(name);
            sessionNodes.get(node).put(name, node1);
        } catch (PathNotFoundException pnfe) {
            node1 = node.addNode(name);
        } catch (RepositoryException e) {
            String msg = "failed to resolve the path of the given node ";
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }
        return node1;
    }

    protected void closeSession(Session session) {
    	if (session!=null) {
			if (currentSessionUseCount!=null) {
				currentSessionUseCount.put(session,
						currentSessionUseCount.get(session) - 1);
			}
			if (session != defaultSession) {
				reallyCloseSession(session);
			}
		}
    }

    protected void reallyCloseSession(Session session) {
    	synchronized (sessionSynchronousObject) {
    		if (session!=null && currentSessionUseCount.get(session)==0){
				if (session != null && session.isLive()) {
					try {
						session.getWorkspace().getObservationManager().removeEventListener(getWorkspaceChangeEventListener());
					} catch (UnsupportedRepositoryOperationException e) {
						e.printStackTrace();
					} catch (RepositoryException e) {
						e.printStackTrace();
					}
					session.logout();
		        }
				sessionNodes=null;
				sessionNodeChildren=null;
//				sessionNodes=new HashMap<Node, Map<String,Node>>();
//				sessionNodeChildren=new HashMap<Node, List<Node>>();
				if (session!=defaultSession){
					currentSessionUseCount.remove(session);
				}
    		}
    	}
	}

    private boolean isSessionInvalid(){
    	boolean isValid=false;
    	synchronized (sessionSynchronousObject) {
    		isValid=(defaultSession==null || !defaultSession.isLive());
		}
    	return isValid;
    }
    
    private Object sessionSynchronousObject=new Object();

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public URI getRepositoryURI() {
        return repositoryURI;
    }

    protected void setRepositoryURI(URI repositoryURI) {
        this.repositoryURI = repositoryURI;
    }

    protected void triggerObservers(Object o) {
        setChanged();
        notifyObservers(o);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return repository.getDescriptor(Repository.REP_NAME_DESC);
    }

    public Repository getRepository() {
        return repository;
    }

	public void setSessionKeepAlive(boolean sessionKeepAlive) {
		this.sessionKeepAlive = sessionKeepAlive;
	}

	private Map<Node,Map<String,Node>> getSessionNodes() {
		if (sessionNodes==null) {
			sessionNodes=new HashMap<Node, Map<String,Node>>();
		}
		return sessionNodes;
	}
	
	protected List<Node> getChildNodes(Node node) throws RepositoryException{
		if (!getSessionNodeChildren().containsKey(node)){
			List<Node> children=new ArrayList<Node>();
			NodeIterator nodes = node.getNodes();
			for (;nodes.hasNext();) {
				children.add(nodes.nextNode());
			}
			getSessionNodeChildren().put(node,children);
		}
		return getSessionNodeChildren().get(node);
	}

	public Map<Node,List<Node>> getSessionNodeChildren() {
		if (sessionNodeChildren==null) {
			sessionNodeChildren=new HashMap<Node, List<Node>>();
		}
		return sessionNodeChildren;
	}
	
	public EventListener getWorkspaceChangeEventListener() {
		return workspaceChangeEventListener;
	}

	public void setWorkspaceChangeEventListener(
			EventListener workspaceChangeEventListener) {
		this.workspaceChangeEventListener = workspaceChangeEventListener;
	}

    private void setThreadRun(boolean threadRun) {
        this.threadRun = threadRun;
    }

    public void closeConnection(){
        setThreadRun(false);
    }
}
