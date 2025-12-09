/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.security.userstore;

import static org.junit.jupiter.api.Assertions.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.airavata.security.UserStore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * User store test 2
 */
@Disabled("Need LDAP server to run these tests")
@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, LDAPUserStoreTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
public class LDAPUserStoreTest {

    private LDAPUserStore ldapUserStore;

    public LDAPUserStoreTest() {
        // Spring Boot test - no dependencies to inject for this utility test
    }

    @Test
    public void setUp() {
        ldapUserStore = new LDAPUserStore();
        ldapUserStore.initializeLDAP("ldap://localhost:10389", "admin", "secret", "uid={0},ou=system");
    }

    @Test
    public void testAuthenticate() throws Exception {
        setUp();
        assertTrue(ldapUserStore.authenticate("amilaj", "secret"));
        assertFalse(ldapUserStore.authenticate("amilaj", "secret1"));
    }

    @Test
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

    @org.springframework.context.annotation.Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.security",
                "org.apache.airavata.config"
            },
            excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.config.ThriftServerLauncher.class
                        })
            })
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {}
}
