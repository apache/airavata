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
 * Test class for JDBC user store.
 */
@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, JDBCUserStoreTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@Transactional
public class JDBCUserStoreTest {

    /**
     * <specificConfigurations>
     * <database>
     * <!--jdbcUrl>jdbc:h2:modules/commons/airavata-registry-rest/src/test/resources/testdb/test</jdbcUrl-->
     * <jdbcUrl>jdbc:h2:src/test/resources/testdb/test</jdbcUrl>
     * <userName>sa</userName>
     * <password>sa</password>
     * <databaseDriver>org.h2.Driver</databaseDriver>
     * <userTableName>AIRAVATA_USER</userTableName>
     * <userNameColumnName>USERID</userNameColumnName>
     * <passwordColumnName>PASSWORD</passwordColumnName>
     * </database>
     * </specificConfigurations>
     * @throws Exception
     */
    public JDBCUserStoreTest() {
        // Spring Boot test - no dependencies to inject for this utility test
    }

    @BeforeEach
    public void setUp() throws Exception {}

    @Test
    public void testAuthenticate() throws Exception {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.getClass().getClassLoader().getResourceAsStream("jdbc-authenticator.xml"));
        doc.getDocumentElement().normalize();

        NodeList configurations = doc.getElementsByTagName("specificConfigurations");
        UserStore userStore = new JDBCUserStore();
        userStore.configure(configurations.item(0));

        assertTrue(userStore.authenticate("amilaj", "secret"));
        assertFalse(userStore.authenticate("amilaj", "1secret"));
        assertFalse(userStore.authenticate("lahiru", "1234"));
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
