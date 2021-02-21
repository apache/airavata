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
 */
package org.apache.airavata.helix.agent.ssh;

import com.jcraft.jsch.*;
import org.apache.airavata.agents.api.*;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class SshAgentAdaptor implements AgentAdaptor {

    private final static Logger logger = LoggerFactory.getLogger(SshAgentAdaptor.class);

    private Session session = null;

    public void init(AdaptorParams adaptorParams) throws AgentException {

        if (adaptorParams instanceof SshAdaptorParams) {
            SshAdaptorParams params = SshAdaptorParams.class.cast(adaptorParams);
            JSch jSch = new JSch();
            try {

                if (params.getPassword() != null) {
                    this.session = jSch.getSession(params.getUserName(), params.getHostName(), params.getPort());
                    session.setPassword(params.getPassword());
                    session.setUserInfo(new SftpUserInfo(params.getPassword()));
                } else {
                    jSch.addIdentity(UUID.randomUUID().toString(), params.getPrivateKey(), params.getPublicKey(),
                            params.getPassphrase().getBytes());
                    this.session = jSch.getSession(params.getUserName(), params.getHostName(),
                            params.getPort());
                    session.setUserInfo(new DefaultUserInfo(params.getUserName(), null, params.getPassphrase()));
                }

                if (params.isStrictHostKeyChecking()) {
                    jSch.setKnownHosts(params.getKnownHostsFilePath());
                } else {
                    session.setConfig("StrictHostKeyChecking", "no");
                }
                session.connect(); // 0 connection timeout

            } catch (JSchException e) {
                throw new AgentException("Could not create ssh session for host " + params.getHostName(), e);
            }
        } else {
            throw new AgentException("Unknown parameter type to ssh initialize agent adaptor. Required SshAdaptorParams type");
        }

    }

    @Override
    public void init(String computeResourceId, String gatewayId, String userId, String token) throws AgentException {
        try {
            ComputeResourceDescription computeResourceDescription = AgentUtils.getRegistryServiceClient().getComputeResource(computeResourceId);

            logger.info("Fetching credentials for cred store token " + token);

            SSHCredential sshCredential = AgentUtils.getCredentialClient().getSSHCredential(token, gatewayId);
            if (sshCredential == null) {
                throw new AgentException("Null credential for token " + token);
            }
            logger.info("Description for token : " + token + " : " + sshCredential.getDescription());

            SshAdaptorParams adaptorParams = new SshAdaptorParams();
            adaptorParams.setHostName(computeResourceDescription.getHostName());
            adaptorParams.setUserName(userId);
            adaptorParams.setPassphrase(sshCredential.getPassphrase());
            adaptorParams.setPrivateKey(sshCredential.getPrivateKey().getBytes());
            adaptorParams.setPublicKey(sshCredential.getPublicKey().getBytes());
            adaptorParams.setStrictHostKeyChecking(false);
            init(adaptorParams);

        } catch (Exception e) {
            logger.error("Error while initializing ssh agent for compute resource " + computeResourceId + " to token " + token, e);
            throw new AgentException("Error while initializing ssh agent for compute resource " + computeResourceId + " to token " + token, e);
        }
    }

    @Override
    public void destroy() {

    }

    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        StandardOutReader commandOutput = new StandardOutReader();
        ChannelExec channelExec = null;
        try {
            channelExec = ((ChannelExec) session.openChannel("exec"));
            channelExec.setCommand((workingDirectory != null ? "cd " + workingDirectory + "; " : "") + command);
            channelExec.setInputStream(null);
            InputStream out = channelExec.getInputStream();
            InputStream err = channelExec.getErrStream();
            channelExec.connect();

            commandOutput.readStdOutFromStream(out);
            commandOutput.readStdErrFromStream(err);
            return commandOutput;
        } catch (JSchException | IOException e) {
            logger.error("Failed to execute command " + command, e);
            throw new AgentException("Failed to execute command " + command, e);
        } finally {
            if (channelExec != null) {
                commandOutput.setExitCode(channelExec.getExitStatus());
                channelExec.disconnect();
            }
        }
    }

    public void createDirectory(String path) throws AgentException {
        createDirectory(path, false);
    }

    @Override
    public void createDirectory(String path, boolean recursive) throws AgentException {
        String command = (recursive? "mkdir -p " : "mkdir ") + path;
        ChannelExec channelExec = null;
        try {
            channelExec = (ChannelExec)session.openChannel("exec");
            StandardOutReader stdOutReader = new StandardOutReader();

            channelExec.setCommand(command);
            InputStream out = channelExec.getInputStream();
            InputStream err = channelExec.getErrStream();
            channelExec.connect();

            stdOutReader.readStdOutFromStream(out);
            stdOutReader.readStdErrFromStream(err);


            if (stdOutReader.getStdError() != null && stdOutReader.getStdError().contains("mkdir:")) {
                throw new AgentException(stdOutReader.getStdError());
            }
        } catch (JSchException e) {
            System.out.println("Unable to retrieve command output. Command - " + command +
                    " on server - " + session.getHost() + ":" + session.getPort() +
                    " connecting user name - "
                    + session.getUserName());
            throw new AgentException(e);
        } catch (IOException e) {
            logger.error("Failed to create directory " + path, e);
            throw new AgentException("Failed to create directory " + path, e);
        } finally {
            if (channelExec != null) {
                channelExec.disconnect();
            }
        }
    }

    public void uploadFile(String localFile, String remoteFile) throws AgentException {

        FileInputStream fis;
        boolean ptimestamp = true;

        ChannelExec channelExec = null;
        try {
            // exec 'scp -t rfile' remotely
            String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + remoteFile;
            channelExec = (ChannelExec)session.openChannel("exec");

            StandardOutReader stdOutReader = new StandardOutReader();
            //channelExec.setErrStream(stdOutReader.getStandardError());
            channelExec.setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channelExec.getOutputStream();
            InputStream in = channelExec.getInputStream();
            InputStream err = channelExec.getErrStream();

            channelExec.connect();

            if (checkAck(in) != 0) {
                String error = "Error Reading input Stream";
                //log.error(error);
                throw new AgentException(error);
            }

            File _lfile = new File(localFile);

            if (ptimestamp) {
                command = "T" + (_lfile.lastModified() / 1000) + " 0";
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
                out.write(command.getBytes());
                out.flush();
                if (checkAck(in) != 0) {
                    String error = "Error Reading input Stream";
                    throw new AgentException(error);
                }
            }

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize = _lfile.length();
            command = "C0644 " + filesize + " ";
            if (localFile.lastIndexOf('/') > 0) {
                command += localFile.substring(localFile.lastIndexOf('/') + 1);
            } else {
                command += localFile;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                String error = "Error Reading input Stream";
                //log.error(error);
                throw new AgentException(error);
            }

            // send a content of localFile
            fis = new FileInputStream(localFile);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) break;
                out.write(buf, 0, len); //out.flush();
            }
            fis.close();
            fis = null;
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                String error = "Error Reading input Stream";
                //log.error(error);
                throw new AgentException(error);
            }
            out.close();
            stdOutReader.readStdErrFromStream(err);

            if (stdOutReader.getStdError().contains("scp:")) {
                throw new AgentException(stdOutReader.getStdError());
            }
            //since remote file is always a file  we just return the file
            //return remoteFile;
        } catch (JSchException e) {
            logger.error("Failed to transfer file from " + localFile + " to remote location " + remoteFile, e);
            throw new AgentException("Failed to transfer file from " + localFile + " to remote location " + remoteFile, e);

        } catch (FileNotFoundException e) {
            logger.error("Failed to find local file " + localFile, e);
            throw new AgentException("Failed to find local file " + localFile, e);

        } catch (IOException e) {
            logger.error("Error while handling streams", e);
            throw new AgentException("Error while handling streams", e);

        } finally {
            if (channelExec != null) {
                channelExec.disconnect();
            }
        }
    }

    @Override
    public void uploadFile(InputStream localInStream, FileMetadata metadata, String remoteFile) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    // TODO file not found does not return exception
    public void downloadFile(String remoteFile, String localFile) throws AgentException {
        FileOutputStream fos = null;
        ChannelExec channelExec = null;
        try {
            String prefix = null;
            if (new File(localFile).isDirectory()) {
                prefix = localFile + File.separator;
            }

            StandardOutReader stdOutReader = new StandardOutReader();

            // exec 'scp -f remotefile' remotely
            String command = "scp -f " + remoteFile;
            channelExec = (ChannelExec)session.openChannel("exec");
            channelExec.setCommand(command);

            //channelExec.setErrStream(stdOutReader.getStandardError());
            // get I/O streams for remote scp
            OutputStream out = channelExec.getOutputStream();
            InputStream in = channelExec.getInputStream();
            InputStream err = channelExec.getErrStream();

            if (!channelExec.isClosed()){
                channelExec.connect();
            }

            byte[] buf = new byte[1024];

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            while (true) {
                int c = checkAck(in);
                if (c != 'C') {
                    break;
                }

                // read '0644 '
                in.read(buf, 0, 5);

                long filesize = 0L;
                while (true) {
                    if (in.read(buf, 0, 1) < 0) {
                        // error
                        break;
                    }
                    if (buf[0] == ' ') break;
                    filesize = filesize * 10L + (long) (buf[0] - '0');
                }

                String file = null;
                for (int i = 0; ; i++) {
                    in.read(buf, i, 1);
                    if (buf[i] == (byte) 0x0a) {
                        file = new String(buf, 0, i);
                        break;
                    }
                }

                //System.out.println("filesize="+filesize+", file="+file);

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                // read a content of lfile
                fos = new FileOutputStream(prefix == null ? localFile : prefix + file);
                int foo;
                while (true) {
                    if (buf.length < filesize) foo = buf.length;
                    else foo = (int) filesize;
                    foo = in.read(buf, 0, foo);
                    if (foo < 0) {
                        // error
                        break;
                    }
                    fos.write(buf, 0, foo);
                    filesize -= foo;
                    if (filesize == 0L) break;
                }
                fos.close();
                fos = null;

                if (checkAck(in) != 0) {
                    String error = "Error transfering the file content";
                    //log.error(error);
                    throw new AgentException(error);
                }

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();
            }


            stdOutReader.readStdErrFromStream(err);
            if (stdOutReader.getStdError().contains("scp:")) {
                throw new AgentException(stdOutReader.getStdError());
            }

        } catch (JSchException e) {
            logger.error("Failed to transfer file remote from file " + remoteFile + " to location " + remoteFile, e);
            throw new AgentException("Failed to transfer remote file from " + localFile + " to location " + remoteFile, e);

        } catch (FileNotFoundException e) {
            logger.error("Failed to find local file " + localFile, e);
            throw new AgentException("Failed to find local file " + localFile, e);

        } catch (IOException e) {
            logger.error("Error while handling streams", e);
            throw new AgentException("Error while handling streams", e);

        } finally {
            try {
                if (fos != null) fos.close();
            } catch (Exception ee) {
                logger.warn("Failed to close file output stream to " + localFile);
            }

            if (channelExec != null) {
                channelExec.disconnect();
            }

        }
    }

    @Override
    public void downloadFile(String remoteFile, OutputStream localOutStream, FileMetadata metadata) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public List<String> listDirectory(String path) throws AgentException {
        String command = "ls " + path;
        ChannelExec channelExec = null;
        try {
            channelExec = (ChannelExec)session.openChannel("exec");
            StandardOutReader stdOutReader = new StandardOutReader();

            channelExec.setCommand(command);

            InputStream out = channelExec.getInputStream();
            InputStream err = channelExec.getErrStream();

            channelExec.connect();

            stdOutReader.readStdOutFromStream(out);
            stdOutReader.readStdErrFromStream(err);
            if (stdOutReader.getStdError().contains("ls:")) {
                throw new AgentException(stdOutReader.getStdError());
            }
            return Arrays.asList(stdOutReader.getStdOut().split("\n"));

        } catch (JSchException e) {
            logger.error("Unable to retrieve command output. Command - " + command +
                    " on server - " + session.getHost() + ":" + session.getPort() +
                    " connecting user name - "
                    + session.getUserName(), e);
            throw new AgentException("Unable to retrieve command output. Command - " + command +
                    " on server - " + session.getHost() + ":" + session.getPort() +
                    " connecting user name - "
                    + session.getUserName(), e);
        } catch (IOException e) {
            logger.error("Error while handling streams", e);
            throw new AgentException("Error while handling streams", e);
        } finally {
            if (channelExec != null) {
                channelExec.disconnect();
            }
        }
    }

    @Override
    public Boolean doesFileExist(String filePath) throws AgentException {
        String command = "ls " + filePath;
        ChannelExec channelExec = null;
        try {
            channelExec = (ChannelExec)session.openChannel("exec");
            StandardOutReader stdOutReader = new StandardOutReader();

            channelExec.setCommand(command);

            InputStream out = channelExec.getInputStream();
            InputStream err = channelExec.getErrStream();

            channelExec.connect();

            stdOutReader.readStdOutFromStream(out);
            stdOutReader.readStdErrFromStream(err);
            if (stdOutReader.getStdError().contains("ls:")) {
                logger.info("Invalid file path " + filePath + ". stderr : " + stdOutReader.getStdError());
                return false;
            } else {
                String[] potentialFiles = stdOutReader.getStdOut().split("\n");
                if (potentialFiles.length > 1) {
                    logger.info("More than one file matching to given path " + filePath);
                    return false;
                } else if (potentialFiles.length == 0) {
                    logger.info("No file found for given path " + filePath);
                    return false;
                } else {
                    if (potentialFiles[0].trim().equals(filePath)) {
                        return true;
                    } else {
                        logger.info("Returned file name " + potentialFiles[0].trim() + " does not match with given name " + filePath);
                        return false;
                    }
                }
            }
        } catch (JSchException e) {
            logger.error("Unable to retrieve command output. Command - " + command +
                    " on server - " + session.getHost() + ":" + session.getPort() +
                    " connecting user name - "
                    + session.getUserName(), e);
            throw new AgentException("Unable to retrieve command output. Command - " + command +
                    " on server - " + session.getHost() + ":" + session.getPort() +
                    " connecting user name - "
                    + session.getUserName(), e);
        } catch (IOException e) {
            logger.error("Error while handling streams", e);
            throw new AgentException("Error while handling streams", e);
        } finally {
            if (channelExec != null) {
                channelExec.disconnect();
            }
        }
    }

    @Override
    public List<String> getFileNameFromExtension(String fileName, String parentPath) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public FileMetadata getFileMetadata(String remoteFile) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    private static class DefaultUserInfo implements UserInfo, UIKeyboardInteractive {

        private String userName;
        private String password;
        private String passphrase;

        DefaultUserInfo(String userName, String password, String passphrase) {
            this.userName = userName;
            this.password = password;
            this.passphrase = passphrase;
        }

        @Override
        public String getPassphrase() {
            return passphrase;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean promptPassword(String s) {
            return true;
        }

        @Override
        public boolean promptPassphrase(String s) {
            return false;
        }

        @Override
        public boolean promptYesNo(String s) {
            return false;
        }

        @Override
        public void showMessage(String s) {

        }

        @Override
        public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
            return new String[0];
        }
    }

    class SftpUserInfo implements UserInfo {

        String password = null;

        public SftpUserInfo(String password) {
            this.password = password;
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public String getPassword() {
            return password;
        }

        public void setPassword(String passwd) {
            password = passwd;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return false;
        }

        @Override
        public boolean promptPassword(String message) {
            return false;
        }

        @Override
        public boolean promptYesNo(String message) {
            return true;
        }

        @Override
        public void showMessage(String message) {
        }
    }

    private static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            //FIXME: Redundant
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
            //log.warn(sb.toString());
        }
        return b;
    }
}
