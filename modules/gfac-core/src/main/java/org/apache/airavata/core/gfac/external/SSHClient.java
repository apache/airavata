package org.apache.airavata.core.gfac.external;

import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.io.IOStreamConnector;
import com.sshtools.j2ssh.io.IOStreamConnectorState;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.ConsoleKnownHostsKeyVerification;
import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;
import com.sshtools.j2ssh.util.InvalidStateException;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xsul.MLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SSHClient {

    protected static final Logger log = LoggerFactory.getLogger(SSHClient.class);

    public static void main(String[] args) {
        try {
            String username = "biovlab";
            String hostname = "gw40.quarry.iu.teragrid.org";
            String password = "";
            String keyFileName = "/home/ptangcha/.ssh/id_rsa";
            String knownHostsFileName = "/home/ptangcha/.ssh/known_hosts";
            SshClient ssh = loginToServer(username, hostname, password, keyFileName, knownHostsFileName);

            SessionChannelClient session = ssh.openSessionChannel();

            if (!session.requestPseudoTerminal("vt100", 80, 24, 0, 0, ""))
                System.out.println("Failed to allocate a pseudo terminal");
            if (session.startShell()) {
                session.setEnvironmentVariable("foo1", "HelloA");
                session.setEnvironmentVariable("foo2", "HelloB");

                InputStream in = session.getInputStream();
                OutputStream out = session.getOutputStream();
                IOStreamConnector input = new IOStreamConnector(System.in, session
                        .getOutputStream());
                IOStreamConnector output = new IOStreamConnector(session.getInputStream(),
                        System.out);
                out.write("/u/hperera/temp/test.sh Hello".getBytes());
                output.getState().waitForState(IOStreamConnectorState.CLOSED);
            } else
                System.out.println("Failed to start the users shell");
            System.out.println("done");

            ssh.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Log in to the remote host and retun a authenticated Ssh Client
     *
     * @param username
     * @param hostname
     * @param password
     * @param keyFileName
     * @param knownHostsFileName
     * @return
     * @throws GfacException
     */

    public static SshClient loginToServer(String username, String hostname, String password,
                                          String keyFileName, String knownHostsFileName) throws GfacException {
        try {

            log.info("SSH host:" + hostname);
            log.info("SSH username:" + username);
            log.info("SSH password:" + password);
            log.info("SSH keyfile:" + keyFileName);
            log.info("SSH hostfile:" + knownHostsFileName);

            SshClient ssh = new SshClient();
            // Connect to the host
            HostKeyVerification hostKeyVerification;
            if (knownHostsFileName != null) {
                hostKeyVerification = new ConsoleKnownHostsKeyVerification(knownHostsFileName);
            } else {
                hostKeyVerification = new IgnoreHostKeyVerification();
            }

            ssh.connect(hostname, hostKeyVerification);
            PublicKeyAuthenticationClient pk = new PublicKeyAuthenticationClient();
            pk.setUsername(username);
            // Get the private key file
            // Open up the private key file
            SshPrivateKeyFile file = SshPrivateKeyFile.parse(new File(keyFileName));
            // If the private key is passphrase protected then ask for the
            // passphrase
            String passphrase = null;
            if (file.isPassphraseProtected()) {
                if (password == null) {
                    throw new GfacException(
                            "Key file is encrypted, but password has not found in configuration. Invalid Config.");
                }
                passphrase = password;
            }
            // Get the key
            SshPrivateKey key = file.toPrivateKey(passphrase);
            pk.setKey(key);
            // Try the authentication
            int result = ssh.authenticate(pk);
            // Evaluate the result
            if (result == AuthenticationProtocolState.COMPLETE) {
                // System.out.println("authenication completed");
                return ssh;
            } else if (result == AuthenticationProtocolState.PARTIAL) {
                throw new GfacException("Further authentication requried!. Invalid Config.");
            } else if (result == AuthenticationProtocolState.FAILED) {
                throw new GfacException("Authentication failed!. Invalid Config.");
            } else {
                throw new GfacException("Authentication failed!, Unknown state. Invalid Config.");
            }
        } catch (InvalidSshKeyException e) {
            throw new GfacException("Invalid Config!", e);
        } catch (InvalidStateException e) {
            throw new GfacException("Invalid Config!", e);
        } catch (IOException e) {
            throw new GfacException("Invalid Config!", e);
        }
    }

}
