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
package org.apache.airavata.credential.store.store.impl.db;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.List;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.DatabaseTestCases;
import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.credential.store.credential.CommunityUser;
import org.apache.airavata.credential.store.repository.CommunityUserRepository;
import org.junit.jupiter.api.*;

/**
 * Test for community user repository.
 */
public class CommunityUserDAOTest extends DatabaseTestCases {

    private CommunityUserRepository communityUserRepository;

    @BeforeAll
    public static void setUpDatabase() throws Exception {
        DerbyUtil.startDerbyInServerMode(getHostAddress(), getPort(), getUserName(), getPassword());
        waitTillServerStarts();
        String createTable = "CREATE TABLE COMMUNITY_USER\n" + "                (\n"
                + "                        GATEWAY_ID VARCHAR(256) NOT NULL,\n"
                + "                        COMMUNITY_USER_NAME VARCHAR(256) NOT NULL,\n"
                + "                        TOKEN_ID VARCHAR(256) NOT NULL,\n"
                + "                        COMMUNITY_USER_EMAIL VARCHAR(256) NOT NULL,\n"
                + "                        PRIMARY KEY (GATEWAY_ID, COMMUNITY_USER_NAME, TOKEN_ID)\n"
                + "                )";
        String dropTable = "drop table COMMUNITY_USER";
        try {
            executeSQL(dropTable);
        } catch (Exception e) {
        }
        executeSQL(createTable);
    }

    @AfterAll
    public static void shutDownDatabase() throws Exception {
        DerbyUtil.stopDerbyServer();
    }

    @BeforeEach
    public void setUp() throws Exception {
        communityUserRepository = new CommunityUserRepository();
        Connection connection = getDbUtil().getConnection();
        try {
            DBUtil.truncate("community_user", connection);
        } finally {
            connection.close();
        }
    }

    @Test
    public void testAddCommunityUser() throws Exception {

        Connection connection = getConnection();

        try {

            CommunityUser communityUser = new CommunityUser("gw1", "ogce", "ogce@sciencegateway.org");
            CommunityUserEntity entity = new CommunityUserEntity("gw1", "ogce", "Token1", "ogce@sciencegateway.org");
            communityUserRepository.create(entity);

            communityUser = new CommunityUser("gw1", "ogce2", "ogce@sciencegateway.org");
            entity = new CommunityUserEntity("gw1", "ogce2", "Token2", "ogce@sciencegateway.org");
            communityUserRepository.create(entity);

            List<CommunityUserEntity> users = communityUserRepository.findByGatewayIdAndCommunityUserName("gw1", "ogce");
            assertNotNull(users);
            assertFalse(users.isEmpty());
            assertEquals("ogce@sciencegateway.org", users.get(0).getCommunityUserEmail());

            users = communityUserRepository.findByGatewayIdAndCommunityUserName("gw1", "ogce2");
            assertNotNull(users);
            assertFalse(users.isEmpty());
            assertEquals("ogce@sciencegateway.org", users.get(0).getCommunityUserEmail());

            users = communityUserRepository.findByTokenId("Token1");
            assertNotNull(users);
            assertFalse(users.isEmpty());
            assertEquals("ogce", users.get(0).getCommunityUserName());
            assertEquals("ogce@sciencegateway.org", users.get(0).getCommunityUserEmail());

            users = communityUserRepository.findByTokenId("Token2");
            assertNotNull(users);
            assertFalse(users.isEmpty());
            assertEquals("ogce2", users.get(0).getCommunityUserName());
            assertEquals("ogce@sciencegateway.org", users.get(0).getCommunityUserEmail());

        } finally {
            connection.close();
        }
    }

    @Test
    public void testDeleteCommunityUser() throws Exception {

        Connection connection = getConnection();

        try {
            CommunityUser communityUser = new CommunityUser("gw1", "ogce", "ogce@sciencegateway.org");
            CommunityUserEntity entity = new CommunityUserEntity("gw1", "ogce", "Token1", "ogce@sciencegateway.org");
            communityUserRepository.create(entity);

            List<CommunityUserEntity> users = communityUserRepository.findByGatewayIdAndCommunityUserName("gw1", "ogce");
            assertNotNull(users);
            assertFalse(users.isEmpty());

            communityUserRepository.deleteByGatewayIdAndCommunityUserName("gw1", "ogce");

            users = communityUserRepository.findByGatewayIdAndCommunityUserName("gw1", "ogce");
            assertTrue(users.isEmpty());

        } finally {
            connection.close();
        }
    }

    @Test
    public void testDeleteCommunityUserByToken() throws Exception {

        Connection connection = getConnection();

        try {
            CommunityUser communityUser = new CommunityUser("gw1", "ogce", "ogce@sciencegateway.org");
            CommunityUserEntity entity = new CommunityUserEntity("gw1", "ogce", "Token1", "ogce@sciencegateway.org");
            communityUserRepository.create(entity);

            List<CommunityUserEntity> users = communityUserRepository.findByGatewayIdAndCommunityUserName("gw1", "ogce");
            assertNotNull(users);
            assertFalse(users.isEmpty());

            communityUserRepository.deleteByTokenId("Token1");

            users = communityUserRepository.findByGatewayIdAndCommunityUserName("gw1", "ogce");
            assertTrue(users.isEmpty());

        } finally {
            connection.close();
        }
    }

    @Test
    public void testGetCommunityUsers() throws Exception {

        Connection connection = getConnection();

        try {
            CommunityUser communityUser = new CommunityUser("gw1", "ogce", "ogce@sciencegateway.org");
            CommunityUserEntity entity = new CommunityUserEntity("gw1", "ogce", "Token1", "ogce@sciencegateway.org");
            communityUserRepository.create(entity);

            List<CommunityUserEntity> users = communityUserRepository.findByGatewayIdAndCommunityUserName("gw1", "ogce");
            assertNotNull(users);
            assertFalse(users.isEmpty());
            assertEquals("ogce@sciencegateway.org", users.get(0).getCommunityUserEmail());

        } finally {
            connection.close();
        }
    }

    @Test
    public void testGetCommunityUsersForGateway() throws Exception {

        Connection connection = getConnection();

        CommunityUser communityUser = new CommunityUser("gw1", "ogce", "ogce@sciencegateway.org");
        CommunityUserEntity entity = new CommunityUserEntity("gw1", "ogce", "Token1", "ogce@sciencegateway.org");
        communityUserRepository.create(entity);

        communityUser = new CommunityUser("gw1", "ogce2", "ogce@sciencegateway.org");
        entity = new CommunityUserEntity("gw1", "ogce2", "Token2", "ogce@sciencegateway.org");
        communityUserRepository.create(entity);

        List<CommunityUserEntity> users = communityUserRepository.findByGatewayId("gw1");
        assertNotNull(users);
        assertEquals(2, users.size());

        assertEquals(users.get(0).getCommunityUserName(), "ogce");
        assertEquals(users.get(1).getCommunityUserName(), "ogce2");
    }
}
