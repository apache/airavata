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
package org.apache.airavata.api.server.security;

import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceStub;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * This enforces XACML based fine grained authorization on the API calls.
 */
public class DefaultXACMLPEP {

    private final static Logger logger = LoggerFactory.getLogger(DefaultXACMLPEP.class);
    private EntitlementServiceStub entitlementServiceStub;

    public DefaultXACMLPEP(String auhorizationServerURL, String username, String password,
                           ConfigurationContext configCtx) throws AiravataSecurityException {
        try {

            String PDPURL = auhorizationServerURL + "EntitlementService";
            entitlementServiceStub = new EntitlementServiceStub(configCtx, PDPURL);
            CarbonUtils.setBasicAccessSecurityHeaders(username, password, true, entitlementServiceStub._getServiceClient());
        } catch (AxisFault e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error initializing XACML PEP client.");
        }

    }

    /**
     * Send the XACML authorization request to XAML PDP and return the authorization decision.
     *
     * @param authzToken
     * @param metaData
     * @return
     */
    public boolean getAuthorizationDecision(AuthzToken authzToken, Map<String, String> metaData) throws AiravataSecurityException {
        String decision;
        try {
            String subject = authzToken.getClaimsMap().get(Constants.USER_NAME);
            String action = "/airavata/" + metaData.get(Constants.API_METHOD_NAME);
            String decisionString = entitlementServiceStub.getDecisionByAttributes(subject, null, action, null);
            //parse the XML decision string and obtain the decision

            if ("NotApplicable".equals(decision) || "Indeterminate".equals(decision) || decision == null) {
                logger.error("Authorization Decision is: " + decision);
                throw new AiravataSecurityException("Error in authorizing the user.");
            }
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in authorizing the user.");
        } catch (EntitlementServiceException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error in authorizing the user.");
        }
        return Boolean.valueOf(decision);
    }

    private String parseDecisionString(String decisionString) {
        
    }
}
