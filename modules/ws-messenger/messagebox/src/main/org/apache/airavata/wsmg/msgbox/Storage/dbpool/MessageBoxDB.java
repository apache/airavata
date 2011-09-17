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
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.apache.airavata.wsmg.commons.storage.JdbcStorage;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

/**
 * This is the core class which used by DatabaseStorageImpl to perform all the service operations, DatabaseStorageImpl class
 * simply use this class in its operation methods to perform the actual funcationality.
 */
public class MessageBoxDB {

    static Logger logger = Logger.getLogger(MessageBoxDB.class);

    private static Set<String> msgBoxids;

    public static final String SELECT_ALL_FROM_MSGBOXES = "SELECT * FROM msgBoxes";    

    public static String SQL_STORE_MESSAGE_STATEMENT = "INSERT INTO msgbox (content, msgboxid, messageid,soapaction) VALUES (?,?,?,?)";

    public static String SQL_CREATE_MSGBOX_STATEMENT = "INSERT INTO %s (msgboxid) VALUES ('%s')";

    public static String SQL_DELETE_ALL_STATEMENT = "DELETE FROM %s WHERE msgboxid='%s'";

    public static String SQL_SELECT_STATEMENT1 = "SELECT * FROM %s WHERE msgboxid='%s' ORDER BY time ";

    public static String SQL_DELETE_ANCIENT_STATEMENT = "DELETE FROM %s WHERE time <'%s'";
    
    private JdbcStorage db;
    
    private static MessageBoxDB instance;
    
    private long time;    

    private MessageBoxDB(JdbcStorage db) {
        this.db = db;
    }
    
    public static MessageBoxDB initialize(JdbcStorage db, long time) throws SQLException{
        if(instance == null){
            instance = new MessageBoxDB(db);
            setMsgBoxidList(db);
        }
        return instance;
    }
    
    public static MessageBoxDB getInstance(){
        if(instance==null){
            throw new RuntimeException("Please initialize this object first using initialize(JdbcStorage, long)");
        }
        return instance;
    }

    public void createMsgBx(String messageBoxId, String tableName) throws SQLException, IOException {
        if (!msgBoxids.contains(messageBoxId)) {
            Connection connection = db.connect();
            Statement statement = connection.createStatement();
            System.out.println(tableName + ":" + messageBoxId);
            statement.execute(String.format(SQL_CREATE_MSGBOX_STATEMENT, tableName, messageBoxId));
            connection.commit();
            db.closeConnection(connection);
            msgBoxids.add(messageBoxId);
        } else
            throw new AxisFault("The message box ID requested already exists");
    }

    public void addMessage(String msgBoxID, String messageID, String soapAction, OMElement message)
            throws SQLException, IOException, XMLStreamException {
        if (msgBoxids.contains(msgBoxID)) {
            Connection connection = db.connect();
            PreparedStatement stmt = connection.prepareStatement(SQL_STORE_MESSAGE_STATEMENT);
            byte[] buffer;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(output);
            out.writeObject(message.toStringWithConsume());
            buffer = output.toByteArray();
            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
            stmt.setBinaryStream(1, in, buffer.length);
            stmt.setString(2, msgBoxID);
            stmt.setString(3, messageID);
            stmt.setString(4, soapAction);
            db.insertAndClose(stmt);
            stmt.close();
            connection.commit();
            db.closeConnection(connection);
        } else {
            throw new AxisFault("Currently a messagebox is not available with given message box id :" + msgBoxID);
        }
    }

    public void deleteMessageBox(String msgBoxId) throws SQLException {

        if (msgBoxids.contains(msgBoxId)) {
            Connection connection = db.connect();
            Statement statement = connection.createStatement();
            statement.execute(String.format(SQL_DELETE_ALL_STATEMENT, "msgbox", msgBoxId));
            statement.execute(String.format(SQL_DELETE_ALL_STATEMENT, "msgBoxes", msgBoxId));
            db.closeConnection(connection);
            msgBoxids.remove(msgBoxId);
        }
    }

    public List<String> removeAllMessagesforClient(String msgBoxId) throws SQLException, IOException,
            ClassNotFoundException, XMLStreamException {
        LinkedList<String> list = new LinkedList<String>();
        if (msgBoxids.contains(msgBoxId)) {
            Connection connection = db.connect();

            PreparedStatement stmt = connection.prepareStatement(String.format(SQL_SELECT_STATEMENT1, "msgbox",
                    msgBoxId));
            ResultSet resultSet = stmt.executeQuery();
//            resultSet.beforeFirst();

            while (resultSet.next()) {
                InputStream in = resultSet.getAsciiStream("content");
                ObjectInputStream s = new ObjectInputStream(in);
                String xmlString = (String) s.readObject();
                System.out.println(xmlString);
                list.addFirst(xmlString);
            }
            resultSet.close();
            stmt.close();
            stmt = connection.prepareStatement(String.format(SQL_DELETE_ALL_STATEMENT, "msgbox", msgBoxId));
            db.insertAndClose(stmt);
            stmt.close();
            connection.commit();
            db.closeConnection(connection);
        }
        return list;
    }

    public void removeAncientMessages() {
        try {
            Connection connection = db.connect();
            long persevetime = System.currentTimeMillis() - this.time;
            PreparedStatement stmt = connection.prepareStatement(String.format(SQL_DELETE_ANCIENT_STATEMENT, "msgBox",
                    persevetime));
            db.insertAndClose(stmt);
            stmt.close();
            db.closeConnection(connection);
        } catch (SQLException e) {
            logger.fatal("Caught exception while removing old entries from msgbox db table", e);
        }

    }

    private static void setMsgBoxidList(JdbcStorage db) throws SQLException {
        msgBoxids = Collections.synchronizedSet(new HashSet<String>());
        Connection connection = db.connect();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(SELECT_ALL_FROM_MSGBOXES);
        while (resultSet.next()) {
            msgBoxids.add(resultSet.getString("msgboxid"));
        }
        statement.close();
        connection.commit();
        db.closeConnection(connection);
    }

}
