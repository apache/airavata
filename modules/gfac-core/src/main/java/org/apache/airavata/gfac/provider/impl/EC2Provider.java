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

package org.apache.airavata.gfac.provider.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;
import com.sshtools.j2ssh.util.Base64;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.security.AmazonSecurityContext;
import org.apache.airavata.gfac.notification.events.EC2ProviderEvent;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.gfac.provider.utils.ProviderUtils;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.Ec2ApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.bouncycastle.openssl.PEMWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EC2Provider implements GFacProvider {

    private static final Logger log = LoggerFactory.getLogger(EC2Provider.class);

    public static final int SLEEP_TIME_SECOND = 120;

    public static final int SOCKET_TIMEOUT = 30000;

    public static final int SSH_PORT = 22;

    public static final String KEY_PAIR_FILE = "ec2_rsa";

    private static final String PRIVATE_KEY_FILE_PATH = System.getProperty("user.home") + "/.ssh/" + KEY_PAIR_FILE;

    private Instance instance = null;

    private AmazonSecurityContext amazonSecurityContext;

    public void initialize(JobExecutionContext jobExecutionContext) throws GFacProviderException,GFacException{
        if (jobExecutionContext != null) {
            if (jobExecutionContext.getSecurityContext(AmazonSecurityContext.AMAZON_SECURITY_CONTEXT)
                    instanceof AmazonSecurityContext) {
                this.amazonSecurityContext = (AmazonSecurityContext) jobExecutionContext.
                        getSecurityContext(AmazonSecurityContext.AMAZON_SECURITY_CONTEXT);
            } else {
                throw new GFacProviderException("Amazon Security Context is not set" + jobExecutionContext);
            }
        } else {
            throw new GFacProviderException("Job Execution Context is null" + jobExecutionContext);
        }

        if (log.isDebugEnabled()) {
            log.debug("ACCESS_KEY:" + amazonSecurityContext.getAccessKey());
            log.debug("SECRET_KEY:" + amazonSecurityContext.getSecretKey());
            log.debug("AMI_ID:" + amazonSecurityContext.getAmiId());
            log.debug("INS_ID:" + amazonSecurityContext.getInstanceId());
            log.debug("INS_TYPE:" + amazonSecurityContext.getInstanceType());
            log.debug("USERNAME:" + amazonSecurityContext.getUserName());
        }

        /* Validation */
        if (amazonSecurityContext.getAccessKey() == null || amazonSecurityContext.getAccessKey().isEmpty())
            throw new GFacProviderException("EC2 Access Key is empty", jobExecutionContext);
        if (amazonSecurityContext.getSecretKey() == null || amazonSecurityContext.getSecretKey().isEmpty())
            throw new GFacProviderException("EC2 Secret Key is empty", jobExecutionContext);
        if ((amazonSecurityContext.getAmiId() == null && amazonSecurityContext.getInstanceId() == null) ||
                (amazonSecurityContext.getAmiId() != null && amazonSecurityContext.getAmiId().isEmpty()) ||
                (amazonSecurityContext.getInstanceId() != null && amazonSecurityContext.getInstanceId().isEmpty()))
            throw new GFacProviderException("EC2 AMI or Instance ID is empty", jobExecutionContext);
        if (amazonSecurityContext.getUserName() == null || amazonSecurityContext.getUserName().isEmpty())
            throw new GFacProviderException("EC2 Username is empty", jobExecutionContext);

        /* Need to start EC2 instance before running it */
        AWSCredentials credential =
                new BasicAWSCredentials(amazonSecurityContext.getAccessKey(), amazonSecurityContext.getSecretKey());
        AmazonEC2Client ec2client = new AmazonEC2Client(credential);

        initEc2Environment(jobExecutionContext, ec2client);
        checkConnection(instance, ec2client);
    }

    public void execute(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        String shellCmd = createShellCmd(jobExecutionContext);
        SshClient sshClient = new SshClient();
        sshClient.setSocketTimeout(SOCKET_TIMEOUT);
        SshConnectionProperties properties = new SshConnectionProperties();
        properties.setHost(this.instance.getPublicDnsName());
        properties.setPort(SSH_PORT);

        // Connect to the host
        try
        {
            String outParamName;
            OutputParameterType[] outputParametersArray = jobExecutionContext.getApplicationContext().
                    getServiceDescription().getType().getOutputParametersArray();
            if(outputParametersArray != null) {
                outParamName = outputParametersArray[0].getParameterName();
            } else {
                throw new GFacProviderException("Output parameter name is not set. Therefore, not being able " +
                        "to filter the job result from standard out ", jobExecutionContext);
            }

            sshClient.connect(properties, new HostKeyVerification() {
                public boolean verifyHost(String s, SshPublicKey sshPublicKey) throws TransportProtocolException {
                    log.debug("Verifying Host: " + s);
                    return true;
                }
            });

            // Initialize the authentication data.
            PublicKeyAuthenticationClient publicKeyAuth = new PublicKeyAuthenticationClient();
            publicKeyAuth.setUsername(amazonSecurityContext.getUserName());
            SshPrivateKeyFile file = SshPrivateKeyFile.parse(new File(PRIVATE_KEY_FILE_PATH));
            SshPrivateKey privateKey = file.toPrivateKey("");
            publicKeyAuth.setKey(privateKey);

            // Authenticate
            int result = sshClient.authenticate(publicKeyAuth);
            if(result== AuthenticationProtocolState.FAILED) {
                throw new GFacProviderException("The authentication failed", jobExecutionContext);
            } else if(result==AuthenticationProtocolState.PARTIAL) {
                throw new GFacProviderException("The authentication succeeded but another"
                        + "authentication is required", jobExecutionContext);
            } else if(result==AuthenticationProtocolState.COMPLETE) {
                log.info("ssh client authentication is complete...");
            }

            SessionChannelClient session = sshClient.openSessionChannel();
            log.info("ssh session successfully opened...");
            session.requestPseudoTerminal("vt100", 80, 25, 0, 0, "");
            session.startShell();
            session.getOutputStream().write(shellCmd.getBytes());

            InputStream in = session.getInputStream();
            byte buffer[] = new byte[255];
            int read;
            String executionResult = "";
            while((read = in.read(buffer)) > 0) {
                String out = new String(buffer, 0, read);
//                System.out.println(out);

                if(out.startsWith(outParamName)) {
                    executionResult = out.split("=")[1];
                    log.debug("Result found in the StandardOut ");
                    break;
                }
            }

            executionResult = executionResult.replace("\r","").replace("\n","");
            log.info("Result of the job : " + executionResult);

            for(OutputParameterType outparamType : outputParametersArray){
                /* Assuming that there is just a single result. If you want to add more results, update the necessary
                   logic below */
                String paramName = outparamType.getParameterName();
                ActualParameter outParam = new ActualParameter();
                outParam.getType().changeType(StringParameterType.type);
                ((StringParameterType) outParam.getType()).setValue(executionResult);
                jobExecutionContext.getOutMessageContext().addParameter(paramName, outParam);
            }

        } catch (InvalidSshKeyException e) {
            throw new GFacProviderException("Invalid SSH key", e);
        } catch (IOException e) {
            throw new GFacProviderException("Error in occurred during IO", e);
        } catch (Exception e) {
            throw new GFacProviderException("Error parsing standard out for job execution result", e);
        }

    }

    /**
     * Creates the command to be executed in the remote shell.
     *
     * @param jobExecutionContext JobExecutionContext for the cloud job
     * @return shell command to be executed
     * @throws GFacProviderException GFacProviderException
     */
    private String createShellCmd(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        String command = "";
        ApplicationDescription appDesc = jobExecutionContext.getApplicationContext().
                getApplicationDeploymentDescription();

        if(appDesc.getType() instanceof Ec2ApplicationDeploymentType) {
            Ec2ApplicationDeploymentType type = (Ec2ApplicationDeploymentType) appDesc.getType();
            if(type.getExecutable() != null) {
                command = type.getExecutableType() + " " + type.getExecutable();
            } else {
                command = "sh" + " " + type.getExecutable();
            }
            command = setCmdParams(jobExecutionContext, command);

        } else {
            ApplicationDeploymentDescriptionType type = appDesc.getType();
            command = "sh" + " " + type.getExecutableLocation();
            command = setCmdParams(jobExecutionContext, command);
        }

        return command + '\n';
    }

    private String setCmdParams(JobExecutionContext jobExecutionContext, String command) throws GFacProviderException {
        List<String> inputParams = null;
        try {
            inputParams = ProviderUtils.getInputParameters(jobExecutionContext);
        } catch (GFacProviderException e) {
            throw new GFacProviderException("Error in extracting input values from JobExecutionContext");
        }

        for(String param : inputParams){
            command = " " + command + " " + param;
        }

        log.info("Command to be executed on EC2 : " + command);
        return command;
    }

    public void dispose(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        // Do nothing
    }

    /**
     * Checks whether the port 22 of the Amazon instance is accessible.
     *
     * @param instance Amazon instance id.
     * @param ec2client AmazonEC2Client object
     */
    private void checkConnection(Instance instance, AmazonEC2Client ec2client) {
        /* Make sure port 22 is connectible */
        for (GroupIdentifier g : instance.getSecurityGroups()) {
            IpPermission ip = new IpPermission();
            ip.setIpProtocol("tcp");
            ip.setFromPort(SSH_PORT);
            ip.setToPort(SSH_PORT);
            AuthorizeSecurityGroupIngressRequest r = new AuthorizeSecurityGroupIngressRequest();
            r = r.withIpPermissions(ip.withIpRanges("0.0.0.0/0"));
            r.setGroupId(g.getGroupId());
            try {
                ec2client.authorizeSecurityGroupIngress(r);
            } catch (AmazonServiceException as) {
                /* If exception is from duplicate room, ignore it. */
                if (!as.getErrorCode().equals("InvalidPermission.Duplicate"))
                    throw as;
            }
        }
    }

    /**
     * Initializes the Amazon EC2 environment needed to run the Cloud job submission. This will bring
     * up an Amazon instance (out of an AMI) or use an existing instance id.
     *
     * @param jobExecutionContext Job execution context.
     * @param ec2client EC2 Client.
     * @return instance id of the running Amazon instance.
     * @throws GFacProviderException
     */
    private void initEc2Environment(JobExecutionContext jobExecutionContext, AmazonEC2Client ec2client)
            throws GFacProviderException {
//        Instance instance;
        try {
            /* Build key pair before start instance */
            buildKeyPair(ec2client);

            // right now, we can run it on one host
            if (amazonSecurityContext.getAmiId() != null)
                instance = startInstances(ec2client, amazonSecurityContext.getAmiId(),
                        amazonSecurityContext.getInstanceType(), jobExecutionContext).get(0);
            else {

                // already running instance
                DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
                DescribeInstancesResult describeInstancesResult =
                        ec2client.describeInstances(describeInstancesRequest.
                                withInstanceIds(amazonSecurityContext.getInstanceId()));

                if (describeInstancesResult.getReservations().size() == 0 ||
                        describeInstancesResult.getReservations().get(0).getInstances().size() == 0) {
                    throw new GFacProviderException("Instance not found:" + amazonSecurityContext.getInstanceId());
                }

                instance = describeInstancesResult.getReservations().get(0).getInstances().get(0);

                // check instance keypair
                if (instance.getKeyName() == null || !instance.getKeyName().equals(KEY_PAIR_FILE)) {
                    throw new GFacProviderException("Keypair for instance:" + amazonSecurityContext.getInstanceId() +
                            " is not valid");
                }
            }

            jobExecutionContext.getNotificationService().publish(new EC2ProviderEvent("EC2 Instance " +
                    this.instance.getInstanceId() + " is running with public name " + this.instance.getPublicDnsName()));

        } catch (Exception e) {
            throw new GFacProviderException("Invalid Request",e,jobExecutionContext);
        }
//        return instance;
    }

    private List<Instance> startInstances(AmazonEC2Client ec2, String amiId, String insType,
                                          JobExecutionContext jobExecutionContext)
            throws AmazonServiceException {
        // start only 1 instance
        RunInstancesRequest request = new RunInstancesRequest(amiId, 1, 1);
        request.setKeyName(KEY_PAIR_FILE);
        request.setInstanceType(insType);

        RunInstancesResult result = ec2.runInstances(request);

        List<Instance> instances = result.getReservation().getInstances();

        while (!allInstancesStateEqual(instances, InstanceStateName.Running)) {

            // instance status should not be Terminated
            if (anyInstancesStateEqual(instances, InstanceStateName.Terminated)) {
                throw new AmazonClientException("Some Instance is terminated before running a job");
            }

            // notify the status
            for (Instance ins: instances) {
                jobExecutionContext.getNotificationService().publish(new EC2ProviderEvent("EC2 Instance " +
                        ins.getInstanceId() + " is " + ins.getState().getName()));
            }

            try {
                Thread.sleep(SLEEP_TIME_SECOND * 1000l);
            } catch (Exception ex) {
                // no op
            }

            DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
            describeInstancesRequest.setInstanceIds(getInstanceIDs(instances));

            DescribeInstancesResult describeInstancesResult = ec2.describeInstances(describeInstancesRequest);
            instances = describeInstancesResult.getReservations().get(0).getInstances();
        }

        log.info("All amazon instances are running");
        return instances;
    }

    private void buildKeyPair(AmazonEC2Client ec2) throws NoSuchAlgorithmException, InvalidKeySpecException,
            AmazonServiceException, AmazonClientException, IOException {
        boolean newKey = false;

        File privateKeyFile = new File(PRIVATE_KEY_FILE_PATH);
        File publicKeyFile = new File(PRIVATE_KEY_FILE_PATH + ".pub");

        /* Check if Key-pair already created on the server */
        if (!privateKeyFile.exists()) {

            // check folder and create if it does not exist
            File sshDir = new File(System.getProperty("user.home") + "/.ssh/");
            if (!sshDir.exists())
                sshDir.mkdir();

            // Generate a 1024-bit RSA key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            java.security.KeyPair keypair = keyGen.genKeyPair();

            FileOutputStream fos = null;

            // Store Public Key.
            try {
                fos = new FileOutputStream(PRIVATE_KEY_FILE_PATH + ".pub");
                fos.write(Base64.encodeBytes(keypair.getPublic().getEncoded(), true).getBytes());
            } catch (IOException ioe) {
                throw ioe;
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                        fos = null;
                    } catch (IOException ioe) {
                        throw ioe;
                    }
                }
            }

            // Store Private Key.
            try {
                fos = new FileOutputStream(PRIVATE_KEY_FILE_PATH);
                StringWriter stringWriter = new StringWriter();

                /* Write in PEM format (openssl support) */
                PEMWriter pemFormatWriter = new PEMWriter(stringWriter);
                pemFormatWriter.writeObject(keypair.getPrivate());
                pemFormatWriter.close();
                fos.write(stringWriter.toString().getBytes());
            } catch (IOException ioe) {
                throw ioe;
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                        fos = null;
                    } catch (IOException ioe) {
                        throw ioe;
                    }
                }
            }

            privateKeyFile.setWritable(false, false);
            privateKeyFile.setExecutable(false, false);
            privateKeyFile.setReadable(false, false);
            privateKeyFile.setReadable(true);
            privateKeyFile.setWritable(true);

            // set that this key is just created
            newKey = true;
        }

        /* Read Public Key */
        String encodedPublicKey = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(publicKeyFile));
            encodedPublicKey = br.readLine();
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            if (br != null) {
                try {
                    br.close();
                    br = null;
                } catch (IOException ioe) {
                    throw ioe;
                }
            }
        }

        /* Generate key pair in Amazon if necessary */
        try {
            /* Get current key pair in Amazon */
            DescribeKeyPairsRequest describeKeyPairsRequest = new DescribeKeyPairsRequest();
            ec2.describeKeyPairs(describeKeyPairsRequest.withKeyNames(KEY_PAIR_FILE));

            /* If key exists and new key is created, delete old key and replace
             * with new one. Else, do nothing */
            if (newKey) {
                DeleteKeyPairRequest deleteKeyPairRequest = new DeleteKeyPairRequest(KEY_PAIR_FILE);
                ec2.deleteKeyPair(deleteKeyPairRequest);
                ImportKeyPairRequest importKeyPairRequest = new ImportKeyPairRequest(KEY_PAIR_FILE, encodedPublicKey);
                ec2.importKeyPair(importKeyPairRequest);
            }

        } catch (AmazonServiceException ase) {
            /* Key doesn't exists, import new key. */
            if(ase.getErrorCode().equals("InvalidKeyPair.NotFound")){
                ImportKeyPairRequest importKeyPairRequest = new ImportKeyPairRequest(KEY_PAIR_FILE, encodedPublicKey);
                ec2.importKeyPair(importKeyPairRequest);
            }else{
                throw ase;
            }
        }
    }

    private boolean anyInstancesStateEqual(List<Instance> instances, InstanceStateName name) {
        for (Instance instance : instances) {
            // if one of instance is not running, return false
            if (InstanceStateName.fromValue(instance.getState().getName()) == name) {
                return true;
            }
        }
        return false;
    }

    private boolean allInstancesStateEqual(List<Instance> instances, InstanceStateName name) {
        for (Instance instance : instances) {
            // if one of instance is not running, return false
            if (InstanceStateName.fromValue(instance.getState().getName()) != name) {
                return false;
            }
        }
        return true;
    }

    private List<String> getInstanceIDs(List<Instance> instances) {
        List<String> ret = new ArrayList<String>();
        for (Instance instance : instances) {
            ret.add(instance.getInstanceId());
        }
        return ret;
    }
    public void initProperties(Map<String, String> properties) throws GFacProviderException, GFacException {

    }

}
