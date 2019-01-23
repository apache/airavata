package org.apache.airavata.registry.core.repositories.common;

import com.ibatis.common.jdbc.ScriptRunner;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.derby.drda.NetworkServerControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Most of the code in this class was influenced by
 * http://svn.apache.org/viewvc/db/derby/code/trunk/java/testing/org/apache/derbyTesting/junit/CleanDatabaseTestSetup.java?view=markup
 */
public class DerbyDBManager {

    private static final Logger logger = LoggerFactory.getLogger(DerbyDBManager.class);

    private static final String DERBY_SERVER_MODE_SYS_PROPERTY = "derby.drda.startNetworkServer";

    private static final String[] CLEAR_DB_PROPERTIES = {"derby.database.classpath",};

    private NetworkServerControl server;
    private String jdbcDriver = null;
    private String jdbcUser = null;
    private String jdbcPassword = null;


    public DerbyDBManager() {

        try {
            jdbcDriver = ServerSettings.getSetting("appcatalog.jdbc.driver");
            jdbcUser = ServerSettings.getSetting("appcatalog.jdbc.user");
            jdbcPassword = ServerSettings.getSetting("appcatalog.jdbc.password");
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read properties", e);
        }
    }

    public void startDatabaseServer() {
        try {
            System.setProperty(DERBY_SERVER_MODE_SYS_PROPERTY, "true");
            server = new NetworkServerControl(InetAddress.getByName("0.0.0.0"),
                    20000,
                    jdbcUser, jdbcPassword);
            java.io.PrintWriter consoleWriter = new java.io.PrintWriter(System.out, true);
            server.start(consoleWriter);
        } catch (IOException e) {
            logger.error("Unable to start Apache derby in the server mode! Check whether " +
                    "specified port is available");
        } catch (Exception e) {
            logger.error("Unable to start Apache derby in the server mode! Check whether " +
                    "specified port is available");
        }
    }

    public void stopDatabaseServer() {
        try {
            server.shutdown();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void initializeDatabase(String databaseName, String initFile) {

        String jdbcUrl = "jdbc:derby:" + databaseName + ";create=true;user=" + jdbcUser + ";password=" + jdbcPassword;

        Connection conn = null;
        try {
            Class.forName(jdbcDriver).newInstance();
            conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
            ScriptRunner scriptRunner = new ScriptRunner(conn, false, false);
            Reader reader = new BufferedReader(new FileReader(initFile));
            scriptRunner.runScript(reader);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Database failure", e);
        } finally {
            try {
                if (conn != null) {
                    if (!conn.getAutoCommit()) {
                        conn.commit();
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    // TODO: move this into DerbyUtil
    public void destroyDatabase(String databaseName) {

        String jdbcUrl = "jdbc:derby:" + databaseName + ";create=true;user=" + jdbcUser + ";password=" + jdbcPassword;

        Connection conn = null;
        try {
            Class.forName(jdbcDriver).newInstance();
            conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
            conn.setAutoCommit(false);
            clearProperties(conn);
            removeObjects(conn);
            removeRoles(conn);
            removeUsers(conn);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Database failure", e);
        } finally {
            try {
                if (conn != null) {
                    if (!conn.getAutoCommit()) {
                        conn.commit();
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    private static void clearProperties(Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareCall(
                "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(?, NULL)");

        for (String CLEAR_DB_PROPERTY : CLEAR_DB_PROPERTIES) {
            ps.setString(1, CLEAR_DB_PROPERTY);
            ps.executeUpdate();
        }
        ps.close();
        conn.commit();
    }

    private static void removeObjects(Connection conn) throws SQLException {

        DatabaseMetaData dmd = conn.getMetaData();

        SQLException sqle = null;
        // Loop a number of arbitary times to catch cases
        // where objects are dependent on objects in
        // different schemas.
        for (int count = 0; count < 5; count++) {
            // Fetch all the user schemas into a list
            List<String> schemas = new ArrayList<>();
            ResultSet rs = dmd.getSchemas();
            while (rs.next()) {

                String schema = rs.getString("TABLE_SCHEM");
                if (schema.startsWith("SYS"))
                    continue;
                if (schema.equals("SQLJ"))
                    continue;
                if (schema.equals("NULLID"))
                    continue;

                schemas.add(schema);
            }
            rs.close();

            // DROP all the user schemas.
            sqle = null;
            for (String schema : schemas) {
                try {
                    JdbcUtil.dropSchema(dmd, schema);
                } catch (SQLException e) {
                    sqle = e;
                }
            }
            // No errors means all the schemas we wanted to
            // drop were dropped, so nothing more to do.
            if (sqle == null)
                return;
        }
        throw sqle;
    }

    private static void removeRoles(Connection conn) throws SQLException {
        // No metadata for roles, so do a query against SYSROLES
        Statement stm = conn.createStatement();
        Statement dropStm = conn.createStatement();

        // cast to overcome territory differences in some cases:
        ResultSet rs = stm.executeQuery(
                "select roleid from sys.sysroles where " +
                        "cast(isdef as char(1)) = 'Y'");

        while (rs.next()) {
            dropStm.executeUpdate("DROP ROLE " + JdbcUtil.escape(rs.getString(1)));
        }

        stm.close();
        dropStm.close();
        conn.commit();
    }

    private static void removeUsers(Connection conn) throws SQLException {
        // Get the users
        Statement stm = conn.createStatement();
        ResultSet rs = stm.executeQuery("select username from sys.sysusers");
        ArrayList<String> users = new ArrayList<String>();

        while (rs.next()) {
            users.add(rs.getString(1));
        }
        rs.close();
        stm.close();

        // Now delete them
        PreparedStatement ps = conn.prepareStatement("call syscs_util.syscs_drop_user( ? )");

        for (int i = 0; i < users.size(); i++) {
            ps.setString(1, (String) users.get(i));

            // you can't drop the DBO's credentials. sorry.
            try {
                ps.executeUpdate();
            } catch (SQLException se) {
                if ("4251F".equals(se.getSQLState())) {
                    continue;
                } else {
                    throw se;
                }
            }
        }

        ps.close();
        conn.commit();
    }
}
