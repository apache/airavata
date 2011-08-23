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

package org.apache.airavata.core.gfac.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.Base64;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

import org.apache.airavata.core.gfac.context.ExecutionContext;
import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.context.impl.AmazonSecurityContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.GfacException.FaultCode;
import org.apache.airavata.core.gfac.notification.NotificationService;
import org.apache.airavata.core.gfac.type.HostDescription;
import org.apache.airavata.core.gfac.type.app.ShellApplicationDeployment;
import org.apache.airavata.core.gfac.utils.GFacConstants;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.core.gfac.utils.OutputUtils;
import org.bouncycastle.openssl.PEMWriter;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.DeleteKeyPairRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.ImportKeyPairRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;

import edu.indiana.extreme.lead.workflow_tracking.common.DurationObj;

public class EC2Provider extends AbstractProvider {

    public static final String AMAZON_SECURITY_CONTEXT = "amazon";

    public static final int SLEEP_TIME_SECOND = 120;

    public static final String KEY_PAIR_NAME = "gfac";

    public static final String KEY_PAIR_FILE = "ec2_rsa";

    private static final String privateKeyFilePath = System.getProperty("user.home") + "/.ssh/" + KEY_PAIR_FILE;

    private Instance instance;

    private static final String SPACE = " ";

    private String buildCommand(List<String> cmdList) {
        StringBuffer buff = new StringBuffer();
        for (String string : cmdList) {
            buff.append(string);
            buff.append(SPACE);
        }
        return buff.toString();
    }

