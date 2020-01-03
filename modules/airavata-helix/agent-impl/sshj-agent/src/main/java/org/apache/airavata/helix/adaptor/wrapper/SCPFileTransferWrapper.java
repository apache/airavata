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
package org.apache.airavata.helix.adaptor.wrapper;

import net.schmizz.sshj.xfer.FileTransfer;
import net.schmizz.sshj.xfer.LocalDestFile;
import net.schmizz.sshj.xfer.LocalSourceFile;
import net.schmizz.sshj.xfer.TransferListener;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

public class SCPFileTransferWrapper implements FileTransfer, Closeable {

    private SCPFileTransfer scpFileTransfer;
    private Consumer<Integer> onCloseFunction;
    private SSHClientWrapper originalSSHClient;

    public SCPFileTransferWrapper(SCPFileTransfer scpFileTransfer, Consumer<Integer> onCloseFunction, SSHClientWrapper originalSSHClient) {
        this.scpFileTransfer = scpFileTransfer;
        this.onCloseFunction = onCloseFunction;
        this.originalSSHClient = originalSSHClient;
    }

    @Override
    public void upload(String localPath, String remotePath) throws IOException {
        scpFileTransfer.upload(localPath, remotePath);
    }

    @Override
    public void download(String remotePath, String localPath) throws IOException {
        scpFileTransfer.download(remotePath, localPath);
    }

    @Override
    public void upload(LocalSourceFile localFile, String remotePath) throws IOException {
        scpFileTransfer.upload(localFile, remotePath);
    }

    @Override
    public void download(String remotePath, LocalDestFile localFile) throws IOException {
        scpFileTransfer.download(remotePath, localFile);
    }

    @Override
    public TransferListener getTransferListener() {
        return scpFileTransfer.getTransferListener();
    }

    @Override
    public void setTransferListener(TransferListener listener) {
        scpFileTransfer.setTransferListener(listener);
    }

    @Override
    public void close() throws IOException {
        onCloseFunction.accept(-1);
    }

    public boolean isErrored() {
        return originalSSHClient.isErrored();
    }

    public void setErrored(boolean errored) {
        this.originalSSHClient.setErrored(errored);
    }
}
