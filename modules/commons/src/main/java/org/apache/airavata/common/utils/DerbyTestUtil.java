/*
 *
 * Derby - Class org.apache.derbyTesting.junit.JDBC
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.apache.airavata.common.utils;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JdbcUtil utility methods for the JUnit tests.
 * Note that JSR 169 is a subset of JdbcUtil 3 and
 * JdbcUtil 3 is a subset of JdbcUtil 4.
 * The base level for the Derby tests is JSR 169.
 *
 * Borrowed from http://svn.apache.org/viewvc/db/derby/code/trunk/java/testing/org/apache/derbyTesting/junit/JDBC.java?view=markup
 */
public class DerbyTestUtil {

    private static final Logger logger = LoggerFactory.getLogger(DerbyUtil.class);

    /**
     * Constant to pass to DatabaseMetaData.getTables() to fetch just synonyms.
     */
    public static final String[] GET_TABLES_SYNONYM = new String[] { "SYNONYM" };

    /**
     * Constant to pass to DatabaseMetaData.getTables() to fetch just views.
     */
    public static final String[] GET_TABLES_VIEW = new String[] { "VIEW" };

    /**
     * Constant to pass to DatabaseMetaData.getTables() to fetch just tables.
     */
    public static final String[] GET_TABLES_TABLE = new String[] { "TABLE" };

    private static final String[] CLEAR_DB_PROPERTIES = {"derby.database.classpath",};

