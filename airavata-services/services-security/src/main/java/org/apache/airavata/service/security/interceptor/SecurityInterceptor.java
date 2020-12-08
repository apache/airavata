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
package org.apache.airavata.service.security.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.patform.monitoring.CountMonitor;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.service.security.AiravataSecurityManager;
import org.apache.airavata.service.security.IdentityContext;
import org.apache.airavata.service.security.SecurityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor of Airavata API calls for the purpose of applying security.
 */
public class SecurityInterceptor implements MethodInterceptor {
    private final static Logger logger = LoggerFactory.getLogger(SecurityInterceptor.class);
    private final static CountMonitor apiRequestCounter = new CountMonitor("api_server_request_counter", "method");

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        //obtain the authz token from the input parameters
        AuthzToken authzToken = (AuthzToken) invocation.getArguments()[0];
        //authorize the API call
        HashMap<String, String> metaDataMap = new HashMap();
        metaDataMap.put(Constants.API_METHOD_NAME, invocation.getMethod().getName());
        apiRequestCounter.inc(invocation.getMethod().getName());
        authorize(authzToken, metaDataMap);
        //set the user identity info in a thread local to be used in downstream execution.
        IdentityContext.set(authzToken);
        //let the method call procees upon successful authorization
        Object returnObj = invocation.proceed();
        //clean the identity context before the method call returns
        IdentityContext.unset();
        return returnObj;
    }

    private void authorize(AuthzToken authzToken, Map<String, String> metaData) throws AuthorizationException {
        try {
            boolean isAPISecured = ServerSettings.isAPISecured();
            if (isAPISecured) {
                AiravataSecurityManager securityManager = SecurityManagerFactory.getSecurityManager();
                boolean isAuthz = securityManager.isUserAuthorized(authzToken, metaData);
                if (!isAuthz) {
                    throw new AuthorizationException("User is not authenticated or authorized.");
                }
            }
        } catch (AiravataSecurityException e) {
            logger.error(e.getMessage(), e);
            throw new AuthorizationException("Error in authenticating or authorizing user.");
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AuthorizationException("Internal error in authenticating or authorizing user.");
        }
    }
}


