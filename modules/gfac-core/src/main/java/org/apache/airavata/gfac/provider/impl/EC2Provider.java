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
import org.apache.airavata.gfac.context.AmazonSecurityContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.bouncycastle.openssl.PEMWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
// TODO
// import com.sshtools.j2ssh.util.Base64;

public class EC2Provider implements GFacProvider {

    private static final Logger log = LoggerFactory.getLogger(EC2Provider.class);

    public static final int SLEEP_TIME_SECOND = 120;

    public static final String KEY_PAIR_NAME = "gfac";

    public static final String KEY_PAIR_FILE = "ec2_rsa";

    private static final String privateKeyFilePath = System.getProperty("user.home") + "/.ssh/" + KEY_PAIR_FILE;

    private AmazonSecurityContext amazonSecurityContext;

    public void initialize(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        if (jobExecutionContext != null) {
            if (jobExecutionContext.getSecurityContext() instanceof AmazonSecurityContext) {
                this.amazonSecurityContext = (AmazonSecurityContext) jobExecutionContext.getSecurityContext();
            } else {
                throw new GFacProviderException("Amazon Security Context is not set" + jobExecutionContext);
            }
        } else {
            throw new GFacProviderException("Job Execution Context is null" + jobExecutionContext);
        }
    }

    public void execute(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        Instance instance;

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

        try {
            /* Build key pair before start instance */
            buildKeyPair(ec2client);

            // right now, we can run it on one host
            if (amazonSecurityContext.getInstanceId() != null)
                instance = startInstances(ec2client, amazonSecurityContext.getInstanceId(),
                        amazonSecurityContext.getInstanceType(), jobExecutionContext).get(0);
            else {

                // already running instance
                DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
                DescribeInstancesResult describeInstancesResult =
                        ec2client.describeInstances(describeInstancesRequest.withInstanceIds(amazonSecurityContext.getInstanceId()));

                if (describeInstancesResult.getReservations().size() == 0 ||
                        describeInstancesResult.getReservations().get(0).getInstances().size() == 0) {
                    throw new GFacProviderException("Instance not found:" + amazonSecurityContext.getInstanceId());
                }

                instance = describeInstancesResult.getReservations().get(0).getInstances().get(0);

                // check instance keypair
                if (instance.getKeyName() == null || !instance.getKeyName().equals(KEY_PAIR_NAME))
                    throw new GFacProviderException("Keypair for instance:" + amazonSecurityContext.getInstanceId() +
                            " is not valid");
            }

            //TODO send out instance id
            //execContext.getNotificationService().sendResourceMappingNotifications(this.instance.getPublicDnsName(), "EC2 Instance " + this.instance.getInstanceId() + " is running with public name " + this.instance.getPublicDnsName(), this.instance.getInstanceId());


            /*
             * Make sure port 22 is connectable
             */
            for (GroupIdentifier g : instance.getSecurityGroups()) {
                IpPermission ip = new IpPermission();
                ip.setIpProtocol("tcp");
                ip.setFromPort(22);
                ip.setToPort(22);
                AuthorizeSecurityGroupIngressRequest r = new AuthorizeSecurityGroupIngressRequest();
                r = r.withIpPermissions(ip.withIpRanges("0.0.0.0/0"));
                r.setGroupId(g.getGroupId());
                try {
                    ec2client.authorizeSecurityGroupIngress(r);
                } catch (AmazonServiceException as) {
                    /*
                     * If exception is from duplicate room, ignore it.
                     */
                    if (!as.getErrorCode().equals("InvalidPermission.Duplicate"))
                        throw as;
                }
            }

        } catch (Exception e) {
            throw new GFacProviderException("Invalied Request",e,jobExecutionContext);
        }

    }

