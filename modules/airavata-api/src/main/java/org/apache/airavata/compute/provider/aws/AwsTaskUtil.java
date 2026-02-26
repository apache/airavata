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
package org.apache.airavata.compute.provider.aws;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.apache.airavata.execution.task.TaskContext;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.iam.service.CredentialStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;

/**
 * Utility bean for AWS-specific task operations.
 * Provides helpers for building EC2 clients and cleaning up EC2 resources.
 * AWS credentials are stored as {@link PasswordCredential} where
 * {@code loginUserName} is the AWS access key ID and {@code password} is the AWS secret access key.
 */
@Component
public class AwsTaskUtil {

    private static final Logger logger = LoggerFactory.getLogger(AwsTaskUtil.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final CredentialStoreService credentialStoreService;

    public AwsTaskUtil(CredentialStoreService credentialStoreService) {
        this.credentialStoreService = credentialStoreService;
    }

    /**
     * Build an authenticated EC2 client using AWS credentials stored as a {@link PasswordCredential}.
     *
     * @param credentialToken the credential store token pointing to the AWS PasswordCredential
     * @param gatewayId       the gateway identifier
     * @param region          the AWS region string (e.g. "us-east-1")
     * @return a configured and open {@link Ec2Client}; caller is responsible for closing it
     * @throws Exception if the credential cannot be retrieved or the client cannot be built
     */
    public Ec2Client buildEc2Client(String credentialToken, String gatewayId, String region) throws Exception {
        logger.debug("Building EC2 client for region {} using credential token {}", region, credentialToken);
        try {
            PasswordCredential cred = credentialStoreService.getPasswordCredential(credentialToken, gatewayId);
            if (cred == null) {
                throw new Exception(
                        "No AWS credential found for token " + credentialToken + " in gateway " + gatewayId);
            }
            var awsBasicCredentials = AwsBasicCredentials.create(cred.getLoginUserName(), cred.getPassword());
            return Ec2Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                    .build();
        } catch (Exception e) {
            logger.error(
                    "Failed to build EC2 client for region {} with credential token {}", region, credentialToken, e);
            throw new Exception("Failed to build EC2 client for region " + region + ": " + e.getMessage(), e);
        }
    }

    /**
     * Terminate the EC2 instance associated with the given task context.
     * Reads instance ID and AWS credential token from the process context JSON.
     *
     * @param taskContext the task context providing process and gateway information
     * @param gatewayId   the gateway identifier
     */
    public void terminateEC2Instance(TaskContext taskContext, String gatewayId) {
        logger.info("Terminating EC2 instance for process {}", taskContext.getProcessId());
        try {
            String instanceId = readProviderContextKey(taskContext, "AWS_INSTANCE_ID");
            if (instanceId == null || instanceId.isBlank()) {
                logger.warn(
                        "No instance ID found in process context for process {}; skipping termination",
                        taskContext.getProcessId());
                return;
            }

            // Determine region from resource schedule if set
            String region = "us-east-1";
            try {
                var schedule = taskContext.getProcessModel().getResourceSchedule();
                if (schedule != null && schedule.get("awsRegion") != null) {
                    region = schedule.get("awsRegion").toString();
                }
            } catch (Exception regionEx) {
                logger.warn("Could not determine AWS region for termination; defaulting to us-east-1", regionEx);
            }

            // Use the AWS credential token from the context (stored as the SSH token placeholder)
            String credentialToken = readProviderContextKey(taskContext, "AWS_SSH_CREDENTIAL_TOKEN");
            if (credentialToken == null) {
                logger.warn(
                        "No credential token found for process {}; cannot terminate instance {}",
                        taskContext.getProcessId(),
                        instanceId);
                return;
            }

            try (Ec2Client ec2 = buildEc2Client(credentialToken, gatewayId, region)) {
                TerminateInstancesRequest request = TerminateInstancesRequest.builder()
                        .instanceIds(instanceId)
                        .build();
                ec2.terminateInstances(request);
                logger.info(
                        "Successfully sent termination request for EC2 instance {} in process {}",
                        instanceId,
                        taskContext.getProcessId());
            }
        } catch (Exception e) {
            logger.error("Failed to terminate EC2 instance for process {}", taskContext.getProcessId(), e);
        }
    }

    private String readProviderContextKey(TaskContext taskContext, String key) {
        try {
            String json = taskContext.getProcessModel().getProviderContext();
            if (json == null || json.isEmpty()) return null;
            Map<String, String> contextMap = MAPPER.readValue(json, new TypeReference<>() {});
            return contextMap.get(key);
        } catch (Exception e) {
            logger.warn("Failed to read provider context key '{}' for process {}", key, taskContext.getProcessId(), e);
            return null;
        }
    }
}
