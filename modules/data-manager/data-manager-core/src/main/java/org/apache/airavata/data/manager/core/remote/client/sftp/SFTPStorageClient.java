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
package org.apache.airavata.data.manager.core.remote.client.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.airavata.data.manager.core.remote.client.RemoteStorageClient;
import org.apache.airavata.model.data.transfer.LSEntryModel;
import org.apache.airavata.model.data.transfer.LSEntryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class SFTPStorageClient implements RemoteStorageClient {
    private final static Logger logger = LoggerFactory.getLogger(SFTPStorageClient.class);

    private JSch jSch;
    private Session session;
    private ChannelSftp sftpChannel;
    private final String hostName;

    public SFTPStorageClient(String hostName, int port, String loginUsername, String password) throws JSchException {
        this.hostName = hostName;
        Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        jSch = new JSch();
        jSch.addIdentity(loginUsername, password);
        session = jSch.getSession(loginUsername, hostName, port);
        session.setConfig(config);
        session.connect();
        sftpChannel = (ChannelSftp) session.openChannel("sftp");
    }

    public SFTPStorageClient(String hostName, int port, String loginUsername, byte[] privateKey, byte[] publicKey,
                            byte[] passPhrase) throws JSchException {
        this.hostName = hostName;
        Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        jSch = new JSch();
        jSch.addIdentity(UUID.randomUUID().toString(), privateKey, publicKey, passPhrase);
        session = jSch.getSession(loginUsername, hostName, port);
        session.setConfig(config);
        session.connect();
        sftpChannel = (ChannelSftp) session.openChannel("sftp");
    }

    /**
     * Reads a remote file, write it to local temporary directory and returns a file pointer to it
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    @Override
    public File readFile(String filePath) throws Exception {
        String localFile = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString();
        return null;
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
        if(directoryPath.endsWith(File.separator)){
            directoryPath = directoryPath.substring(0, directoryPath.length() -1);
        }
        final String finalDirPath =  directoryPath;
        //channel may get timeout
        if(sftpChannel.isClosed()){
            sftpChannel.connect();
        }
        sftpChannel.cd(directoryPath);
        Vector<ChannelSftp.LsEntry> lsEntryVector = sftpChannel.ls(directoryPath);
        ArrayList<LSEntryModel> fileNodeList = new ArrayList<>();
        lsEntryVector.stream().forEach(lsEntry -> {
            LSEntryModel fileNode = new LSEntryModel();
            fileNode.setName(lsEntry.getFilename());
            fileNode.setPath(finalDirPath + File.separator + lsEntry.getFilename());
            fileNode.setStorageHostName(hostName);
            fileNode.setSize(lsEntry.getAttrs().getSize());
            if(lsEntry.getAttrs().isDir())
                fileNode.setType(LSEntryType.DIRECTORY);
            else
                fileNode.setType(LSEntryType.FILE);
            fileNodeList.add(fileNode);
        });
        return fileNodeList;
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
}