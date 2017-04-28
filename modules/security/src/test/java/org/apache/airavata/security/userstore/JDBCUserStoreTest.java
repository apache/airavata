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

import org.apache.airavata.common.utils.DatabaseTestCases;
import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.security.UserStore;
import org.junit.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Test class for JDBC user store.
 */
public class JDBCUserStoreTest extends DatabaseTestCases {

    /**
     * <specificConfigurations>
     <database>
     <!--jdbcUrl>jdbc:h2:modules/commons/airavata-registry-rest/src/test/resources/testdb/test</jdbcUrl-->
     <jdbcUrl>jdbc:h2:src/test/resources/testdb/test</jdbcUrl>
     <userName>sa</userName>
     <password>sa</password>
     <databaseDriver>org.h2.Driver</databaseDriver>
     <userTableName>AIRAVATA_USER</userTableName>
     <userNameColumnName>USERID</userNameColumnName>
     <passwordColumnName>PASSWORD</passwordColumnName>
     </database>
     </specificConfigurations>
     * @throws Exception
     */


    @BeforeClass
    public static void setUpDatabase() throws Exception{
        DerbyUtil.startDerbyInServerMode(getHostAddress(), getPort(), getUserName(), getPassword());

        waitTillServerStarts();

        String dropTable = "drop table AIRAVATA_USER";

        try {
            executeSQL(dropTable);
        } catch (Exception e) {
        }

        String createTable = "create table AIRAVATA_USER ( USERID varchar(255), PASSWORD varchar(255) )";
        executeSQL(createTable);

        String insertSQL = "INSERT INTO AIRAVATA_USER VALUES('amilaj', 'secret')";
        executeSQL(insertSQL);


    }

    @AfterClass
    public static void shutDownDatabase() throws Exception {
        DerbyUtil.stopDerbyServer();
    }

    @Before
    public void setUp() throws Exception{
    }

    @Test
    public void testAuthenticate() throws Exception {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.getClass().getClassLoader().getResourceAsStream("jdbc-authenticator.xml"));
        doc.getDocumentElement().normalize();

        NodeList configurations = doc.getElementsByTagName("specificConfigurations");
        UserStore userStore = new JDBCUserStore();
        userStore.configure(configurations.item(0));

        Assert.assertTrue(userStore.authenticate("amilaj", "secret"));
        Assert.assertFalse(userStore.authenticate("amilaj", "1secret"));
        Assert.assertFalse(userStore.authenticate("lahiru", "1234"));

    }
}
