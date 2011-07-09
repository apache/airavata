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

package org.apache.airavata.wsmg.msgbox.Storage;

import java.util.List;

import org.apache.axiom.om.OMElement;

/**
 * Message Box storage backend.
 */
public interface MsgBoxStorage {
    public String createMsgBox() throws Exception;

    // DELETE FROM msgbox WHERE msgboxid=key
    public void destroyMsgBox(String key) throws Exception;

    // SELECT * FROM msgbox WHERE msgboxid=key ORDER BY id LIMIT 1
    // DELETE FROM msgbox WHERE msgboxid=key AND id=*
    public List<String> takeMessagesFromMsgBox(String key) throws Exception;

    // INSERT INTO msgbox ...
    public void putMessageIntoMsgBox(String msgBoxID, String messageID, String soapAction, OMElement message)
            throws Exception;

    /**
     * The ancientness is defined in the db.config file.
     */
    public void removeAncientMessages() throws Exception;

    // public void closeConnection() throws Exception;
}
