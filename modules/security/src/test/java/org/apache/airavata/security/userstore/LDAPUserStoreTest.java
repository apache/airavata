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
package org.apache.airavata.security.userstore;

import junit.framework.TestCase;
import org.apache.airavata.security.UserStore;
import org.junit.Ignore;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * User store test 2
 */
@Ignore("Need LDAP server to run these tests")
public class LDAPUserStoreTest extends TestCase{

    private LDAPUserStore ldapUserStore;

    public void setUp() {
        ldapUserStore = new LDAPUserStore();

        ldapUserStore.initializeLDAP("ldap://localhost:10389", "admin", "secret", "uid={0},ou=system");
    }

    public void testAuthenticate() throws Exception {
        assertTrue(ldapUserStore.authenticate("amilaj", "secret"));
        assertFalse(ldapUserStore.authenticate("amilaj", "secret1"));
    }

    public void testConfigure() throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.getClass().getClassLoader().getResourceAsStream("ldap-authenticator.xml"));
        doc.getDocumentElement().normalize();

        NodeList configurations = doc.getElementsByTagName("specificConfigurations");
        UserStore userStore = new LDAPUserStore();
        userStore.configure(configurations.item(0));

        assertTrue(userStore.authenticate("amilaj", "secret"));
    }



}