    public static void destroyDatabase(JDBCConfig jdbcConfig) {

        Connection conn = null;
        try {
            DBUtil dbUtil = new DBUtil(jdbcConfig);
            conn = dbUtil.getConnection();
            clearProperties(conn);
            removeObjects(conn);
            removeRoles(conn);
            removeUsers(conn);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Database failure", e);
        } finally {
            DBUtil.cleanup(conn);
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
                    dropSchema(dmd, schema);
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
            dropStm.executeUpdate("DROP ROLE " + escape(rs.getString(1)));
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
    public static String escape(String name) {
        StringBuffer buffer = new StringBuffer(name.length() + 2);
        buffer.append('"');
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            // escape double quote characters with an extra double quote
            if (c == '"')
                buffer.append('"');
            buffer.append(c);
        }
        buffer.append('"');
        return buffer.toString();
    }

    /**
     * Escape a schama-qualified name so that it is suitable for use in a SQL query
     * executed by JdbcUtil.
     */
    public static String escape(String schema, String name) {
        return escape(schema) + "." + escape(name);
    }

    /**
     * Drop a database schema by dropping all objects in it and then executing DROP
     * SCHEMA. If the schema is APP it is cleaned but DROP SCHEMA is not executed.
     * <p>
     * TODO: Handle dependencies by looping in some intelligent way until everything
     * can be dropped.
     *
     * @param dmd    DatabaseMetaData object for database
     * @param schema Name of the schema
     * @throws SQLException database error
     */
    public static void dropSchema(DatabaseMetaData dmd, String schema) throws SQLException {
        Connection conn = dmd.getConnection();
        Assert.assertFalse(conn.getAutoCommit());
        Statement s = dmd.getConnection().createStatement();

        // Triggers
        PreparedStatement pstr = conn.prepareStatement("SELECT TRIGGERNAME FROM SYS.SYSSCHEMAS S, SYS.SYSTRIGGERS T "
                + "WHERE S.SCHEMAID = T.SCHEMAID AND SCHEMANAME = ?");
        pstr.setString(1, schema);
        ResultSet trrs = pstr.executeQuery();
        while (trrs.next()) {
            String trigger = trrs.getString(1);
            s.execute("DROP TRIGGER " + DerbyTestUtil.escape(schema, trigger));
        }
        trrs.close();
        pstr.close();

        // Functions - not supported by JdbcUtil meta data until JdbcUtil 4
        // Need to use the CHAR() function on A.ALIASTYPE
        // so that the compare will work in any schema.
        PreparedStatement psf = conn.prepareStatement("SELECT ALIAS FROM SYS.SYSALIASES A, SYS.SYSSCHEMAS S"
                + " WHERE A.SCHEMAID = S.SCHEMAID " + " AND CHAR(A.ALIASTYPE) = ? " + " AND S.SCHEMANAME = ?");
        psf.setString(1, "F");
        psf.setString(2, schema);
        ResultSet rs = psf.executeQuery();
        dropUsingDMD(s, rs, schema, "ALIAS", "FUNCTION");

        // Procedures
        rs = dmd.getProcedures((String) null, schema, (String) null);

        dropUsingDMD(s, rs, schema, "PROCEDURE_NAME", "PROCEDURE");

        // Views
        rs = dmd.getTables((String) null, schema, (String) null, GET_TABLES_VIEW);

        dropUsingDMD(s, rs, schema, "TABLE_NAME", "VIEW");

        // Tables
        rs = dmd.getTables((String) null, schema, (String) null, GET_TABLES_TABLE);

        dropUsingDMD(s, rs, schema, "TABLE_NAME", "TABLE");

        // At this point there may be tables left due to
        // foreign key constraints leading to a dependency loop.
        // Drop any constraints that remain and then drop the tables.
        // If there are no tables then this should be a quick no-op.
        ResultSet table_rs = dmd.getTables((String) null, schema, (String) null, GET_TABLES_TABLE);

        while (table_rs.next()) {
            String tablename = table_rs.getString("TABLE_NAME");
            rs = dmd.getExportedKeys((String) null, schema, tablename);
            while (rs.next()) {
                short keyPosition = rs.getShort("KEY_SEQ");
                if (keyPosition != 1)
                    continue;
                String fkName = rs.getString("FK_NAME");
                // No name, probably can't happen but couldn't drop it anyway.
                if (fkName == null)
                    continue;
                String fkSchema = rs.getString("FKTABLE_SCHEM");
                String fkTable = rs.getString("FKTABLE_NAME");

                String ddl = "ALTER TABLE " + DerbyTestUtil.escape(fkSchema, fkTable) + " DROP FOREIGN KEY "
                        + DerbyTestUtil.escape(fkName);
                s.executeUpdate(ddl);
            }
            rs.close();
        }
        table_rs.close();
        conn.commit();

        // Tables (again)
        rs = dmd.getTables((String) null, schema, (String) null, GET_TABLES_TABLE);
        dropUsingDMD(s, rs, schema, "TABLE_NAME", "TABLE");

        // drop UDTs
        psf.setString(1, "A");
        psf.setString(2, schema);
        rs = psf.executeQuery();
        dropUsingDMD(s, rs, schema, "ALIAS", "TYPE");

        // drop aggregates
        psf.setString(1, "G");
        psf.setString(2, schema);
        rs = psf.executeQuery();
        dropUsingDMD(s, rs, schema, "ALIAS", "DERBY AGGREGATE");
        psf.close();

        // Synonyms - need work around for DERBY-1790 where
        // passing a table type of SYNONYM fails.
        rs = dmd.getTables((String) null, schema, (String) null, GET_TABLES_SYNONYM);

        dropUsingDMD(s, rs, schema, "TABLE_NAME", "SYNONYM");

        // sequences
        if (sysSequencesExists(conn)) {
            psf = conn.prepareStatement("SELECT SEQUENCENAME FROM SYS.SYSSEQUENCES A, SYS.SYSSCHEMAS S"
                    + " WHERE A.SCHEMAID = S.SCHEMAID " + " AND S.SCHEMANAME = ?");
            psf.setString(1, schema);
            rs = psf.executeQuery();
            dropUsingDMD(s, rs, schema, "SEQUENCENAME", "SEQUENCE");
            psf.close();
        }

        // Finally drop the schema if it is not APP
        if (!schema.equals("APP")) {
            s.executeUpdate("DROP SCHEMA " + DerbyTestUtil.escape(schema) + " RESTRICT");
        }
        conn.commit();
        s.close();
    }

    /**
     * DROP a set of objects based upon a ResultSet from a DatabaseMetaData call.
     * <p>
     * TODO: Handle errors to ensure all objects are dropped, probably requires
     * interaction with its caller.
     *
     * @param s        Statement object used to execute the DROP commands.
     * @param rs       DatabaseMetaData ResultSet
     * @param schema   Schema the objects are contained in
     * @param mdColumn The column name used to extract the object's name from rs
     * @param dropType The keyword to use after DROP in the SQL statement
     * @throws SQLException database errors.
     */
    private static void dropUsingDMD(Statement s, ResultSet rs, String schema, String mdColumn, String dropType)
            throws SQLException {
        String dropLeadIn = "DROP " + dropType + " ";

        // First collect the set of DROP SQL statements.
        ArrayList<String> ddl = new ArrayList<String>();
        while (rs.next()) {
            String objectName = rs.getString(mdColumn);
            String raw = dropLeadIn + DerbyTestUtil.escape(schema, objectName);
            if (
                    "TYPE".equals(dropType) ||
                            "SEQUENCE".equals(dropType) ||
                            "DERBY AGGREGATE".equals(dropType)
                    ) {
                raw = raw + " restrict ";
            }
            ddl.add(raw);
        }
        rs.close();
        if (ddl.isEmpty())
            return;

        // Execute them as a complete batch, hoping they will all succeed.
        s.clearBatch();
        int batchCount = 0;
        for (Iterator<String> i = ddl.iterator(); i.hasNext(); ) {
            String sql = i.next();
            if (sql != null) {
                s.addBatch(sql);
                batchCount++;
            }
        }

        int[] results;
        boolean hadError;
        try {
            results = s.executeBatch();
            Assert.assertNotNull(results);
            Assert.assertEquals("Incorrect result length from executeBatch",
                    batchCount, results.length);
            hadError = false;
        } catch (BatchUpdateException batchException) {
            results = batchException.getUpdateCounts();
            Assert.assertNotNull(results);
            Assert.assertTrue("Too many results in BatchUpdateException",
                    results.length <= batchCount);
            hadError = true;
        }

        // Remove any statements from the list that succeeded.
        boolean didDrop = false;
        for (int i = 0; i < results.length; i++) {
            int result = results[i];
            if (result == Statement.EXECUTE_FAILED)
                hadError = true;
            else if (result == Statement.SUCCESS_NO_INFO || result >= 0) {
                didDrop = true;
                ddl.set(i, null);
            } else
                Assert.fail("Negative executeBatch status");
        }
        s.clearBatch();
        if (didDrop) {
            // Commit any work we did do.
            s.getConnection().commit();
        }

        // If we had failures drop them as individual statements
        // until there are none left or none succeed. We need to
        // do this because the batch processing stops at the first
        // error. This copes with the simple case where there
        // are objects of the same type that depend on each other
        // and a different drop order will allow all or most
        // to be dropped.
        if (hadError) {
            do {
                hadError = false;
                didDrop = false;
                for (ListIterator<String> i = ddl.listIterator(); i.hasNext(); ) {
                    String sql = i.next();
                    if (sql != null) {
                        try {
                            s.executeUpdate(sql);
                            i.set(null);
                            didDrop = true;
                        } catch (SQLException e) {
                            hadError = true;
                        }
                    }
                }
                if (didDrop)
                    s.getConnection().commit();
            } while (hadError && didDrop);
        }
    }

    /**
     * Return true if the SYSSEQUENCES table exists.
     */
    private static boolean sysSequencesExists(Connection conn) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement
                    (
                            "select count(*) from sys.systables t, sys.sysschemas s\n" +
                                    "where t.schemaid = s.schemaid\n" +
                                    "and ( cast(s.schemaname as varchar(128)))= 'SYS'\n" +
                                    "and ( cast(t.tablename as varchar(128))) = 'SYSSEQUENCES'");
            rs = ps.executeQuery();
            rs.next();
            return (rs.getInt(1) > 0);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
    }
}