    public void initialize(InvocationContext context) throws GfacException {
        HostDescription host = context.getGfacContext().getHost();
        ShellApplicationDeployment app = (ShellApplicationDeployment)context.getGfacContext().getApp();

        AmazonSecurityContext amazonSecurityContext = ((AmazonSecurityContext) context
                .getSecurityContext(AMAZON_SECURITY_CONTEXT));
        String access_key = amazonSecurityContext.getAccessKey();
        String secret_key = amazonSecurityContext.getSecretKey();

        // TODO way to read value (header or xregistry)
        String ami_id = "";
        String ins_type = "";
        String ins_id = "";

        /*
         * Need to start EC2 instance before running it
         */
        AWSCredentials credential = new BasicAWSCredentials(access_key, secret_key);
        AmazonEC2Client ec2client = new AmazonEC2Client(credential);

        try {
            /*
             * Build key pair before start instance
             */
            buildKeyPair(ec2client);

            // right now, we can run it on one host
            if (ami_id != null)
                this.instance = startInstances(ec2client, ami_id, ins_type,
                        context.getExecutionContext().getNotificationService()).get(0);
            else {
                // already running instance
                DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
                DescribeInstancesResult describeInstancesResult = ec2client.describeInstances(describeInstancesRequest
                        .withInstanceIds(ins_id));

                if (describeInstancesResult.getReservations().size() == 0
                        || describeInstancesResult.getReservations().get(0).getInstances().size() == 0) {
                    throw new GfacException("Instance not found:" + ins_id, FaultCode.InvalidRequest);
                }

                this.instance = describeInstancesResult.getReservations().get(0).getInstances().get(0);

                // check instance keypair
                if (this.instance.getKeyName() == null || !this.instance.getKeyName().equals(KEY_PAIR_NAME))
                    throw new GfacException("Keypair for instance:" + ins_id + " is not valid",
                            FaultCode.InvalidRequest);
            }

            // send out instance id
            context
                    .getExecutionContext()
                    .getNotificationService()
                    .sendResourceMappingNotifications(
                            this.instance.getPublicDnsName(),
                            "EC2 Instance " + this.instance.getInstanceId() + " is running with public name "
                                    + this.instance.getPublicDnsName(), this.instance.getInstanceId());

            /*
             * Make sure port 22 is connectable
             */
            for (GroupIdentifier g : this.instance.getSecurityGroups()) {
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
            // TODO throw out
            e.printStackTrace();
            log.error(e.getMessage(), e);
            throw new GfacException(e, FaultCode.InvalidRequest);
        }

        // set Host location
        host.setName(this.instance.getPublicDnsName());

        /*
         * Make directory
         */
        SSHClient ssh = new SSHClient();
        try {
            ssh.loadKnownHosts();
            ssh.connect(this.instance.getPublicDnsName());

            ssh.authPublickey(privateKeyFilePath);
            final Session session = ssh.startSession();
            try {
                StringBuilder command = new StringBuilder();
                command.append("mkdir -p ");
                command.append(app.getTmpDir());
                command.append(" | ");
                command.append("mkdir -p ");
                command.append(app.getWorkingDir());
                command.append(" | ");
                command.append("mkdir -p ");
                command.append(app.getInputDir());
                command.append(" | ");
                command.append("mkdir -p ");
                command.append(app.getOutputDir());
                Command cmd = session.exec(command.toString());
                cmd.join(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw e;
            } finally {
                try {
                    session.close();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            throw new GfacException(e.getMessage(), e);
        } finally {
            try {
                ssh.disconnect();
            } catch (Exception e) {
            }
        }
    }

    public void execute(InvocationContext context) throws GfacException {
        HostDescription host = context.getGfacContext().getHost();
        ShellApplicationDeployment app = (ShellApplicationDeployment)context.getGfacContext().getApp();

     // input parameter
        ArrayList<String> tmp = new ArrayList<String>();
        for (Iterator<String> iterator = context.getMessageContext("input").getParameterNames(); iterator.hasNext();) {
            String key = iterator.next();
            tmp.add(context.getMessageContext("input").getStringParameterValue(key));
        }
        
        List<String> cmdList = new ArrayList<String>();

        SSHClient ssh = new SSHClient();
        try {

            /*
             * Notifier
             */
            NotificationService notifier = context.getExecutionContext().getNotificationService();

            /*
             * Builder Command
             */
            cmdList.add(app.getExecutable());
            cmdList.addAll(tmp);

            // create process builder from command
            String command = buildCommand(cmdList);

            // redirect StdOut and StdErr
            command += SPACE + "1>" + SPACE + app.getStdOut();
            command += SPACE + "2>" + SPACE + app.getStdErr();

            // get the env of the host and the application
            Map<String, String> nv = app.getEnv();

            // extra env's
            nv.put(GFacConstants.INPUT_DATA_DIR_VAR_NAME, app.getInputDir());
            nv.put(GFacConstants.OUTPUT_DATA_DIR_VAR_NAME, app.getOutputDir());
            
            // log info
            log.info("Command = " + buildCommand(cmdList));
            for (String key : nv.keySet()) {
                log.info("Env[" + key + "] = " + nv.get(key));
            }

            // notify start
            DurationObj compObj = notifier.computationStarted();

            /*
             * Create ssh connection
             */
            ssh.loadKnownHosts();
            ssh.connect(host.getName());
            ssh.authPublickey(privateKeyFilePath);

            final Session session = ssh.startSession();
            try {
                /*
                 * Build working Directory
                 */
                log.info("WorkingDir = " + app.getWorkingDir());
                session.exec("mkdir -p " + app.getWorkingDir());
                session.exec("cd " + app.getWorkingDir());

                /*
                 * Set environment
                 */
                for (String key : nv.keySet()) {
                    session.setEnvVar(key, nv.get(key));
                }

                /*
                 * Execute
                 */
                Command cmd = session.exec(command);
                log.info("stdout=" + GfacUtils.readFromStream(session.getInputStream()));
                cmd.join(5, TimeUnit.SECONDS);

                // notify end
                notifier.computationFinished(compObj);

                /*
                 * check return value. usually not very helpful to draw conclusions based on return values so don't
                 * bother. just provide warning in the log messages
                 */
                if (cmd.getExitStatus() != 0) {
                    log.error("Process finished with non zero return value. Process may have failed");
                } else {
                    log.info("Process finished with return value of zero.");
                }

                File logDir = new File("./service_logs");
                if (!logDir.exists()) {
                    logDir.mkdir();
                }

                // Get the Stdouts and StdErrs
                QName x = QName.valueOf(context.getServiceName());
                String timeStampedServiceName = GfacUtils.createServiceDirName(x);
                File localStdOutFile = new File(logDir, timeStampedServiceName + ".stdout");
                File localStdErrFile = new File(logDir, timeStampedServiceName + ".stderr");

                SCPFileTransfer fileTransfer = ssh.newSCPFileTransfer();
                fileTransfer.download(app.getStdOut(), localStdOutFile.getAbsolutePath());
                fileTransfer.download(app.getStdErr(), localStdErrFile.getAbsolutePath());

                String stdOutStr = GfacUtils.readFile(localStdOutFile.getAbsolutePath());
                String stdErrStr = GfacUtils.readFile(localStdErrFile.getAbsolutePath());

                // set to context
                OutputUtils.fillOutputFromStdout(context.getMessageContext("output"), stdOutStr, stdErrStr);

            } catch (Exception e) {
                throw e;
            } finally {
                try {
                    session.close();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            throw new GfacException(e.getMessage(), e);
        } finally {
            try {
                ssh.disconnect();
            } catch (Exception e) {
            }
        }
    }

    public void dispose(InvocationContext invocationContext) throws GfacException {
        // TODO Auto-generated method stub

    }

    public void abort(InvocationContext invocationContext) throws GfacException {
        // TODO Auto-generated method stub

    }

    private List<Instance> startInstances(AmazonEC2Client ec2, String AMI_ID, String INS_TYPE,
            NotificationService notifier) throws AmazonServiceException {
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
            for (Instance ins : instances) {
                notifier.info("EC2 Instance " + ins.getInstanceId() + " is " + ins.getState().getName().toString());
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

        log.info("All instances is running");
        return instances;
    }

    private void buildKeyPair(AmazonEC2Client ec2) throws NoSuchAlgorithmException, InvalidKeySpecException,
            AmazonServiceException, AmazonClientException, IOException {

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
            KeyPair keypair = keyGen.genKeyPair();

            FileOutputStream fos = null;

            // Store Public Key.
            try {
                fos = new FileOutputStream(privateKeyFilePath + ".pub");
                fos.write(Base64.encodeBytes(keypair.getPublic().getEncoded()).getBytes());
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
             * If key exists and new key is created, delete old key and replace with new one. Else, do nothing
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
            if (ase.getErrorCode().equals("InvalidKeyPair.NotFound")) {
                ImportKeyPairRequest importKeyPairRequest = new ImportKeyPairRequest(KEY_PAIR_NAME, encodedPublicKey);
                ec2.importKeyPair(importKeyPairRequest);
            } else {
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
