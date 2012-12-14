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

    private static final String createTableScript =
            "create table Users\n" +
                    "(\n" +
                    "        user_name varchar(255),\n" +
                    "        password varchar(255),\n" +
                    "        PRIMARY KEY(user_name)\n" +
                    ")";


    @BeforeClass
    public static void setUpDatabase() throws Exception{
        DerbyUtil.startDerbyInServerMode(getHostAddress(), getPort(), getUserName(), getPassword());

        waitTillServerStarts();

        executeSQL(createTableScript);

    }

    @AfterClass
    public static void shutDownDatabase() throws Exception {
        DerbyUtil.stopDerbyServer();
    }

    @Before
    public void setUp() throws Exception{

        DBUtil dbUtil = new DBUtil(getJDBCUrl(), getUserName(), getPassword(), getDriver());
        dbUtil.init();

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
