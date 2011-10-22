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

package org.apache.airavata.wsmg.msgbox.Storage.dbpool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.apache.airavata.wsmg.commons.storage.JdbcStorage;
import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the core class which used by DatabaseStorageImpl to perform all the service operations, DatabaseStorageImpl
 * class simply use this class in its operation methods to perform the actual functionality.
 */
public class MessageBoxDB {

    private static final String MSGBOXES_TABLENAME = "msgBoxes";
    private static final String MSGBOX_TABLENAME = "msgbox";

    private static final Logger logger = LoggerFactory.getLogger(MessageBoxDB.class);

    private static Set<String> msgBoxids;

    public static final String SELECT_ALL_FROM_MSGBOXES = "SELECT * FROM " + MSGBOXES_TABLENAME;

    public static final String SQL_CREATE_MSGBOXES_STATEMENT = "INSERT INTO " + MSGBOXES_TABLENAME
            + " (msgboxid) VALUES (?)";

    public static final String SQL_DELETE_MSGBOXES_STATEMENT = "DELETE FROM " + MSGBOXES_TABLENAME
            + " WHERE msgboxid = ?";

    public static final String SQL_STORE_MESSAGE_STATEMENT = "INSERT INTO " + MSGBOX_TABLENAME
            + " (content, msgboxid, messageid,soapaction) VALUES (?,?,?,?)";

    public static final String SQL_SELECT_MSGBOX_STATEMENT = "SELECT * FROM " + MSGBOX_TABLENAME
            + " WHERE msgboxid = ? ORDER BY time";

    public static final String SQL_DELETE_MSGBOX_STATEMENT = "DELETE FROM " + MSGBOX_TABLENAME + " WHERE msgboxid = ?";

    public static final String SQL_DELETE_ANCIENT_STATEMENT = "DELETE FROM " + MSGBOX_TABLENAME
            + " WHERE {fn TIMESTAMPDIFF(SQL_TSI_FRAC_SECOND, CURRENT_TIMESTAMP, time) } > ?";

    private JdbcStorage db;

    private static MessageBoxDB instance;

    private long time;

    private MessageBoxDB(JdbcStorage db, long time) {
        this.db = db;
        this.time = time;
    }

    public static synchronized MessageBoxDB initialize(JdbcStorage db, long time) throws SQLException {
        if (instance == null) {
            instance = new MessageBoxDB(db, time);
            setMsgBoxidList(db);
        }
        return instance;
    }

    public static MessageBoxDB getInstance() {
        if (instance == null) {
            throw new RuntimeException("Please initialize this object first using initialize(JdbcStorage, long)");
        }
        return instance;
    }

    public synchronized void createMsgBx(String messageBoxId) throws SQLException, IOException {
        if (!msgBoxids.contains(messageBoxId)) {

            Connection connection = null;
            try {
                logger.debug(MSGBOXES_TABLENAME + ":" + messageBoxId);

                connection = db.connect();
                PreparedStatement statement = connection.prepareStatement(SQL_CREATE_MSGBOXES_STATEMENT);
                statement.setString(1, messageBoxId);
                db.executeUpdateAndClose(statement);
                db.commitAndFree(connection);

                msgBoxids.add(messageBoxId);

            } catch (SQLException sql) {
                db.rollbackAndFree(connection);
                throw sql;
            }
        } else {
            throw new IOException("The message box ID requested already exists");
        }
    }

    public synchronized void addMessage(String msgBoxID, String messageID, String soapAction, OMElement message)
            throws SQLException, IOException, XMLStreamException {
        if (msgBoxids.contains(msgBoxID)) {

            Connection connection = null;
            try {
                connection = db.connect();
                PreparedStatement stmt = connection.prepareStatement(SQL_STORE_MESSAGE_STATEMENT);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(output);
                out.writeObject(message.toStringWithConsume());
                byte[] buffer = output.toByteArray();
                ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                stmt.setBinaryStream(1, in, buffer.length);
                stmt.setString(2, msgBoxID);
                stmt.setString(3, messageID);
                stmt.setString(4, soapAction);

                db.executeUpdateAndClose(stmt);
                db.commitAndFree(connection);

            } catch (SQLException sql) {
                db.rollbackAndFree(connection);
                throw sql;
            }
        } else {
            throw new IOException("Currently a messagebox is not available with given message box id :" + msgBoxID);
        }
    }

    public synchronized void deleteMessageBox(String msgBoxId) throws SQLException {

        if (msgBoxids.contains(msgBoxId)) {

            Connection connection = null;
            try {
                connection = db.connect();
                PreparedStatement statement = connection.prepareStatement(SQL_DELETE_MSGBOXES_STATEMENT);
                statement.setString(1, msgBoxId);
                db.executeUpdateAndClose(statement);
                statement = connection.prepareStatement(SQL_DELETE_MSGBOX_STATEMENT);
                statement.setString(1, msgBoxId);
                db.executeUpdateAndClose(statement);

                // commit
                db.commitAndFree(connection);

                // remove from set
                msgBoxids.remove(msgBoxId);

            } catch (SQLException sql) {
                db.rollbackAndFree(connection);
                throw sql;
            }
        }
    }

    public synchronized List<String> removeAllMessagesforClient(String msgBoxId) throws SQLException, IOException,
            ClassNotFoundException, XMLStreamException {
        ArrayList<String> list = new ArrayList<String>();
        if (msgBoxids.contains(msgBoxId)) {

            Connection connection = null;
            PreparedStatement stmt = null;
            PreparedStatement stmt2 = null;
            try {
                connection = db.connect();
                stmt = connection.prepareStatement(SQL_SELECT_MSGBOX_STATEMENT);
                stmt.setString(1, msgBoxId);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    InputStream in = resultSet.getAsciiStream("content");
                    ObjectInputStream s = new ObjectInputStream(in);
                    String xmlString = (String) s.readObject();
                    logger.debug(xmlString);
                    list.add(xmlString);
                }

                /*
                 * Delete all retrieved messages
                 */
                stmt2 = connection.prepareStatement(SQL_DELETE_MSGBOX_STATEMENT);
                stmt2.setString(1, msgBoxId);
                stmt2.executeUpdate();

                // commit
                db.commit(connection);
            } catch (SQLException sql) {
                db.rollback(connection);
                throw sql;
            } finally {

                /*
                 * If there is error during query, close everything and throw error
                 */
                db.quietlyClose(connection, stmt, stmt2);
            }
        }
        return list;
    }

    public synchronized void removeAncientMessages() {
        Connection connection = null;
        try {
            connection = db.connect();
            PreparedStatement stmt = connection.prepareStatement(SQL_DELETE_ANCIENT_STATEMENT);
            stmt.setLong(1, this.time);
            db.executeUpdateAndClose(stmt);
            db.commitAndFree(connection);
        } catch (SQLException sql) {
            db.rollbackAndFree(connection);
            logger.error("Caught exception while removing old entries from msgbox db table", sql);
        }
    }

    private static void setMsgBoxidList(JdbcStorage db) throws SQLException {
        msgBoxids = new HashSet<String>();

        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = db.connect();
            stmt = connection.prepareStatement(SELECT_ALL_FROM_MSGBOXES);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                msgBoxids.add(resultSet.getString("msgboxid"));
            }
            db.commit(connection);
        } catch (SQLException e) {
            db.rollback(connection);
            throw e;
        } finally {
            db.quietlyClose(connection, stmt);
        }
    }

}
