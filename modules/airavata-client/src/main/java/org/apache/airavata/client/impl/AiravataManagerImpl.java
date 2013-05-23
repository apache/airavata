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

package org.apache.airavata.client.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.airavata.client.AiravataClient;
import org.apache.airavata.client.api.AiravataManager;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;

public class AiravataManagerImpl implements AiravataManager {
	private AiravataClient client;
	
	public AiravataManagerImpl(AiravataClient client) {
		setClient(client);
	}

	@Override
	public List<URI> getWorkflowInterpreterServiceURLs()  throws AiravataAPIInvocationException{
		try {
			return getClient().getRegistryClient().getWorkflowInterpreterURIs();
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}



	public AiravataClient getClient() {
		return client;
	}

	public void setClient(AiravataClient client) {
		this.client = client;
	}

	@Override
	public URI getWorkflowInterpreterServiceURL()
			throws AiravataAPIInvocationException {
		try {
			return getClient().getClientConfiguration().getXbayaServiceURL().toURI();
		} catch (URISyntaxException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public URI getWorkflowInterpreterServiceURL(URI defaultURL)
			throws AiravataAPIInvocationException {
		if (getWorkflowInterpreterServiceURL()==null){
			return defaultURL;	
		}
		return getWorkflowInterpreterServiceURL();
		
	}

	@SuppressWarnings("serial")
	@Override
	public List<URI> getMessageBoxServiceURLs()
			throws AiravataAPIInvocationException {
		try {
			return new ArrayList<URI>(){{add(getClient().getRegistryClient().getMessageBoxURI());}};
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public URI getMessageBoxServiceURL() throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getMessageBoxURI();
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public URI getMessageBoxServiceURL(URI defaultURL)
			throws AiravataAPIInvocationException {
		if (getMessageBoxServiceURL()==null){
			return defaultURL;	
		}
		return getMessageBoxServiceURL();
	}

	@SuppressWarnings("serial")
	@Override
	public List<URI> getEventingServiceURLs()
			throws AiravataAPIInvocationException {
		try {
			return new ArrayList<URI>(){{add(getClient().getRegistryClient().getEventingServiceURI());}};
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public URI getEventingServiceURL() throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getEventingServiceURI();
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
    }

	@Override
	public URI getEventingServiceURL(URI defaultURL)
			throws AiravataAPIInvocationException {
		if (getEventingServiceURL()==null){
			return defaultURL;	
		}
		return getEventingServiceURL();
	}

    @Override
    public void setConfiguration(String key, String value, Date expire) throws AiravataAPIInvocationException {
       try{
           getClient().getRegistryClient().setConfiguration(key, value, expire);
       }catch (Exception e){
           throw new AiravataAPIInvocationException(e);
       }
    }

    @Override
    public void addConfiguration(String key, String value, Date expire) throws AiravataAPIInvocationException {
        try{
            getClient().getRegistryClient().addConfiguration(key, value, expire);
        }catch (Exception e){
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void removeAllConfiguration(String key) throws AiravataAPIInvocationException {
        try{
            getClient().getRegistryClient().removeAllConfiguration(key);
        }catch (Exception e){
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void removeConfiguration(String key, String value) throws AiravataAPIInvocationException {
        try{
            getClient().getRegistryClient().removeConfiguration(key, value);
        }catch (Exception e){
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void addWorkflowInterpreterURI(URI uri) throws AiravataAPIInvocationException {
        try{
            getClient().getRegistryClient().addWorkflowInterpreterURI(uri);
        }catch (Exception e){
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void setEventingURI(URI uri) throws AiravataAPIInvocationException {
        try{
            getClient().getRegistryClient().setEventingURI(uri);
        }catch (Exception e){
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void setMessageBoxURI(URI uri) throws AiravataAPIInvocationException {
        try{
            getClient().getRegistryClient().setMessageBoxURI(uri);
        }catch (Exception e){
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void addWorkflowInterpreterURI(URI uri, Date expire) throws AiravataAPIInvocationException {
        try{
            getClient().getRegistryClient().addWorkflowInterpreterURI(uri, expire);
        }catch (Exception e){
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void setEventingURI(URI uri, Date expire) throws AiravataAPIInvocationException {
        try{
            getClient().getRegistryClient().setEventingURI(uri, expire);
        }catch (Exception e){
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void setMessageBoxURI(URI uri, Date expire) throws AiravataAPIInvocationException {
        try{
            getClient().getRegistryClient().setMessageBoxURI(uri, expire);
        }catch (Exception e){
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void removeWorkflowInterpreterURI(URI uri) throws AiravataAPIInvocationException {
        try{
            getClient().getRegistryClient().removeWorkflowInterpreterURI(uri);
        }catch (Exception e){
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void removeAllWorkflowInterpreterURI() throws AiravataAPIInvocationException {
        try{
            getClient().getRegistryClient().removeAllWorkflowInterpreterURI();
        }catch (Exception e){
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void unsetEventingURI() throws AiravataAPIInvocationException {
        try{
            getClient().getRegistryClient().unsetEventingURI();
        }catch (Exception e){
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void unsetMessageBoxURI() throws AiravataAPIInvocationException {
        try{
            getClient().getRegistryClient().unsetMessageBoxURI();
        }catch (Exception e){
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public Gateway getGateway() throws AiravataAPIInvocationException {
        try {
			return getClient().getRegistryClient().getGateway();
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
    }

    @Override
    public AiravataUser getUser() throws AiravataAPIInvocationException {
    	try{
    		return getClient().getRegistryClient().getUser();
    	} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
    }

}
