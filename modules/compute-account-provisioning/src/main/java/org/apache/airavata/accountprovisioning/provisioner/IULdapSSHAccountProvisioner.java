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
package org.apache.airavata.accountprovisioning.provisioner;

import org.apache.airavata.accountprovisioning.ConfigParam;
import org.apache.airavata.accountprovisioning.InvalidUsernameException;
import org.apache.airavata.accountprovisioning.SSHAccountManager;
import org.apache.airavata.accountprovisioning.SSHAccountProvisioner;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.*;
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
    public static final String GROUP_MEMBER_ATTRIBUTE_NAME = "memberUid";

    private String ldapHost, ldapUsername, ldapPassword, ldapBaseDN, canonicalScratchLocation, cybergatewayGroupDN;
    private int ldapPort;
    @Override
    public void init(Map<ConfigParam, String> config) {

        ldapHost =  config.get(IULdapSSHAccountProvisionerProvider.LDAP_HOST);//"bazooka.hps.iu.edu"
        ldapPort = Integer.valueOf(config.get(IULdapSSHAccountProvisionerProvider.LDAP_PORT));//"636"
        ldapUsername = config.get(IULdapSSHAccountProvisionerProvider.LDAP_USERNAME);//"cn=sgrcusr"
        ldapPassword = config.get(IULdapSSHAccountProvisionerProvider.LDAP_PASSWORD); //"secret password"
        ldapBaseDN = config.get(IULdapSSHAccountProvisionerProvider.LDAP_BASE_DN);//"dc=rt,dc=iu,dc=edu"
        canonicalScratchLocation = config.get(IULdapSSHAccountProvisionerProvider.CANONICAL_SCRATCH_LOCATION); //"/N/dc2/scratch/username/iu-gateway"
        cybergatewayGroupDN = config.get(IULdapSSHAccountProvisionerProvider.CYBERGATEWAY_GROUP_DN); // "cn=cybergateway,ou=Group,dc=rt,dc=iu,dc=edu"
    }

    @Override
    public boolean hasAccount(String userId) throws InvalidUsernameException {
        String username = getUsername(userId);
        boolean result = withLdapConnection(ldapConnection -> {
            try {
                return hasClusterAccount(ldapConnection, username);
            } catch (LdapException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }

    private boolean hasClusterAccount(LdapConnection ldapConnection, String username) throws LdapException {

        return ldapConnection.exists("uid=" + username + "," + ldapBaseDN);
    }

    private boolean isInCybergatewayGroup(LdapConnection ldapConnection, String username) throws LdapException {

        final String filter = "(memberUid=" + username + ")";
        try(EntryCursor entryCursor = ldapConnection.search(this.cybergatewayGroupDN, filter, SearchScope.OBJECT)) {

            int count = 0;
            for (Entry entry : entryCursor) {
                count++;
                logger.info("Found {} in cybergateway group", username);
            }
            return count == 1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String createAccount(String userId, String sshPublicKey) throws InvalidUsernameException {

        throw new UnsupportedOperationException("IULdapSSHAccountProvisioner does not support creating cluster accounts at this time.");
    }

    @Override
    public boolean isSSHAccountProvisioningComplete(String userId, String sshPublicKey) throws InvalidUsernameException {
        String username = getUsername(userId);
        boolean result = withLdapConnection(ldapConnection -> {
            try {
                return hasClusterAccount(ldapConnection, username)
                        && isInCybergatewayGroup(ldapConnection, username)
                        && isSSHKeyInstalled(ldapConnection, username, sshPublicKey);
            } catch (LdapException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }

    public boolean isSSHKeyInstalled(LdapConnection ldapConnection, String username, String sshPublicKey) throws LdapException {

        String ldapPublicKey = getLdapPublicKey(ldapConnection, username);
        return ldapPublicKey != null && ldapPublicKey.equals(sshPublicKey.trim());
    }

    @Override
    public String installSSHKey(String userId, String sshPublicKey) throws InvalidUsernameException {
        String username = getUsername(userId);
        String finalSSHPublicKey = sshPublicKey.trim();
        boolean success = withLdapConnection(ldapConnection -> {
            try {
                // 1) Check to see if key is installed, and if not, install it
                // 2) Check to see if user is in cybergateway group and if not add the user
                // This is because the user may have the key installed but not be in the group
                if (!isSSHKeyInstalled(ldapConnection, username, finalSSHPublicKey)) {
                    installLdapPublicKey(ldapConnection, username, finalSSHPublicKey);
                }
                if (!isInCybergatewayGroup(ldapConnection, username)) {
                    addUserToCybergatewayGroup(ldapConnection, username);
                }
                return true;
            } catch (LdapException e) {
                throw new RuntimeException(e);
            }
        });
        return username;
    }

    private void addUserToCybergatewayGroup(LdapConnection ldapConnection, String username) throws LdapException {

        ModifyRequest modifyRequest = new ModifyRequestImpl();
        modifyRequest.setName(new Dn(cybergatewayGroupDN));
        modifyRequest.addModification(new DefaultAttribute(GROUP_MEMBER_ATTRIBUTE_NAME, username),
                ModificationOperation.ADD_ATTRIBUTE);
        ModifyResponse modifyResponse = ldapConnection.modify(modifyRequest);
        if (modifyResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS) {
            logger.warn("add member to cybergateway group ldap operation reported not being successful: " + modifyResponse);
        } else {
            logger.debug("add member to cybergateway group ldap operation was successful: " + modifyResponse);
        }
    }

    private void installLdapPublicKey(LdapConnection ldapConnection, String username, String finalSSHPublicKey) throws LdapException {
        String dn = "uid=" + username + "," + ldapBaseDN;

        String ldapPublicKey = getLdapPublicKey(ldapConnection, username);
        Entry entry = ldapConnection.lookup(dn);
        if (entry == null) {
            throw new RuntimeException("User [" + username + "] has no entry for " + dn);
        }

        ModifyRequest modifyRequest = new ModifyRequestImpl();
        modifyRequest.setName(new Dn(dn));

        // Add or Replace, depending on whether there is already an ldapPublicKey on the entry
        if (ldapPublicKey == null) {

            modifyRequest.addModification(new DefaultAttribute("objectclass", LDAP_PUBLIC_KEY_OBJECT_CLASS), ModificationOperation.ADD_ATTRIBUTE);
            modifyRequest.addModification(new DefaultAttribute(SSH_PUBLIC_KEY_ATTRIBUTE_NAME, finalSSHPublicKey),
                    ModificationOperation.ADD_ATTRIBUTE);
        } else {

            if (!ldapPublicKey.equals(finalSSHPublicKey)) {
                 modifyRequest.addModification(new DefaultAttribute(SSH_PUBLIC_KEY_ATTRIBUTE_NAME,
                        finalSSHPublicKey), ModificationOperation.REPLACE_ATTRIBUTE);
            }
        }
        ModifyResponse modifyResponse = ldapConnection.modify(modifyRequest);
        if (modifyResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS) {
            logger.warn("installSSHKey ldap operation reported not being successful: " + modifyResponse);
        } else {
            logger.debug("installSSHKey ldap operation was successful: " + modifyResponse);
        }
    }

    private String getLdapPublicKey(LdapConnection ldapConnection, String username) throws LdapException {

        String dn = "uid=" + username + "," + ldapBaseDN;

        Entry entry = ldapConnection.lookup(dn);
        if (entry == null) {
            throw new RuntimeException("User [" + username + "] has no entry for " + dn);
        }
        boolean hasLdapPublicKey = entry.hasObjectClass(LDAP_PUBLIC_KEY_OBJECT_CLASS);
        return hasLdapPublicKey ? entry.get(SSH_PUBLIC_KEY_ATTRIBUTE_NAME).getString() : null;
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
        config.put(IULdapSSHAccountProvisionerProvider.CYBERGATEWAY_GROUP_DN, "cn=cybergateway,ou=Group,dc=rt,dc=iu,dc=edu");
        sshAccountProvisioner.init(config);
        String userId = "machrist@iu.edu";
        System.out.println("hasAccount=" + sshAccountProvisioner.hasAccount(userId));
        System.out.println("scratchLocation=" + sshAccountProvisioner.getScratchLocation(userId));
        String sshPublicKey = "foobar12345";
        boolean sshAccountProvisioningComplete = sshAccountProvisioner.isSSHAccountProvisioningComplete(userId, sshPublicKey);
        System.out.println("isSSHAccountProvisioningComplete=" + sshAccountProvisioningComplete);
        if (!sshAccountProvisioningComplete) {
            sshAccountProvisioner.installSSHKey(userId, sshPublicKey);
            sshAccountProvisioningComplete = sshAccountProvisioner.isSSHAccountProvisioningComplete(userId, sshPublicKey);
            System.out.println("isSSHAccountProvisioningComplete=" + sshAccountProvisioningComplete);
        }
    }
}
