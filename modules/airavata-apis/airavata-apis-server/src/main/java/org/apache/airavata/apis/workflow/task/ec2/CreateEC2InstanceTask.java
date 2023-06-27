package org.apache.airavata.apis.workflow.task.ec2;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.apache.airavata.api.execution.stubs.EC2Backend;
import org.apache.airavata.apis.workflow.task.common.BaseTask;
import org.apache.airavata.apis.workflow.task.common.annotation.TaskDef;
import org.apache.airavata.apis.workflow.task.common.annotation.TaskParam;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.common.UserTokenAuth;
import org.apache.airavata.mft.credential.stubs.s3.S3Secret;
import org.apache.airavata.mft.credential.stubs.s3.S3SecretGetRequest;
import org.apache.airavata.mft.credential.stubs.scp.SCPSecret;
import org.apache.airavata.mft.credential.stubs.scp.SCPSecretCreateRequest;
import org.apache.airavata.mft.credential.stubs.scp.SCPSecretGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@TaskDef(name = "CreateEC2InstanceTask")
public class CreateEC2InstanceTask extends BaseTask {

    public static final String EC2_INSTANCE_SECRET_ID = "EC2_INSTANCE_SECRET_ID";
    public static final String EC2_INSTANCE_ID = "EC2_INSTANCE_ID";
    public static final String EC2_INSTANCE_IP = "EC2_INSTANCE_IP";

    private final static Logger logger = LoggerFactory.getLogger(CreateEC2InstanceTask.class);

    @TaskParam(name = "ec2Backend")
    private ThreadLocal<EC2Backend> ec2Backend = new ThreadLocal<>();

    @TaskParam(name = "secretServiceHost")
    private ThreadLocal<String> secretServiceHost = new ThreadLocal<>();

    @TaskParam(name = "secretServicePort")
    private ThreadLocal<Integer> secretServicePort = new ThreadLocal<>();

    @TaskParam(name = "userToken")
    private ThreadLocal<String> userToken = new ThreadLocal<>();

    @Override
    public TaskResult onRun() throws Exception {

        String keyNamePrefix = "airavata-aws-agent-key-";
        String secGroupName = "AiravataSecurityGroup";
        String airavataKeyDir = System.getProperty("user.home") + File.separator + ".airavata" + File.separator + "keys";

        logger.info("Starting Create EC2 Instance Task {}", getTaskId());
        logger.info("EC2 Backend {}", getEc2Backend().toString());

        EC2Backend ec2BackendObj = getEc2Backend();
        try (SecretServiceClient secretServiceClient = SecretServiceClientBuilder
                .buildClient(getSecretServiceHost(), getSecretServicePort())) {

            S3Secret s3Secret = secretServiceClient.s3().getS3Secret(S3SecretGetRequest.newBuilder()
                    .setAuthzToken(AuthToken.newBuilder()
                            .setUserTokenAuth(UserTokenAuth.newBuilder().setToken(getUserToken()).build())
                            .build())
                    .setSecretId(ec2BackendObj.getAwsCredentialId()).build());

            BasicAWSCredentials awsCreds = new BasicAWSCredentials(s3Secret.getAccessKey(), s3Secret.getSecretKey());

            AmazonEC2 amazonEC2 = AmazonEC2ClientBuilder.standard().withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                            "https://ec2." + ec2BackendObj.getRegion() + ".amazonaws.com", ec2BackendObj.getRegion()))
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .build();

            DescribeSecurityGroupsRequest desSecGrp = new DescribeSecurityGroupsRequest();
            DescribeSecurityGroupsResult describeSecurityGroupsResult = amazonEC2.describeSecurityGroups(desSecGrp);
            List<SecurityGroup> securityGroups = describeSecurityGroupsResult.getSecurityGroups();
            boolean hasSecGroup = securityGroups.stream().anyMatch(sg -> sg.getGroupName().equals(secGroupName));

            if (!hasSecGroup) {
                CreateSecurityGroupRequest csgr = new CreateSecurityGroupRequest();
                csgr.withGroupName(secGroupName).withDescription("Airavata Security Group");

                CreateSecurityGroupResult createSecurityGroupResult = amazonEC2.createSecurityGroup(csgr);

                IpPermission ipPermission = new IpPermission();

                IpRange ipRange1 = new IpRange().withCidrIp("0.0.0.0/0");

                ipPermission.withIpv4Ranges(Collections.singletonList(ipRange1))
                        .withIpProtocol("tcp")
                        .withFromPort(22)
                        .withToPort(22);

                AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest =
                        new AuthorizeSecurityGroupIngressRequest();
                authorizeSecurityGroupIngressRequest.withGroupName(secGroupName)
                        .withIpPermissions(ipPermission);
                amazonEC2.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
            }

            String keyName = null;
            SCPSecret scpSecret = null;
            DescribeKeyPairsResult keyPairs = amazonEC2.describeKeyPairs();
            List<KeyPairInfo> keyPairsWithSecretRegistration = keyPairs.getKeyPairs().stream().filter(kp -> kp.getTags().stream()
                            .anyMatch(tg -> tg.getKey().equals("AIRAVATA_SECRET_ID")))
                    .collect(Collectors.toList());

