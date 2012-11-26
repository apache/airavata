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

package org.apache.airavata.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.impl.PasswordCallBackImpl;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.registry.api.exception.RegistryException;

public class AiravataAPIFactory {
	
	public static AiravataAPI getAPI(String gateway, String username, String alternativeUser) throws MalformedURLException, RepositoryException, RegistryException{
        return getAPI(null, gateway, username, alternativeUser, (PasswordCallback)null);
    }
	
	public static AiravataAPI getAPI(String gateway, String username) throws MalformedURLException, RepositoryException, RegistryException{
        return getAPI(null, gateway, username, (PasswordCallback)null);
    }
	
	public static AiravataAPI getAPI(URI registryURL, String gateway, String username, PasswordCallback callback) throws MalformedURLException, RepositoryException, RegistryException{
        return getAPI(registryURL, gateway, username, username, callback);
    }
    
    public static AiravataAPI getAPI(URI registryURL, String gateway, String username, String alternateUsername, PasswordCallback callback) throws MalformedURLException, RepositoryException, RegistryException{
        AiravataClient apiObj = new AiravataClient();
        apiObj.setCurrentUser(alternateUsername);
        apiObj.setCallBack(callback);
        apiObj.setPassword(callback.getPassword(username));
        apiObj.setRegitryURI(registryURL);
        apiObj.setGateway(gateway);
        return apiObj;
    }

	@Deprecated
    public static AiravataAPI getAPI(URI registryURL, String username, String password) throws MalformedURLException, RepositoryException, RegistryException{
		return getAPI(registryURL, username, password, username);
	}
	
    @Deprecated
    public static AiravataAPI getAPI(URI registryURL, String username, PasswordCallback callback) throws MalformedURLException, RepositoryException, RegistryException{
        return getAPI(registryURL, null, username, callback);
    }

    @Deprecated
	public static AiravataAPI getAPI(URI registryURL, String username, String password, String alternateUsername) throws MalformedURLException, RepositoryException, RegistryException{
		return getAPI(registryURL, null, username, alternateUsername, new PasswordCallBackImpl(username, password));
	}

    @Deprecated
	public static AiravataAPI getAPI(Map<String,String> configuration) throws MalformedURLException{
		AiravataClient apiObj = new AiravataClient(configuration);
		return apiObj;
	}

    @Deprecated
	public static AiravataAPI getAPI(String filename) throws MalformedURLException, RegistryException, IOException{
		AiravataClient apiObj = new AiravataClient(filename);
		return apiObj;
	}
}
