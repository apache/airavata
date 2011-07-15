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

package org.apache.airavata.wsmg.msgbox.Storage.memory;

import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.airavata.wsmg.msgbox.Storage.MsgBoxStorage;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 * This is the inmemoery storage implementation for MsgBoxService, this will be initialized if msgBox.properties is
 * configured not to use database implementation.
 */
public class InMemoryImpl implements MsgBoxStorage {
    static Logger logger = Logger.getLogger(InMemoryImpl.class);

    ConcurrentHashMap<String, LinkedList<String>> map;

    public ConcurrentHashMap<String, LinkedList<String>> getMap() {
        return map;
    }

    public void setMap(ConcurrentHashMap<String, LinkedList<String>> map) {
        this.map = map;
    }

    public String createMsgBox() throws Exception {

        String clientid = UUID.randomUUID().toString();
        lookupQueue(clientid); // that will create an empty queue
        return clientid;
    }

    public void destroyMsgBox(String key) throws Exception {
        if (map.containsKey(key))
            map.remove(key);
    }

    public LinkedList<String> takeMessagesFromMsgBox(String key) throws Exception {

        LinkedList<String> list;

        synchronized (map) {
            list = map.get(key);

            if (list == null)
                throw new IllegalArgumentException("no message box with key " + key);
        }
        return list;

    }

    public void putMessageIntoMsgBox(String msgBoxID, String messageID, String soapAction, OMElement message)
            throws Exception {

        // To change body of implemented methods use File | Settings | File Templates.
        LinkedList<String> list = lookupQueue(msgBoxID);
        if (list == null) {
            throw new IllegalArgumentException("no message box with key " + msgBoxID + " to store the msg");
        }
        synchronized (list) {
            list.addLast(message.toStringWithConsume());
            logger.info("Message Stored in list with key " + msgBoxID);
        }
    }

    /**
     * The ancientness is defined in the db.config file.
     */
    public void removeAncientMessages() {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    public void closeConnection() throws Exception {
        // TODOn - store map back
    }

    private LinkedList<String> lookupQueue(String key) {

        logger.debug("lookupQueue: calling the getMap method...");

        if (key == null)
            throw new IllegalArgumentException();
        synchronized (map) {
            LinkedList<String> v = map.get(key);
            logger.debug(key + " is being searched in map..");

            if (v != null) {
                logger.info("key found in map.. " + key);
                return v;
            }

            logger.info("key not found in map.. " + key);
            LinkedList<String> list = new LinkedList<String>();
            map.put(key, list);
            logger.debug("new list created in map.. calling the setMap method...");
            // this.setMap(map);
            return list;
        }
    }
}
