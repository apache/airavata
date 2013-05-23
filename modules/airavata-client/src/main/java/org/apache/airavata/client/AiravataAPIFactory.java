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

import java.net.URI;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.impl.PasswordCallBackImpl;
import org.apache.airavata.common.utils.SecurityUtil;
import org.apache.airavata.registry.api.PasswordCallback;

public class AiravataAPIFactory {
	
	public static AiravataAPI getAPI(String gateway, String username, String alternativeUser) throws AiravataAPIInvocationException{
        return getAPI(null, gateway, username, alternativeUser, (PasswordCallback)null);
    }
	
	public static AiravataAPI getAPI(String gateway, String username) throws AiravataAPIInvocationException{
        return getAPI(null, gateway, username, (PasswordCallback)null);
    }
	
	public static AiravataAPI getAPI(URI registryURL, String gateway, String username, PasswordCallback callback) throws AiravataAPIInvocationException{
        return getAPI(registryURL, gateway, username, username, callback);
    }

    public static AiravataAPI getAPI(URI registryURL, String gateway, String username, PasswordCallback callback,
                                     String trustStoreFilePath, String trustStorePassword)
            throws AiravataAPIInvocationException{

        SecurityUtil.setTrustStoreParameters(trustStoreFilePath, trustStorePassword);
        return getAPI(registryURL, gateway, username, username, callback);
    }
    
    public static AiravataAPI getAPI(URI registryURL, String gateway, String username, String alternateUsername, PasswordCallback callback) throws AiravataAPIInvocationException{
        try {
			AiravataAPI apiObj = new AiravataClient();
			apiObj.setCurrentUser(alternateUsername);
			apiObj.setCallBack(callback);
			apiObj.setRegitryURI(registryURL);
			apiObj.setGateway(gateway);
			apiObj.initialize();
			return apiObj;
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
    }

}
