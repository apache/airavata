/*
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

package org.apache.airavata.accountprovisioning.provisioner;

import org.apache.airavata.accountprovisioning.ConfigParam;
import org.apache.airavata.accountprovisioning.InvalidUsernameException;
import org.apache.airavata.accountprovisioning.SSHAccountManager;
import org.apache.airavata.accountprovisioning.SSHAccountProvisioner;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.api.ldap.model.message.ModifyResponse;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class IULdapSSHAccountProvisioner implements SSHAccountProvisioner  {

    private final static Logger logger = LoggerFactory.getLogger(SSHAccountManager.class);
    public static final String LDAP_PUBLIC_KEY_OBJECT_CLASS = "ldapPublicKey";
    public static final String SSH_PUBLIC_KEY_ATTRIBUTE_NAME = "sshPublicKey";

    private String ldapHost, ldapUsername, ldapPassword, ldapBaseDN, canonicalScratchLocation;
    private int ldapPort;
    @Override
    public void init(Map<ConfigParam, String> config) {

        ldapHost =  config.get(IULdapSSHAccountProvisionerProvider.LDAP_HOST);//"bazooka.hps.iu.edu"
        ldapPort = Integer.valueOf(config.get(IULdapSSHAccountProvisionerProvider.LDAP_PORT));//"636"
        ldapUsername = config.get(IULdapSSHAccountProvisionerProvider.LDAP_USERNAME);//"cn=sgrcusr"
        ldapPassword = config.get(IULdapSSHAccountProvisionerProvider.LDAP_PASSWORD); //"secret password"
        ldapBaseDN = config.get(IULdapSSHAccountProvisionerProvider.LDAP_BASE_DN);//"dc=rt,dc=iu,dc=edu"
        canonicalScratchLocation = config.get(IULdapSSHAccountProvisionerProvider.CANONICAL_SCRATCH_LOCATION); //"/N/dc2/scratch/username/iu-gateway"
    }

    @Override
    public boolean hasAccount(String userId) throws InvalidUsernameException {
        String username = getUsername(userId);
        boolean result = withLdapConnection(ldapConnection -> {
            try {
                return ldapConnection.exists("uid=" + username + "," + ldapBaseDN);
            } catch (LdapException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }

    @Override
    public String createAccount(String userId, String sshPublicKey) throws InvalidUsernameException {

        throw new UnsupportedOperationException("IULdapSSHAccountProvisioner does not support creating cluster accounts at this time.");
    }

    @Override
    public String installSSHKey(String userId, String sshPublicKey) throws InvalidUsernameException {
        String username = getUsername(userId);
        String finalSSHPublicKey = sshPublicKey.trim();
        boolean success = withLdapConnection(ldapConnection -> {
            try {
                String dn = "uid=" + username + "," + ldapBaseDN;

                Entry entry = ldapConnection.lookup(dn);
                if (entry == null) {
                    throw new RuntimeException("User [" + username + "] has no entry for " + dn);
                }
                boolean hasLdapPublicKey = entry.hasObjectClass(LDAP_PUBLIC_KEY_OBJECT_CLASS);

                ModifyRequest modifyRequest = new ModifyRequestImpl();
                modifyRequest.setName(new Dn(dn));

                // Add or Replace, depending on whether there is already an ldapPublicKey on the entry
                if (!hasLdapPublicKey) {

                    modifyRequest.addModification(new DefaultAttribute("objectclass", LDAP_PUBLIC_KEY_OBJECT_CLASS), ModificationOperation.ADD_ATTRIBUTE);
                    modifyRequest.addModification(new DefaultAttribute(SSH_PUBLIC_KEY_ATTRIBUTE_NAME, finalSSHPublicKey),
                            ModificationOperation.ADD_ATTRIBUTE);
                } else {

                    String oldSshPublicKey = entry.get(SSH_PUBLIC_KEY_ATTRIBUTE_NAME).getString();
                    if (!oldSshPublicKey.equals(finalSSHPublicKey)) {
                        // Disallow overwriting the SSH key
                        throw new RuntimeException("User [" + username + "] already has an SSH public key in LDAP for ["
                                + ldapBaseDN + "] and overwriting it isn't allowed.");
                        // modifyRequest.addModification(new DefaultAttribute(SSH_PUBLIC_KEY_ATTRIBUTE_NAME,
                        //        sshPublicKey), ModificationOperation.REPLACE_ATTRIBUTE);
                    } else {
                        // SSH key is already installed so just return
                        return true;
                    }
                }
                ModifyResponse modifyResponse = ldapConnection.modify(modifyRequest);
                if (modifyResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS) {
                    logger.warn("installSSHKey ldap operation reported not being successful: " + modifyResponse);
                } else {
                    logger.debug("installSSHKey ldap operation was successful: " + modifyResponse);
                }
                return true;
            } catch (LdapException e) {
                throw new RuntimeException(e);
            }
        });
        return username;
    }

    @Override
    public String getScratchLocation(String userId) throws InvalidUsernameException {
        String username = getUsername(userId);
        String scratchLocation = canonicalScratchLocation.replace("${username}",username);
        return scratchLocation;
    }

    private <R> R withLdapConnection(Function<LdapConnection,R> function) {

        try (LdapConnection connection = new LdapNetworkConnection(ldapHost, ldapPort, true)) {

            connection.bind(ldapUsername, ldapPassword);

            R result = function.apply(connection);

            connection.unBind();

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert from Airavata userId to cluster username. The assumption here is that a userId will be
     * an IU email address and the username is just the username portion of the email address.
     */
    private String getUsername(String userId) throws InvalidUsernameException {
        int atSignIndex = userId.indexOf("@");
        if (atSignIndex < 0) {
            throw new InvalidUsernameException("userId is not an email address: " + userId);
        }
        return userId.substring(0, atSignIndex);
    }

    public static void main(String[] args) throws InvalidUsernameException {
        String ldapPassword = args[0];
        IULdapSSHAccountProvisioner sshAccountProvisioner = new IULdapSSHAccountProvisioner();
        Map<ConfigParam,String> config = new HashMap<>();
        // Create SSH tunnel to server that has firewall access to bazooka:
        //   ssh airavata@apidev.scigap.org -L 9000:bazooka.hps.iu.edu:636 -N &
        // Put entry in /etc/hosts with the following
        //   127.0.0.1	bazooka.hps.iu.edu
        config.put(IULdapSSHAccountProvisionerProvider.LDAP_HOST, "bazooka.hps.iu.edu");
        config.put(IULdapSSHAccountProvisionerProvider.LDAP_PORT, "9000"); // ssh tunnel port
        config.put(IULdapSSHAccountProvisionerProvider.LDAP_USERNAME, "cn=sgrcusr,dc=rt,dc=iu,dc=edu");
        config.put(IULdapSSHAccountProvisionerProvider.LDAP_PASSWORD, ldapPassword);
        config.put(IULdapSSHAccountProvisionerProvider.LDAP_BASE_DN, "ou=bigred2-sgrc,dc=rt,dc=iu,dc=edu");
        config.put(IULdapSSHAccountProvisionerProvider.CANONICAL_SCRATCH_LOCATION, "/N/dc2/scratch/${username}/iu-gateway");
        sshAccountProvisioner.init(config);
        String userId = "machrist@iu.edu";
        System.out.println("hasAccount=" + sshAccountProvisioner.hasAccount(userId));
        System.out.println("scratchLocation=" + sshAccountProvisioner.getScratchLocation(userId));
        sshAccountProvisioner.installSSHKey(userId, "foobar1234");
    }
}
