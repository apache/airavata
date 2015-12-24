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
package org.apache.airavata.file.manager.core.remote.client.scp;

import com.jcraft.jsch.*;
import org.apache.airavata.file.manager.core.remote.client.RemoteStorageClient;
import org.apache.airavata.model.file.transfer.LSEntryModel;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class SCPStorageClient implements RemoteStorageClient {
    private final static Logger logger = LoggerFactory.getLogger(SCPStorageClient.class);

    private JSch jSch;
    private Session session;

    /**
     * Constructor
     * @param hostName
     * @param port
     * @param loginUsername
     * @param password
     * @throws JSchException
     */
    public SCPStorageClient(String hostName, int port, String loginUsername, String password) throws JSchException {
        Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        jSch = new JSch();
        jSch.addIdentity(loginUsername, password);
        session = jSch.getSession(loginUsername, hostName, port);
        session.setConfig(config);
        session.connect();
    }

    /**
     * Constructor
     * @param hostName
     * @param port
     * @param loginUsername
     * @param privateKey
     * @param publicKey
     * @param passPhrase
     * @throws JSchException
     */
    public SCPStorageClient(String hostName, int port, String loginUsername, byte[] privateKey, byte[] publicKey,
                            byte[] passPhrase) throws JSchException {
        Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        jSch = new JSch();
        jSch.addIdentity(UUID.randomUUID().toString(), privateKey, publicKey, passPhrase);
        session = jSch.getSession(loginUsername, hostName, port);
        session.setConfig(config);
        session.connect();
    }


    /**
     * Reads a remote file, write it to local temporary directory and returns a File
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    @Override
    public File readFile(String filePath) throws Exception {
        if (!session.isConnected())
            session.connect();

        FileOutputStream fos;
        String localFile = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString();
        String prefix = null;
        if (new File(localFile).isDirectory()) {
            prefix = localFile + File.separator;
        }

        // exec 'scp -f remotefile' remotely
        String command = "scp -f " + filePath;
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        StandardOutReader stdOutReader = new StandardOutReader();
        ((ChannelExec) channel).setErrStream(stdOutReader.getStandardError());
        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        if (!channel.isClosed()) {
            channel.connect();
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

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            // read a content of lfile
            localFile = prefix == null ? localFile : prefix + file;
            fos = new FileOutputStream(localFile);
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
                String error = "Error transferring the file content";
                logger.error(error);
                throw new SCPApiException(error);
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
        }
        stdOutReader.onOutput(channel);
        if (stdOutReader.getStdErrorString().contains("scp:")) {
            throw new SCPApiException(stdOutReader.getStdErrorString());
        }

        return new File(localFile);
    }

    /**
     * Writes the source file in the local storage to specified path in the remote storage
     *
     * @param sourceFile
     * @param filePath
     * @return
     * @throws Exception
     */
    @Override
    public void writeFile(File sourceFile, String filePath) throws Exception {
        if (!session.isConnected())
            session.connect();

        FileInputStream fis;
        String localFile = sourceFile.getAbsolutePath();
        boolean ptimestamp = true;

        // exec 'scp -t rfile' remotely
        String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + filePath;
        Channel channel = session.openChannel("exec");

        StandardOutReader stdOutReader = new StandardOutReader();
        ((ChannelExec) channel).setErrStream(stdOutReader.getStandardError());
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

        if (checkAck(in) != 0) {
            String error = "Error Reading input Stream";
            logger.error(error);
            throw new SCPApiException(error);
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
                logger.error(error);
                throw new SCPApiException(error);
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
            logger.error(error);
            throw new SCPApiException(error);
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
            logger.error(error);
            throw new SCPApiException(error);
        }
        out.close();
        stdOutReader.onOutput(channel);


        channel.disconnect();
        if (stdOutReader.getStdErrorString().contains("scp:")) {
            throw new SCPApiException(stdOutReader.getStdErrorString());
        }
    }

    /**
     * Returns a directory listing of the specified directory
     *
     * @param directoryPath
     * @return
     * @throws Exception
     */
    @Override
    public List<LSEntryModel> getDirectoryListing(String directoryPath) throws Exception {
        return null;
    }

    /**
     * Move the specified file from source to destination within the same storage resource
     *
     * @param currentPath
     * @param newPath
     * @throws Exception
     */
    @Override
    public void moveFile(String currentPath, String newPath) throws Exception {

    }

    /**
     * @param sourcePath
     * @param destinationPath
     * @throws Exception
     */
    @Override
    public void copyFile(String sourcePath, String destinationPath) throws Exception {

    }

    /**
     * Rename file with the given name
     *
     * @param filePath
     * @param newFileName
     * @throws Exception
     */
    @Override
    public void renameFile(String filePath, String newFileName) throws Exception {

    }

    /**
     * Delete the specified file
     *
     * @param filePath
     * @throws Exception
     */
    @Override
    public void deleteFile(String filePath) throws Exception {

    }

    /**
     * Create new directory in the specified file
     *
     * @param newDirPath
     * @throws Exception
     */
    @Override
    public void mkdir(String newDirPath) throws Exception {

    }

    /**
     * Checks whether specified file exists in the remote storage system
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    @Override
    public boolean checkFileExists(String filePath) throws Exception {
        return false;
    }

    /**
     * Checks whether the given path is a directory
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    @Override
    public boolean checkIsDirectory(String filePath) throws Exception {
        return false;
    }

    private int checkAck(InputStream in) throws IOException {
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
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }
}