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
package org.apache.airavata.helix.impl.task.aws;

import org.apache.airavata.agents.api.AgentUtils;
import org.apache.airavata.helix.agent.ssh.SSHUtil;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.aws.utils.AWSTaskUtil;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.appcatalog.groupresourceprofile.AwsComputeResourcePreference;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.helix.task.TaskResult;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairResponse;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupResponse;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;

import java.security.Security;
import java.util.UUID;

/**
 * Create all required AWS resources (SecurityGroup, KeyPair) and launches an EC2 instance
 */
@TaskDef(name = "Create EC2 Instance Task")
public class CreateEC2InstanceTask extends AiravataTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateEC2InstanceTask.class);

    @Override
    public TaskResult onRun(TaskHelper helper, TaskContext taskContext) {
        LOGGER.info("Starting Create EC2 Instance Task for process {}", getProcessId());
        Security.addProvider(new BouncyCastleProvider());

        AWSProcessContextManager awsContext = new AWSProcessContextManager(taskContext);
        Ec2Client ec2Client = null;

        try {
            AwsComputeResourcePreference awsPrefs = taskContext.getGroupComputeResourcePreference().getSpecificPreferences().getAws();
            String credentialToken = taskContext.getGroupComputeResourcePreference().getResourceSpecificCredentialStoreToken();

            ec2Client = AWSTaskUtil.buildEc2Client(credentialToken, getGatewayId(), awsPrefs.getRegion());
            LOGGER.info("Successfully built EC2 client for region {}", awsPrefs.getRegion());

            String securityGroupId = createSecurityGroup(ec2Client);
            awsContext.saveSecurityGroupId(securityGroupId);
            LOGGER.info("Created and saved security group: {}", securityGroupId);

            String keyPairName = "airavata-key-" + getProcessId();
            CreateKeyPairResponse kpRes = ec2Client.createKeyPair(req -> req.keyName(keyPairName));
            awsContext.saveKeyPairName(keyPairName);

            String privateKeyPEM = kpRes.keyMaterial();
            String publicKey = SSHUtil.generatePublicKey(privateKeyPEM);

            String sshCredentialToken = saveSSHCredential(privateKeyPEM, publicKey);
            awsContext.saveSSHCredentialToken(sshCredentialToken);
            LOGGER.info("Created key pair {} and saved credential with token {}", keyPairName, sshCredentialToken);

            RunInstancesRequest runRequest = RunInstancesRequest.builder().imageId(awsPrefs.getPreferredAmiId()).instanceType(InstanceType.fromValue(awsPrefs.getPreferredInstanceType())).keyName(keyPairName).securityGroupIds(securityGroupId).minCount(1).maxCount(1).build();
            RunInstancesResponse runResponse = ec2Client.runInstances(runRequest);

            if (runResponse.instances() == null || runResponse.instances().isEmpty()) {
                LOGGER.error("No instances were launched by AWS even after successful SDK call");
                return onFail("No instances were launched by AWS even after successful SDK call", false, null);
            }

            String instanceId = runResponse.instances().get(0).instanceId();
            awsContext.saveInstanceId(instanceId);
            LOGGER.info("Successfully launched EC2 instance {}", instanceId);

            return onSuccess("AWS Env setup task successfully completed " + getTaskId());

        } catch (Exception e) {
            // TODO catch for AMI issues, etc
            LOGGER.error("Error creating EC2 instance for process {}", getProcessId(), e);
            LOGGER.warn("Triggering cleanup due to failure in onRun().");
            this.onCancel(taskContext);
            return onFail("Error creating EC2 instance for process " + getProcessId(), false, e); // fatal: false to retry EC2 instance creation since cleanup-action was triggerred

        } finally {
            if (ec2Client != null) {
                ec2Client.close();
            }
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {
        AWSTaskUtil.terminateEC2Instance(getTaskContext(), getGatewayId());
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        LOGGER.info("AWS Create EC2 Instance Task cleanup for process {}", getProcessId());
        AWSTaskUtil.terminateEC2Instance(getTaskContext(), getGatewayId());
    }

    private String saveSSHCredential(String privateKey, String publicKey) throws Exception {
        SSHCredential credential = new SSHCredential();
        credential.setGatewayId(getGatewayId());
        credential.setToken(UUID.randomUUID().toString());
        credential.setPrivateKey(privateKey);
        credential.setPublicKey(publicKey);
        credential.setUsername(getProcessModel().getUserName());

        String savedToken = AgentUtils.getCredentialClient().addSSHCredential(credential);
        LOGGER.info("Successfully saved temporary SSH credential with token {}", savedToken);

        return savedToken;
    }

    private String createSecurityGroup(Ec2Client ec2) throws Exception {
        String vpcId = ec2.describeVpcs(req -> req.filters(f -> f.name("is-default").values("true"))).vpcs().get(0).vpcId();
        CreateSecurityGroupResponse sgRes = ec2.createSecurityGroup(req -> req
                .groupName("airavata-sg-" + getProcessId())
                .description("Airavata temporary security group for " + getProcessId())
                .vpcId(vpcId));

        ec2.authorizeSecurityGroupIngress(req -> req
                .groupId(sgRes.groupId())
                .ipPermissions(p -> p.ipProtocol("tcp").fromPort(22).toPort(22).ipRanges(r -> r.cidrIp("0.0.0.0/0")))); // TODO restrict the IP

        return sgRes.groupId();
    }
}
