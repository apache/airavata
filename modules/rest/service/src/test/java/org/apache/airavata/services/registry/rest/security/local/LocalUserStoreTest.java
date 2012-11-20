package org.apache.airavata.services.registry.rest.security.local;

import junit.framework.TestCase;
import org.apache.airavata.common.utils.DBUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * A test class for local user store.
 */
public class LocalUserStoreTest extends TestCase {

    private DBUtil dbUtil = new DBUtil("jdbc:h2:modules/airavata-rest-services/src/test/resources/testdb/test",
            "sa", "sa", "org.h2.Driver");

    private LocalUserStore localUserStore;

    private static final String createTableScript =
            "create table Users\n" +
                    "(\n" +
                    "        user_name varchar(255),\n" +
                    "        password varchar(255),\n" +
                    "        PRIMARY KEY(user_name)\n" +
                    ");";

    private static final String dropTableScript =
            "drop table Users";

    private void createTable(Connection connection, String query) {


        Statement stmt = null;

        try {
            stmt = connection.createStatement();
            stmt.executeUpdate(query);
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setUp() throws Exception{

        dbUtil.init();

        File f = new File(".");
        System.out.println(f.getAbsolutePath());

        Connection connection = dbUtil.getConnection();
        createTable(connection, dropTableScript);
        createTable(connection, createTableScript);

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        localUserStore = new LocalUserStore(dbUtil);
    }

    public void testAddUser() throws Exception {

        localUserStore.addUser("thejaka", "qwqwqw");

        List<String> users = localUserStore.getUsers();
        assertEquals(1, users.size());
        assertEquals("thejaka", users.get(0));
    }

    public void testChangePassword() throws Exception {

        localUserStore.addUser("thejaka", "qwqwqw");

        localUserStore.changePassword("thejaka", "qwqwqw", "sadsad");
    }

    public void testChangePasswordByAdmin() throws Exception {

        localUserStore.addUser("thejaka", "qwqwqw");

        localUserStore.changePasswordByAdmin("thejaka", "sadsad");
    }

    public void testDeleteUser() throws Exception {

        localUserStore.addUser("thejaka", "qwqwqw");

        List<String> users = localUserStore.getUsers();
        assertEquals(1, users.size());
        assertEquals("thejaka", users.get(0));

        localUserStore.deleteUser("thejaka");

        users = localUserStore.getUsers();
        assertEquals(0, users.size());

    }
}
