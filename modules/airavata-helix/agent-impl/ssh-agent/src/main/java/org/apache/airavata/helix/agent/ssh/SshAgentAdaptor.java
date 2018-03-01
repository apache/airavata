package org.apache.airavata.helix.agent.ssh;

import com.jcraft.jsch.*;
import org.apache.airavata.agents.api.*;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.store.impl.CredentialReaderImpl;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class SshAgentAdaptor implements AgentAdaptor {

    private Session session = null;
    private AppCatalog appCatalog;
    private ComputeResourceDescription computeResourceDescription;
    private ResourceJobManager resourceJobManager;
    private SSHJobSubmission sshJobSubmission;

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
            this.appCatalog = RegistryFactory.getAppCatalog();
            this.computeResourceDescription = this.appCatalog.getComputeResource().getComputeResource(computeResourceId);
            List<JobSubmissionInterface> jobSubmissionInterfaces = this.computeResourceDescription.getJobSubmissionInterfaces();
            Optional<JobSubmissionInterface> jobSubmissionInterfaceOp = jobSubmissionInterfaces.stream()
                    .filter(iface -> JobSubmissionProtocol.SSH == iface.getJobSubmissionProtocol() ||
                            JobSubmissionProtocol.SSH_FORK == iface.getJobSubmissionProtocol())
                    .findFirst();

            JobSubmissionInterface jobSubmissionInterface = jobSubmissionInterfaceOp.orElseThrow(() -> new AgentException("Could not find a Job submission interface with SSH"));

            this.sshJobSubmission = this.appCatalog.getComputeResource().getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
            this.resourceJobManager = sshJobSubmission.getResourceJobManager();

            String jdbcUrl = ServerSettings.getCredentialStoreDBURL();
            String jdbcUsr = ServerSettings.getCredentialStoreDBUser();
            String jdbcPass = ServerSettings.getCredentialStoreDBPassword();
            String driver = ServerSettings.getCredentialStoreDBDriver();
            CredentialReaderImpl credentialReader = new CredentialReaderImpl(new DBUtil(jdbcUrl, jdbcUsr, jdbcPass, driver));
            Credential credential = credentialReader.getCredential(gatewayId, token);

            if (credential instanceof SSHCredential) {
                SSHCredential sshCredential = SSHCredential.class.cast(credential);
                SshAdaptorParams adaptorParams = new SshAdaptorParams();
                adaptorParams.setHostName(this.computeResourceDescription.getHostName());
                adaptorParams.setUserName(userId);
                adaptorParams.setPassphrase(sshCredential.getPassphrase());
                adaptorParams.setPrivateKey(sshCredential.getPrivateKey());
                adaptorParams.setPublicKey(sshCredential.getPublicKey());
                adaptorParams.setStrictHostKeyChecking(false);
                init(adaptorParams);
            }

        } catch (AppCatalogException e) {
            e.printStackTrace();
            throw new AgentException(e);
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
            throw new AgentException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new AgentException(e);
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new AgentException(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new AgentException(e);
        } catch (CredentialStoreException e) {
            e.printStackTrace();
            throw new AgentException(e);
        }
    }

    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        StandardOutReader commandOutput = new StandardOutReader();
        ChannelExec channelExec = null;
        try {
            channelExec = ((ChannelExec) session.openChannel("exec"));
            channelExec.setCommand(command);
            channelExec.setInputStream(null);
            InputStream out = channelExec.getInputStream();
            InputStream err = channelExec.getErrStream();
            channelExec.connect();

            commandOutput.setExitCode(channelExec.getExitStatus());
            commandOutput.readStdOutFromStream(out);
            commandOutput.readStdErrFromStream(err);
            return commandOutput;
        } catch (JSchException e) {
            e.printStackTrace();
            throw new AgentException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new AgentException(e);
        } finally {
            if (channelExec != null) {
                channelExec.disconnect();
            }
        }
    }

    public void createDirectory(String path) throws AgentException {
        String command = "mkdir -p " + path;
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
            e.printStackTrace();
            throw new AgentException(e);
        } finally {
            if (channelExec != null) {
                channelExec.disconnect();
            }
        }
    }

    public void copyFileTo(String localFile, String remoteFile) throws AgentException {
        FileInputStream fis = null;
        String prefix = null;
        if (new File(localFile).isDirectory()) {
            prefix = localFile + File.separator;
        }
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
            e.printStackTrace();
            throw new AgentException(e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new AgentException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new AgentException(e);
        } finally {
            if (channelExec != null) {
                channelExec.disconnect();
            }
        }
    }

    // TODO file not found does not return exception
    public void copyFileFrom(String remoteFile, String localFile) throws AgentException {
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

        } catch (Exception e) {
            //log.error(e.getMessage(), e);
            throw new AgentException(e);
        } finally {
            try {
                if (fos != null) fos.close();
            } catch (Exception ee) {
            }

            if (channelExec != null) {
                channelExec.disconnect();
            }

        }
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
            throw new AgentException("Unable to retrieve command output. Command - " + command +
                    " on server - " + session.getHost() + ":" + session.getPort() +
                    " connecting user name - "
                    + session.getUserName(), e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new AgentException(e);
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

    private static class DefaultUserInfo implements UserInfo, UIKeyboardInteractive {

        private String userName;
        private String password;
        private String passphrase;

        public DefaultUserInfo(String userName, String password, String passphrase) {
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

    static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
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
