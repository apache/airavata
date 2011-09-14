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

package org.apache.airavata.wsmg.client.util;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class ClientUtil {
    
    public static final long EXPIRE_TIME = 1000 * 60 * 60 * 72l;
    
    public static String formatMessageBoxUrl(String msgBoxServiceUrl, String msgboxId) {
        return msgBoxServiceUrl.endsWith("/") ? msgBoxServiceUrl + "clientid/" + msgboxId : msgBoxServiceUrl
                + "/clientid/" + msgboxId;
    }
    

    public static String formatURLString(String url) {
       if (url == null) {
           throw new IllegalArgumentException("url can't be null");
       }
       if (url.indexOf("//") < 0) {
           url = "http://" + url; // use default http
       }
       return url;
   }
    
    public static String getHostIP() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            System.out.println("Error - unable to resolve localhost");
        }
        // Use IP address since DNS entry cannot update the laptop's entry
        // promptly
        String hostIP = localAddress.getHostAddress();
        return hostIP;
    }
}
