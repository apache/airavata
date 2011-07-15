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

package org.apache.airavata.wsmg.commons.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.airavata.wsmg.broker.subscription.SubscriptionEntry;
import org.apache.airavata.wsmg.broker.subscription.SubscriptionState;
import org.apache.airavata.wsmg.commons.CommonRoutines;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.config.ConfigurationManager;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.apache.airavata.wsmg.msgbox.Storage.DB_Pool.DatabaseCreator;
import org.apache.airavata.wsmg.util.Counter;
import org.apache.airavata.wsmg.util.TimerThread;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;
import org.apache.airavata.wsmg.msgbox.Storage.DB_Pool.ConnectionPool;

public class WsmgPersistantStorage implements WsmgStorage {
    org.apache.log4j.Logger logger = Logger.getLogger(WsmgPersistantStorage.class);

    JdbcStorage db = null;

    Connection conn = null;

    private Counter storeToDBCounter = new Counter();

    /*
     * this thing is never used in this context
     */
    // private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    String dbName = null;

    // private ConnectionPool connectionPool;
    public WsmgPersistantStorage(String ordinarySubsTblName, String specialSubsTblName, ConfigurationManager config)
            throws AxisFault {
        /*
         * try { conn = db.connect(); } catch (SQLException ex) { ex.printStackTrace(); }
         */
        this.dbName = ordinarySubsTblName;

        db = new JdbcStorage(config.getConfig(WsmgCommonConstants.CONFIG_JDBC_URL),
                config.getConfig(WsmgCommonConstants.CONFIG_JDBC_DRIVER));

        // Lets connect to the database and create tables if they are not
        // already there.


        // inject dbname to sql statement.

        SubscriptionConstants.ORDINARY_SUBSCRIPTION_INSERT_QUERY = String.format(
                SubscriptionConstants.INSERT_SQL_QUERY, ordinarySubsTblName);

        SubscriptionConstants.SPECIAL_SUBSCRIPTION_INSERT_QUERY = String.format(SubscriptionConstants.INSERT_SQL_QUERY,
                specialSubsTblName);

        if (WSMGParameter.measureMessageRate) {
            TimerThread timerThread = new TimerThread(storeToDBCounter, " StoreSubScriptionToDBCounter");
            new Thread(timerThread).start();
        }

        try {
            initMessageQueueStorage();

        } catch (SQLException sqlEx) {
            throw AxisFault.makeFault(sqlEx);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.airavata.wsmg.commons.storage.SubscriptionStorage#getAllSubscription()
     */
    public List<SubscriptionEntry> getAllSubscription() {

        ArrayList<SubscriptionEntry> ret = new ArrayList<SubscriptionEntry>();

        int totalRecord = 0;
        String query = "SELECT * FROM " + dbName;
        ResultSet rs = null;
        try {
            rs = db.query(query);
            if (rs != null) {
                // Get total number of rows

                // Point to the last row in resultset.
                rs.last();
                // Get the row position which is also the number of rows in the
                // ResultSet.
                totalRecord = rs.getRow();
                logger.debug("TotalRecord=" + totalRecord);
                // Create String array to return

                ret.ensureCapacity(totalRecord);

                // Reposition at the beginning of the ResultSet to take up
                // rs.next() call.
//                rs.beforeFirst();

                while (rs.next()) {
                    SubscriptionEntry subscriptionEntry = new SubscriptionEntry();
                    subscriptionEntry.setSubscriptionId(rs.getString("SubscriptionId"));
                    subscriptionEntry.setSubscribeXml(rs.getString("content"));

                    /*
                     * int policyValue = rs.getInt("wsrm"); subscriptionEntry[i] .setWsrmPolicy(policyValue ==
                     * WsmgCommonConstants.WSRM_POLICY_TRUE);
                     */

                    ret.add(subscriptionEntry);

                }
                rs.close();
            }
        } catch (SQLException ex) {
            logger.fatal("sql exception occured", ex);
            ret = new ArrayList<SubscriptionEntry>();
        }
        return ret;
    }

    public int insert(SubscriptionState subscription) {

        PreparedStatement stmt = null;
        String address = subscription.getConsumerReference().getAddress();
        Map<QName, OMElement> referenceParametersMap = subscription.getConsumerReference().getAllReferenceParameters();

        String consumerReferenceParameters = null;
        if (referenceParametersMap == null) {
            consumerReferenceParameters = "";
        } else {

            StringBuffer buffer = new StringBuffer();

            for (Iterator<OMElement> ite = referenceParametersMap.values().iterator(); ite.hasNext();) {
                OMElement currentReferenceParameter = ite.next();

                try {
                    buffer.append(currentReferenceParameter.toStringWithConsume());
                } catch (XMLStreamException se) {
                    logger.error("unable to convert reference parameter", se);
                }

            }

            consumerReferenceParameters = buffer.toString();

        }
        int policyValue = WsmgCommonConstants.WSRM_POLICY_FALSE;
        if (subscription.isWsrmPolicy()) {
            policyValue = WsmgCommonConstants.WSRM_POLICY_TRUE;
        }

        java.util.Date today = new java.util.Date();
        java.sql.Timestamp now = new java.sql.Timestamp(today.getTime());

        String sql = subscription.isNeverExpire() ? SubscriptionConstants.SPECIAL_SUBSCRIPTION_INSERT_QUERY
                : SubscriptionConstants.ORDINARY_SUBSCRIPTION_INSERT_QUERY;

        int result = 0;

        Connection connection = null;
        try {

            connection = db.connect();
            stmt = connection.prepareStatement(sql);

            stmt.setString(1, subscription.getId());
            stmt.setString(2, subscription.getSubscribeXml());
            stmt.setInt(3, policyValue);
            stmt.setString(4, subscription.getLocalTopic());
            stmt.setString(5, subscription.getXpathString());
            stmt.setString(6, address);
            stmt.setString(7, consumerReferenceParameters);
            stmt.setTimestamp(8, now);
            result = db.executeUpdate(stmt);
            storeToDBCounter.addCounter();
        } catch (SQLException ex) {
            logger.fatal("sql exception occured", ex);
        } finally {

            if (connection != null) {
                try {
                    db.closeConnection(connection);
                } catch (SQLException e) {

                    e.printStackTrace();
                }
            }

        }

        return result;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.airavata.wsmg.commons.storage.SubscriptionStorage#delete(java.lang.String)
     */
    public int delete(String subscriptionId) {
        String query;
        query = "DELETE FROM " + dbName + " WHERE SubscriptionId='" + subscriptionId + "'";
        try {
            db.update(query);
        } catch (SQLException ex) {
            logger.fatal("sql exception occured", ex);
            // TODO : throw this exception
        }
        return 0;
    }

    /**
     * If data base tables are defined as SQL queries in file placed at xregistry/tables.sql in the classpath, those SQL
     * queries are execuated against the data base. On the file, any line start # is igonred as a comment.
     * 
     * @throws XregistryException
     */
    private void initDatabaseTables(JdbcStorage jdbcStorage) throws AxisFault {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream sqltablesAsStream = cl.getResourceAsStream("broker-tables.sql");

        if (sqltablesAsStream == null) {
            return;
        }

        Connection connection = jdbcStorage.connect();
        try {
            Statement statement = connection.createStatement();

            String docAsStr = CommonRoutines.readFromStream(sqltablesAsStream);
            StringTokenizer t = new StringTokenizer(docAsStr, ";");

            while (t.hasMoreTokens()) {
                String line = t.nextToken();
                if (line.trim().length() > 0 && !line.startsWith("#")) {
                    System.out.println(line.trim());
                    statement.executeUpdate(line.trim());
                }
            }
        } catch (SQLException e) {
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        } finally {
            try {
                jdbcStorage.closeConnection(connection);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    // QUEUE related

    public Object blockingDequeue() {

        boolean gotValue = false;

        while (!gotValue) {
            try {
                KeyValueWrapper wrapper = retrive();

                done(wrapper.getKey());
                return wrapper.getValue();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;

    }

    public void cleanup() {

        try {
            cleanDB();
            initMessageQueueStorage();
        } catch (SQLException e) {
            logger.error(e);
        }

    }

    public void enqueue(Object object, String trackId) {

        // Get the Max ID cache and update and unlock the table
        Connection connection = null;
        try {
            connection = db.connect();

            PreparedStatement stmt = null;
            lockTables(connection,stmt);
            // System.out.println("locked maxId table");

            stmt = connection.prepareStatement(QueueContants.SQL_MAX_ID_SEPERATE_TABLE);
            ResultSet result = stmt.executeQuery();

            int nextkey;

            if (result.next()) {
                nextkey = result.getInt(1);
                result.close();
                stmt.close();
                stmt = connection.prepareStatement(QueueContants.SQL_MAX_ID_INCREMENT + (nextkey));
                stmt.executeUpdate();
                stmt.close();
            } else {
                nextkey = 1;
                result.close();
                stmt.close();
                stmt = connection.prepareStatement(QueueContants.SQL_MAX_ID_INSERT);
                stmt.executeUpdate();
                stmt.close();
            }
            unLockTables(connection,stmt);

            stmt = connection.prepareStatement(QueueContants.SQL_INSERT_STATEMENT);

            stmt.setInt(1, nextkey);
            stmt.setString(2, trackId);
            byte[] buffer;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(output);
            out.writeObject(object);
            buffer = output.toByteArray();
            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
            stmt.setBinaryStream(3, in, buffer.length);
            db.executeUpdate(stmt);
        } catch (SQLException sqlEx) {
            logger.error("unable to enque the message in persistant storage", sqlEx);
        } catch (IOException ioEx) {
            logger.error("unable to enque the message in persistant storage", ioEx);

        } finally {

            if (connection != null) {
                try {
                    db.closeConnection(connection);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void flush() {
        // nothing to do.
    }

    public int size() {
        throw new UnsupportedOperationException();
    }

    private void initMessageQueueStorage() throws SQLException {
        Connection connection = db.connect();
        String sql = "";
        String databaseType = "";
        PreparedStatement stmt = null;
        lockTables(connection,stmt);
        // System.out.println("locked tables (maxId and minId)4");
        stmt = connection.prepareStatement(QueueContants.SQL_MAX_ID_SEPERATE_TABLE);
        ResultSet result = stmt.executeQuery();
        if (!result.next()) {
            result.close();
            stmt.close();
            stmt = connection.prepareStatement(QueueContants.SQL_MAX_ID_INSERT);
            stmt.executeUpdate();
            stmt.close();
        }

        stmt = connection.prepareStatement(QueueContants.SQL_MIN_ID_SEPERATE_TABLE);
        result = stmt.executeQuery();

        if (!result.next()) {
            result.close();
            stmt.close();
            stmt = connection.prepareStatement(QueueContants.SQL_MIN_ID_INSERT);
            stmt.executeUpdate();
            stmt.close();
        }
        unLockTables(connection,stmt);
        db.closeConnection(connection);
        // System.out.println("unlocked tables (maxId and minId)4");
    }

    public KeyValueWrapper retrive() throws SQLException, IOException {
        Object obj = null;

        // Get the smallest id
        Connection connection = db.connect();
        boolean loop = true;

        int nextkey = -1;
        int maxid = -2;
        PreparedStatement stmt = null;
        ResultSet result = null;
        boolean connectionClosed = false;
        long wait = 1000;
        while (loop) {
            lockTables(connection,stmt);
            // System.out.println("locked maxId and minId table");
            // System.out.println("looping in retrive");

            stmt = connection.prepareStatement(QueueContants.SQL_MIN_ID_SEPERATE_TABLE);
            result = stmt.executeQuery();

            if (result.next()) {
                nextkey = result.getInt(1);
                result.close();
                stmt.close();

            } else {
                throw new RuntimeException("Queue init has failed earlier");
            }

            stmt = connection.prepareStatement(QueueContants.SQL_MAX_ID_SEPERATE_TABLE);
            result = stmt.executeQuery();

            if (result.next()) {
                maxid = result.getInt(1);
                result.close();
                stmt.close();

            } else {
                throw new RuntimeException("Queue init has failed earlier");
            }


            unLockTables(connection,stmt);
            // System.out.println("unlocked tables (maxId and minId)1");
            if (maxid > nextkey) {
                loop = false;
                stmt = connection.prepareStatement(QueueContants.SQL_MIN_ID_INCREMENT + (nextkey));
                stmt.executeUpdate();
                stmt.close();
                unLockTables(connection,stmt);
                // System.out.println("unlocked tables (maxId and minId) 2");

            } else {
                try {
                    unLockTables(connection,stmt);
                    db.closeConnection(connection);
                    connectionClosed = true;
                    wait = Math.min((wait + 1000), QueueContants.FINAL_WAIT_IN_MILI);
                    // System.out.println("Wait="+wait);
                    Thread.sleep(wait);
                } catch (InterruptedException e) {

                }
            }
            if (connectionClosed) {
                connection = db.connect();
                connectionClosed = false;
            }
        }

        stmt = connection.prepareStatement(QueueContants.SQL_SELECT_STATEMENT + nextkey);
        result = stmt.executeQuery();
        // FIXME: THis loop caused out-of-memory
        while (!result.next()) {
            // FIXME Remove this while loop and change the
            // order of db update if possible in the save.
            result.close();
            stmt.close();
            // System.out.println("Looping in 1");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {

                logger.error("interruped while thread sleep", e);
            }
            stmt = connection.prepareStatement(QueueContants.SQL_SELECT_STATEMENT + nextkey);
            result = stmt.executeQuery();

        }
        int id = result.getInt(1);
        InputStream in = result.getAsciiStream(2);
        ObjectInputStream s = new ObjectInputStream(in);
        try {
            obj = s.readObject();
        } catch (ClassNotFoundException e) {

            e.printStackTrace();
        }
        result.close();
        stmt.close();

        db.closeConnection(connection);

        return new KeyValueWrapper(id, obj);

    }

    public void done(int key) throws SQLException {
        String query = null;
        PreparedStatement stmt = null;
        Connection connection = db.connect();
        try {
            query = QueueContants.SQL_DELETE_STATEMENT + key;
            // System.out.println("Deleting key="+key);
            stmt = connection.prepareStatement(query);
            db.executeUpdate(stmt);
            stmt.close();
        } finally {
            db.closeConnection(connection);
        }

    }

    public void cleanDB() throws SQLException {
        Connection con = db.connect();
        Statement stmt = con.createStatement();
        int[] aiupdateCounts = new int[0];
        boolean bError = false;
        try {

            con.setAutoCommit(false);
            // ...
            bError = false;
            stmt.clearBatch();
            int totalStatement = 0;
            // add SQL statements
            stmt.addBatch("lock tables disQ write, MaxIDTable write, MinIDTable write;");
            // System.out.println("locked tables (maxId and minId) 5");
            stmt.addBatch("Delete from disQ;");
            stmt.addBatch("Delete from MaxIDTable;");
            stmt.addBatch("Delete from MinIDTable;");
            String databaseType = "";
            try {
                databaseType = DatabaseCreator.getDatabaseType(conn);
            } catch (Exception e) {
                logger.error("Error evaluating database type");
            }

            if ("mysql".equals(databaseType)) {
                stmt.addBatch("unlock tables;");
            }
            // System.out.println("unlocked tables (maxId and minId) 5");
            totalStatement = 5;

            aiupdateCounts = new int[totalStatement];

            // execute the statements
            aiupdateCounts = stmt.executeBatch();

        } // end try

        // catch blocks
        // ...
        catch (BatchUpdateException bue) {
            bError = true;
            aiupdateCounts = bue.getUpdateCounts();
            logger.error("SQLException: " + bue.getMessage());
            logger.error("SQLState:  " + bue.getSQLState());
            logger.error("Message:  " + bue.getMessage());
            logger.error("Vendor:  " + bue.getErrorCode());
            logger.info("Update counts:  ");
            int[] updateCounts = bue.getUpdateCounts();

            for (int i = 0; i < updateCounts.length; i++) {
                logger.error(updateCounts[i] + "   ");
            }

            SQLException SQLe = bue;
            while (SQLe != null) {
                // do exception stuff

                SQLe = SQLe.getNextException();
                logger.error(SQLe);
            }
        } // end BatchUpdateException catch
        catch (SQLException SQLe) {
            // ...
            logger.error(SQLe);
        } // end SQLException catch
        finally {
            db.closeConnection(con);
            // determine operation result
            for (int i = 0; i < aiupdateCounts.length; i++) {
                int iProcessed = aiupdateCounts[i];
                // The int values that can be returned in the update counts
                // array are:
                // -3--Operation error. A driver has the option to stop at the
                // first error and throw a BatchUpdateException or to report the
                // error and continue. This value is only seen in the latter
                // case.
                // -2--The operation was successful, but the number of rows
                // affected is unknown.
                // Zero--DDL statement or no rows affected by the operation.
                // Greater than zero--Operation was successful, number of rows
                // affected by the operation.
                if (iProcessed > 0 || iProcessed == -2) {
                    // statement was successful
                    // ...
                } else {
                    // error on statement
                    logger.info("Batch update successful.");
                    bError = true;
                    break;
                }
            } // end for

            if (bError) {
                con.rollback();
            } else {
                con.commit();
            }
            con.setAutoCommit(true);
        } // end finally
        logger.info("Queue is cleaned.");
    }

    private static class SubscriptionConstants {

        public static String INSERT_SQL_QUERY = "INSERT INTO %s(SubscriptionId, content, wsrm, Topics, XPath, ConsumerAddress, ReferenceProperties, CreationTime) "
                + "VALUES( ? , ? , ? , ? , ? , ? , ? , ?)";

        public static String ORDINARY_SUBSCRIPTION_INSERT_QUERY = null;

        public static String SPECIAL_SUBSCRIPTION_INSERT_QUERY = null;

    }

    private static class QueueContants {
        public static final int FINAL_WAIT_IN_MILI = 5000;

        public static final String TABLE_NAME = "disQ";

        public static final String TABLE_NAME_MAXID = "MaxIDTable";

        public static final String TABLE_NAME_MINID = "MinIDTable";

        public static final int STATUS_OPEN = 0;

        public static final String SQL_INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME
                + " (id, trackId, message, status) " + "VALUES (?,?,?," + STATUS_OPEN + ")";

        public static String SQL_DELETE_STATEMENT = "DELETE FROM " + TABLE_NAME + " WHERE id=";

        public static String SQL_SELECT_STATEMENT = "SELECT id,message FROM " + TABLE_NAME + " WHERE id=";

        public static String SQL_MAX_ID_SEPERATE_TABLE = "SELECT maxID FROM " + TABLE_NAME_MAXID;

        public static String SQL_MIN_ID_SEPERATE_TABLE = "SELECT minID FROM " + TABLE_NAME_MINID;

        public static String SQL_MAX_ID_INSERT = "INSERT INTO " + TABLE_NAME_MAXID + " (maxID) VALUES (1)";

        public static String SQL_MIN_ID_INSERT = "INSERT INTO " + TABLE_NAME_MINID + " (minID) VALUES (1)";

        public static String SQL_MAX_ID_INCREMENT = "UPDATE " + TABLE_NAME_MAXID + " SET maxID = maxID+1 WHERE maxID =";

        public static String SQL_MIN_ID_INCREMENT = "UPDATE " + TABLE_NAME_MINID + " SET minID = minID+1 WHERE minID =";

    }
    private void lockTables(Connection connection,PreparedStatement stmt)throws SQLException{
        String sql = "";
        String databaseType = "";
        try {
            databaseType = DatabaseCreator.getDatabaseType(connection);
        } catch (Exception e) {
            logger.error("Error evaluating database type");
        }

        if ("derby".equals(databaseType)) {
            sql = "lock table " + QueueContants.TABLE_NAME_MAXID + " IN SHARE MODE";
            stmt = connection.prepareStatement(sql);
            stmt.executeUpdate();
            sql = "lock table " + QueueContants.TABLE_NAME_MINID + " IN SHARE MODE";
            connection.prepareStatement(sql);
            stmt.executeUpdate();
            stmt.close();
        } else if ("mysql".equals(databaseType)) {
            sql = "lock tables " + QueueContants.TABLE_NAME_MAXID + " write" + ","
                    + QueueContants.TABLE_NAME_MINID + " write";
            stmt = connection.prepareStatement(sql);
            stmt.executeQuery();
            stmt.close();
        }
    }
        private void unLockTables(Connection connection,PreparedStatement stmt)throws SQLException{
        String sql = "";
        String databaseType = "";
        try {
            databaseType = DatabaseCreator.getDatabaseType(connection);
        } catch (Exception e) {
            logger.error("Error evaluating database type");
        }

        if ("mysql".equals(databaseType)) {
            sql = "unlock tables";
            stmt = connection.prepareStatement(sql);
            stmt.executeQuery();
            stmt.close();
        }
    }

}
