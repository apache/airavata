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
package org.apache.airavata.service.security.xacml;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceStub;
import org.wso2.carbon.identity.entitlement.stub.dto.PaginatedStatusHolder;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;

/**
 * This publishes the airavata-default-xacml-policy.xml to the PDP via PAP API (of WSO2 Identity Server)
 */
public class DefaultPAPClient {

    private final static Logger logger = LoggerFactory.getLogger(DefaultPAPClient.class);
    private EntitlementPolicyAdminServiceStub entitlementPolicyAdminServiceStub;

    public DefaultPAPClient(String auhorizationServerURL, String username, String password,
                            ConfigurationContext configCtx) throws AiravataSecurityException {
        try {

            String PDPURL = auhorizationServerURL + "EntitlementPolicyAdminService";
            entitlementPolicyAdminServiceStub = new EntitlementPolicyAdminServiceStub(configCtx, PDPURL);
            CarbonUtils.setBasicAccessSecurityHeaders(username, password, true,
                    entitlementPolicyAdminServiceStub._getServiceClient());
        } catch (AxisFault e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSecurityException("Error initializing XACML PEP client.");
        }

    }

    public boolean isPolicyAdded(String policyName) {
        try {
            PolicyDTO policyDTO = entitlementPolicyAdminServiceStub.getPolicy(policyName, false);
        } catch (RemoteException e) {
            logger.debug("Error in retrieving the policy.", e);
            return false;
        } catch (EntitlementPolicyAdminServiceEntitlementException e) {
            logger.debug("Error in retrieving the policy.", e);
            return false;
        }
        return true;
    }

    public void addPolicy(String policy) throws AiravataSecurityException {
        new Thread() {
            public void run() {
                try {
                    PolicyDTO policyDTO = new PolicyDTO();
                    policyDTO.setPolicy(policy);
                    entitlementPolicyAdminServiceStub.addPolicy(policyDTO);
                    entitlementPolicyAdminServiceStub.publishToPDP(new String[]{ServerSettings.getAuthorizationPoliyName()},
                            EntitlementConstants.PolicyPublish.ACTION_CREATE, null, false, 0);

                    //Since policy publishing happens asynchronously, we need to retrieve the status and verify.
                    Thread.sleep(2000);
                    PaginatedStatusHolder paginatedStatusHolder = entitlementPolicyAdminServiceStub.
                            getStatusData(EntitlementConstants.Status.ABOUT_POLICY, ServerSettings.getAuthorizationPoliyName(),
                                    EntitlementConstants.StatusTypes.PUBLISH_POLICY, "*", 1);
                    StatusHolder statusHolder = paginatedStatusHolder.getStatusHolders()[0];
                    if (statusHolder.getSuccess() && EntitlementConstants.PolicyPublish.ACTION_CREATE.equals(statusHolder.getTargetAction())) {
                        logger.info("Authorization policy is published successfully.");
                    } else {
                        throw new AiravataSecurityException("Failed to publish the authorization policy.");
                    }

                    //enable the published policy
                    entitlementPolicyAdminServiceStub.enableDisablePolicy(ServerSettings.getAuthorizationPoliyName(), true);
                    //Since policy enabling happens asynchronously, we need to retrieve the status and verify.
                    Thread.sleep(2000);
                    paginatedStatusHolder = entitlementPolicyAdminServiceStub.
                            getStatusData(EntitlementConstants.Status.ABOUT_POLICY, ServerSettings.getAuthorizationPoliyName(),
                                    EntitlementConstants.StatusTypes.PUBLISH_POLICY, "*", 1);
                    statusHolder = paginatedStatusHolder.getStatusHolders()[0];
                    if (statusHolder.getSuccess() && EntitlementConstants.PolicyPublish.ACTION_ENABLE.equals(statusHolder.getTargetAction())) {
                        logger.info("Authorization policy is enabled successfully.");
                    } else {
                        throw new AiravataSecurityException("Failed to enable the authorization policy.");
                    }
                } catch (RemoteException e) {
                    logger.error(e.getMessage(), e);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                } catch (ApplicationSettingsException e) {
                    logger.error(e.getMessage(), e);
                } catch (AiravataSecurityException e) {
                    logger.error(e.getMessage(), e);
                } catch (EntitlementPolicyAdminServiceEntitlementException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }.start();
    }
}
