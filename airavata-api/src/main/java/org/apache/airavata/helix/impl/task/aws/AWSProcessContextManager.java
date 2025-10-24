/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.helix.impl.task.aws;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.factory.AiravataServiceFactory;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage AWS context
 */
public class AWSProcessContextManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AWSProcessContextManager.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String AWS_INSTANCE_ID_KEY = "AWS_INSTANCE_ID";
    private static final String AWS_SECURITY_GROUP_ID_KEY = "AWS_SECURITY_GROUP_ID";
    private static final String AWS_KEY_PAIR_NAME_KEY = "AWS_KEY_PAIR_NAME";
    private static final String AWS_SSH_CREDENTIAL_TOKEN = "AWS_SSH_CREDENTIAL_TOKEN";
    private static final String AWS_PUBLIC_IP = "AWS_PUBLIC_IP";
    private static final String AWS_JOB_ID = "AWS_JOB_ID";

    private final RegistryService.Iface registry;
    private final TaskContext taskContext;
    private final String processId;

    public AWSProcessContextManager(TaskContext taskContext) {
        try {
            this.registry = AiravataServiceFactory.getRegistry();
            this.taskContext = taskContext;
            this.processId = taskContext.getProcessId();
            LOGGER.info("Initialized AWSProcessContextManager for process {}", processId);

        } catch (RuntimeException e) {
            LOGGER.error("Failed to initialize AWSProcessContextManager", e);
            throw new RuntimeException("Failed to initialize AWSProcessContextManager", e);
        }
    }

    public String getInstanceId() throws IOException {
        return getContextMap().get(AWS_INSTANCE_ID_KEY);
    }

    public void saveInstanceId(String instanceId) throws TException, IOException {
        updateContext(AWS_INSTANCE_ID_KEY, instanceId);
    }

    public String getSecurityGroupId() throws IOException {
        return getContextMap().get(AWS_SECURITY_GROUP_ID_KEY);
    }

    public void saveSecurityGroupId(String securityGroupId) throws TException, IOException {
        updateContext(AWS_SECURITY_GROUP_ID_KEY, securityGroupId);
    }

    public String getKeyPairName() throws IOException {
        return getContextMap().get(AWS_KEY_PAIR_NAME_KEY);
    }

    public void saveKeyPairName(String keyPairName) throws TException, IOException {
        updateContext(AWS_KEY_PAIR_NAME_KEY, keyPairName);
    }

    public String getSSHCredentialToken() throws IOException {
        return getContextMap().get(AWS_SSH_CREDENTIAL_TOKEN);
    }

    public void saveSSHCredentialToken(String credentialToken) throws TException, IOException {
        updateContext(AWS_SSH_CREDENTIAL_TOKEN, credentialToken);
    }

    public String getPublicIp() throws IOException {
        return getContextMap().get(AWS_PUBLIC_IP);
    }

    public void savePublicIp(String publicIp) throws TException, IOException {
        updateContext(AWS_PUBLIC_IP, publicIp);
    }

    public String getJobId() throws IOException {
        return getContextMap().get(AWS_JOB_ID);
    }

    public void saveJobId(String jobId) throws TException, IOException {
        updateContext(AWS_JOB_ID, jobId);
    }

    public void cleanup() throws TException, IOException {
        updateContext(AWS_INSTANCE_ID_KEY, null);
        updateContext(AWS_SECURITY_GROUP_ID_KEY, null);
        updateContext(AWS_KEY_PAIR_NAME_KEY, null);
    }

    private Map<String, String> getContextMap() throws IOException {
        String jsonContext = taskContext.getProcessModel().getProcessDetail();
        if (jsonContext == null || jsonContext.isEmpty()) {
            return new HashMap<>();
        }
        return MAPPER.readValue(jsonContext, new TypeReference<>() {});
    }

    private void updateContext(String key, String value) throws TException, IOException {
        Map<String, String> contextMap = getContextMap();
        contextMap.put(key, value);
        ProcessModel processModel = taskContext.getProcessModel();
        processModel.setProcessDetail(MAPPER.writeValueAsString(contextMap));
        registry.updateProcess(processModel, processId);
        LOGGER.info("Updated process detail for process {} with key '{}'", processId, key);
    }
}
