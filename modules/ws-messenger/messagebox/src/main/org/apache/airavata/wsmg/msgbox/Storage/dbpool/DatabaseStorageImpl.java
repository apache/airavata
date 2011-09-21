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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.apache.airavata.wsmg.commons.storage.DatabaseCreator;
import org.apache.airavata.wsmg.commons.storage.JdbcStorage;
import org.apache.airavata.wsmg.msgbox.Storage.MsgBoxStorage;
import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database message Storage Implementation, if msgBox.properties configured to
 * use database this will set as the storage for MsgBoxSerivceSkeleton
 */
public class DatabaseStorageImpl implements MsgBoxStorage {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseStorageImpl.class);
    
    private static final String TABLE_NAME_TO_CHECK = "msgbox";
    
    private JdbcStorage db;   

    public DatabaseStorageImpl(String jdbcUrl, String jdbcDriver, long timeOfOldMessage) {
        try {
            db = new JdbcStorage(10, 50, jdbcUrl, jdbcDriver, true);
            
            /*
             * Check database
             */
            Connection conn = db.connect();
            if (!DatabaseCreator.isDatabaseStructureCreated(TABLE_NAME_TO_CHECK, conn)) {
                DatabaseCreator.createMsgBoxDatabase(conn);
                logger.info("New Database created for Message Box");
            } else {
                logger.info("Database already created for Message Box!");
            }
            db.closeConnection(conn);
                       
            MessageBoxDB.initialize(db, timeOfOldMessage);
            
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Database failure");
        }
    }

    public String createMsgBox() throws SQLException, IOException {
        String uuid = UUID.randomUUID().toString();
        MessageBoxDB.getInstance().createMsgBx(uuid);
        return uuid;
    }

    public void destroyMsgBox(String key) throws Exception {
        try {
            MessageBoxDB.getInstance().deleteMessageBox(key);
        } catch (SQLException e) {
            throw new Exception("Could not destroy the message box with key " + key, e);
        }
    }

    public List<String> takeMessagesFromMsgBox(String key) throws Exception {
        List<String> list = null;

        try {
            list = MessageBoxDB.getInstance().removeAllMessagesforClient(key);

        } catch (SQLException e) {
            throw new Exception("Error reading the message with the key " + key, e);
        } catch (IOException e) {
            throw new Exception("Error reading the message with the key " + key, e);
        }

        return list;
    }

    public void putMessageIntoMsgBox(String msgBoxID, String messageID, String soapAction, OMElement message)
            throws SQLException, IOException, XMLStreamException {
        MessageBoxDB.getInstance().addMessage(msgBoxID, messageID, soapAction, message);
    }

    public void removeAncientMessages() throws Exception {
        MessageBoxDB.getInstance().removeAncientMessages();
    }

    public void dispose() {
        if(db != null){
            db.closeAllConnections();   
        }
    }

}
