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
import java.util.Date;
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

public class WsmgPersistantStorage implements WsmgStorage {
    private static final Logger logger = LoggerFactory.getLogger(WsmgPersistantStorage.class);

    private static final String TABLE_NAME_TO_CHECK = "subscription";

    private Counter storeToDBCounter = new Counter();

    private JdbcStorage db = null;

    private String dbName = null;

    public WsmgPersistantStorage(String jdbcUrl, String jdbcDriver) {

        this.dbName = WsmgCommonConstants.TABLE_NAME_EXPIRABLE_SUBCRIPTIONS;

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

            // inject dbname to sql statement.
            SubscriptionConstants.ORDINARY_SUBSCRIPTION_INSERT_QUERY = String.format(
                    SubscriptionConstants.INSERT_SQL_QUERY, WsmgCommonConstants.TABLE_NAME_EXPIRABLE_SUBCRIPTIONS);

            SubscriptionConstants.SPECIAL_SUBSCRIPTION_INSERT_QUERY = String.format(
                    SubscriptionConstants.INSERT_SQL_QUERY, WsmgCommonConstants.TABLE_NAME_NON_EXPIRABLE_SUBCRIPTIONS);

            if (WSMGParameter.measureMessageRate) {
                TimerThread timerThread = new TimerThread(storeToDBCounter, " StoreSubScriptionToDBCounter");
                new Thread(timerThread).start();
            }

            initMessageQueueStorage();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Database failure");
        } finally {
            if (conn != null) {
                db.closeConnection(conn);
            }
        }
    }

    private void quietlyClose(Statement stmt, Connection conn) {
        try {
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
        } catch (SQLException sql) {
            logger.error(sql.getMessage(), sql);
        }

        if (conn != null) {
            db.closeConnection(conn);
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

        String query = "SELECT * FROM " + dbName;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {

            // get number of row first and increase the arrayList size for
            // better performance
            int size = db.countRow(dbName, "*");

            conn = db.connect();
            stmt = conn.prepareStatement(query);
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
            quietlyClose(stmt, conn);
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

        Date today = new Date();
        Timestamp now = new Timestamp(today.getTime());

        String sql = subscription.isNeverExpire() ? SubscriptionConstants.SPECIAL_SUBSCRIPTION_INSERT_QUERY
                : SubscriptionConstants.ORDINARY_SUBSCRIPTION_INSERT_QUERY;

        int result = 0;
        Connection connection = null;
        PreparedStatement stmt = null;
        try {

            connection = db.connect();
            stmt = connection.prepareStatement(sql);

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
            storeToDBCounter.addCounter();
            db.commitAndFree(connection);
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
        String query;
        query = "DELETE FROM " + dbName + " WHERE SubscriptionId='" + subscriptionId + "'";
        try {
            return db.update(query);
        } catch (SQLException ex) {
            logger.error("sql exception occured", ex);
        }
        return 0;
    }

    // QUEUE related

    public Object blockingDequeue() {
        while (true) {
            try {
                // FIXME::: WHY RETURN KeyValueWrapper Object??????
                // FIXME::: Can it cast to OutGoingMessage????
                KeyValueWrapper wrapper = retrive();
                done(wrapper.getKey());
                return wrapper.getValue();
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
                e.printStackTrace();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }
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

            quietlyClose(stmt, conn);
        }
    }

    public void enqueue(Object object, String trackId) {

        // Get the Max ID cache and update and unlock the table
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            int nextkey;

            connection = db.connect();

            lockMaxMinTables(connection);

            stmt = connection.prepareStatement(QueueContants.SQL_MAX_ID_SEPERATE_TABLE);

            ResultSet result = stmt.executeQuery();

            if (result.next()) {
                nextkey = result.getInt(1);
                result.close();
                stmt.close();

                stmt = connection.prepareStatement(QueueContants.SQL_MAX_ID_INCREMENT + (nextkey));
                db.executeUpdateAndClose(stmt);
            } else {
                throw new RuntimeException("MAX_ID Table is not init, redeploy the service !!!");
            }

            /*
             * After update MAX_ID put data into queue table
             */
            stmt = connection.prepareStatement(QueueContants.SQL_INSERT_STATEMENT);
            stmt.setInt(1, nextkey);
            stmt.setString(2, trackId);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(output);
            out.writeObject(object);
            byte[] buffer = output.toByteArray();
            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
            stmt.setBinaryStream(3, in, buffer.length);
            db.executeUpdateAndClose(stmt);
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

            quietlyClose(stmt, connection);
        }
    }

    public void flush() {
        // nothing to do.
    }

    public int size() {
        throw new UnsupportedOperationException();
    }

    private void initMessageQueueStorage() throws SQLException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = db.connect();

            lockMaxMinTables(connection);

            /*
             * Get Max ID
             */
            stmt = connection.prepareStatement(QueueContants.SQL_MAX_ID_SEPERATE_TABLE);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                stmt.close();
                stmt = connection.prepareStatement(QueueContants.SQL_MAX_ID_INSERT);
                db.executeUpdateAndClose(stmt);
            }

            /*
             * Get Min ID
             */
            stmt = connection.prepareStatement(QueueContants.SQL_MIN_ID_SEPERATE_TABLE);
            result = stmt.executeQuery();
            if (!result.next()) {
                stmt.close();
                stmt = connection.prepareStatement(QueueContants.SQL_MIN_ID_INSERT);
                db.executeUpdateAndClose(stmt);
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

            quietlyClose(stmt, connection);
        }
    }

    private KeyValueWrapper retrive() throws SQLException, IOException {
        Object obj = null;
        int nextkey = -1;
        int maxid = -2;
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        long wait = 1000;

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
                    stmt.close();
                } else {
                    throw new RuntimeException("Queue init has failed earlier");
                }

                /*
                 * Get Max ID
                 */
                stmt = connection.prepareStatement(QueueContants.SQL_MAX_ID_SEPERATE_TABLE);
                result = stmt.executeQuery();
                if (result.next()) {
                    maxid = result.getInt(1);
                    stmt.close();
                } else {
                    throw new RuntimeException("Queue init has failed earlier");
                }

                /*
                 * Update value and exit the loop
                 */
                if (maxid > nextkey) {
                    stmt = connection.prepareStatement(QueueContants.SQL_MIN_ID_INCREMENT + (nextkey));
                    db.executeUpdateAndClose(stmt);
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

                quietlyClose(stmt, connection);
            }

            /*
             * Sleep if there is nothing to do
             */
            try {
                wait = Math.min((wait + 1000), QueueContants.FINAL_WAIT_IN_MILI);
                logger.debug("Wait=" + wait);
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                break;
            }
        }

        /*
         * Create Subscription Object from MIN_ID
         */
        try {
            connection = db.connect();
            stmt = connection.prepareStatement(QueueContants.SQL_SELECT_STATEMENT + nextkey);
            result = stmt.executeQuery();
            if (result.next()) {
                int id = result.getInt(1);
                InputStream in = result.getAsciiStream(2);
                ObjectInputStream s = new ObjectInputStream(in);
                try {
                    obj = s.readObject();
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage(), e);
                }
                return new KeyValueWrapper(id, obj);
            } else {
                throw new RuntimeException(
                        "MAX_ID and MIN_ID are inconsistent with subscription table, need to reset all data value");
            }
        } finally {
            quietlyClose(stmt, connection);
        }
    }

    /**
     * Delete data in subscription table since it is read
     * 
     * @param key
     * @throws SQLException
     */
    private void done(int key) throws SQLException {
        String query = null;
        Connection connection = null;
        try {
            connection = db.connect();
            query = QueueContants.SQL_DELETE_STATEMENT + key;
            PreparedStatement stmt = connection.prepareStatement(query);
            db.executeUpdateAndClose(stmt);
            db.commitAndFree(connection);
        } catch (SQLException sqle) {
            db.rollbackAndFree(connection);
            throw sqle;
        }
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

}
