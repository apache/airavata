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
package org.apache.airavata.gfac.ssh.security;

import java.io.IOException;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import org.apache.airavata.gfac.core.SecurityContext;
import org.apache.airavata.gfac.core.cluster.RemoteCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle SSH security
 */
public class SSHSecurityContext implements SecurityContext {
	private static final Logger log = LoggerFactory.getLogger(SSHSecurityContext.class);

	private String username;
	private String privateKeyLoc;
	private String keyPass;
	private SSHClient sshClient;
	private Session session;

    private RemoteCluster remoteCluster;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPrivateKeyLoc() {
		return privateKeyLoc;
	}

	public void setPrivateKeyLoc(String privateKeyLoc) {
		this.privateKeyLoc = privateKeyLoc;
	}

	public String getKeyPass() {
		return keyPass;
	}

	public void setKeyPass(String keyPass) {
		this.keyPass = keyPass;
	}

	public void closeSession(Session session) {
		if (session != null) {
			try {
				session.close();
			} catch (Exception e) {
				log.warn("Cannot Close SSH Session");
			}
		}
	}

	public Session getSession(String hostAddress) throws IOException {
		try {
			if (sshClient == null) {
				sshClient = new SSHClient();
			}
			if (getSSHClient().isConnected())
				return getSSHClient().startSession();

			KeyProvider pkey = getSSHClient().loadKeys(getPrivateKeyLoc(), getKeyPass());

			getSSHClient().loadKnownHosts();

			getSSHClient().connect(hostAddress);
			getSSHClient().authPublickey(getUsername(), pkey);
			session = getSSHClient().startSession();
			return session;

		} catch (NullPointerException ne) {
			throw new SecurityException("Cannot load security context for SSH", ne);
		}
	}

	public SSHClient getSSHClient() {
		if (sshClient == null) {
			sshClient = new SSHClient();
		}
		return sshClient;
	}

    public void setRemoteCluster(RemoteCluster remoteCluster) {
        this.remoteCluster = remoteCluster;
    }

    public RemoteCluster getRemoteCluster() {
        return this.remoteCluster;
    }
}