            try {

                for (KeyPairInfo keyPairInfo : keyPairsWithSecretRegistration) {
                    Optional<Tag> secretTag = keyPairInfo.getTags().stream().filter(t -> t.getKey().equals("AIRAVATA_SECRET_ID")).findFirst();
                    if (secretTag.isPresent()) {
                        SCPSecret secret = secretServiceClient.scp().getSCPSecret(SCPSecretGetRequest.newBuilder()
                                .setAuthzToken(AuthToken.newBuilder()
                                        .setUserTokenAuth(UserTokenAuth.newBuilder()
                                                .setToken(getUserToken()).build()).build())
                                .setSecretId(secretTag.get().getValue()).build());

                        if (secret != null) {
                            keyName = keyPairInfo.getKeyName();
                            scpSecret = secret;
                            logger.info("Found previously created Key Pair {} with Airavata secret id {}", keyName, secret.getSecretId());
                        }
                    }
                }
            } catch (Exception e ){
                // Ignore
            }

            //if (availableKeyPair.isEmpty()) {
            if (keyName == null) {
                keyName = keyNamePrefix + UUID.randomUUID().toString();
                logger.info("Creating EC2 key pair with name {}", keyName);
                CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest();

                createKeyPairRequest.withKeyName(keyName);

                CreateKeyPairResult createKeyPairResult = amazonEC2.createKeyPair(createKeyPairRequest);

                KeyPair keyPair = createKeyPairResult.getKeyPair();

                String privateKey = keyPair.getKeyMaterial();

                scpSecret = secretServiceClient.scp()
                        .createSCPSecret(SCPSecretCreateRequest.newBuilder()
                                .setAuthzToken(AuthToken.newBuilder()
                                        .setUserTokenAuth(UserTokenAuth.newBuilder()
                                                .setToken(getUserToken()).build()).build())
                                .setUser(ec2BackendObj.getLoginUserName())
                                .setPrivateKey(privateKey).build());

                logger.info("Created SSH Secret {}", scpSecret.getSecretId());

                CreateTagsRequest tagsRequest = new CreateTagsRequest();
                tagsRequest.setResources(Collections.singletonList(createKeyPairResult.getKeyPair().getKeyPairId()));
                Tag secretIdTag = new Tag();
                secretIdTag.setKey("AIRAVATA_SECRET_ID");
                secretIdTag.setValue(scpSecret.getSecretId());
                tagsRequest.setTags(Collections.singletonList(secretIdTag));
                amazonEC2.createTags(tagsRequest);
                logger.info("Created tag on SSH keypair with secret id {}", scpSecret.getSecretId());
            }
            putUserContent(EC2_INSTANCE_SECRET_ID, scpSecret.getSecretId(), Scope.WORKFLOW);

            RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

            runInstancesRequest.withImageId(ec2BackendObj.getImageId())
                    .withInstanceType(InstanceType.T1Micro) // TODO Externalize
                    .withMinCount(1)
                    .withMaxCount(1)
                    .withKeyName(keyName)
                    .withTagSpecifications(
                            new TagSpecification().withResourceType(ResourceType.Instance)
                                    .withTags(new Tag().withKey("Type").withValue("Airavata"),
                                            new Tag().withKey("Task").withValue(getTaskId()),
                                            new Tag().withKey("Name").withValue("Airavata Application VM")))
                    .withSecurityGroups(secGroupName);

            logger.info("Launching the EC2 VM");
            RunInstancesResult result = amazonEC2.runInstances(runInstancesRequest);

            String instanceId = result.getReservation().getInstances().get(0).getInstanceId();
            putUserContent(EC2_INSTANCE_ID, instanceId, Scope.WORKFLOW);

            Thread.sleep(5000); // Waiting 5 seconds until instance details to be consistent in amazon side

            try {
                DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
                describeInstancesRequest.setInstanceIds(Collections.singletonList(instanceId));

                InstanceState instanceState = null;
                String publicIpAddress = null;

                logger.info("Waiting until instance {} is ready", instanceId);

                for (int i = 0; i < 30; i++) {
                    DescribeInstancesResult describeInstancesResult = amazonEC2.describeInstances(describeInstancesRequest);
                    Instance instance = describeInstancesResult.getReservations().get(0).getInstances().get(0);
                    instanceState = instance.getState();
                    publicIpAddress = instance.getPublicIpAddress();

                    logger.info("Instance state {}, public ip {}", instanceState.getName(), publicIpAddress);

                    if (instanceState.getName().equals("running") && publicIpAddress != null) {
                        break;
                    }
                    Thread.sleep(2000);
                }

                putUserContent(EC2_INSTANCE_IP, publicIpAddress, Scope.WORKFLOW);

                logger.info("Waiting 30 seconds until the ssh interface comes up in instance {}", instanceId);
                Thread.sleep(30000);
                logger.info("EC2 Instance is running...");

            } catch (Exception e) {
                logger.error("Failed preparing instance {}. Deleting the instance", instanceId, e);
                TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
                terminateInstancesRequest.setInstanceIds(Collections.singleton(instanceId));
                amazonEC2.terminateInstances(terminateInstancesRequest);
                throw e;
            }
        }

        return new TaskResult(TaskResult.Status.COMPLETED, "Completed");
    }

    @Override
    public void onCancel() throws Exception {

    }

    public EC2Backend getEc2Backend() {
        return ec2Backend.get();
    }

    public void setEc2Backend(EC2Backend ec2Backend) {
        this.ec2Backend.set(ec2Backend);
    }

    public String getSecretServiceHost() {
        return secretServiceHost.get();
    }

    public void setSecretServiceHost(String secretServiceHost) {
        this.secretServiceHost.set( secretServiceHost);
    }

    public Integer getSecretServicePort() {
        return secretServicePort.get();
    }

    public void setSecretServicePort(Integer secretServicePort) {
        this.secretServicePort.set(secretServicePort);
    }

    public String getUserToken() {
        return userToken.get();
    }

    public void setUserToken(String userToken) {
        this.userToken.set(userToken);
    }
}
