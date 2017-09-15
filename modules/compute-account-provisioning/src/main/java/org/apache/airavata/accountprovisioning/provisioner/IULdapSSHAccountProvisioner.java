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
import org.apache.airavata.accountprovisioning.SSHAccountProvisioner;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.ldap.client.api.*;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.message.DeleteResponse;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.junit.Assert;
import java.util.Map;
import java.util.List;

public class IULdapSSHAccountProvisioner implements SSHAccountProvisioner  {

    String ldaphost, adminDN, ldap_username, ldap_password, adminPass, ldapBaseDN;
    int ldapport, ldapPortId;
    LdapConnection connection;
    @Override
    public void init(Map<ConfigParam, String> config) {

        // TODO: implement
        ldapServerName =  config.get(new ConfigParam("ldaphost"));//"bazooka.hps.iu.edu"
        ldapPortId = config.get(new ConfigParam("ldapport"));//"636"
        ldap_username = config.get(new ConfigParam("ldap_username"));//"cn=sgrcusr"
        ldap_password = config.get(new ConfigParam("ldap_password"));//"lore footwork engorge"
        ldapBaseDN = config.get(new ConfigParam( "ldapBaseDN" ));//"dc=rt,dc=iu,dc=edu"
        try {
            connection = new LdapNetworkConnection(ldaphost, ldapport, true);


            System.out.println( "binding connection:" );
            String AuthDN=ldap_username+","+ldapBaseDN;
            connection.bind(AuthDN,ldap_password);
            //check that we're auth'ed and connected
            System.out.println("asserting bound:");
            Assert.assertTrue( connection.isAuthenticated() );
            Assert.assertTrue( connection.isConnected() );
            }  catch (Exception e) {
            System.out.println("Exception caught!");
            System.out.println(e.getClass().getCanonicalName());
            System.out.println(e.getMessage());
            System.out.println(e.getCause());

            }
            //catch (LdapException le) {
            //System.out.println("Ldap Exception caught!", le);
            //}

    }

    @Override
    public boolean hasAccount(String username) {
        // TODO: implement
        // To verify if the user has a login on a remote host
        // if not advice the user to get an account (if possible) before returning.
        // a search at the ldap is used to set the value
        System.out.println("attempting search:");
        String uidName="uid="+username;
        List<String>  userClusters = new ArrayList();
        try {
            EntryCursor cursor = connection.search( ldapBaseDN, uidName, SearchScope.SUBTREE, "*" );
            System.out.println( "Printing LDAP-wide results for " + username + ":" );
            while (cursor.next()) {
                Entry entry = cursor.get();
                String DNName = entry.getDn().getName();
                String[] words=DNName.split(",");
                String cluster = words[1].replace("ou=");
                userClusters.add(cluster);
                //System.out.println( entry.getDn().getName() );
                //System.out.println( entry.getAttributes() );
                return true;
            }
            cursor.close();
        }catch (Exception e) {
            System.out.println( "Exception caught!" );
            System.out.println( e.getClass().getCanonicalName() );
            System.out.println( e.getMessage() );
            System.out.println( e.getCause() );
        } catch (CursorException ce) {
            System.out.println( "Cursor Exception caught!" );
        }catch (LdapException le) {
            System.out.println( "Ldap Exception caught!" );
        }
        return false;
    }

    @Override
    public void createAccount(String username, String sshPublicKey) {

        throw new UnsupportedOperationException("IULdapSSHAccountProvisioner does not support creating cluster accounts at this time.");
    }

    @Override
    public void installSSHKey(String username, String sshPublicKey) {
        // TODO: implement
        // use Eric Coulter's LdapBazookaSearchAndAdd to accomplsih this
        String GatewaySSHPublicKey = sshPublicKey;
        String IULocalUserName = username;


        /*
        LdapConnectionConfig lcconfig = new LdapConnectionConfig();
        lcconfig.setLdapHost(ldapservername);// LdapServerName = ldapserverName;//from ConfigParam should be like bazooka.hpc.iu.edu
        lcconfig.setLdapPort(ldapPortId);// LdapPortID = ldapPortId;//from ConfigParam 636
        lcconfig.setName(adminDN);// = adminName;//from ConfigParam sgrcusr
        lcconfig.setCredentials(AdminPass);//from ConfigParam "lore footwork engorge"

        DefaultLdapConnectionFactory lcfactory = new DefaultLdapConnectionFactory( lcconfig );
        lcfactory.setTimeOut( connectionTimeout );
        */

            Modification addSSHPublicKeyAdd = new DefaultModification(ModificationOperation.ADD_ATTRIBUTE,"add","sshPublicKey");
            Modification SSHPublicKey = new DefaultModification(ModificationOperation.ADD_ATTRIBUTE, "sshPublicKey",GatewaySSHPublicKey);
            /*
            Entry modentry = New DefaultEntry(
                    "cn=sgrcusr,dc=rt,dc=iu,dc=edu",
                    "ObjectClass: person",
                    "ObjectClass: ldapPublicKey",
                    "cn", username,
                    "dn", "uid=",username, "ou=bigred2-sgrc,dc=rt,dc=iu,dc=edu",
                    "add: sshPublicKey",
                    "sshPublicKey", GatewaySSHPublicKey );
                    */
            try {
                connection.modify(ldapBaseDN, addSSHPublicKeyAdd );//ldapmodify
                connection.modify(ldapBaseDN, SSHPublicKey );
            } catch (Exception e) {
                System.out.println("Exception caught!", e);
                System.out.println( e.getClass().getCanonicalName() );
                System.out.println( e.getMessage() );
                System.out.println( e.getCause() );
            }

    }

    @Override
    public String getScratchLocation(String username) {
        // TODO: implement
        //if scratch location is available get it or else set a new scratch location for the user
        String canonicalScratch =  config.get(new ConfigParam("canonicalScratch"));//"/N/cd2/_USER_/scratch"
        String scratchLocation = canonicalScratch.replace("_USER_",username);
        return scratchLocation;
        //return null;
    }
}
