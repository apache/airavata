package org.apache.airavata.apis.workflow.task.ec2;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import org.apache.airavata.api.execution.stubs.EC2Backend;
import org.apache.airavata.apis.workflow.task.common.BaseTask;
import org.apache.airavata.apis.workflow.task.common.annotation.TaskDef;
import org.apache.airavata.apis.workflow.task.common.annotation.TaskParam;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.common.UserTokenAuth;
import org.apache.airavata.mft.credential.stubs.s3.S3Secret;
import org.apache.airavata.mft.credential.stubs.s3.S3SecretGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@TaskDef(name = "DestroyEC2InstanceTask")
public class DestroyEC2InstanceTask extends BaseTask {

    private final static Logger logger = LoggerFactory.getLogger(DestroyEC2InstanceTask.class);

    @TaskParam(name = "ec2Backend")
    private ThreadLocal<EC2Backend> ec2Backend = new ThreadLocal<>();

    @TaskParam(name = "secretServiceHost")
    private ThreadLocal<String> secretServiceHost = new ThreadLocal<>();

    @TaskParam(name = "secretServicePort")
    private ThreadLocal<Integer> secretServicePort = new ThreadLocal<>();

    @TaskParam(name = "userToken")
    private ThreadLocal<String> userToken = new ThreadLocal<>();

    @TaskParam(name = "instanceId")
    private ThreadLocal<String> instanceId = new ThreadLocal<>();

    @Override
    public TaskResult onRun() throws Exception {

        logger.info("Destroying the instance {}", getInstanceId());
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

            TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
            terminateInstancesRequest.setInstanceIds(Collections.singleton(getInstanceId()));
            amazonEC2.terminateInstances(terminateInstancesRequest);
            logger.info("EC2 instance {} was successfully destroyed", getInstanceId());
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

    public String getInstanceId() {
        return instanceId.get();
    }

    public void setInstanceId(String instanceId) {
        this.instanceId.set(instanceId);
    }
}
