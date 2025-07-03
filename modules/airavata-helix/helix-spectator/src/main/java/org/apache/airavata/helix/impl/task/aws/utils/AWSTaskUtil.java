/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.helix.impl.task.aws.utils;

import org.apache.airavata.agents.api.AgentUtils;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.aws.AWSProcessContextManager;
import org.apache.airavata.model.appcatalog.groupresourceprofile.AwsComputeResourcePreference;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;

import java.util.concurrent.TimeUnit;

public final class AWSTaskUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AWSTaskUtil.class);

    private AWSTaskUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Ec2Client buildEc2Client(String token, String gatewayId, String region) throws Exception {
        LOGGER.info("Building EC2 client for token {} and gateway id {} in region {}", token, gatewayId, region);
        PasswordCredential pwdCred = AgentUtils.getCredentialClient().getPasswordCredential(token, gatewayId);
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(pwdCred.getLoginUserName(), pwdCred.getPassword()); // TODO support using AWS Credential
        return Ec2Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }

    public static void terminateEC2Instance(TaskContext taskContext, String gatewayId) {
        LOGGER.warn("Full resource cleanup triggered for process {}", taskContext.getProcessId());
        try {
            AWSProcessContextManager awsContext = new AWSProcessContextManager(taskContext);
            AwsComputeResourcePreference awsPrefs = taskContext.getGroupComputeResourcePreference().getSpecificPreferences().getAws();
            String credentialToken = taskContext.getGroupComputeResourcePreference().getResourceSpecificCredentialStoreToken();
            String instanceId = awsContext.getInstanceId();

            if (instanceId == null) {
                LOGGER.warn("No instance ID found in context for process {}. Nothing to terminate.", taskContext.getProcessId());
                return;
            }

            try (Ec2Client ec2Client = buildEc2Client(credentialToken, gatewayId, awsPrefs.getRegion())) {

                LOGGER.info("Terminating EC2 instance: {}", instanceId);
                ec2Client.terminateInstances(req -> req.instanceIds(instanceId));

                ExponentialBackoffWaiter waiter = new ExponentialBackoffWaiter("EC2 instance " + instanceId + " to terminate", 10, 5, 15, TimeUnit.SECONDS);

                waiter.waitUntil(() -> {
                    DescribeInstancesResponse response = ec2Client.describeInstances(req -> req.instanceIds(instanceId));
                    if (response.reservations().isEmpty() || response.reservations().get(0).instances().isEmpty()) {
                        return true; // Instance is gone
                    }
                    InstanceStateName state = response.reservations().get(0).instances().get(0).state().name();
                    LOGGER.info("Waiting for instance {} termination. Current state: {}", instanceId, state);
                    if (state == InstanceStateName.TERMINATED) {
                        return true; // Success
                    }
                    return null; // Not terminated yet, continue waiting
                });
                LOGGER.info("Instance {} has been terminated.", instanceId);

                String sgId = awsContext.getSecurityGroupId();
                String keyName = awsContext.getKeyPairName();
                String sshCredentialToken = awsContext.getSSHCredentialToken();

                if (sgId != null) {
                    LOGGER.info("Deleting security group: {} of the instance: {}", sgId, instanceId);
                    ec2Client.deleteSecurityGroup(req -> req.groupId(sgId));
                }
                if (keyName != null) {
                    LOGGER.warn("Deleting key pair: {} of the instance: {}", keyName, instanceId);
                    ec2Client.deleteKeyPair(req -> req.keyName(keyName));
                }
                if (sshCredentialToken != null) {
                    AgentUtils.getCredentialClient().deleteSSHCredential(sshCredentialToken, gatewayId);
                }
            }

        } catch (Exception e) {
            LOGGER.error("Failed to cleanup resources during onCancel.", e);
        }
    }
}
