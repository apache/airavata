package org.apache.airavata.k8s.compute.impl;

import com.jcraft.jsch.*;
import org.apache.airavata.k8s.compute.api.ComputeOperations;
import org.apache.airavata.k8s.compute.api.ExecutionResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class SSHComputeOperations implements ComputeOperations {

    private String computeHost;
    private String userName;
    private String password;
    private int port = 22;

    public SSHComputeOperations(String computeHost, String userName, String password) {
        this.computeHost = computeHost;
        this.userName = userName;
        this.password = password;
    }

    public SSHComputeOperations(String computeHost, String userName, String password, int port) {
        this.computeHost = computeHost;
        this.userName = userName;
        this.password = password;
        this.port = port;
    }

    public ExecutionResult executeCommand(String command) throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(userName, this.computeHost, port);
        session.setConfig("StrictHostKeyChecking", "no");

        session.setUserInfo(new UserInfo() {
            @Override
            public String getPassphrase() {
                return password;
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
        });

        session.connect();
        Channel channel=session.openChannel("exec");
        ((ChannelExec)channel).setCommand(command);

        ByteArrayOutputStream sysOut = new ByteArrayOutputStream();
        channel.setOutputStream(sysOut);
        ByteArrayOutputStream sysErr = new ByteArrayOutputStream();
        ((ChannelExec) channel).setErrStream(sysErr);

        InputStream in = channel.getInputStream();

        channel.connect();

        ExecutionResult result = new ExecutionResult();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available()>0) {
                int i = in.read(tmp, 0, 1024);
                if (i<0) break;
                System.out.print(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                if (in.available() > 0) continue;
                System.out.println("exit-status: " + channel.getExitStatus());
                result.setExitStatus(channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch(Exception e){}
        }

        channel.disconnect();
        session.disconnect();

        result.setStdErr(sysErr.toString("UTF-8"));
        result.setStdOut(sysOut.toString("UTF-8"));
        return result;
    }

    public void transferDataIn(String source, String target, String protocol) {

    }

    public void transferDataOut(String source, String target, String protocol) {

    }

    public static void main(String args[]) throws IOException, JSchException {
        SSHComputeOperations operations = new SSHComputeOperations("192.168.1.101", "dimuthu", "123456");
        ExecutionResult result = operations.executeCommand("sh /opt/sample.sh > /tmp/stdout.txt 2> /tmp/stderr.txt");
        System.out.println(result.getStdOut());
        System.out.println(result.getStdErr());
    }
}