    public void dispose(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private List<Instance> startInstances(AmazonEC2Client ec2, String AMI_ID, String INS_TYPE, JobExecutionContext jobExecutionContext) throws AmazonServiceException {
        // start only 1 instance
        RunInstancesRequest request = new RunInstancesRequest(AMI_ID, 1, 1);
        request.setKeyName(KEY_PAIR_NAME);
        request.setInstanceType(INS_TYPE);

        RunInstancesResult result = ec2.runInstances(request);

        List<Instance> instances = result.getReservation().getInstances();

        while (!allInstancesStateEqual(instances, InstanceStateName.Running)) {

            // instance status should not be Terminated
            if (anyInstancesStateEqual(instances, InstanceStateName.Terminated)) {
                throw new AmazonClientException("Some Instance is terminated before running a job");
            }

            // notify the status
            for (Instance ins: instances) {
                // TODO
                //executionContext.getNotificationService().info("EC2 Instance " +ins.getInstanceId() + " is " + ins.getState().getName().toString());
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

    private void buildKeyPair(AmazonEC2Client ec2) throws NoSuchAlgorithmException, InvalidKeySpecException, AmazonServiceException, AmazonClientException, IOException {

        boolean newKey = false;

        File privateKeyFile = new File(privateKeyFilePath);
        File publicKeyFile = new File(privateKeyFilePath + ".pub");

        /*
         * Check if Keypair already created on the server
         */
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
                fos = new FileOutputStream(privateKeyFilePath + ".pub");
                // TODO
                //fos.write(Base64.encodeBytes(keypair.getPublic().getEncoded(), true).getBytes());
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
                fos = new FileOutputStream(privateKeyFilePath);
                StringWriter stringWriter = new StringWriter();

                /*
                 * Write in PEM format (openssl support)
                 */
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

        /*
         * Read Public Key
         */
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

        /*
         * Generate key pair in Amazon if necessary
         */
        try {
            /*
             * Get current key pair in Amazon
             */
            DescribeKeyPairsRequest describeKeyPairsRequest = new DescribeKeyPairsRequest();
            ec2.describeKeyPairs(describeKeyPairsRequest.withKeyNames(KEY_PAIR_NAME));

            /*
             * If key exists and new key is created, delete old key and replace
             * with new one. Else, do nothing
             */

            if (newKey) {
                DeleteKeyPairRequest deleteKeyPairRequest = new DeleteKeyPairRequest(KEY_PAIR_NAME);
                ec2.deleteKeyPair(deleteKeyPairRequest);
                ImportKeyPairRequest importKeyPairRequest = new ImportKeyPairRequest(KEY_PAIR_NAME, encodedPublicKey);
                ec2.importKeyPair(importKeyPairRequest);
            }

        } catch (AmazonServiceException ase) {
            /*
             * Key doesn't exists, import new key.
             */
            if(ase.getErrorCode().equals("InvalidKeyPair.NotFound")){
                ImportKeyPairRequest importKeyPairRequest = new ImportKeyPairRequest(KEY_PAIR_NAME, encodedPublicKey);
                ec2.importKeyPair(importKeyPairRequest);
            }else{
                throw ase;
            }
        }
    }

    private boolean anyInstancesStateEqual(List<Instance> instances, InstanceStateName name) {
        for (Iterator<Instance> iterator = instances.iterator(); iterator.hasNext();) {
            Instance instance = (Instance) iterator.next();

            // if one of instance is not running, return false
            if (InstanceStateName.fromValue(instance.getState().getName()) == name) {
                return true;
            }
        }
        return false;
    }

    private boolean allInstancesStateEqual(List<Instance> instances, InstanceStateName name) {
        for (Iterator<Instance> iterator = instances.iterator(); iterator.hasNext();) {
            Instance instance = (Instance) iterator.next();

            // if one of instance is not running, return false
            if (InstanceStateName.fromValue(instance.getState().getName()) != name) {
                return false;
            }
        }
        return true;
    }

    private List<String> getInstanceIDs(List<Instance> instances) {
        List<String> ret = new ArrayList<String>();
        for (Iterator<Instance> iterator = instances.iterator(); iterator.hasNext();) {
            Instance instance = (Instance) iterator.next();
            ret.add(instance.getInstanceId());
        }
        return ret;
    }

}
