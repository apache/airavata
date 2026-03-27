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
package org.apache.airavata.security.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.airavata.common.utils.DatabaseTestCases;
import org.apache.airavata.security.util.UserStore;
import org.junit.jupiter.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Test class for JDBC user store.
 */
public class JDBCUserStoreTest extends DatabaseTestCases {

    @BeforeAll
    public static void setUpDatabase() throws Exception {
        waitTillServerStarts();

        String createTable = "CREATE TABLE IF NOT EXISTS AIRAVATA_USER ( USERID varchar(255), PASSWORD varchar(255) )";
        executeSQL(createTable);

        executeSQL("DELETE FROM AIRAVATA_USER");
        String insertSQL = "INSERT INTO AIRAVATA_USER VALUES('amilaj', 'secret')";
        executeSQL(insertSQL);
    }

    @AfterAll
    public static void shutDownDatabase() throws Exception {
        // Testcontainers handles cleanup
    }

    @BeforeEach
    public void setUp() throws Exception {}

    @Test
    public void testAuthenticate() throws Exception {

        // Build config XML dynamically with Testcontainers JDBC URL
        String xml = "<authenticators>"
                + "<authenticator><specificConfigurations><database>"
                + "<jdbcUrl>" + getJDBCUrl() + "</jdbcUrl>"
                + "<userName>" + getUserName() + "</userName>"
                + "<password>" + getPassword() + "</password>"
                + "<databaseDriver>" + getDriver() + "</databaseDriver>"
                + "<userTableName>AIRAVATA_USER</userTableName>"
                + "<userNameColumnName>USERID</userNameColumnName>"
                + "<passwordColumnName>PASSWORD</passwordColumnName>"
                + "</database></specificConfigurations></authenticator></authenticators>";

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        doc.getDocumentElement().normalize();

        NodeList configurations = doc.getElementsByTagName("specificConfigurations");
        UserStore userStore = new JDBCUserStore();
        userStore.configure(configurations.item(0));

        assertTrue(userStore.authenticate("amilaj", "secret"));
        assertFalse(userStore.authenticate("amilaj", "1secret"));
        assertFalse(userStore.authenticate("lahiru", "1234"));
    }
}
