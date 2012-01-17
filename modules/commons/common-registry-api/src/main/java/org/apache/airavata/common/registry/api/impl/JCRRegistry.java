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
import java.util.Map;
import java.util.Observable;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

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

    private static Logger log = LoggerFactory.getLogger(JCRRegistry.class);

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
        } catch (ClassNotFoundException e) {
            log.error("Error class path settting", e);
        } catch (RepositoryException e) {
            log.error("Error connecting Remote Registry instance", e);
            throw e;
        } catch (Exception e) {
            log.error("Error init", e);
        }
    }

    public JCRRegistry(Repository repo, Credentials credentials) {
        this.repository = repo;
        this.credentials = credentials;
    }

    public Session getSession() throws RepositoryException {
        Session session = null;
        try {
            session = repository.login(credentials);
            if (session == null) {
                session = resetSession(session);
            }
        } catch (Exception e) {
            session = resetSession(session);
        }
        return session;
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

    protected void closeSession(Session session) {
        if (session != null && session.isLive()) {
            session.logout();
        }
    }


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


}
