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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.airavata.wsmg.msgbox.Storage.MsgBoxStorage;
import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the in memory storage implementation for MsgBoxService, this will be
 * initialized if msgBox.properties is configured not to use database
 * implementation.
 */
public class InMemoryImpl implements MsgBoxStorage {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryImpl.class);

    private HashMap<String, List<Content>> map = new HashMap<String, List<Content>>();

    private long time;

    public InMemoryImpl(long time) {
        this.time = time;
    }

    public String createMsgBox() throws Exception {
        synchronized (map) {
            String clientid = UUID.randomUUID().toString();
            if(map.containsKey(clientid))
                throw new Exception("Message Box is existed with key:" + clientid);            
            map.put(clientid, new ArrayList<Content>());            
            return clientid;
        }
    }

    public void destroyMsgBox(String key) throws Exception {
        synchronized (map) {
            map.remove(key);
        }
    }

    public List<String> takeMessagesFromMsgBox(String key) throws Exception {
        synchronized (map) {
            List<Content> x = map.get(key);
            ArrayList<String> result = new ArrayList<String>(x.size());
            for (Content content : x) {
                result.add(content.getContent());
            }
            map.put(key, new ArrayList<Content>());
            return result;
        }
    }

    public void putMessageIntoMsgBox(String msgBoxID, String messageID, String soapAction, OMElement message)
            throws Exception {
        synchronized (map) {
            if (!map.containsKey(msgBoxID)) {
                throw new IllegalArgumentException("no message box with key " + msgBoxID + " to store the msg");
            }
            List<Content> list = map.get(msgBoxID);
            list.add(new Content(message.toStringWithConsume(), System.currentTimeMillis()));
            logger.debug("Message Stored in list with key " + msgBoxID);
        }
    }

    public void removeAncientMessages() {
        /*
         * O(n^2) algorithms. Better performance can be achieved with more Cache.
         */
        synchronized (map) {
            long currentTime = System.currentTimeMillis();
            Iterator<List<Content>> it = map.values().iterator();
            while(it.hasNext()){
                Iterator<Content> itToRemove = it.next().iterator();
                while(itToRemove.hasNext()){
                    Content content = itToRemove.next();
                    if(currentTime - this.time > content.getTime()){
                        itToRemove.remove();
                    }
                }                
            }
        }
    }   

    public void dispose() {
        synchronized (map) {
            map.clear();
        }
    }

    class Content {
        private String content;
        private long time;
        public Content(String content, long time) {
            this.content = content;
            this.time = time;
        }
        public String getContent() {
            return content;
        }
        public long getTime() {
            return time;
        }
    }

}
