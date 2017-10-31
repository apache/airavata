/**
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
package org.apache.airavata.k8s.compute.impl;

import com.jcraft.jsch.*;
import org.apache.airavata.k8s.compute.api.ComputeOperations;
import org.apache.airavata.k8s.compute.api.ExecutionResult;

import java.io.*;

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

    public void transferDataOut(String source, String target, String protocol) throws Exception {
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

        copyRemoteToLocal(session, source, target);
    }

    private static void copyRemoteToLocal(Session session, String source, String target) throws JSchException, IOException {

        // exec 'scp -f rfile' remotely
        String command = "scp -f " + source;
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

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

            System.out.println("file-size=" + filesize + ", file=" + file);

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            // read a content of lfile
            FileOutputStream fos = new FileOutputStream(target);
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

            if (checkAck(in) != 0) {
                System.exit(0);
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            try {
                if (fos != null) fos.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }

        channel.disconnect();
        session.disconnect();
    }

    public static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //         -1
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
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }

    public static void main(String args[]) throws IOException, Exception {
        SSHComputeOperations operations = new SSHComputeOperations("192.168.1.101", "dimuthu", "123456");
        //ExecutionResult result = operations.executeCommand("sh /opt/sample.sh > /tmp/stdout.txt 2> /tmp/stderr.txt");
        //System.out.println(result.getStdOut());
        //System.out.println(result.getStdErr());
        operations.transferDataOut("/tmp/stdout.txt", "/tmp/b.txt", "SCP");
    }
}
