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

import net.schmizz.sshj.common.LoggerFactory;
import net.schmizz.sshj.common.Message;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.common.SSHPacket;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.PTYMode;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SessionWrapper implements Session {

    private Session session;
    private Consumer<Integer> onCloseFunction;
    private SSHClientWrapper originalSSHClient;

    public SessionWrapper(Session session, Consumer<Integer> onCloseFunction, SSHClientWrapper originalSSHClient) {
        this.session = session;
        this.onCloseFunction = onCloseFunction;
        this.originalSSHClient = originalSSHClient;
    }

    @Override
    public void allocateDefaultPTY() throws ConnectionException, TransportException {
        session.allocateDefaultPTY();
    }

    @Override
    public void allocatePTY(String term, int cols, int rows, int width, int height, Map<PTYMode, Integer> modes) throws ConnectionException, TransportException {
        session.allocatePTY(term, cols, rows, width, height, modes);
    }

    @Override
    public Command exec(String command) throws ConnectionException, TransportException {
        return session.exec(command);
    }

    @Override
    public void reqX11Forwarding(String authProto, String authCookie, int screen) throws ConnectionException, TransportException {
        session.reqX11Forwarding(authProto, authCookie, screen);
    }

    @Override
    public void setEnvVar(String name, String value) throws ConnectionException, TransportException {
        session.setEnvVar(name, value);
    }

    @Override
    public Shell startShell() throws ConnectionException, TransportException {
        return session.startShell();
    }

    @Override
    public Subsystem startSubsystem(String name) throws ConnectionException, TransportException {
        return session.startSubsystem(name);
    }

    @Override
    public void close() throws TransportException, ConnectionException {
        onCloseFunction.accept(getID());
        session.close();
    }

    @Override
    public boolean getAutoExpand() {
        return session.getAutoExpand();
    }

    @Override
    public int getID() {
        return session.getID();
    }

    @Override
    public InputStream getInputStream() {
        return session.getInputStream();
    }

    @Override
    public int getLocalMaxPacketSize() {
        return session.getLocalMaxPacketSize();
    }

    @Override
    public long getLocalWinSize() {
        return session.getLocalWinSize();
    }

    @Override
    public OutputStream getOutputStream() {
        return session.getOutputStream();
    }

    @Override
    public int getRecipient() {
        return session.getRecipient();
    }

    @Override
    public Charset getRemoteCharset() {
        return session.getRemoteCharset();
    }

    @Override
    public int getRemoteMaxPacketSize() {
        return session.getRemoteMaxPacketSize();
    }

    @Override
    public long getRemoteWinSize() {
        return session.getRemoteWinSize();
    }

    @Override
    public String getType() {
        return session.getType();
    }

    @Override
    public boolean isOpen() {
        return session.isOpen();
    }

    @Override
    public void setAutoExpand(boolean autoExpand) {
        session.setAutoExpand(autoExpand);
    }

    @Override
    public void join() throws ConnectionException {
        session.join();
    }

    @Override
    public void join(long timeout, TimeUnit unit) throws ConnectionException {
        session.join(timeout, unit);
    }

    @Override
    public boolean isEOF() {
        return session.isEOF();
    }

    @Override
    public LoggerFactory getLoggerFactory() {
        return session.getLoggerFactory();
    }

    @Override
    public void notifyError(SSHException error) {
        session.notifyError(error);
    }

    @Override
    public void handle(Message msg, SSHPacket buf) throws SSHException {
        session.handle(msg, buf);
    }

    public boolean isErrored() {
        return originalSSHClient.isErrored();
    }

    public void setErrored(boolean errored) {
        this.originalSSHClient.setErrored(errored);
    }
}
