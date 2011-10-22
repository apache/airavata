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

package org.apache.airavata.core.gfac.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class represents GridFTP Endpoint
 * 
 */
public class GridFTPContactInfo {
    protected final static Logger log = LoggerFactory.getLogger(GridFTPContactInfo.class);
    public String hostName;
    public int port;

    public GridFTPContactInfo(String hostName, int port) {
        if (port <= 0 || port == 80) {
            log.debug(hostName + "port recived " + port + " setting it to " + GFacConstants.DEFAULT_GSI_FTP_PORT);
            port = GFacConstants.DEFAULT_GSI_FTP_PORT;
        }
        this.hostName = hostName;
        this.port = port;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GridFTPContactInfo) {
            return hostName.equals(((GridFTPContactInfo) obj).hostName) && port == ((GridFTPContactInfo) obj).port;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hostName.hashCode();
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(hostName).append(":").append(port);
        return buf.toString();
    }
}
