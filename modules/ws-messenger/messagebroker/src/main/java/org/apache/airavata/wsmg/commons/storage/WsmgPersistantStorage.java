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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.airavata.wsmg.broker.subscription.SubscriptionEntry;
import org.apache.airavata.wsmg.broker.subscription.SubscriptionState;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.storage.DatabaseCreator.DatabaseType;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.apache.airavata.wsmg.util.Counter;
import org.apache.airavata.wsmg.util.TimerThread;
import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsmgPersistantStorage implements WsmgStorage, WsmgQueue {
    private static final Logger logger = LoggerFactory.getLogger(WsmgPersistantStorage.class);

    /*
     * Table name
     */
    private static final String TABLE_NAME_TO_CHECK = SubscriptionConstants.TABLE_NAME_EXPIRABLE_SUBCRIPTIONS;

    private Counter storeToDBCounter = new Counter();

    private JdbcStorage db;

    public WsmgPersistantStorage(String jdbcUrl, String jdbcDriver) {

        db = new JdbcStorage(jdbcUrl, jdbcDriver);

        Connection conn = null;
        try {
            /*
             * Check database
             */
            conn = db.connect();
            if (!DatabaseCreator.isDatabaseStructureCreated(TABLE_NAME_TO_CHECK, conn)) {
                DatabaseCreator.createMsgBrokerDatabase(conn);
                logger.info("New Database created for Message Broker");
            } else {
                logger.info("Database already created for Message Broker!");
            }

            if (WSMGParameter.measureMessageRate) {
                TimerThread timerThread = new TimerThread(storeToDBCounter, " StoreSubScriptionToDBCounter");
                new Thread(timerThread).start();
            }

            initMessageQueueStorage();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Database failure");
        } finally {
            db.closeConnection(conn);
        }
    }

    public void dispose() {
        if (db != null) {
            db.closeAllConnections();
        }        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.airavata.wsmg.commons.storage.WsmgStorage#getAllSubscription()
     */
    public List<SubscriptionEntry> getAllSubscription() {

        ArrayList<SubscriptionEntry> ret = new ArrayList<SubscriptionEntry>();

        Connection conn = null;
        PreparedStatement stmt = null;
        try {

            // get number of row first and increase the arrayList size for
            // better performance
            int size = db.countRow(SubscriptionConstants.TABLE_NAME_EXPIRABLE_SUBCRIPTIONS, "*");

            conn = db.connect();
            stmt = conn.prepareStatement(SubscriptionConstants.EXP_SELECT_QUERY);
            ResultSet rs = stmt.executeQuery();
            ret.ensureCapacity(size);

            if (rs != null) {
                while (rs.next()) {
                    SubscriptionEntry subscriptionEntry = new SubscriptionEntry();
                    subscriptionEntry.setSubscriptionId(rs.getString("SubscriptionId"));
                    subscriptionEntry.setSubscribeXml(rs.getString("content"));
                    ret.add(subscriptionEntry);
                }
            }
        } catch (SQLException ex) {
            logger.error("sql exception occured", ex);
        } finally {
            db.quietlyClose(conn, stmt);
        }
        return ret;
    }

    public int insert(SubscriptionState subscription) {
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

        Timestamp now = new Timestamp(System.currentTimeMillis());

        int result = 0;
        Connection connection = null;
        PreparedStatement stmt = null;
        try {

            connection = db.connect();
            stmt = connection.prepareStatement(SubscriptionConstants.EXP_INSERT_SQL_QUERY);

            stmt.setString(1, subscription.getId());
            stmt.setBinaryStream(2, new ByteArrayInputStream(subscription.getSubscribeXml().getBytes()), subscription
                    .getSubscribeXml().getBytes().length);
            stmt.setInt(3, policyValue);
            stmt.setString(4, subscription.getLocalTopic());
            stmt.setString(5, subscription.getXpathString());
            stmt.setString(6, address);
            stmt.setBinaryStream(7, new ByteArrayInputStream(consumerReferenceParameters.getBytes()),
                    consumerReferenceParameters.getBytes().length);
            stmt.setTimestamp(8, now);
            result = db.executeUpdateAndClose(stmt);
            db.commitAndFree(connection);

            storeToDBCounter.addCounter();

        } catch (SQLException ex) {
            logger.error("sql exception occured", ex);
            db.rollbackAndFree(connection);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.airavata.wsmg.commons.storage.SubscriptionStorage#delete(java
     * .lang.String)
     */
    public int delete(String subscriptionId) {
        int result = 0;
        Connection connection = null;
        try {
            connection = db.connect();
            PreparedStatement stmt = connection.prepareStatement(SubscriptionConstants.EXP_DELETE_SQL_QUERY);
            stmt.setString(1, subscriptionId);
            result = db.executeUpdateAndClose(stmt);
            db.commitAndFree(connection);
        } catch (SQLException sql) {
            db.rollbackAndFree(connection);
            logger.error("sql exception occured", sql);
        }
        return result;
    }

    public void cleanup() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = db.connect();
            stmt = conn.createStatement();
            batchCleanDB(stmt, conn);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (db.isAutoCommit()) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            db.quietlyClose(conn, stmt);
        }
    }

    public Object blockingDequeue() throws InterruptedException {
        while (true) {
            try {
                return retrive();
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
                e.printStackTrace();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }

    public void enqueue(Object object, String trackId) {

        // Get the Max ID cache and update and unlock the table
        Connection connection = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        try {
            int nextkey;

            connection = db.connect();

            lockMaxMinTables(connection);

            stmt = connection.prepareStatement(QueueContants.SQL_MAX_ID_SEPERATE_TABLE);

            ResultSet result = stmt.executeQuery();

            if (result.next()) {
                nextkey = result.getInt(1);

                stmt2 = connection.prepareStatement(QueueContants.SQL_MAX_ID_INCREMENT + (nextkey));
                stmt2.executeUpdate();
            } else {
                throw new RuntimeException("MAX_ID Table is not init, redeploy the service !!!");
            }

            /*
             * After update MAX_ID put data into queue table
             */
            stmt3 = connection.prepareStatement(QueueContants.SQL_INSERT_STATEMENT);
            stmt3.setInt(1, nextkey);
            stmt3.setString(2, trackId);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(output);
            out.writeObject(object);
            byte[] buffer = output.toByteArray();
            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
            stmt3.setBinaryStream(3, in, buffer.length);
            stmt3.executeUpdate();
            db.commit(connection);
        } catch (SQLException sqlEx) {
            db.rollback(connection);
            logger.error("unable to enque the message in persistant storage", sqlEx);
        } catch (IOException ioEx) {
            db.rollback(connection);
            logger.error("unable to enque the message in persistant storage", ioEx);
        } finally {
            try {
                unLockTables(connection);
            } catch (SQLException sql) {
                logger.error("Cannot Unlock Table", sql);
            }

            db.quietlyClose(connection, stmt, stmt2, stmt3);
        }
    }

    private void initMessageQueueStorage() throws SQLException {
        Connection connection = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        PreparedStatement stmt4 = null;
        try {
            connection = db.connect();

            lockMaxMinTables(connection);

            /*
             * Get Max ID
             */
            stmt = connection.prepareStatement(QueueContants.SQL_MAX_ID_SEPERATE_TABLE);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                stmt2 = connection.prepareStatement(QueueContants.SQL_MAX_ID_INSERT);
                stmt2.executeUpdate();
            }

            /*
             * Get Min ID
             */
            stmt3 = connection.prepareStatement(QueueContants.SQL_MIN_ID_SEPERATE_TABLE);
            result = stmt3.executeQuery();
            if (!result.next()) {
                stmt4 = connection.prepareStatement(QueueContants.SQL_MIN_ID_INSERT);
                stmt4.executeUpdate();
            }
            db.commit(connection);
        } catch (SQLException sqle) {
            db.rollback(connection);
            throw sqle;
        } finally {
            try {
                unLockTables(connection);
            } catch (SQLException sql) {
                logger.error("Cannot Unlock Table", sql);
            }

            db.quietlyClose(connection, stmt, stmt2, stmt3, stmt4);
        }
    }

    private Object retrive() throws SQLException, IOException, InterruptedException {
        long wait = 1000;
        int nextkey = -1;
        int maxid = -2;
        Connection connection = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        ResultSet result = null;
        while (true) {
            try {
                connection = db.connect();

                lockMaxMinTables(connection);

                /*
                 * Get Min ID
                 */
                stmt = connection.prepareStatement(QueueContants.SQL_MIN_ID_SEPERATE_TABLE);
                result = stmt.executeQuery();
                if (result.next()) {
                    nextkey = result.getInt(1);
                } else {
                    throw new RuntimeException("Queue init has failed earlier");
                }

                /*
                 * Get Max ID
                 */
                stmt2 = connection.prepareStatement(QueueContants.SQL_MAX_ID_SEPERATE_TABLE);
                result = stmt2.executeQuery();
                if (result.next()) {
                    maxid = result.getInt(1);
                } else {
                    throw new RuntimeException("Queue init has failed earlier");
                }

                /*
                 * Update value and exit the loop
                 */
                if (maxid > nextkey) {
                    stmt3 = connection.prepareStatement(QueueContants.SQL_MIN_ID_INCREMENT + (nextkey));
                    stmt3.executeUpdate();
                    logger.debug("Update MIN ID by one");
                    db.commit(connection);
                    break;
                }

                db.commit(connection);
            } catch (SQLException sql) {
                db.rollback(connection);
                throw sql;
            } finally {
                try {
                    unLockTables(connection);
                } catch (SQLException sql) {
                    sql.printStackTrace();
                    logger.error("Cannot Unlock Table", sql);
                }

                db.quietlyClose(connection, stmt, stmt2, stmt3);
            }

            /*
             * Sleep if there is nothing to do
             */
            try {
                wait = Math.min((wait + 1000), QueueContants.FINAL_WAIT_IN_MILI);
                logger.debug("Wait=" + wait);
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                logger.warn("Queue is interrupted to close");
                throw e;
            }
        }

        /*
         * Create Subscription Object from MIN_ID and delete data in table
         */
        Object resultObj = null;
        int key = -1;
        try {
            connection = db.connect();
            stmt = connection.prepareStatement(QueueContants.SQL_SELECT_STATEMENT + nextkey);
            result = stmt.executeQuery();
            if (result.next()) {
                key = result.getInt(1);
                InputStream in = result.getAsciiStream(2);
                ObjectInputStream s = new ObjectInputStream(in);
                try {
                    resultObj = s.readObject();
                } catch (ClassNotFoundException e) {
                    logger.error("Cannot Deserialize Object from Database, ClassNotFound. ", e);
                }
            } else {
                throw new RuntimeException(
                        "MAX_ID and MIN_ID are inconsistent with subscription table, need to reset all data value");
            }

            try {
                String query = QueueContants.SQL_DELETE_STATEMENT + key;
                stmt2 = connection.prepareStatement(query);
                stmt2.executeUpdate();
                db.commit(connection);
            } catch (SQLException sqle) {
                db.rollback(connection);
                throw sqle;
            }
        } finally {
            db.quietlyClose(connection, stmt, stmt2);
        }
        return resultObj;
    }

    private void batchCleanDB(Statement stmt, Connection con) throws SQLException {
        DatabaseType databaseType = DatabaseType.other;
        int[] aiupdateCounts = new int[0];
        boolean bError = false;
        try {

            con.setAutoCommit(false);

            stmt.clearBatch();

            int totalStatement = 0;

            try {
                databaseType = DatabaseCreator.getDatabaseType(con);
            } catch (Exception e) {
                logger.error("Error evaluating database type", e);
            }
            // add SQL statements
            if (DatabaseType.mysql.equals(databaseType)) {
                stmt.addBatch("lock tables disQ write, MaxIDTable write, MinIDTable write;");
                totalStatement++;
            } else if (DatabaseType.derby.equals(databaseType)) {
                stmt.addBatch("lock table disQ in exclusive mode;");
                totalStatement++;
                stmt.addBatch("lock table MaxIDTable in exclusive mode;");
                totalStatement++;
                stmt.addBatch("lock table MinIDTable in exclusive mode;");
                totalStatement++;
            }
            stmt.addBatch("Delete from disQ;");
            totalStatement++;
            stmt.addBatch("Delete from MaxIDTable;");
            totalStatement++;
            stmt.addBatch("Delete from MinIDTable;");
            totalStatement++;

            aiupdateCounts = new int[totalStatement];

            // execute the statements
            aiupdateCounts = stmt.executeBatch();

        } catch (BatchUpdateException bue) {
            bError = true;
            aiupdateCounts = bue.getUpdateCounts();
            logger.error("SQLException: " + bue.getMessage());
            logger.error("SQLState:  " + bue.getSQLState());
            logger.error("Message:  " + bue.getMessage());
            logger.error("Vendor:  " + bue.getErrorCode());
            logger.info("Update counts:  ");

            for (int i = 0; i < aiupdateCounts.length; i++) {
                logger.error(aiupdateCounts[i] + "   ");
            }

            SQLException SQLe = bue;
            while (SQLe != null) {
                SQLe = SQLe.getNextException();
                logger.error(SQLe.getMessage(), SQLe);
            }
        } catch (SQLException SQLe) {
            bError = true;
            throw SQLe;
        } finally {
            // determine operation result
            for (int i = 0; !bError && i < aiupdateCounts.length; i++) {
                int iProcessed = aiupdateCounts[i];
                /**
                 * The int values that can be returned in the update counts
                 * array are: <br/>
                 * -3--Operation error. A driver has the option to stop at the
                 * first error and throw a BatchUpdateException or to report the
                 * error and continue. This value is only seen in the latter
                 * case. <br/>
                 * -2--The operation was successful, but the number of rows
                 * affected is unknown. <br/>
                 * Zero--DDL statement or no rows affected by the operation.
                 * Greater than zero--Operation was successful, number of rows
                 * affected by the operation.
                 */
                if (iProcessed < 0 && iProcessed != -2) {
                    // error on statement
                    logger.info("Error batch." + iProcessed);
                    bError = true;
                }
            }

            if (bError) {
                con.rollback();
            } else {
                con.commit();
            }

            /*
             * Unlock table after rollback and commit, since it is not automatic
             * in MySql
             */

            if (DatabaseType.mysql.equals(databaseType)) {
                PreparedStatement prepareStmt = con.prepareCall("unlock tables;");
                db.executeUpdateAndClose(prepareStmt);
            }
        } // end finally
        logger.info("Queue is cleaned.");
    }

    private void lockMaxMinTables(Connection connection) throws SQLException {
        DatabaseType databaseType = DatabaseType.other;
        try {
            databaseType = DatabaseCreator.getDatabaseType(connection);
        } catch (Exception e) {
            logger.error("Error evaluating database type", e);
        }

        /*
         * Must turn off auto commit
         */
        connection.setAutoCommit(false);
        String sql = null;
        Statement stmt = null;
        try {
            switch (databaseType) {
            case derby:
                sql = "LOCK TABLE " + QueueContants.TABLE_NAME_MAXID + " IN EXCLUSIVE MODE";
                String sql2 = "LOCK TABLE " + QueueContants.TABLE_NAME_MINID + " IN EXCLUSIVE MODE";
                stmt = connection.createStatement();
                stmt.addBatch(sql);
                stmt.addBatch(sql2);
                stmt.executeBatch();
                break;
            case mysql:
                sql = "lock tables " + QueueContants.TABLE_NAME_MAXID + " write" + "," + QueueContants.TABLE_NAME_MINID
                        + " write";
                stmt = connection.createStatement();
                stmt.executeQuery(sql);
                break;
            default:
                return;
            }

        } finally {
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
        }
    }

    private void unLockTables(Connection connection) throws SQLException {
        DatabaseType databaseType = DatabaseType.other;
        try {
            databaseType = DatabaseCreator.getDatabaseType(connection);
        } catch (Exception e) {
            logger.error("Error evaluating database type", e);
        }

        try {
            switch (databaseType) {
            case derby:
                /*
                 * Derby doesn't have explicit unlock SQL It uses commit or
                 * rollback as a unlock mechanism, so make sure that connection
                 * is always commited or rollbacked
                 */
                break;
            case mysql:
                String sql = "unlock tables";
                PreparedStatement stmt = null;
                try {
                    stmt = connection.prepareStatement(sql);
                    stmt.executeQuery();
                    db.commit(connection);
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
                break;
            default:
                return;
            }
        } finally {
            /*
             * Set auto commit when needed
             */
            if (db.isAutoCommit()) {
                connection.setAutoCommit(true);
            }
        }
    }

    private static class SubscriptionConstants {

        public static final String TABLE_NAME_EXPIRABLE_SUBCRIPTIONS = "subscription";

        public static final String TABLE_NAME_NON_EXPIRABLE_SUBCRIPTIONS = "specialSubscription";

        public static final String EXP_INSERT_SQL_QUERY = "INSERT INTO " + TABLE_NAME_EXPIRABLE_SUBCRIPTIONS
                + "(SubscriptionId, content, wsrm, Topics, XPath, ConsumerAddress, ReferenceProperties, CreationTime) "
                + "VALUES( ? , ? , ? , ? , ? , ? , ? , ?)";

        public static final String EXP_DELETE_SQL_QUERY = "DELETE FROM " + TABLE_NAME_EXPIRABLE_SUBCRIPTIONS
                + " WHERE SubscriptionId= ?";

        public static final String EXP_SELECT_QUERY = "SELECT * FROM " + TABLE_NAME_EXPIRABLE_SUBCRIPTIONS;

        public static final String NONEXP_INSERT_SQL_QUERY = "INSERT INTO " + TABLE_NAME_NON_EXPIRABLE_SUBCRIPTIONS
                + "(SubscriptionId, content, wsrm, Topics, XPath, ConsumerAddress, ReferenceProperties, CreationTime) "
                + "VALUES( ? , ? , ? , ? , ? , ? , ? , ?)";

        public static final String NONEXP_DELETE_SQL_QUERY = "DELETE FROM " + TABLE_NAME_NON_EXPIRABLE_SUBCRIPTIONS
                + " WHERE SubscriptionId= ?";

        public static final String NONEXP_SELECT_QUERY = "SELECT * FROM " + TABLE_NAME_NON_EXPIRABLE_SUBCRIPTIONS;
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
}
