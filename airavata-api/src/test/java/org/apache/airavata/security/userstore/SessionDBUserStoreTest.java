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

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Test class for session DB authenticator.
 */
@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, SessionDBUserStoreTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@Transactional
public class SessionDBUserStoreTest {

    private SessionDBUserStore sessionDBUserStore;

    private InputStream configurationFileStream =
            this.getClass().getClassLoader().getResourceAsStream("session-authenticator.xml");

    public SessionDBUserStoreTest() {
        // Spring Boot test - no dependencies to inject for this utility test
    }

    @BeforeEach
    public void setUp() throws Exception {
        sessionDBUserStore = new SessionDBUserStore();
        loadConfigurations();
    }

    private void loadConfigurations() throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(configurationFileStream);
        doc.getDocumentElement().normalize();

        NodeList specificConfigurations = doc.getElementsByTagName("specificConfigurations");
        sessionDBUserStore.configure(specificConfigurations.item(0));
    }

    @Test
    public void testAuthenticate() throws Exception {
        assertTrue(sessionDBUserStore.authenticate("1234"));
    }

    @Test
    public void testAuthenticateFailure() throws Exception {
        assertFalse(sessionDBUserStore.authenticate("12345"));
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
