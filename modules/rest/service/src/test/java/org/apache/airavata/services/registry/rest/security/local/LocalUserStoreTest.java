/*
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
 *
 */

package org.apache.airavata.services.registry.rest.security.local;

import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.DatabaseTestCases;
import org.apache.airavata.common.utils.DerbyUtil;
import org.junit.*;

import java.util.List;

/**
 * A test class for local user store.
 */
public class LocalUserStoreTest extends DatabaseTestCases {

    private LocalUserStore localUserStore;

    private static final String createTableScript = "create table Users\n" + "(\n"
            + "        user_name varchar(255),\n" + "        password varchar(255),\n"
            + "        PRIMARY KEY(user_name)\n" + ")";

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        DerbyUtil.startDerbyInServerMode(getHostAddress(), getPort(), getUserName(), getPassword());

        waitTillServerStarts();

        executeSQL(createTableScript);

    }

    @AfterClass
    public static void shutDownDatabase() throws Exception {
        DerbyUtil.stopDerbyServer();
    }

    @Before
    public void setUp() throws Exception {

        DBUtil dbUtil = new DBUtil(getJDBCUrl(), getUserName(), getPassword(), getDriver());

        localUserStore = new LocalUserStore(dbUtil);
    }

    @Test
    public void testAddUser() throws Exception {

        localUserStore.addUser("thejaka", "qwqwqw");

        List<String> users = localUserStore.getUsers();
        Assert.assertEquals(1, users.size());
        Assert.assertEquals("thejaka", users.get(0));

        localUserStore.deleteUser("thejaka");

    }

    @Test
    public void testChangePassword() throws Exception {

        localUserStore.addUser("thejaka", "qwqwqw");

        localUserStore.changePassword("thejaka", "qwqwqw", "sadsad");

        localUserStore.deleteUser("thejaka");
    }

    @Test
    public void testChangePasswordByAdmin() throws Exception {

        localUserStore.addUser("thejaka", "qwqwqw");

        localUserStore.changePasswordByAdmin("thejaka", "sadsad");

        localUserStore.deleteUser("thejaka");
    }

    @Test
    public void testDeleteUser() throws Exception {

        localUserStore.addUser("thejaka", "qwqwqw");

        List<String> users = localUserStore.getUsers();
        Assert.assertEquals(1, users.size());
        Assert.assertEquals("thejaka", users.get(0));

        localUserStore.deleteUser("thejaka");

        users = localUserStore.getUsers();
        Assert.assertEquals(0, users.size());

    }
}
