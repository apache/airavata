package org.apache.airavata.helix.agent.ssh;

import org.apache.airavata.agents.api.AdaptorParams;

import java.io.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class SshAdaptorParams extends AdaptorParams implements Serializable {

    private int port = 22;
    private String hostName;
    private String userName;

    private String password;

    private byte[] publicKey;
    private byte[] privateKey;
    private String passphrase;

    private String knownHostsFilePath;
    private boolean strictHostKeyChecking;

    public int getPort() {
        return port;
    }

    public SshAdaptorParams setPort(int port) {
        this.port = port;
        return this;
    }

    public String getHostName() {
        return hostName;
    }

    public SshAdaptorParams setHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public SshAdaptorParams setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public SshAdaptorParams setPassword(String password) {
        this.password = password;
        return this;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public SshAdaptorParams setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public SshAdaptorParams setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public SshAdaptorParams setPassphrase(String passphrase) {
        this.passphrase = passphrase;
        return this;
    }

    public String getKnownHostsFilePath() {
        return knownHostsFilePath;
    }

    public SshAdaptorParams setKnownHostsFilePath(String knownHostsFilePath) {
        this.knownHostsFilePath = knownHostsFilePath;
        return this;
    }

    public boolean isStrictHostKeyChecking() {
        return strictHostKeyChecking;
    }

    public SshAdaptorParams setStrictHostKeyChecking(boolean strictHostKeyChecking) {
        this.strictHostKeyChecking = strictHostKeyChecking;
        return this;
    }

    public static void main(String args[]) throws IOException {
        SshAdaptorParams params = new SshAdaptorParams();
        params.setUserName("dimuthu");
        params.setPassword("upe");
        params.setHostName("localhost");
        params.writeToFile(new File("/tmp/ssh-param.json"));
    }
}
